package jvm.heap.api;

import javax.annotation.Nonnull;
import java.util.Set;

public interface InstanceObject {

    @Nonnull
    Set<String> getFieldNames();

    boolean isArray();

    int getFieldCount();

    void setFieldValue(int index, long value);

    long getFieldValue(int fieldIndex);

    int getIndexByFieldName(@Nonnull String fieldName);

    int getKlassIndex();

}
