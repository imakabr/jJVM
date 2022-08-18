package jvm.util;

import jvm.VirtualMachine;
import jvm.heap.Heap;
import jvm.parser.Method;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;

public class HashMapTest {

    public static final String klass = "jvm/examples/util/HashMapExample";

    @Test
    public void checkPutGetMethod() {
        checkMethod("checkPutGetMethod:()Z", 1);
    }

    @Test
    public void checkPutGetMethod2() {
        checkMethod("checkPutGetMethod2:()Z", 1);
    }

    @Test
    public void checkContainsMethod() {
        checkMethod("checkContainsMethod:()Z", 1);
    }

    @Test
    public void checkClearEmptyMethod() {
        checkMethod("checkClearEmptyMethod:()Z", 1);
    }

    private void checkMethod(@Nonnull String methodName, long expected) {
        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(klass);

        int methodIndex = heap.getMethodRepo().getIndexByName(klass + "." + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long actual = virtualMachine.getEngine().invoke(method);
        assertEquals(expected, actual);
    }
}
