package jvm.heap.api;

import jvm.JVMType;

import javax.annotation.Nonnull;
import java.util.Set;

public interface InstanceKlass {

    @Nonnull
    Set<String> getVirtualMethodNames();

    @Nonnull
    Set<String> getStaticFieldNames();

    @Nonnull
    Set<String> getFieldNames();

    @Nonnull
    JVMType[] getFieldTypes();

    @Nonnull
    Set<String> getStaticMethodNames();

    int getIndexByStaticMethodName(@Nonnull String methodName);

    int getIndexByStaticFieldName(@Nonnull String name);

    int getIndexByFieldName(@Nonnull String fieldName);

    int getMethodIndex(int virtualMethodIndex);

    int getVirtualIndexByMethodName(@Nonnull String methodName);

    int getObjectRef();

    @Nonnull
    String getName();
}
