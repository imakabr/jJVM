package jvm;

import jvm.engine.ExecutionEngine;
import jvm.engine.StackFrame;
import jvm.garbage_collector.GarbageCollector;
import jvm.garbage_collector.MarkAndSweep;
import jvm.heap.Heap;
import jvm.heap.HeapImpl;
import jvm.heap.KlassLoader;
import jvm.monitor.HeapMonitor;

import javax.annotation.Nonnull;
import java.util.Set;

public class VirtualMachine {

    private final Heap heap;
    private final ExecutionEngine engine;
    private final StackFrame stackFrame;
    private final GarbageCollector collector;
    private final KlassLoader klassLoader;

    public VirtualMachine(int instancesSize, int klassesSize, int stackSize) {
        this.stackFrame = new StackFrame(stackSize);
        this.collector = new MarkAndSweep(stackFrame);
        this.heap = new HeapImpl(collector, instancesSize, klassesSize);
        this.collector.setHeap(heap);
        this.engine = new ExecutionEngine(heap, stackFrame);
        this.klassLoader = heap.getKlassLoader();
    }

    public static void main(String[] args) {
        new VirtualMachine();
    }

    public VirtualMachine() {
        this(500, 50, 10000);
    }

    public void runHeapMonitor(@Nonnull Set<String> classNames) {
        new HeapMonitor(heap, classNames).run();
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

}






