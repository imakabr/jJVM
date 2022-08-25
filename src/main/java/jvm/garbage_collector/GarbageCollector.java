package jvm.garbage_collector;

import jvm.heap.api.Heap;

import javax.annotation.Nonnull;

public interface GarbageCollector {

    void run();

    boolean isInProgress();

    void setHeap(@Nonnull Heap heap);
}
