package jvm;

import jvm.engine.ExecutionEngine;
import jvm.engine.StackFrame;
import jvm.garbage_collector.GarbageCollector;
import jvm.garbage_collector.MarkAndSweep;
import jvm.heap.Heap;
import jvm.heap.KlassLoader;

public class Main {

    private final Heap heap;
    private final ExecutionEngine engine;
    private final StackFrame stackFrame;
    private final GarbageCollector collector;
    private final KlassLoader klassLoader;

    public Main(int instancesSize, int klassesSize, int stackSize) {
        this.stackFrame = new StackFrame(stackSize);
        this.collector = new MarkAndSweep(stackFrame);
        this.heap = new Heap(collector, instancesSize, klassesSize);
        this.collector.setHeap(heap);
        this.engine = new ExecutionEngine(heap, stackFrame);
        this.klassLoader = heap.getKlassLoader();
    }

    public Main() {
        this(500, 50, 10000);
    }

    public Heap getHeap() {
        return heap;
    }

    public KlassLoader getKlassLoader() {
        return klassLoader;
    }

    public ExecutionEngine getEngine() {
        return engine;
    }

    public static void main(String[] args) {

    }
}






