package jvm;

import jvm.heap.Heap;
import jvm.parser.Method;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class ShapesTest {

    public static final String klass = "jvm/examples/heap_test/Main";

    @Test
    @Ignore
    public void runShapes() {
        checkMethod("main:([Ljava/lang/String;)V", 0);
    }

    private void checkMethod(@Nonnull String methodName, long expected) {
        VirtualMachine virtualMachine = new VirtualMachine(10000, 50, 10000);
        virtualMachine.getKlassLoader().loadKlass(klass);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.runHeapMonitor(new HashSet<>(Arrays.asList("java/lang/String", "java/lang/StringBuilder")));

        int methodIndex = heap.getMethodRepo().getIndexByName(klass + "." + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long actual = virtualMachine.getEngine().invoke(method);
        assertEquals(expected, actual);
    }
}
