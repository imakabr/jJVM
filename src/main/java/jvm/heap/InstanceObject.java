package jvm.heap;

import jvm.JVMType;
import jvm.Utils;
import jvm.lang.NullPointerExceptionJVM;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class InstanceObject {
    private final long[] fieldValues;
    private final Map<String, Integer> indexByFieldName;
    private int klassIndex;
    private final boolean array;
    @Nullable
    private String arrayType;
    @Nullable
    private JVMType valueType;
    @Nonnull
    private final Heap heap;

    public InstanceObject(@Nonnull Heap heap, List<String> fields, int klassIndex) {
        this(null, null, heap, fields, klassIndex);
    }

    public InstanceObject(@Nullable InstanceObject objectFromStaticContent,
                          @Nullable String staticContentKlassName,
                          @Nonnull Heap heap,
                          @Nonnull List<String> fields,
                          int klassIndex) {
        if (staticContentKlassName == null) {
            this.heap = heap;
            this.fieldValues = new long[fields.size()];
            this.indexByFieldName = new HashMap<>();
            this.klassIndex = klassIndex;
            this.array = false;
            for (int fieldIndex = 0; fieldIndex < fieldValues.length; fieldIndex++) {
                String field = fields.get(fieldIndex);
                setDefaultValue(fieldIndex, getValueType(field));
                indexByFieldName.put(field, fieldIndex);
            }
        } else {
            // a chain of inherited classes for storing data in static fields contain a single InstanceObject
            this.heap = heap;
            this.indexByFieldName = new HashMap<>(objectFromStaticContent != null ? objectFromStaticContent.indexByFieldName : Collections.emptyMap());
            this.klassIndex = -1;
            int count = fields.size();
            this.fieldValues = new long[objectFromStaticContent != null ? objectFromStaticContent.fieldValues.length + count : fields.size()];
            this.array = false;
            if (objectFromStaticContent != null) {
                System.arraycopy(objectFromStaticContent.fieldValues, 0, this.fieldValues, 0, objectFromStaticContent.fieldValues.length);
            }
            for (String newField : fields) {
                int index = fieldValues.length - count;
                indexByFieldName.put(staticContentKlassName + "." + newField, index);
                setDefaultValue(index, getValueType(newField));
                count--;
            }
        }
    }

    @Nonnull
    public Map<String, Integer> getIndexByFieldNameFromStaticContent(@Nonnull String klassName, @Nullable InstanceKlass parentKlass) {
        Map<String, Integer> result = new HashMap<>(parentKlass != null ? parentKlass.getIndexByFieldName() : Collections.emptyMap());
        result.putAll(indexByFieldName.keySet()
                .stream()
                .filter(klassNameField -> klassNameField.contains(klassName))
                .collect(Collectors.toMap(field -> field.substring(field.indexOf('.') + 1), indexByFieldName::get)));
        return result;
    }

    public InstanceObject(@Nonnull Heap heap, @Nonnull String arrayType, @Nonnull String valueType, int size, int klassIndex) {
        this.heap = heap;
        this.fieldValues = new long[size];
        this.indexByFieldName = new HashMap<>();
        this.klassIndex = klassIndex;
        this.array = true;
        this.valueType = getValueType(valueType);
        this.arrayType = arrayType;
        for (int index = 0; index < size; index++) {
            setDefaultValue(index, getValueType(valueType));
        }
    }

    public Set<String> getFieldNames() {
        return indexByFieldName.keySet();
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
        int first = getValueType(firstValue);
        int second = getValueType(secondValue);
        if (first == JVMType.I.ordinal() && second == JVMType.Z.ordinal() ||
                first == JVMType.Z.ordinal() && second == JVMType.I.ordinal()) {
            return;
        }
        if (first != second ) {
            throw new RuntimeException("Wrong types: " + JVMType.values()[getValueType(firstValue)] + " is not equal " + JVMType.values()[getValueType(secondValue)]);
        }
    }

    public void setKlassIndex(int klassIndex) {
        this.klassIndex = klassIndex;
    }

    public void setValue(int index, long value) {
        checkType(fieldValues[index], value);
        fieldValues[index] = value;
    }

    public long getValue(int fieldIndex) {
        return fieldValues[fieldIndex];
    }

    public void setIndexByFieldName(String name, int index) {
        indexByFieldName.put(name, index);
    }

    public int getIndexByFieldName(String name) throws NullPointerExceptionJVM {
        Integer result = indexByFieldName.get(name);
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
        String fields = Utils.toString(fieldValues, fieldValues.length);
        String str = valueType == JVMType.C ? " | String : " + Utils.toStringFromCharArray(fieldValues) : "";
        return type + (!str.isEmpty() ? str : (fields.isEmpty() ? "absence" : fields));
    }
}
