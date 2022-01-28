package jvm.heap;

import jvm.garbage_collector.GarbageCollector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

    private int klassIndex;
    private int objectIndex = 0;
    private int instanceObjectSize;

    public Heap(@Nonnull GarbageCollector collector, int instancesSize, int klassesSize) {
        this.collector = collector;
        this.refTable = new ReferenceTable(instancesSize);
        this.instanceObjects = new InstanceObject[instancesSize];
        this.instanceKlasses = new InstanceKlass[klassesSize];
        this.methodRepo = new MethodRepo();
        this.klassLoader = new KlassLoader(this);
        this.klassLoader.initSystemKlasses();
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

    public ReferenceTable getReferenceTable() {
        return refTable;
    }

    public MethodRepo getMethodRepo() {
        return methodRepo;
    }

    public KlassLoader getKlassLoader() {
        return klassLoader;
    }

    public int setInstanceKlass(InstanceKlass klass) {
        instanceKlasses[klassIndex] = klass;
        return klassIndex++;
    }

    public int setInstanceObject(InstanceObject object) {
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
        if (instanceObjectSize > instanceObjects.length) {
            throw new OutOfMemoryError("Java heap space");
        }
    }

    public void decrementInstanceObjectSize() {
        instanceObjectSize--;
        if (instanceObjectSize < 0) {
            throw new RuntimeException("size of instance objects can not be negative");
        }
    }

    public InstanceKlass getInstanceKlass(int instKlassIndex) {
        return instanceKlasses[instKlassIndex];
    }

    @Nonnull
    public InstanceKlass[] getInstanceKlasses() {
        return instanceKlasses;
    }

    @Nonnull
    public InstanceObject[] getInstanceObjects() {
        return instanceObjects;
    }

}
