package jvm.heap.api;

public interface ReferenceTable {

    int getObjectReference(int objectIndex);

    int getInstanceObjectIndex(int objectRef);

    void clearObjectIndex(int objRef);

    int size();
}
