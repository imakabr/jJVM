package jvm.heap.concurrent;

import jvm.heap.api.InstanceKlass;
import jvm.parser.Klass;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class InstanceKlassVolImpl implements InstanceKlass {

    private final int objectReference;
    @Nonnull
    private final Map<String, Integer> staticFieldNameToIndexMap; //fields
    @Nonnull
    private final Map<String, Integer> staticMethodNameToIndexMap; //methods
    @Nonnull
    private final AtomicIntegerArray virtualMethodTable; //virtual method table
    @Nonnull
    private final Map<String, Integer> virtualMethodNameToIndexMap;
    @Nonnull
    private final String name;

    public InstanceKlassVolImpl(@Nonnull Map<String, Integer> staticFieldNameToIndexMap,
                                @Nonnull Map<String, Integer> staticMethodNameToIndexMap,
                                @Nonnull Map<String, Integer> virtualMethodNameToIndexMap,
                                @Nonnull int[] virtualMethodTable,
                                int objectReference, @Nonnull Klass cpKlass) {
        this.name = cpKlass.getKlassName();
        this.objectReference = objectReference;
        this.virtualMethodNameToIndexMap = new ConcurrentHashMap<>(virtualMethodNameToIndexMap);
        this.staticFieldNameToIndexMap = new ConcurrentHashMap<>(staticFieldNameToIndexMap);
        this.staticMethodNameToIndexMap = new ConcurrentHashMap<>(staticMethodNameToIndexMap);
        this.virtualMethodTable = new AtomicIntegerArray(virtualMethodTable);
    }

    @Nonnull
    @Override
    public Set<String> getVirtualMethodNames() {
        return Collections.unmodifiableSet(virtualMethodNameToIndexMap.keySet());
    }

    @Nonnull
    @Override
    public Set<String> getStaticFieldNames() {
        return Collections.unmodifiableSet(staticFieldNameToIndexMap.keySet());
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getIndexByStaticMethodName(@Nonnull String methodName) {
        return staticMethodNameToIndexMap.get(methodName);
    }

    @Nonnull
    @Override
    public Set<String> getStaticMethodNames() {
        return Collections.unmodifiableSet(staticMethodNameToIndexMap.keySet());
    }

    @Override
    public int getObjectRef() {
        return objectReference;
    }

    @Override
    public int getIndexByStaticFieldName(@Nonnull String name) {
        return staticFieldNameToIndexMap.get(name);
    }

    @Override
    public int getMethodIndex(int virtualMethodIndex) {
        return virtualMethodTable.get(virtualMethodIndex);
    }

    @Override
    public int getVirtualIndexByMethodName(@Nonnull String methodName) {
        return virtualMethodNameToIndexMap.get(methodName);
    }

    @Override
    public String toString() {
        return name;
    }

}
