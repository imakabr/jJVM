package jvm.heap;

import jvm.parser.Klass;

import java.util.*;

public class InstanceKlass {
    private final int objectReference;
    //fields
    private final Map<String, Integer> indexByFieldName;
    //methods
    private Map<String, Integer> indexByMethodName;
    //virtual method table
    private int[] virtualMethodTable;
    private final Map<String, Integer> indexByVirtualMethodName;

    private int parentIndex;
    private final Klass cpKlass;
    private final String name;

    public InstanceKlass(List<String> fields, int objectReference, Klass cpKlass) {
        this.name = cpKlass.getKlassName();
        this.objectReference = objectReference;
        this.cpKlass = cpKlass;
        this.indexByVirtualMethodName = new HashMap<>();
        this.indexByFieldName = new HashMap<>();
        for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
            indexByFieldName.put(fields.get(fieldIndex), fieldIndex);
        }
    }

    public Map<String, Integer> getVirtualMethods() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : indexByVirtualMethodName.entrySet()) {
            result.put(entry.getKey(), virtualMethodTable[entry.getValue()]);
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public int getIndexByMethodName(String methodName) {
        return indexByMethodName.get(methodName);
    }

    public Map<String, Integer> getAllIndexesByMethodName() {
        return indexByMethodName;
    }

    public void setAllIndexesByMethodName(Map<String, Integer> indexByFieldName) {
        this.indexByMethodName = indexByFieldName;
    }

    public void setParentIndex(int parentIndex) {
        this.parentIndex = parentIndex;
    }

    public int getParentIndex() {
        return parentIndex;
    }

    public int getObjectRef() {
        return objectReference;
    }

    public Klass getCpKlass() {
        return cpKlass;
    }

    public void setIndexByFieldName(String name, int index) {
        indexByFieldName.put(name, index);
    }

    public void setIndexByVirtualMethodName(String name, int index) {
        indexByVirtualMethodName.put(name, index);
    }

    public int getIndexByFieldName(String name) {
        return indexByFieldName.get(name);
    }

    public List<String> getOrderedFieldNames() {
        String[] result = new String[indexByFieldName.size()];
        for (Map.Entry<String, Integer> entry : indexByFieldName.entrySet()) {
            result[entry.getValue()] = entry.getKey();
        }
        return Arrays.asList(result);
    }

    public final void setVirtualMethodTable(int[] virtualMethodTable) {
        this.virtualMethodTable = virtualMethodTable;
    }

    public int getMethodIndex(int virtualMethodIndex) {
        return virtualMethodTable[virtualMethodIndex];
    }

    public int getIndexByVirtualMethodName(String methodName) {
        return indexByVirtualMethodName.get(methodName);
    }

    @Override
    public String toString() {
        return name;
    }
}
