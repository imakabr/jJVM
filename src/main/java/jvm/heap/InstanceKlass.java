package jvm.heap;

import jvm.parser.Klass;

import javax.annotation.Nonnull;
import java.util.*;

public class InstanceKlass {
    private final int objectReference;
    //fields
    private final Map<String, Integer> indexByFieldName;
    //methods
    private Map<String, Integer> indexByMethodName;
    //virtual method table
    private int[] virtualMethodTable;
    private final Map<String, Integer> indexByVirtualMethodName;

    private int parentIndex;
    private final Klass cpKlass;
    private final String name;

    public InstanceKlass(@Nonnull Map<String, Integer> indexByFieldName, int objectReference, @Nonnull Klass cpKlass) {
        this.name = cpKlass.getKlassName();
        this.objectReference = objectReference;
        this.cpKlass = cpKlass;
        this.indexByVirtualMethodName = new HashMap<>();
        this.indexByFieldName = indexByFieldName;
    }

    public Map<String, Integer> getVirtualMethods() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : indexByVirtualMethodName.entrySet()) {
            result.put(entry.getKey(), virtualMethodTable[entry.getValue()]);
        }
        return result;
    }

    @Nonnull
    public Map<String, Integer> getIndexByFieldName() {
        return indexByFieldName;
    }

    public String getName() {
        return name;
    }

    public int getIndexByMethodName(String methodName) {
        return indexByMethodName.get(methodName);
    }

    public Map<String, Integer> getAllIndexesByMethodName() {
        return indexByMethodName;
    }

    public void setAllIndexesByMethodName(Map<String, Integer> indexByFieldName) {
        this.indexByMethodName = indexByFieldName;
    }

    public int getObjectRef() {
        return objectReference;
    }

    public Klass getCpKlass() {
        return cpKlass;
    }

    public void setIndexByVirtualMethodName(String name, int index) {
        indexByVirtualMethodName.put(name, index);
    }

    public int getIndexByFieldName(String name) {
        return indexByFieldName.get(name);
    }

    public final void setVirtualMethodTable(int[] virtualMethodTable) {
        this.virtualMethodTable = virtualMethodTable;
    }

    public int getMethodIndex(int virtualMethodIndex) {
        return virtualMethodTable[virtualMethodIndex];
    }

    public int getIndexByVirtualMethodName(String methodName) {
        return indexByVirtualMethodName.get(methodName);
    }

    @Override
    public String toString() {
        return name;
    }
}
