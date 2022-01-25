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
        int index = pointer;
        instanceTable[index] = objectIndex;
        incrementPointer();
        return index;
    }

    private void incrementPointer() {
        pointer = (pointer + 1) % instanceTable.length;
    }

    public int getInstanceObjectIndex(int objectRef) {
        return instanceTable[objectRef];
    }

}
