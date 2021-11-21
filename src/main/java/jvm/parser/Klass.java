package jvm.parser;

import java.util.*;
import java.util.stream.Collectors;

import static jvm.parser.ParserConstants.ACC_STATIC;

public final class Klass {

    private String name;
    private String superClass;

    private ConstantPoolEntry[] items;

    private final Map<Short, String> klassNameByCPIndex = new HashMap<>();
    private final Map<Short, String> methodNameByCPIndex = new HashMap<>();
    private final Map<Short, String> fieldNameByCPIndex = new HashMap<>();

    private final List<Field> objectFields = new ArrayList<>();
    private final List<Field> staticFields = new ArrayList<>();

    private final Map<String, Method> methodByName = new LinkedHashMap<>();

    public Klass() {
    }

    public Klass(String name, String superClassName) {
        setKlassName(name);
        setSuperClassName(superClassName);
    }

    public void setCPItems(ConstantPoolEntry[] items) {
        this.items = items;
    }

    public ConstantPoolEntry getCPItem(int index) {
        return items[index];
    }

    public void setKlassName(String name) {
        this.name = name;
    }

    public String getKlassName() {
        return name;
    }

    public void setSuperClassName(String superClass) {
        this.superClass = superClass;
    }

    public String getParent() {
        return superClass;
    }

    public void addMethod(Method m) {
        methodByName.put(m.getNameAndType(), m);
    }

    public void addMethods(Method[] m) {
        for (Method method : m) {
            methodByName.put(method.getNameAndType(), method);
        }
    }

    public void addCPMethodRef(short index, String methodName) {
        methodNameByCPIndex.put(index, methodName);
    }

    public void addCPKlassRef(short index, String methodName) {
        klassNameByCPIndex.put(index, methodName);
    }

    public Method getMethodByName(String nameAndType) {
        return methodByName.get(nameAndType);
    }

    public Collection<Method> getMethods() {
        return methodByName.values();
    }

    public List<Field> getStaticFields() {
        return staticFields;
    }

    public List<String> getStaticFieldNames() {
        return staticFields.stream().map(Field::getName).collect(Collectors.toList());
    }

    public String getMethodNameByCPIndex(short cpIndex) {
        return methodNameByCPIndex.get(cpIndex);
    }

    public String getKlassNameByCPIndex(short cpIndex) {
        return klassNameByCPIndex.get(cpIndex);
    }

    public void addField(Field field) {
        if ((field.getFlags() & ACC_STATIC) > 0) {
            staticFields.add(field);
        } else {
            objectFields.add(field);
        }
    }

    public void addCPFieldRef(short index, String name) {
        fieldNameByCPIndex.put(index, name);
    }

    public String getFieldByCPIndex(short cpIndex) {
        return fieldNameByCPIndex.get(cpIndex);
    }

    public List<Field> getObjectFields() {
        return objectFields;
    }

    public List<String> getObjectFieldNames() {
        return objectFields.stream().map(Field::getName).collect(Collectors.toList());
    }

}
