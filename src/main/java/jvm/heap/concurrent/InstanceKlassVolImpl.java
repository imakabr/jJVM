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
    private final Klass cpKlass;
    @Nonnull
    private final String name;

    public InstanceKlassVolImpl(@Nonnull Map<String, Integer> staticFieldNameToIndexMap,
                                @Nonnull Map<String, Integer> staticMethodNameToIndexMap,
                                @Nonnull Map<String, Integer> virtualMethodNameToIndexMap,
                                @Nonnull int[] virtualMethodTable,
                                int objectReference, @Nonnull Klass cpKlass) {
        this.name = cpKlass.getKlassName();
        this.objectReference = objectReference;
        this.cpKlass = cpKlass;
        this.virtualMethodNameToIndexMap = new ConcurrentHashMap<>(virtualMethodNameToIndexMap);
        this.staticFieldNameToIndexMap = new ConcurrentHashMap<>(staticFieldNameToIndexMap);
        this.staticMethodNameToIndexMap = new ConcurrentHashMap<>(staticMethodNameToIndexMap);
        this.virtualMethodTable = new AtomicIntegerArray(virtualMethodTable);
    }

    @Nonnull
    public Map<String, Integer> getVirtualMethods() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, Integer> entry : virtualMethodNameToIndexMap.entrySet()) {
            result.put(entry.getKey(), virtualMethodTable.get(entry.getValue()));
        }
        return result;
    }

    @Nonnull
    public Set<String> getStaticFieldNames() {
        return Collections.unmodifiableSet(staticFieldNameToIndexMap.keySet());
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public int getIndexByStaticMethodName(@Nonnull String methodName) {
        return staticMethodNameToIndexMap.get(methodName);
    }

    @Nonnull
    @Override
    public Set<String> getStaticMethodNames() {
        return Collections.unmodifiableSet(staticMethodNameToIndexMap.keySet());
    }

    public int getObjectRef() {
        return objectReference;
    }

    @Nonnull
    public Klass getCpKlass() {
        return cpKlass;
    }

    public int getIndexByStaticFieldName(@Nonnull String name) {
        return staticFieldNameToIndexMap.get(name);
    }

    public int getMethodIndex(int virtualMethodIndex) {
        return virtualMethodTable.get(virtualMethodIndex);
    }

    public int getIndexByVirtualMethodName(@Nonnull String methodName) {
        return virtualMethodNameToIndexMap.get(methodName);
    }

    @Override
    public String toString() {
        return name;
    }

}
