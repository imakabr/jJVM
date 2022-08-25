package jvm.heap.api;

import jvm.parser.Klass;

import javax.annotation.Nonnull;
import java.util.Map;

public interface InstanceKlass {

    @Nonnull
    Map<String, Integer> getVirtualMethods();

    @Nonnull
    Map<String, Integer> getIndexByFieldName();

    @Nonnull
    String getName();

    int getIndexByMethodName(@Nonnull String methodName);

    @Nonnull
    Map<String, Integer> getAllIndexesByMethodName();

    void setAllIndexesByMethodName(@Nonnull Map<String, Integer> indexByFieldName);

    int getObjectRef();

    @Nonnull
    Klass getCpKlass();

    void setIndexByVirtualMethodName(@Nonnull String name, int index);

    int getIndexByFieldName(@Nonnull String name);

    void setVirtualMethodTable(int[] virtualMethodTable);

    int getMethodIndex(int virtualMethodIndex);

    int getIndexByVirtualMethodName(@Nonnull String methodName);
}
