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
    private final Map<String, Integer> indexByFieldName;

    public InstanceObjectImpl(@Nullable InstanceObject objectFromStaticContent,
                              @Nullable String staticContentKlassName,
                              @Nonnull Heap heap,
                              @Nonnull List<String> fields,
                              int klassIndex) {
        super(staticContentKlassName, heap, klassIndex);
        if (staticContentKlassName == null) {
            this.fieldValues = new long[fields.size()];
            this.indexByFieldName = new HashMap<>();
            for (int fieldIndex = 0; fieldIndex < fieldValues.length; fieldIndex++) {
                String field = fields.get(fieldIndex);
                setDefaultValue(fieldIndex, getValueType(field));
                indexByFieldName.put(field, fieldIndex);
            }
        } else {
            // a chain of inherited classes for storing data in static fields contain a single InstanceObject
            this.indexByFieldName = new HashMap<>(objectFromStaticContent != null ?
                    objectFromStaticContent.getIndexFieldNameMap() : Collections.emptyMap());
            int count = fields.size();
            this.fieldValues = new long[objectFromStaticContent != null ?
                    objectFromStaticContent.getFieldValuesSize() + count : fields.size()];
            if (objectFromStaticContent != null) {
                System.arraycopy(objectFromStaticContent.getFieldValues(), 0,
                        this.fieldValues, 0,
                        objectFromStaticContent.getFieldValuesSize());
            }
            for (String newField : fields) {
                int index = fieldValues.length - count;
                indexByFieldName.put(staticContentKlassName + "." + newField, index);
                setDefaultValue(index, getValueType(newField));
                count--;
            }
        }
    }

    public InstanceObjectImpl(@Nonnull Heap heap, @Nonnull JVMType valueType, int size, int klassIndex) {
        super(heap, klassIndex);
        this.fieldValues = new long[size];
        this.indexByFieldName = new HashMap<>();
        for (int index = 0; index < size; index++) {
            setDefaultValue(index, valueType);
        }
    }

    public InstanceObjectImpl(@Nonnull Heap heap, @Nonnull List<String> fields, int klassIndex) {
        this(null, null, heap, fields, klassIndex);
    }

    @Nonnull
    public Set<String> getFieldNames() {
        return Collections.unmodifiableSet(indexByFieldName.keySet());
    }

    public int size() {
        return fieldValues.length;
    }

    public long[] getFieldValues() {
        return fieldValues;
    }

    public void setValue(int index, long value) {
        checkType(fieldValues[index], value);
        fieldValues[index] = value;
    }

    public long getValue(int fieldIndex) {
        return fieldValues[fieldIndex];
    }

    @Nonnull
    public Map<String, Integer> getIndexFieldNameMap() {
        return Collections.unmodifiableMap(indexByFieldName);
    }

    public int getFieldValuesSize() {
        return fieldValues.length;
    }

    private void setDefaultValue(int index, @Nonnull JVMType type) {
        fieldValues[index] = setValueType(type.ordinal());
    }

}
