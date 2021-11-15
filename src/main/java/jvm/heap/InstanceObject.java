package jvm.heap;

import jvm.JVMType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceObject {
    private final long[] fieldValues;
    private final Map<String, Integer> indexByFieldName;
    private int klassIndex;

    public InstanceObject(List<String> fields, int klassIndex) {
        this.fieldValues = new long[fields.size()];
        this.indexByFieldName = new HashMap<>();
        this.klassIndex = klassIndex;
        for (int fieldIndex = 0; fieldIndex < fieldValues.length; fieldIndex++) {
            String field = fields.get(fieldIndex);
            setDefaultValue(fieldIndex, field);
            indexByFieldName.put(field, fieldIndex);
        }
    }

    private void setDefaultValue(int index, String field) {
        String t = field.substring(field.indexOf(':') + 1);
        int type = t.startsWith("L") ? JVMType.valueOf("A").ordinal() : JVMType.valueOf(t).ordinal();
        fieldValues[index] = setTypeValue(type);
    }

    private long setTypeValue(int type) {
        return ((long) type << 32);
    }

    private int getTypeValue(long value) {
        return (int) (value >> 32);
    }

    private void checkType(long firstValue, long secondValue) {
        if (getTypeValue(firstValue) != getTypeValue(secondValue)) {
            throw new RuntimeException("Wrong types: " + JVMType.values()[getTypeValue(firstValue)] + " is not equal " + JVMType.values()[getTypeValue(secondValue)]);
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
