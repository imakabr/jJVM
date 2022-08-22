package jvm.heap;

import jvm.garbage_collector.GarbageCollector;
import jvm.lang.OutOfMemoryErrorJVM;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Heap {
    @Nonnull
    private final ReferenceTable refTable;
    @Nonnull
    private final InstanceObject[] instanceObjects;
    @Nonnull
    private final InstanceKlass[] instanceKlasses;
    @Nonnull
    private final MethodRepo methodRepo;
    @Nonnull
    private final KlassLoader klassLoader;
    @Nonnull
    private final GarbageCollector collector;

    private final AtomicInteger klassIndex = new AtomicInteger();
    private int objectIndex = 0;
    private final AtomicInteger instanceObjectSize = new AtomicInteger();
    @Nonnull
    private final Map<String, Integer> poolOfStrings; // str -> objRef
    @Nonnull
    private final Set<Integer> cachedStringRefs; // objRef from pool of Strings
    private boolean enabledCacheString = true;

    public Heap(@Nonnull GarbageCollector collector, int instancesSize, int klassesSize) {
        this.collector = collector;
        this.refTable = new ReferenceTable(instancesSize);
        this.instanceObjects = new InstanceObject[instancesSize];
        this.instanceKlasses = new InstanceKlass[klassesSize];
        this.methodRepo = new MethodRepo();
        this.klassLoader = new KlassLoader(this);
        this.klassLoader.initSystemKlasses();
        this.poolOfStrings = new HashMap<>();
        this.cachedStringRefs = new HashSet<>();
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

    public int getObjectRef(@Nonnull InstanceObject object) {
        return refTable.getObjectReference(setInstanceObject(object));
    }

    public int changeObject(int objectRef, @Nonnull InstanceObject object) {
        if (objectRef != -1) {
            instanceObjects[refTable.getInstanceObjectIndex(objectRef)] = object;
            return objectRef;
        } else {
            return refTable.getObjectReference(setInstanceObject(object));
        }
    }

    @Nonnull
    public InstanceObject getInstanceObject(int objectRef) {
        int objectIndex = refTable.getInstanceObjectIndex(objectRef);
        if (objectIndex == -1) {
            throw new RuntimeException("No such object on the heap");
        }
        return instanceObjects[objectIndex];
    }

    public void clearInstanceObject(int objectIndex) {
        instanceObjects[objectIndex] = null;
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

    public int setInstanceKlass(InstanceKlass klass) {
        instanceKlasses[klassIndex.get()] = klass;
        return klassIndex.getAndIncrement();
    }

    public int getInstanceKlassSize() {
        return klassIndex.get();
    }

    private int setInstanceObject(InstanceObject object) {
        incrementInstanceObjectSize();
        while (instanceObjects[objectIndex] != null) {
            incrementObjectIndex();
        }
        int index = objectIndex;
        incrementObjectIndex();
        instanceObjects[index] = object;
        return index;
    }

    private void incrementObjectIndex() {
        objectIndex = (objectIndex + 1) % instanceObjects.length;
    }

    private void incrementInstanceObjectSize() {
        instanceObjectSize.incrementAndGet();
        if (collector.isInProgress()) {
            checkCapacity();
        } else {
            if (instanceObjectSize.get() > instanceObjects.length / 10 * 7) {
                collector.run();
                checkCapacity();
            }
        }
    }

    private void checkCapacity() {
        if (instanceObjectSize.get() >= instanceObjects.length) {
            throw new OutOfMemoryErrorJVM("Java heap space");
        }
    }

    public void decrementInstanceObjectSize() {
        instanceObjectSize.decrementAndGet();
        if (instanceObjectSize.get() < 0) {
            throw new RuntimeException("size of instance objects can not be negative");
        }
    }

    public InstanceKlass getInstanceKlass(int instKlassIndex) {
        return instanceKlasses[instKlassIndex];
    }

    public int getInstanceObjectSize() {
        return instanceObjectSize.get();
    }

    public int getInstanceObjectCapacity() {
        return instanceObjects.length;
    }
}
