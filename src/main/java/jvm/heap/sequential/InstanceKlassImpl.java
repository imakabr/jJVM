package jvm.heap.sequential;

import jvm.parser.Klass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class InstanceKlassImpl implements jvm.heap.api.InstanceKlass {
    private final int objectReference;
    //fields
    @Nonnull
    private final Map<String, Integer> indexByFieldName;
    //methods
    @Nullable
    private Map<String, Integer> indexByMethodName;
    //virtual method table
    @Nullable
    private int[] virtualMethodTable;
    @Nonnull
    private final Map<String, Integer> indexByVirtualMethodName;

    @Nonnull
    private final Klass cpKlass;
    @Nonnull
    private final String name;

    public InstanceKlassImpl(@Nonnull Map<String, Integer> indexByFieldName, int objectReference, @Nonnull Klass cpKlass) {
        this.name = cpKlass.getKlassName();
        this.objectReference = objectReference;
        this.cpKlass = cpKlass;
        this.indexByVirtualMethodName = new HashMap<>();
        this.indexByFieldName = indexByFieldName;
    }

    @Nonnull
    public Map<String, Integer> getVirtualMethods() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : indexByVirtualMethodName.entrySet()) {
            result.put(entry.getKey(), requireNonNull(virtualMethodTable)[entry.getValue()]);
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
        this.indexByMethodName = indexByFieldName;
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
        this.virtualMethodTable = virtualMethodTable;
    }

    public int getMethodIndex(int virtualMethodIndex) {
        return requireNonNull(virtualMethodTable)[virtualMethodIndex];
    }

    public int getIndexByVirtualMethodName(@Nonnull String methodName) {
        return indexByVirtualMethodName.get(methodName);
    }

    @Override
    public String toString() {
        return name;
    }
}
