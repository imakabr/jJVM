package jvm.heap;

public class HeapHolder {

    private static final Heap heap = new Heap(500, 50);

    public static Heap getHeap() {
        return heap;
    }


}
