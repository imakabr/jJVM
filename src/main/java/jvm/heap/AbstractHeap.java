package jvm.heap;

import jvm.garbage_collector.GarbageCollector;
import jvm.heap.api.Heap;
import jvm.heap.api.InstanceObject;
import jvm.heap.api.ReferenceTable;
import jvm.heap.sequential.ReferenceTableImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public abstract class AbstractHeap implements Heap {
    @Nonnull
    private final KlassLoader klassLoader;
    @Nonnull
    private final ReferenceTable refTable;
    @Nonnull
    private final MethodRepo methodRepo;
    @Nonnull
    public final GarbageCollector collector;
    @Nonnull
    private final Map<String, Integer> poolOfStrings; // str -> objRef
    @Nonnull
    private final Set<Integer> cachedStringRefs; // objRef from pool of Strings
    private boolean enabledCacheString = true;

    public AbstractHeap(@Nonnull GarbageCollector collector, int instancesSize) {
        this.collector = collector;
        this.refTable = new ReferenceTableImpl(instancesSize);
        this.methodRepo = new MethodRepo();
        this.poolOfStrings = new HashMap<>();
        this.cachedStringRefs = new HashSet<>();
        this.klassLoader = new KlassLoader(this);
    }

    public int getObjectRef(@Nonnull InstanceObject object) {
        return refTable.getObjectReference(addInstanceObjectInternal(object));
    }

    public int changeObject(int objectRef, @Nonnull InstanceObject object) {
        if (objectRef != -1) {
            setInstanceObjectInternal(refTable.getInstanceObjectIndex(objectRef), object);
            return objectRef;
        } else {
            return refTable.getObjectReference(addInstanceObjectInternal(object));
        }
    }

    @Nonnull
    public InstanceObject getInstanceObject(int objectRef) {
        int objectIndex = refTable.getInstanceObjectIndex(objectRef);
        if (objectIndex == -1) {
            throw new RuntimeException("No such object on the heap");
        }
        return getInstanceObjectInternal(objectIndex);
    }

    @Nullable
    @Override
    public InstanceObject getInstanceObjectByObjInd(int objectIndex) {
        return getInstanceObjectInternal(objectIndex);
    }

    @Nonnull
    public ReferenceTable getReferenceTable() {
        return refTable;
    }

    @Nonnull
    public MethodRepo getMethodRepo() {
        return methodRepo;
    }

    @Nonnull
    public KlassLoader getKlassLoader() {
        return klassLoader;
    }

    public boolean isCachedStringObjRef(int objRef) {
        return cachedStringRefs.contains(objRef);
    }

    public boolean isEnabledCacheString() {
        return enabledCacheString;
    }

    @Nullable
    public Integer getStringRefFromPool(@Nonnull String str) {
        return poolOfStrings.get(str);
    }

    public void putStringRefToPool(@Nonnull String str, int strRef, int charArrayRef) {
        poolOfStrings.put(str, strRef);
        cachedStringRefs.add(strRef);
        cachedStringRefs.add(charArrayRef);
    }

    public void disableCacheString() {
        this.enabledCacheString = false;
    }

    @Nonnull
    protected abstract InstanceObject getInstanceObjectInternal(int objectIndex);

    protected abstract int addInstanceObjectInternal(@Nonnull InstanceObject object);

    protected abstract void setInstanceObjectInternal(int objectIndex, @Nonnull InstanceObject object);

}
