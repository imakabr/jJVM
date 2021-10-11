package jvm.parser;

import java.util.*;
import java.util.stream.Collectors;

import static jvm.parser.ParserConstants.ACC_STATIC;

public final class Klass {

    private String name;
    private String superClass;

    private final Map<Short, String> klassNamesByIndex = new HashMap<>();
    private final Map<String, Method> methodsByName = new HashMap<>();
    private final Map<Short, String> methodNamesByIndex = new HashMap<>();
    private final List<Field> objectFields = new ArrayList<>();
    private final List<Field> staticFields = new ArrayList<>();
    private final Map<Short, String> fieldNamesByCPIndex = new HashMap<>();

    public Klass() {
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

    public void addDefinedMethod(Method m) {
        methodsByName.put(m.getNameAndType(), m);
    }

    public void addCPMethodRef(short index, String methodName) {
        methodNamesByIndex.put(index, methodName);
    }

    public void addCPKlassRef(short index, String methodName) {
        klassNamesByIndex.put(index, methodName);
    }

    public Method getMethodByName(String nameAndType) {
        return methodsByName.get(nameAndType);
    }

    public Collection<Method> getMethods() {
        return methodsByName.values();
    }

    public List<Field> getStaticFields() {
        return staticFields;
    }

    public List<String> getStaticFieldNames() {
        return staticFields.stream().map(Field::getName).collect(Collectors.toList());
    }

    public String getMethodNameByCPIndex(short cpIndex) {
        return methodNamesByIndex.get(cpIndex);
    }

    public String getKlassNameByCPIndex(short cpIndex) {
        return klassNamesByIndex.get(cpIndex);
    }

    public void addField(Field field) {
        if ((field.getFlags() & ACC_STATIC) > 0) {
            staticFields.add(field);
        } else {
            objectFields.add(field);
        }
    }

    public void addCPFieldRef(short index, String name) {
        fieldNamesByCPIndex.put(index, name);
    }

    public String getFieldByCPIndex(short cpIndex) {
        return fieldNamesByCPIndex.get(cpIndex);
    }

    public List<Field> getObjectFields() {
        return objectFields;
    }

    public List<String> getObjectFieldNames() {
        return objectFields.stream().map(Field::getName).collect(Collectors.toList());
    }

}
