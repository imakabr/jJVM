package jvm.garbage_collector;

import jvm.JVMType;
import jvm.VirtualMachine;
import jvm.engine.StackFrame;
import jvm.heap.Heap;
import jvm.parser.Method;
import org.junit.Test;


import static org.junit.Assert.*;

public class MarkAndSweepTest {

    public static final String klass = "jvm/examples/garbage_collector/MarkAndSweepExample";

    @Test
    public void markAndSweepTest() {
        VirtualMachine virtualMachine = new VirtualMachine(20, 4, 100, false);
        virtualMachine.getKlassLoader().loadKlass(klass);
        Heap heap = virtualMachine.getHeap();
        int systemObjectSize = heap.getInstanceObjectSize();
        int methodIndex = heap.getMethodRepo().getIndexByName(klass + ".createObjects:()Ljava/lang/Object;");
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        int reference = (int) virtualMachine.getEngine().invoke(method); // Should create 9 objects on the heap (1 - instance class, 8 - instance objects)

        assertEquals(systemObjectSize + 9, heap.getInstanceObjectSize());

        StackFrame stackFrame = new StackFrame(1,0);
        stackFrame.setLocalVar(0, setRefValueType(reference));
        GarbageCollector garbageCollector = new MarkAndSweep(stackFrame);
        garbageCollector.setHeap(heap);
        garbageCollector.run(); // Should remove 4 objects from the heap

        assertEquals(systemObjectSize + 5, heap.getInstanceObjectSize());
    }

    private long setRefValueType(int value) {
        return setValueType(JVMType.A.ordinal()) ^ value;
    }

    private long setValueType(int type) {
        return ((long) type << 32);
    }

}