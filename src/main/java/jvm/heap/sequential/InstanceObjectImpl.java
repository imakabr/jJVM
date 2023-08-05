package jvm.heap.sequential;

import jvm.JVMType;
import jvm.heap.AbstractInstanceObject;
import jvm.heap.api.Heap;

import javax.annotation.Nonnull;

public class InstanceObjectImpl extends AbstractInstanceObject {

    @Nonnull
    private final long[] fieldValues;

    public InstanceObjectImpl(@Nonnull Heap heap, @Nonnull JVMType valueType, int size, int klassIndex) {
        super(heap, klassIndex, true);
        this.fieldValues = new long[size];
        for (int index = 0; index < size; index++) {
            setDefaultValue(index, valueType);
        }
    }

    public InstanceObjectImpl(@Nonnull Heap heap, @Nonnull JVMType[] valueType, int klassIndex) {
        super(heap, klassIndex, false);
        this.fieldValues = new long[valueType.length];
        for (int index = 0; index < valueType.length; index++) {
            setDefaultValue(index, valueType[index]);
        }
    }

    public int getFieldCount() {
        return fieldValues.length;
    }

    public void setFieldValue(int index, long value) {
        checkType(fieldValues[index], value);
        fieldValues[index] = value;
    }

    public long getFieldValue(int fieldIndex) {
        return fieldValues[fieldIndex];
    }

    private void setDefaultValue(int index, @Nonnull JVMType type) {
        fieldValues[index] = setValueType(type.ordinal());
    }

}
