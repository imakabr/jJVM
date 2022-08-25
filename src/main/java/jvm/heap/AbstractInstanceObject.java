package jvm.heap;

import jvm.JVMType;
import jvm.Utils;
import jvm.heap.api.Heap;
import jvm.heap.api.InstanceKlass;
import jvm.heap.api.InstanceObject;
import jvm.heap.sequential.InstanceObjectImpl;
import jvm.lang.NullPointerExceptionJVM;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractInstanceObject implements InstanceObject {

    private final int klassIndex;
    private final boolean array;
    @Nullable
    private String arrayType;
    @Nullable
    private JVMType valueType;
    @Nonnull
    private final Heap heap;
    private static boolean heapMonitor;

    public AbstractInstanceObject(@Nullable String staticContentKlassName, @Nonnull Heap heap, int klassIndex) {
        this.heap = heap;
        this.array = false;
        this.klassIndex = staticContentKlassName == null ? klassIndex : -1;
    }

    public AbstractInstanceObject(@Nonnull Heap heap, @Nonnull String arrayType, @Nonnull String valueType, int klassIndex) {
        this.heap = heap;
        this.klassIndex = klassIndex;
        this.array = true;
        this.valueType = getValueType(valueType);
        this.arrayType = arrayType;
    }


    public static void setHeapMonitor(boolean heapMonitor) {
        AbstractInstanceObject.heapMonitor = true;
    }

    @Nonnull
    public static InstanceObject valueOf(@Nullable InstanceObject objectFromStaticContent,
                                         @Nullable String staticContentKlassName,
                                         @Nonnull Heap heap,
                                         @Nonnull List<String> fields,
                                         int klassIndex) {
        return heapMonitor ? new InstanceObjectImpl(objectFromStaticContent, staticContentKlassName, heap, fields, klassIndex) : null;
    }

    @Nonnull
    public static InstanceObject valueOf(@Nonnull Heap heap,
                                         @Nonnull String arrayType,
                                         @Nonnull String valueType,
                                         int size,
                                         int klassIndex) {
        return heapMonitor ? new InstanceObjectImpl(heap, arrayType, valueType, size, klassIndex) : null;
    }

    @Nonnull
    public static InstanceObject valueOf(@Nonnull Heap heap, @Nonnull List<String> fields, int klassIndex) {
        return heapMonitor ? new InstanceObjectImpl(heap, fields, klassIndex) : null;
    }

    @Nonnull
    public Map<String, Integer> getIndexByFieldNameFromStaticContent(@Nonnull String klassName, @Nullable InstanceKlass parentKlass) {
        Map<String, Integer> result = new HashMap<>(parentKlass != null ? parentKlass.getIndexByFieldName() : Collections.emptyMap());
        result.putAll(getIndexFieldNameMap().keySet()
                .stream()
                .filter(klassNameField -> klassNameField.contains(klassName))
                .collect(Collectors.toMap(field -> field.substring(field.indexOf('.') + 1),
                        field -> getIndexFieldNameMap().get(field))));
        return Collections.unmodifiableMap(result);
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
            throw new RuntimeException("Wrong types: " + JVMType.values()[getValueType(firstValue)] + " is not equal " + JVMType.values()[getValueType(secondValue)]);
        }
    }

    public int getIndexByFieldName(@Nonnull String name) throws NullPointerExceptionJVM {
        Integer result = getIndexFieldNameMap().get(name);
        if (result == null) {
            throw new NullPointerExceptionJVM();
        }
        return result;
    }

    public int getKlassIndex() {
        return klassIndex;
    }

    @Nullable
    public JVMType getValueType() {
        return valueType;
    }

    @Nullable
    public String getArrayType() {
        return arrayType;
    }

    @Override
    public String toString() {
        String type = array ? "Array" : klassIndex == -1 ? "Object | Fields : " : heap.getInstanceKlass(klassIndex).getName() + " | Fields : ";
        type = array && valueType != JVMType.C ? type + " | Values : " : type;
        String fields = Utils.toString(getFieldValues(), getFieldValuesSize());
        String str = valueType == JVMType.C ? " | String : " + Utils.toStringFromCharArray(getFieldValues()) : "";
        return type + (!str.isEmpty() ? str : (fields.isEmpty() ? "absence" : fields));
    }

}
