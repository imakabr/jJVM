package jvm.heap.api;

import jvm.lang.NullPointerExceptionJVM;

import javax.annotation.Nonnull;
import java.util.Set;

public interface InstanceObject {

    @Nonnull
    Set<String> getFieldNames();

    boolean isArray();

    int getFieldValuesSize();

    void setFieldValue(int index, long value);

    long getFieldValue(int fieldIndex);

    int getIndexByFieldName(@Nonnull String name) throws NullPointerExceptionJVM;

    int getKlassIndex();

}
