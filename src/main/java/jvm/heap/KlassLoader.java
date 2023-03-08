package jvm.heap;

import jvm.engine.ExecutionEngine;
import jvm.Utils;
import jvm.engine.StackFrame;
import jvm.heap.api.Heap;
import jvm.heap.api.InstanceKlass;
import jvm.heap.api.InstanceObject;
import jvm.lang.ObjectJVM;
import jvm.parser.Method;
import jvm.parser.Klass;
import jvm.parser.KlassParser;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static jvm.Utils.changeSystemKlassNameToJVMKlassName;
import static jvm.Utils.changeJVMKlassNameToSystemKlassName;
import static jvm.heap.InstanceFactory.getInstanceKlass;
import static jvm.heap.InstanceFactory.getInstanceObject;

public class KlassLoader {

    public static final String JAVA_LANG_OBJECT = "java/lang/Object";
    public static final String ABSENCE = "absence";
    public static final String CLASS_INIT = "<clinit>:()V";
    public static final String OBJECT_INIT = "<init>";
    public static final String STRING_JVM = "jvm/lang/StringJVM";
    public static final String STRING = "java/lang/String";
    public static final String STRING_BUILDER = "jvm/lang/StringBuilder";
    public static final String SYSTEM = "jvm/lang/SystemJVM";
    public static final String PRINT_STREAM = "jvm/io/PrintStreamJVM";


    public static final String[] arrayNames = {"boolean[]", "char[]", "float[]", "double[]", "byte[]", "short[]", "int[]", "long[]"};

    @Nonnull
    private final Map<String, Integer> indexByName; // index to Heap.instanceKlasses
    @Nonnull
    private final Map<String, Klass> loadedKlasses;

    @Nonnull
    private final Set<String> preparedKlasses;
    @Nonnull
    private final Heap heap;

    KlassLoader(@Nonnull Heap heap) {
        this.indexByName = new HashMap<>();
        this.loadedKlasses = new HashMap<>();
        this.preparedKlasses = new HashSet<>();
        this.heap = heap;
    }

    public void initSystemKlasses() {
        for (String arrayName : arrayNames) {
            initArrayKlass(arrayName);
        }
        initKlass(ObjectJVM.getObjectKlass());
        loadKlass(STRING_JVM);
    }

    private void initKlass(@Nonnull Klass klass) {
        prepareKlass(klass);
        setConstantPoolKlassByName(klass.getKlassName(), klass);
    }

    public void initArrayKlass(@Nonnull String name) {
        initKlass(new Klass(name, ABSENCE));
    }

    public void setIndexByName(@Nonnull String name, int index) {
        this.indexByName.put(name, index);
    }

    @Nullable
    public Integer getInstanceKlassIndexByName(@Nonnull String name, boolean loadIfAbsent) {
        Integer index = indexByName.get(name);
        if (index != null) {
            return index;
        } else if (loadIfAbsent) {
            loadKlass(changeSystemKlassNameToJVMKlassName(name));
            return indexByName.get(name);
        }
        return null;
    }

    public void setConstantPoolKlassByName(String name, Klass cpKlass) {
        this.loadedKlasses.put(name, cpKlass);
    }

    public Klass getLoadedKlassByName(String name) {
        Klass klass = loadedKlasses.get(name);
        if (klass == null) {
            loadKlass(changeSystemKlassNameToJVMKlassName(name));
        }
        return loadedKlasses.get(name);
    }

    public void loadKlass(@Nonnull String name) {
        loadCurrentKlass(name);
        List<Method> clInitMethods = prepareCurrentAndInheritedKlasses(getLoadedKlassByName(changeJVMKlassNameToSystemKlassName(name)));

        //init Klass from top to bottom
        for (Method clInit : clInitMethods) {
            new ExecutionEngine(heap, new StackFrame(10000)).invoke(clInit);
        }
    }

