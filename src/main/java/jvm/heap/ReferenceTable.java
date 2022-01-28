package jvm.heap;

import java.util.Arrays;

public class ReferenceTable {
    private final int[] instanceTable;
    private int pointer;

    public ReferenceTable(int size) {
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

}
