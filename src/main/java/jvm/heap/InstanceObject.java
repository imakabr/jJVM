package jvm.heap;

import jvm.JVMType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceObject {
    private final long[] fieldValues;
    private final Map<String, Integer> indexByFieldName;
    private int klassIndex;
    private final boolean array;

    public InstanceObject(List<String> fields, int klassIndex) {
        this.fieldValues = new long[fields.size()];
        this.indexByFieldName = new HashMap<>();
        this.klassIndex = klassIndex;
        this.array = false;
        for (int fieldIndex = 0; fieldIndex < fieldValues.length; fieldIndex++) {
            String field = fields.get(fieldIndex);
            setDefaultValue(fieldIndex, field);
            indexByFieldName.put(field, fieldIndex);
        }
    }

    public InstanceObject(String type, int size, int klassIndex) {
        this.fieldValues = new long[size];
        this.indexByFieldName = new HashMap<>();
        this.klassIndex = klassIndex;
        this.array = true;
        for (int index = 0; index < size; index++) {
            setDefaultValue(index, type);
        }
    }

    public boolean isArray() {
        return array;
    }

    public int size() {
        return fieldValues.length;
    }

    private void setDefaultValue(int index, String field) {
        String t = field.substring(field.indexOf(':') + 1);
        int type = t.startsWith("L") ? JVMType.valueOf("A").ordinal() : JVMType.valueOf(t).ordinal();
        fieldValues[index] = setValueType(type);
    }

    private long setValueType(int type) {
        return ((long) type << 32);
    }

    private int getValueType(long value) {
        return (int) (value >> 32);
    }

    private void checkType(long firstValue, long secondValue) {
        if (getValueType(firstValue) != getValueType(secondValue)) {
            throw new RuntimeException("Wrong types: " + JVMType.values()[getValueType(firstValue)] + " is not equal " + JVMType.values()[getValueType(secondValue)]);
        }
    }

    public void setKlassIndex(int klassIndex) {
        this.klassIndex = klassIndex;
    }

    public void setValue(int index, long value) {
        checkType(value, fieldValues[index]);
        fieldValues[index] = value;
    }

    public long getValue(int fieldIndex) {
        return fieldValues[fieldIndex];
    }

    public void setIndexByFieldName(String name, int index) {
        indexByFieldName.put(name, index);
    }

    public int getIndexByFieldName(String name) {
        return indexByFieldName.get(name);
    }

    public int getKlassIndex() {
        return klassIndex;
    }
}
