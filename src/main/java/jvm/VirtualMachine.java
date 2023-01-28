package jvm;

import jvm.engine.ExecutionEngine;
import jvm.engine.StackFrame;
import jvm.garbage_collector.GarbageCollector;
import jvm.garbage_collector.MarkAndSweep;
import jvm.heap.*;
import jvm.heap.api.Heap;
import jvm.heap.concurrent.HeapVolImpl;
import jvm.heap.sequential.HeapImpl;
import jvm.monitor.HeapMonitor;

import javax.annotation.Nonnull;
import java.util.Set;

public class VirtualMachine {

    private final Heap heap;
    private final ExecutionEngine engine;
    private final StackFrame stackFrame;
    private final GarbageCollector collector;
    private final KlassLoader klassLoader;
    private final boolean heapMonitor;

    public VirtualMachine(int instancesSize, int klassesSize, int stackSize, boolean heapMonitor) {
        this.stackFrame = new StackFrame(stackSize);
        this.collector = new MarkAndSweep(stackFrame);
        this.heapMonitor = heapMonitor;
        this.heap = heapMonitor ? new HeapVolImpl(collector, instancesSize, klassesSize) :
                new HeapImpl(collector, instancesSize, klassesSize);
        InstanceFactory.setHeapMonitor(heapMonitor);
        this.collector.setHeap(heap);
        this.engine = new ExecutionEngine(heap, stackFrame);
        this.klassLoader = heap.getKlassLoader();
        this.klassLoader.initSystemKlasses();
    }

    public static void main(String[] args) {
        new VirtualMachine();
    }

    public VirtualMachine() {
        this(500, 50, 10000, false);
    }

    public void runHeapMonitor(@Nonnull Set<String> classNames) {
        if (!heapMonitor) {
            throw new RuntimeException("JVM was started without the Heap Monitor");
        }
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






