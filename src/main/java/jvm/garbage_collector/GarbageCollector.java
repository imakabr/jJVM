package jvm.garbage_collector;

import jvm.heap.Heap;

import javax.annotation.Nonnull;

public interface GarbageCollector {

    void run();

    boolean isInProgress();

    void setHeap(@Nonnull Heap heap);
}
