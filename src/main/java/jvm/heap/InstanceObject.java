package jvm.heap;

import jvm.JVMType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InstanceObject {
    private final long[] fieldValues;
    private final Map<String, Integer> indexByFieldName;
    private int klassIndex;
    private final boolean array;
    @Nullable
    private JVMType arrayType;

    public InstanceObject(List<String> fields, int klassIndex) {
        this.fieldValues = new long[fields.size()];
        this.indexByFieldName = new HashMap<>();
        this.klassIndex = klassIndex;
        this.array = false;
        for (int fieldIndex = 0; fieldIndex < fieldValues.length; fieldIndex++) {
            String field = fields.get(fieldIndex);
            setDefaultValue(fieldIndex, getValueType(field));
            indexByFieldName.put(field, fieldIndex);
        }
    }

    public InstanceObject(String type, int size, int klassIndex) {
        this.fieldValues = new long[size];
        this.indexByFieldName = new HashMap<>();
        this.klassIndex = klassIndex;
        this.array = true;
        this.arrayType = getValueType(type);
        for (int index = 0; index < size; index++) {
            setDefaultValue(index, getValueType(type));
        }
    }

    public long[] getFieldValues() {
        return fieldValues;
    }

    public boolean isArray() {
        return array;
    }

    public int size() {
        return fieldValues.length;
    }

    @Nonnull
    private JVMType getValueType(@Nonnull String field) {
        String t = field.substring(field.indexOf(':') + 1);
        return t.startsWith("L") || t.startsWith("[") ? JVMType.valueOf("A") : JVMType.valueOf(t);
    }

    private void setDefaultValue(int index, @Nonnull JVMType type) {
        fieldValues[index] = setValueType(type.ordinal());
    }

    private long setValueType(int type) {
        return ((long) type << 32);
    }

    private int getValueType(long value) {
        int type = (int) (value >> 32);
        return type >>> 31 == 1 ? ~type : type; // if 'type >>> 31 == 1' (negative sign) type was inverted
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

    @Nullable
    public JVMType getArrayType() {
        return arrayType;
    }
}
