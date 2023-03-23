package jvm.heap.api;

import jvm.parser.Klass;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

public interface InstanceKlass {

    @Nonnull
    Set<String> getVirtualMethodNames();

    @Nonnull
    Set<String> getStaticFieldNames();

    @Nonnull
    Set<String> getStaticMethodNames();

    int getIndexByStaticMethodName(@Nonnull String methodName);

    int getIndexByStaticFieldName(@Nonnull String name);

    int getMethodIndex(int virtualMethodIndex);

    int getIndexByVirtualMethodName(@Nonnull String methodName);

    int getObjectRef();

    @Nonnull
    Klass getCpKlass();

    @Nonnull
    String getName();
}
