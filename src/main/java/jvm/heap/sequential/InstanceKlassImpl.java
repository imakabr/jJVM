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
    public Map<String, Integer> getVirtualMethods() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : virtualMethodNameToIndexMap.entrySet()) {
            result.put(entry.getKey(), virtualMethodTable[entry.getValue()]);
        }
        return result;
    }

    @Nonnull
    public Map<String, Integer> getStaticFieldNameToIndexMap() {
        return staticFieldNameToIndexMap;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public int getIndexByStaticMethodName(@Nonnull String methodName) {
        return staticMethodNameToIndexMap.get(methodName);
    }

    @Nonnull
    public Map<String, Integer> getStaticMethodNameToIndexMap() {
        return staticMethodNameToIndexMap;
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
        return virtualMethodTable[virtualMethodIndex];
    }

    public int getIndexByVirtualMethodName(@Nonnull String methodName) {
        return virtualMethodNameToIndexMap.get(methodName);
    }

    @Override
    public String toString() {
        return name;
    }
}
