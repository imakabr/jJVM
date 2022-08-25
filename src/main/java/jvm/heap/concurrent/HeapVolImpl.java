package jvm.heap.concurrent;

import jvm.garbage_collector.GarbageCollector;
import jvm.heap.AbstractHeap;
import jvm.heap.api.InstanceKlass;
import jvm.heap.api.InstanceObject;
import jvm.lang.OutOfMemoryErrorJVM;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class HeapVolImpl extends AbstractHeap {
    @Nonnull
    private final AtomicReferenceArray<InstanceObject> instanceObjects;
    @Nonnull
    private final AtomicReferenceArray<InstanceKlass> instanceKlasses;
    @Nonnull
    private final AtomicInteger klassIndex = new AtomicInteger();
    @Nonnull
    private final AtomicInteger instanceObjectSize = new AtomicInteger();
    private int objectIndex = 0;

    public HeapVolImpl(@Nonnull GarbageCollector collector, int instancesSize, int klassesSize) {
        super(collector, instancesSize, true);
        this.instanceObjects = new AtomicReferenceArray<>(instancesSize);
        this.instanceKlasses = new AtomicReferenceArray<>(klassesSize);
    }

    @Override
    public int getInstanceObjectCapacity() {
        return instanceObjects.length();
    }

    @Nonnull
    @Override
    protected InstanceObject getInstanceObjectInternal(int objectIndex) {
        return instanceObjects.get(objectIndex);
    }

    @Override
    public int getInstanceKlassSize() {
        return klassIndex.get();
    }

    @Override
    protected int addInstanceObjectInternal(@Nonnull InstanceObject object) {
        incrementInstanceObjectSize();
        while (instanceObjects.get(objectIndex) != null) {
            incrementObjectIndex();
        }
        int index = objectIndex;
        incrementObjectIndex();
        instanceObjects.set(index, object);
        return index;
    }

    @Override
    protected void setInstanceObjectInternal(int objectIndex, @Nonnull InstanceObject object) {
        instanceObjects.set(objectIndex, object);
    }

    @Override
    public void decrementInstanceObjectSize() {
        instanceObjectSize.decrementAndGet();
        if (instanceObjectSize.get() < 0) {
            throw new RuntimeException("size of instance objects can not be negative");
        }
    }

    @Override
    public void clearInstanceObject(int objectIndex) {
        instanceObjects.set(objectIndex, null);
    }

    @Override
    public int setInstanceKlass(@Nonnull InstanceKlass klass) {
        int index = klassIndex.getAndIncrement();
        instanceKlasses.set(index, klass);
        return index;
    }

    @Nonnull
    @Override
    public InstanceKlass getInstanceKlass(int instKlassIndex) {
        return instanceKlasses.get(instKlassIndex);
    }

    @Override
    public int getInstanceObjectSize() {
        return instanceObjectSize.get();
    }

    private void incrementObjectIndex() {
        objectIndex = (objectIndex + 1) % instanceObjects.length();
    }

    private void incrementInstanceObjectSize() {
        instanceObjectSize.incrementAndGet();
        if (collector.isInProgress()) {
            checkCapacity();
        } else {
            if (instanceObjectSize.get() > instanceObjects.length() / 10 * 7) {
                collector.run();
                checkCapacity();
            }
        }
    }

    private void checkCapacity() {
        if (instanceObjectSize.get() >= instanceObjects.length()) {
            throw new OutOfMemoryErrorJVM("Java heap space");
        }
    }

}
