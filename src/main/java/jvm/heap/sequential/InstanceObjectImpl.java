package jvm.heap.sequential;

import jvm.JVMType;
import jvm.heap.AbstractInstanceObject;
import jvm.heap.api.Heap;
import jvm.heap.api.InstanceObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class InstanceObjectImpl extends AbstractInstanceObject {

    @Nonnull
    private final long[] fieldValues;
    @Nonnull
    private final Map<String, Integer> fieldNameToIndexMap;

    public InstanceObjectImpl(@Nullable InstanceObject objectFromStaticContent,
                              @Nullable String staticContentKlassName,
                              @Nonnull Heap heap,
                              @Nonnull List<String> fields,
                              int klassIndex) {
        super(staticContentKlassName, heap, klassIndex);
        if (staticContentKlassName == null) {
            this.fieldValues = new long[fields.size()];
            this.fieldNameToIndexMap = new HashMap<>();
            for (int fieldIndex = 0; fieldIndex < fieldValues.length; fieldIndex++) {
                String field = fields.get(fieldIndex);
                setDefaultValue(fieldIndex, getValueType(field));
                fieldNameToIndexMap.put(field, fieldIndex);
            }
        } else {
            // a chain of inherited classes for storing data in static fields contains a single InstanceObject
            this.fieldNameToIndexMap = new HashMap<>(objectFromStaticContent != null ?
                    getFieldNameIndexMap(objectFromStaticContent) : Collections.emptyMap());
            int count = fields.size();
            this.fieldValues = new long[objectFromStaticContent != null ?
                    objectFromStaticContent.getFieldCount() + count : fields.size()];
            if (objectFromStaticContent != null) {
                for (int i = 0; i < objectFromStaticContent.getFieldCount(); i++) {
                    this.fieldValues[i] = objectFromStaticContent.getFieldValue(i);
                }
            }
            for (String newField : fields) {
                int index = fieldValues.length - count;
                fieldNameToIndexMap.put(staticContentKlassName + "." + newField, index);
                setDefaultValue(index, getValueType(newField));
                count--;
            }
        }
    }

    private Map<String, Integer> getFieldNameIndexMap(@Nonnull InstanceObject object) {
        Map<String, Integer> result = new HashMap<>();
        for (String fieldName : object.getFieldNames()) {
            result.put(fieldName, object.getIndexByFieldName(fieldName));
        }
        return result;
    }

    public InstanceObjectImpl(@Nonnull Heap heap, @Nonnull JVMType valueType, int size, int klassIndex) {
        super(heap, klassIndex);
        this.fieldValues = new long[size];
        this.fieldNameToIndexMap = new HashMap<>();
        for (int index = 0; index < size; index++) {
            setDefaultValue(index, valueType);
        }
    }

    public InstanceObjectImpl(@Nonnull Heap heap, @Nonnull List<String> fields, int klassIndex) {
        this(null, null, heap, fields, klassIndex);
    }

    @Nonnull
    public Set<String> getFieldNames() {
        return Collections.unmodifiableSet(fieldNameToIndexMap.keySet());
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

    @Override
    public int getIndexByFieldName(@Nonnull String fieldName) {
        return fieldNameToIndexMap.get(fieldName);
    }

    private void setDefaultValue(int index, @Nonnull JVMType type) {
        fieldValues[index] = setValueType(type.ordinal());
    }

}
