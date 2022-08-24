package jvm.util;

import jvm.VirtualMachine;
import jvm.heap.Heap;
import jvm.parser.Method;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;

public class ArrayDequeTest {

    public static final String klass = "jvm/examples/util/ArrayDequeExample";

    @Test
    public void checkRemoveObjectMethod() {
        checkMethod("checkAddIsEmptyIntMethod:()Z", 0);
    }

    @Test
    public void checkAddPollIntMethod() {
        checkMethod("checkAddPollIntMethod:()Z", 1);
    }

    @Test
    public void checkAddPollStringMethod() {
        checkMethod("checkAddPollStringMethod:()Z", 1);
    }

    private void checkMethod(@Nonnull String methodName, long expected) {
        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000, false);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(klass);

        int methodIndex = heap.getMethodRepo().getIndexByName(klass + "." + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long actual = virtualMachine.getEngine().invoke(method);
        assertEquals(expected, actual);
    }
}
