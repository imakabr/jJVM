package jvm.heap.concurrent;

import jvm.JVMType;
import jvm.heap.AbstractInstanceObject;
import jvm.heap.api.Heap;
import jvm.heap.api.InstanceObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongArray;

public class InstanceObjectVolImpl extends AbstractInstanceObject {

    @Nonnull
    private final AtomicLongArray fieldValues;
    @Nonnull
    private final Map<String, Integer> indexByFieldName;

    public InstanceObjectVolImpl(@Nullable InstanceObject objectFromStaticContent,
                                 @Nullable String staticContentKlassName,
                                 @Nonnull Heap heap,
                                 @Nonnull List<String> fields,
                                 int klassIndex) {
        super(staticContentKlassName, heap, klassIndex);
        if (staticContentKlassName == null) {
            this.fieldValues = new AtomicLongArray(fields.size());
            this.indexByFieldName = new ConcurrentHashMap<>();
            for (int fieldIndex = 0; fieldIndex < fieldValues.length(); fieldIndex++) {
                String field = fields.get(fieldIndex);
                setDefaultValue(fieldIndex, getValueType(field));
                indexByFieldName.put(field, fieldIndex);
            }
        } else {
            // a chain of inherited classes for storing data in static fields contain a single InstanceObject
            this.indexByFieldName = new ConcurrentHashMap<>(objectFromStaticContent != null ?
                    objectFromStaticContent.getIndexFieldNameMap() : Collections.emptyMap());
            int count = fields.size();
            this.fieldValues = new AtomicLongArray(objectFromStaticContent != null ?
                    objectFromStaticContent.getFieldValuesSize() + count : fields.size());
            if (objectFromStaticContent != null) {
                for (int i = 0; i < objectFromStaticContent.getFieldValuesSize(); i++) {
                    this.fieldValues.set(i, objectFromStaticContent.getFieldValue(i));
                }
            }
            for (String newField : fields) {
                int index = fieldValues.length() - count;
                indexByFieldName.put(staticContentKlassName + "." + newField, index);
                setDefaultValue(index, getValueType(newField));
                count--;
            }
        }
    }

    public InstanceObjectVolImpl(@Nonnull Heap heap, @Nonnull JVMType valueType, int size, int klassIndex) {
        super(heap, klassIndex);
        this.fieldValues = new AtomicLongArray(size);
        this.indexByFieldName = new ConcurrentHashMap<>();
        for (int index = 0; index < size; index++) {
            setDefaultValue(index, valueType);
        }
    }

    public InstanceObjectVolImpl(@Nonnull Heap heap, @Nonnull List<String> fields, int klassIndex) {
        this(null, null, heap, fields, klassIndex);
    }

    @Nonnull
    public Set<String> getFieldNames() {
        return Collections.unmodifiableSet(indexByFieldName.keySet());
    }

    public int getFieldValuesSize() {
        return fieldValues.length();
    }

    public long[] getFieldValues() {
        long[] result = new long[fieldValues.length()];
        for (int i = 0; i < fieldValues.length(); i++) {
            result[i] = fieldValues.get(i);
        }
        return result;
    }

    public void setFieldValue(int index, long value) {
        checkType(fieldValues.get(index), value);
        fieldValues.set(index, value);
    }

    public long getFieldValue(int fieldIndex) {
        return fieldValues.get(fieldIndex);
    }

    @Nonnull
    public Map<String, Integer> getIndexFieldNameMap() {
        return Collections.unmodifiableMap(indexByFieldName);
    }

    private void setDefaultValue(int index, @Nonnull JVMType type) {
        fieldValues.set(index, setValueType(type.ordinal()));
    }

}
