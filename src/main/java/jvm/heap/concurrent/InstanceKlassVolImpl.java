package jvm.heap.concurrent;

import jvm.heap.api.InstanceKlass;
import jvm.parser.Klass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static java.util.Objects.requireNonNull;

public class InstanceKlassVolImpl implements InstanceKlass {

    private final int objectReference;
    @Nonnull
    private final Map<String, Integer> indexByFieldName; //fields
    @Nullable
    private Map<String, Integer> indexByMethodName; //methods
    @Nullable
    private AtomicIntegerArray virtualMethodTable; //virtual method table
    @Nonnull
    private final Map<String, Integer> indexByVirtualMethodName;

    @Nonnull
    private final Klass cpKlass;
    @Nonnull
    private final String name;

    public InstanceKlassVolImpl(@Nonnull Map<String, Integer> indexByFieldName, int objectReference, @Nonnull Klass cpKlass) {
        this.name = cpKlass.getKlassName();
        this.objectReference = objectReference;
        this.cpKlass = cpKlass;
        this.indexByVirtualMethodName = new ConcurrentHashMap<>();
        this.indexByFieldName = new ConcurrentHashMap<>(indexByFieldName);
    }

    @Nonnull
    public Map<String, Integer> getVirtualMethods() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        for (Map.Entry<String, Integer> entry : indexByVirtualMethodName.entrySet()) {
            result.put(entry.getKey(), requireNonNull(virtualMethodTable).get(entry.getValue()));
        }
        return result;
    }

    @Nonnull
    public Map<String, Integer> getIndexByFieldName() {
        return indexByFieldName;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public int getIndexByMethodName(@Nonnull String methodName) {
        return requireNonNull(indexByMethodName).get(methodName);
    }

    @Nonnull
    public Map<String, Integer> getAllIndexesByMethodName() {
        return requireNonNull(indexByMethodName);
    }

    public void setAllIndexesByMethodName(@Nonnull Map<String, Integer> indexByFieldName) {
        this.indexByMethodName = new ConcurrentHashMap<>(indexByFieldName);
    }

    public int getObjectRef() {
        return objectReference;
    }

    @Nonnull
    public Klass getCpKlass() {
        return cpKlass;
    }

    public void setIndexByVirtualMethodName(@Nonnull String name, int index) {
        indexByVirtualMethodName.put(name, index);
    }

    public int getIndexByFieldName(@Nonnull String name) {
        return indexByFieldName.get(name);
    }

    public final void setVirtualMethodTable(@Nonnull int[] virtualMethodTable) {
        this.virtualMethodTable = new AtomicIntegerArray(virtualMethodTable);
    }

    public int getMethodIndex(int virtualMethodIndex) {
        return requireNonNull(virtualMethodTable).get(virtualMethodIndex);
    }

    public int getIndexByVirtualMethodName(@Nonnull String methodName) {
        return indexByVirtualMethodName.get(methodName);
    }

    @Override
    public String toString() {
        return name;
    }

}
