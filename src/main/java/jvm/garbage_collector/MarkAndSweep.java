package jvm.garbage_collector;

import jvm.engine.StackFrame;
import jvm.heap.Heap;

import javax.annotation.Nonnull;

public class MarkAndSweep implements GarbageCollector{

    @Nonnull
    private final StackFrame stackFrame;
    @Nonnull
    private Heap heap;

    public MarkAndSweep(@Nonnull StackFrame stackFrame, @Nonnull Heap heap) {
        this.stackFrame = stackFrame;
        this.heap = heap;
    }

}
