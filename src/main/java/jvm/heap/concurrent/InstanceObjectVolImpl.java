package jvm.heap.concurrent;

import jvm.JVMType;
import jvm.heap.AbstractInstanceObject;
import jvm.heap.api.Heap;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicLongArray;

public class InstanceObjectVolImpl extends AbstractInstanceObject {

    @Nonnull
    private final AtomicLongArray fieldValues;

    public InstanceObjectVolImpl(@Nonnull Heap heap, @Nonnull JVMType valueType, int size, int klassIndex) {
        super(heap, klassIndex, true);
        this.fieldValues = new AtomicLongArray(size);
        for (int index = 0; index < size; index++) {
            setDefaultValue(index, valueType);
        }
    }

    public InstanceObjectVolImpl(@Nonnull Heap heap, @Nonnull JVMType[] valueType, int klassIndex) {
        super(heap, klassIndex, false);
        this.fieldValues = new AtomicLongArray(valueType.length);
        for (int index = 0; index < valueType.length; index++) {
            setDefaultValue(index, valueType[index]);
        }
    }

    public int getFieldCount() {
        return fieldValues.length();
    }

    public void setFieldValue(int index, long value) {
        checkType(fieldValues.get(index), value);
        fieldValues.set(index, value);
    }

    public long getFieldValue(int fieldIndex) {
        return fieldValues.get(fieldIndex);
    }

    private void setDefaultValue(int index, @Nonnull JVMType type) {
        fieldValues.set(index, setValueType(type.ordinal()));
    }

}
