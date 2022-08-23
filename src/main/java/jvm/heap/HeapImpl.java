package jvm.heap;

import jvm.garbage_collector.GarbageCollector;
import jvm.lang.OutOfMemoryErrorJVM;

import javax.annotation.Nonnull;

public class HeapImpl extends AbstractHeap {
    @Nonnull
    private final InstanceObject[] instanceObjects;
    @Nonnull
    private final InstanceKlass[] instanceKlasses;
    private int klassIndex;
    private int instanceObjectSize;
    private int objectIndex;

    public HeapImpl(@Nonnull GarbageCollector collector, int instancesSize, int klassesSize) {
        super(collector, instancesSize);
        this.instanceObjects = new InstanceObject[instancesSize];
        this.instanceKlasses = new InstanceKlass[klassesSize];
    }

    @Override
    public int getInstanceObjectCapacity() {
        return instanceObjects.length;
    }

    @Nonnull
    @Override
    InstanceObject getInstanceObjectInternal(int objectIndex) {
        return instanceObjects[objectIndex];
    }

    @Override
    public int getInstanceKlassSize() {
        return klassIndex;
    }

    @Override
    int addInstanceObjectInternal(@Nonnull InstanceObject object) {
        incrementInstanceObjectSize();
        while (instanceObjects[objectIndex] != null) {
            incrementObjectIndex();
        }
        int index = objectIndex;
        incrementObjectIndex();
        instanceObjects[index] = object;
        return index;
    }

    @Override
    void setInstanceObjectInternal(int objectIndex, @Nonnull InstanceObject object) {
        instanceObjects[objectIndex] = object;
    }

    @Override
    public void decrementInstanceObjectSize() {
        instanceObjectSize--;
        if (instanceObjectSize < 0) {
            throw new RuntimeException("size of instance objects can not be negative");
        }
    }

    @Override
    public void clearInstanceObject(int objectIndex) {
        instanceObjects[objectIndex] = null;
    }

    @Override
    public int setInstanceKlass(@Nonnull InstanceKlass klass) {
        instanceKlasses[klassIndex] = klass;
        return klassIndex++;
    }

    @Nonnull
    @Override
    public InstanceKlass getInstanceKlass(int instKlassIndex) {
        return instanceKlasses[instKlassIndex];
    }

    @Override
    public int getInstanceObjectSize() {
        return instanceObjectSize;
    }

    private void incrementObjectIndex() {
        objectIndex = (objectIndex + 1) % instanceObjects.length;
    }

    private void incrementInstanceObjectSize() {
        instanceObjectSize++;
        if (collector.isInProgress()) {
            checkCapacity();
        } else {
            if (instanceObjectSize > instanceObjects.length / 10 * 7) {
                collector.run();
                checkCapacity();
            }
        }
    }

    private void checkCapacity() {
        if (instanceObjectSize >= instanceObjects.length) {
            throw new OutOfMemoryErrorJVM("Java heap space");
        }
    }

}
