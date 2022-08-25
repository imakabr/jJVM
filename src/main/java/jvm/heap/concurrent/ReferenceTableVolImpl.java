package jvm.heap.concurrent;

import jvm.heap.api.ReferenceTable;

import javax.annotation.Nonnull;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ReferenceTableVolImpl implements ReferenceTable {

    @Nonnull
    private final AtomicIntegerArray instanceTable;
    @Nonnull
    private final AtomicInteger pointer;

    public ReferenceTableVolImpl(int size) {
        pointer = new AtomicInteger();
        this.instanceTable = new AtomicIntegerArray(size);
        for (int i = 0; i < size; i++) {
            this.instanceTable.set(i, -1);
        }
    }

    public int getObjectReference(int objectIndex) {
        while (instanceTable.get(pointer.get()) != -1) {
            incrementPointer();
        }
        int objRef = pointer.get();
        instanceTable.set(objRef, objectIndex);
        incrementPointer();
        return objRef;
    }

    private void incrementPointer() {
        pointer.set((pointer.get() + 1) % instanceTable.length());
    }

    public int getInstanceObjectIndex(int objectRef) {
        return instanceTable.get(objectRef);
    }

    public void clearObjectIndex(int objRef) {
        instanceTable.set(objRef, -1);
    }

    public int size() {
        return instanceTable.length();
    }
}
