package jvm.heap.sequential;

import jvm.heap.api.InstanceKlass;
import jvm.parser.Klass;

import javax.annotation.Nonnull;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class InstanceKlassImpl implements InstanceKlass {

    private final int objectReference;
    @Nonnull
    private final Map<String, Integer> staticFieldNameToIndexMap; //fields
    @Nonnull
    private final Map<String, Integer> staticMethodNameToIndexMap; //methods
    @Nonnull
    private final int[] virtualMethodTable; //virtual method table
    @Nonnull
    private final Map<String, Integer> virtualMethodNameToIndexMap;

    @Nonnull
    private final Klass cpKlass;
    @Nonnull
    private final String name;

    public InstanceKlassImpl(@Nonnull Map<String, Integer> staticFieldNameToIndexMap,
                             @Nonnull Map<String, Integer> staticMethodNameToIndexMap,
                             @Nonnull Map<String, Integer> virtualMethodNameToIndexMap,
                             @Nonnull int[] virtualMethodTable,
                             int objectReference, @Nonnull Klass cpKlass) {
        this.name = cpKlass.getKlassName();
        this.objectReference = objectReference;
        this.cpKlass = cpKlass;
        this.virtualMethodNameToIndexMap = new HashMap<>(virtualMethodNameToIndexMap);
        this.staticFieldNameToIndexMap = new HashMap<>(staticFieldNameToIndexMap);
        this.staticMethodNameToIndexMap = new HashMap<>(staticMethodNameToIndexMap);
        this.virtualMethodTable = virtualMethodTable;
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
    public Set<String> getStaticMethodNames() {
        return Collections.unmodifiableSet(staticMethodNameToIndexMap.keySet());
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

    @Override
    public int getObjectRef() {
        return objectReference;
    }

    @Nonnull
    @Override
    public Klass getCpKlass() {
        return cpKlass;
    }

    @Override
    public int getIndexByStaticFieldName(@Nonnull String name) {
        return staticFieldNameToIndexMap.get(name);
    }

    @Override
    public int getMethodIndex(int virtualMethodIndex) {
        return virtualMethodTable[virtualMethodIndex];
    }

    @Override
    public int getIndexByVirtualMethodName(@Nonnull String methodName) {
        return virtualMethodNameToIndexMap.get(methodName);
    }

    @Override
    public String toString() {
        return name;
    }
}
