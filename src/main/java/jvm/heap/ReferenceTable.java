package jvm.heap;

public class ReferenceTable {
    private final int[] instanceTable;
    private int pointer;

    public ReferenceTable(int size) {
        this.instanceTable = new int[size];
    }

    public int getObjectReference(int objectIndex) {
        instanceTable[pointer] = objectIndex;
        return pointer++;
    }

    public int getInstanceObjectIndex(int objectRef) {
        return instanceTable[objectRef];
    }

}
