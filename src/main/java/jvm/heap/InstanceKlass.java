package jvm.heap;

import jvm.JVMType;
import jvm.JVMValue;
import jvm.parser.Klass;

import java.util.*;

public class InstanceKlass {
    private final int objectReference;
    //fields
    private final JVMValue[] fieldValues;
    private final Map<String, Integer> indexByFieldName;
    //virtual method table
    private int[] virtualMethodTable;
    private final Map<String, Integer> indexByVirtualMethodName;

    private int parentIndex;
    private final Klass cpKlass;

    public InstanceKlass(List<String> fields, int objectReference, Klass cpKlass) {
        this.objectReference = objectReference;
        this.fieldValues = new JVMValue[fields.size()];
        this.cpKlass = cpKlass;
        this.indexByVirtualMethodName = new HashMap<>();
        this.indexByFieldName = new HashMap<>();
        for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++) {
            fieldValues[fieldIndex] = new JVMValue(JVMType.I, 0);
            indexByFieldName.put(fields.get(fieldIndex), fieldIndex);
        }
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

    public void setValue(int index, JVMValue value) {
        fieldValues[index] = value;
    }

    public JVMValue getValue(int fieldValIndex) {
        return fieldValues[fieldValIndex];
    }

    public void setIndexByFieldName(String name, int index) {
        indexByFieldName.put(name, index);
    }

    public void setIndexByVirtualMethodName(String name, int index) {
        indexByVirtualMethodName.put(name, index);
    }

    public JVMValue[] getFieldValues() {
        return fieldValues;
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

    public int getIndexByMethodName(String methodName) {
        return indexByVirtualMethodName.get(methodName);
    }
}
