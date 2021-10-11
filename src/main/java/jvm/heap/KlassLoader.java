package jvm.heap;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import jvm.engine.ExecutionEngine;
import jvm.JVMValue;
import jvm.Utils;
import jvm.parser.Method;
import jvm.parser.Field;
import jvm.parser.Klass;
import jvm.parser.KlassParser;

import java.util.*;
import java.util.stream.Collectors;

public class KlassLoader {

    Map<String, Integer> indexByName; // index to Heap.instanceKlasses
    Map<String, Klass> loadedKlasses;
    Heap heap;

    KlassLoader(Heap heap) {
        this.indexByName = new HashMap<>();
        this.loadedKlasses = new HashMap<>();
        this.heap = heap;
    }

    public void setIndexByName(String name, int index) {
        this.indexByName.put(name, index);
    }

    @Nullable
    public Integer getInstanceKlassIndexByName(String name, boolean loadIfAbsent) {
        Integer index = indexByName.get(name);
        if (index == null && loadIfAbsent) {
            loadKlass(name);
        }
        return indexByName.get(name);
    }

    public void setConstantPoolKlassByName(String name, Klass cpKlass) {
        this.loadedKlasses.put(name, cpKlass);
    }

    public Klass getLoadedKlassByName(String name) {
        Klass klass = loadedKlasses.get(name);
        if (klass == null) {
            loadKlass(name);
        }
        return loadedKlasses.get(name);
    }

    public void loadKlass(String name) {
        loadCurrentKlass(name);
        List<Method> clInitMethods = prepareKlass(getLoadedKlassByName(name));

        //init Klass from top to bottom
        ExecutionEngine engine = new ExecutionEngine(heap);
        for (Method clInit : clInitMethods) {
            engine.invoke(clInit);
        }
    }

    private void loadCurrentKlass(@NotNull String name) {
        byte[] klassData = Utils.getClassFileData(name);
        Klass constantPoolKlass = new KlassParser(klassData, name).getKlass();
        setConstantPoolKlassByName(constantPoolKlass.getKlassName(), constantPoolKlass);
        if (!"java/lang/Object".equals(constantPoolKlass.getParent())) {
            loadCurrentKlass(constantPoolKlass.getParent());
        }
    }

    private List<Method> prepareKlass(Klass constantPoolKlass) {
        List<Klass> klasses = new ArrayList<>();
        Klass current = constantPoolKlass;
        while (!"java/lang/Object".equals(current.getParent())) {
            klasses.add(getLoadedKlassByName(current.getKlassName()));
            current = getLoadedKlassByName(current.getParent());
        }
        klasses.add(getLoadedKlassByName(current.getKlassName()));
        List<Method> clinitMethods = new ArrayList<>();
        for (int i = klasses.size() - 1; i >= 0; i--) {
            Method method = prepareChildKlass(klasses.get(i));
            if (method != null) {
                clinitMethods.add(method);
            }
        }
        return clinitMethods;
    }

    private Method prepareChildKlass(Klass constantPoolKlass) {
        Integer parentKlassIndex = getInstanceKlassIndexByName(constantPoolKlass.getParent(), false);
        InstanceKlass parentKlass = parentKlassIndex != null ? heap.getInstanceKlass(parentKlassIndex) : null;
        List<String> allFields = new ArrayList<>();
        allFields.addAll(parentKlass != null ? parentKlass.getOrderedFieldNames() : Collections.emptyList());
        allFields.addAll(constantPoolKlass.getStaticFieldNames());
        InstanceObject object = new InstanceObject(allFields, -1);
        int objectRef = heap.changeObject(parentKlass != null ? parentKlass.getObjectRef() : -1, object);
        InstanceKlass instanceKlass = new InstanceKlass(allFields, objectRef, constantPoolKlass);
        int klassIndex = heap.setInstanceKlass(instanceKlass);
        setIndexByName(constantPoolKlass.getKlassName(), klassIndex);
        object.setKlassIndex(klassIndex);

        //find clinit and virtual methods
        Map<String, Integer> virtualMethods = new TreeMap<>();
        Collection<Method> methodsList = constantPoolKlass.getMethods();
        Method clInit = null;
        for (Method method : methodsList) {
            if ("<clinit>:()V".equals(method.getNameAndType())) {
                clInit = method;
                continue;
            }
            int index = heap.getMethodRepo().setMethod(method);
            if (!(method.getNameAndType().startsWith("<init>")) && !(method.isStatic()) && !(method.isPrivate())) {
                virtualMethods.put(method.getNameAndType(), index);
            }
        }

        //create and set virtualMethodTable
        List<String> methodNames = new ArrayList<>(virtualMethods.keySet());
        int[] virtualMethodTable = new int[virtualMethods.size()];
        for (int index = 0; index < virtualMethods.size(); index++) {
            String methodName = methodNames.get(index);
            virtualMethodTable[index] = virtualMethods.get(methodName);
            instanceKlass.setIndexByVirtualMethodName(methodName, index);
        }
        instanceKlass.setVirtualMethodTable(virtualMethodTable);

        return clInit;
    }

}
