package jvm.heap.api;

import jvm.parser.Klass;

import javax.annotation.Nonnull;
import java.util.Map;

public interface InstanceKlass {

    @Nonnull
    Map<String, Integer> getVirtualMethods();

    @Nonnull
    Map<String, Integer> getStaticFieldNameToIndexMap();

    @Nonnull
    String getName();

    int getIndexByStaticMethodName(@Nonnull String methodName);

    @Nonnull
    Map<String, Integer> getStaticMethodNameToIndexMap();

    int getObjectRef();

    @Nonnull
    Klass getCpKlass();

    int getIndexByStaticFieldName(@Nonnull String name);

    int getMethodIndex(int virtualMethodIndex);

    int getIndexByVirtualMethodName(@Nonnull String methodName);
}
