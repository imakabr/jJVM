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
    @Nullable
    private GarbageCollector collector;

    private int klassIndex;
    private int objectIndex = 1; // 0 is null, so object indices begin with 1

    public Heap(int instancesSize, int klassesSize) {
        this.refTable = new ReferenceTable(instancesSize);
        this.instanceObjects = new InstanceObject[instancesSize];
        this.instanceKlasses = new InstanceKlass[klassesSize];
        this.methodRepo = new MethodRepo();
        this.klassLoader = new KlassLoader(this);
        this.klassLoader.initSystemKlasses();
    }

    public void setGarbageCollector(@Nonnull GarbageCollector collector) {
        this.collector = collector;
    }

    public int getObjectRef(InstanceObject object) {
        return refTable.getObjectReference(setInstanceObject(object));
    }

    public int changeObject(int objectRef, InstanceObject object) {
        if (objectRef != -1) {
            instanceObjects[refTable.getInstanceObjectIndex(objectRef)] = object;
            return objectRef;
        } else {
            return refTable.getObjectReference(setInstanceObject(object));
        }
    }

    public InstanceObject getInstanceObject(int objectRef) {
        return instanceObjects[refTable.getInstanceObjectIndex(objectRef)];
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
        instanceObjects[objectIndex] = object;
        return objectIndex++;
    }

    public InstanceKlass getInstanceKlass(int instKlassIndex) {
        return instanceKlasses[instKlassIndex];
    }


}
