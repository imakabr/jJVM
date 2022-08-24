package jvm.heap;

import jvm.JVMType;
import jvm.lang.NullPointerExceptionJVM;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public interface InstanceObject {

    @Nonnull
    Map<String, Integer> getIndexByFieldNameFromStaticContent(@Nonnull String klassName, @Nullable InstanceKlass parentKlass);

    @Nonnull
    Set<String> getFieldNames();

    int getFieldValuesSize();

    @Nonnull
    Map<String, Integer> getIndexFieldNameMap();

    long[] getFieldValues();

    boolean isArray();

    int size();

    void setValue(int index, long value);

    long getValue(int fieldIndex);

    int getIndexByFieldName(@Nonnull String name) throws NullPointerExceptionJVM;

    int getKlassIndex();

    @Nullable
    JVMType getValueType();

    @Nullable
    String getArrayType();

}
