package jvm.heap;

import jvm.JVMType;
import jvm.Utils;
import jvm.heap.api.Heap;
import jvm.heap.api.InstanceObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractInstanceObject implements InstanceObject {

    private final int klassIndex;
    private final boolean array;
    @Nonnull
    private final Heap heap;

    public AbstractInstanceObject(@Nullable String staticContentKlassName, @Nonnull Heap heap, int klassIndex) {
        this.heap = heap;
        this.array = false;
        this.klassIndex = staticContentKlassName == null ? klassIndex : -1;
    }

    public AbstractInstanceObject(@Nonnull Heap heap, int klassIndex) {
        this.heap = heap;
        this.klassIndex = klassIndex;
        this.array = true;
    }

    public boolean isArray() {
        return array;
    }

    @Nonnull
    public JVMType getValueType(@Nonnull String field) {
        String t = field.substring(field.indexOf(':') + 1);
        return t.startsWith("L") || t.startsWith("[") ? JVMType.valueOf("A") : JVMType.valueOf(t);
    }

    public long setValueType(int type) {
        return ((long) type << 32);
    }

    private int getValueType(long value) {
        int type = (int) (value >> 32);
        return type >>> 31 == 1 ? ~type : type; // if 'type >>> 31 == 1' (negative sign) type was inverted
    }

    public void checkType(long firstValue, long secondValue) {
        int first = getValueType(firstValue);
        int second = getValueType(secondValue);
        if (first == JVMType.I.ordinal() && second == JVMType.Z.ordinal() ||
                first == JVMType.Z.ordinal() && second == JVMType.I.ordinal()) {
            return;
        }
        if (first != second) {
            throw new RuntimeException("Wrong types: " + JVMType.values()[getValueType(firstValue)]
                    + " is not equal " + JVMType.values()[getValueType(secondValue)]);
        }
    }

    public int getKlassIndex() {
        return klassIndex;
    }

    @Override
    public String toString() {
        String type;
        if (array) {
            if (isCharType()) {
                return "Array | String : " + Utils.toStringFromCharArray(getFieldValues());
            } else {
                type = "Array " + heap.getInstanceKlass(klassIndex).getName() + " | Values : ";
            }
        } else {
            type = klassIndex == -1 ? "Static | Fields : " : heap.getInstanceKlass(klassIndex).getName() + " | Fields : ";
        }
        return type + Utils.toString(getFieldValues(), getFieldCount());
    }

    private boolean isCharType() {
        return getFieldCount() > 0 && getValueType(getFieldValue(0)) == JVMType.C.ordinal();
    }

    private long[] getFieldValues() {
        long[] result = new long[getFieldCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getFieldValue(i);
        }
        return result;
    }

}