    private void loadCurrentKlass(@Nonnull String name) {
        byte[] klassData = Utils.getClassFileData(name);
        Klass constantPoolKlass = new KlassParser(klassData, name).getKlass();
        setConstantPoolKlassByName(constantPoolKlass.getKlassName(), constantPoolKlass);
        if (JAVA_LANG_OBJECT.equals(constantPoolKlass.getParent()) || loadedKlasses.containsKey(constantPoolKlass.getParent())) {
            return;
        }
        loadCurrentKlass(changeSystemKlassNameToJVMKlassName(constantPoolKlass.getParent()));
    }

    private List<Method> prepareCurrentAndInheritedKlasses(Klass constantPoolKlass) {
        List<Klass> klasses = new ArrayList<>();
        Klass current = constantPoolKlass;
        while (!JAVA_LANG_OBJECT.equals(current.getParent()) && !preparedKlasses.contains(current.getParent())) {
            klasses.add(getLoadedKlassByName(current.getKlassName()));
            current = getLoadedKlassByName(current.getParent());
        }
        klasses.add(getLoadedKlassByName(current.getKlassName()));
        List<Method> clinitMethods = new ArrayList<>();
        for (int i = klasses.size() - 1; i >= 0; i--) {
            Method method = prepareKlass(klasses.get(i));
            preparedKlasses.add(klasses.get(i).getKlassName());
            if (method != null) {
                clinitMethods.add(method);
            }
        }
        return clinitMethods;
    }

    @Nullable
    private Method prepareKlass(@Nonnull Klass constantPoolKlass) {
        Integer parentKlassIndex = getInstanceKlassIndexByName(constantPoolKlass.getParent(), false);
        InstanceKlass parentKlass = parentKlassIndex != null ? heap.getInstanceKlass(parentKlassIndex) : null;
        InstanceObject object = getInstanceObject(parentKlass != null && !JAVA_LANG_OBJECT.equals(parentKlass.getName()) ?
                        heap.getInstanceObject(parentKlass.getObjectRef()) : null,
                constantPoolKlass.getKlassName(),
                heap, constantPoolKlass.getStaticFieldNames(), -1);
        int objectRef = heap.changeObject(parentKlass != null
                && !JAVA_LANG_OBJECT.equals(parentKlass.getName()) // we don't want to change InstanceObject inside Object
                ? parentKlass.getObjectRef() : -1, object);

        InstanceKlass instanceKlass = getInstanceKlass(getIndexByFieldNameFromStaticContent(object.getIndexFieldNameMap(),
                constantPoolKlass.getKlassName(), parentKlass), objectRef, constantPoolKlass);
        setIndexByName(constantPoolKlass.getKlassName(), heap.setInstanceKlass(instanceKlass));

        Map<String, Integer> allStaticMethods = new HashMap<>(
                parentKlass != null ? parentKlass.getAllIndexesByMethodName() : Collections.emptyMap());

        //find clinit and virtual methods
        Map<String, Integer> virtualMethods = new TreeMap<>(parentKlass != null ? parentKlass.getVirtualMethods() : Collections.emptyMap());
        Collection<Method> methods = constantPoolKlass.getMethods();
        Method clInit = null;
        for (Method method : methods) {
            if (CLASS_INIT.equals(method.getNameAndType())) {
                clInit = method;
                continue;
            }
            int index = heap.getMethodRepo().setMethod(method);
            if (method.isStatic()) {
                allStaticMethods.put(method.getNameAndType(), index);
            }
            if (!(method.getNameAndType().startsWith(OBJECT_INIT)) && !(method.isStatic()) && !(method.isPrivate())) {
                virtualMethods.put(method.getNameAndType(), index);
            }
        }

        instanceKlass.setAllIndexesByMethodName(allStaticMethods);

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

    public Map<String, Integer> getIndexByFieldNameFromStaticContent(@Nonnull Map<String, Integer> indexFieldName,
                                                                     @Nonnull String klassName,
                                                                     @Nullable InstanceKlass parentKlass) {
        Map<String, Integer> result = new HashMap<>(parentKlass != null ? parentKlass.getIndexByFieldName() : Collections.emptyMap());
        result.putAll(indexFieldName.keySet().stream()
                .filter(klassNameField -> klassNameField.contains(klassName))
                .collect(Collectors.toMap(field -> field.substring(field.indexOf('.') + 1), indexFieldName::get)));
        return Collections.unmodifiableMap(result);
    }

}
