package jvm.heap.sequential;

import jvm.heap.api.ReferenceTable;

import java.util.Arrays;

public class ReferenceTableImpl implements ReferenceTable {

    private final int[] instanceTable;
    private int pointer;

    public ReferenceTableImpl(int size) {
        this.instanceTable = new int[size];
        Arrays.fill(instanceTable, -1);
    }

    public int getObjectReference(int objectIndex) {
        while (instanceTable[pointer] != -1) {
            incrementPointer();
        }
        int objRef = pointer;
        instanceTable[objRef] = objectIndex;
        incrementPointer();
        return objRef;
    }

    private void incrementPointer() {
        pointer = (pointer + 1) % instanceTable.length;
    }

    public int getInstanceObjectIndex(int objectRef) {
        return instanceTable[objectRef];
    }

    public void clearObjectIndex(int objRef) {
        instanceTable[objRef] = -1;
    }

    public int size() {
        return instanceTable.length;
    }

}
