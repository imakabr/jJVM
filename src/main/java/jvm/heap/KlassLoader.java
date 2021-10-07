package jvm.heap;

import jvm.engine.ExecutionEngine;
import jvm.JVMValue;
import jvm.Utils;
import jvm.parser.Method;
import jvm.parser.Field;
import jvm.parser.Klass;
import jvm.parser.KlassParser;

import java.util.*;

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

    public int getInstanceKlassIndexByName(String name) {
        Integer index = indexByName.get(name);
        if (index == null) {
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
        byte[] klassData = Utils.getClassFileData(name);
        KlassParser kParser = new KlassParser(klassData, name);
//        kParser.parse();

//        Heap heap = HeapHolder.getHeap();
        Klass constantPoolKlass = kParser.getKlass();
        int parentKlassIndex = -1;
        if (!"java/lang/Object".equals(constantPoolKlass.getParent())) {
//            parentKlassIndex = loadKlass(constantPoolKlass.getParent());
        }
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

        //create InstanceKlass
        List<Field> staticFields = constantPoolKlass.getStaticFields();
//        InstanceKlass instanceKlass = null;
//        if (parentKlassIndex == -1) {
//            InstanceObject object = new InstanceObject(staticFields, -1);
//            int objectRef = heap.getObjectRef(object);
//            instanceKlass = new InstanceKlass(staticFields.size(), objectRef, constantPoolKlass);
//            int klassIndex = heap.setInstanceKlass(instanceKlass);
//            object.setKlassIndex(klassIndex);
//            setIndexByName(kParser.className(), klassIndex);
//            setConstantPoolKlassByName(kParser.className(), constantPoolKlass);
//        }


        InstanceKlass instanceKlass = new InstanceKlass(staticFields.size(), -1, constantPoolKlass);
        for (int index = 0; index < staticFields.size(); index++) {
            Field value = staticFields.get(index);
            instanceKlass.setValue(index, new JVMValue(value.getType(), 0));
            instanceKlass.setIndexByFieldName(value.getName(), index);
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

        int index = heap.setInstanceKlass(instanceKlass);
        setIndexByName(constantPoolKlass.getKlassName(), index);
        setConstantPoolKlassByName(constantPoolKlass.getKlassName(), constantPoolKlass);

        //init Klass
        if (clInit != null) {
            ExecutionEngine engine = new ExecutionEngine(heap);
            engine.invoke(clInit);
        }

    }
}
