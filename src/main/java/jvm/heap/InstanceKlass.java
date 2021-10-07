package jvm.heap;

import jvm.JVMValue;
import jvm.parser.Klass;

import java.util.HashMap;
import java.util.Map;

public class InstanceKlass {
    private final int reference;
    //fields
    private final JVMValue[] fieldValues;
    private final Map<String, Integer> indexByFieldName;
    //virtual method table
    private int[] virtualMethodTable;
    private final Map<String, Integer> indexByVirtualMethodName;

    private int parentIndex;
    private final Klass cpKlass;

    public InstanceKlass(int valuesSize, int reference, Klass cpKlass) {
        this.reference = reference;
        this.fieldValues = new JVMValue[valuesSize];
        this.cpKlass = cpKlass;
        this.indexByVirtualMethodName = new HashMap<>();
        this.indexByFieldName = new HashMap<>();
    }

    public void setParentIndex(int parentIndex) {
        this.parentIndex = parentIndex;
    }

    public int getParentIndex() {
        return parentIndex;
    }

    public int getReference() {
        return reference;
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
