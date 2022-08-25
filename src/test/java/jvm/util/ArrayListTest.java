package jvm.util;

import jvm.VirtualMachine;
import jvm.heap.api.Heap;
import jvm.parser.Method;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;

public class ArrayListTest {

    public static final String klass = "jvm/examples/util/ArrayListExample";

    @Test
    public void checkStringAddGetMethod() {
        checkMethod("checkStringAddGetMethod:()I", 1);
    }

    @Test
    public void checkIntAddGetMethod() {
        checkMethod("checkIntAddGetMethod:()Z", 1);
    }

    @Test
    public void checkIntsAddGetMethod() {
        checkMethod("checkIntsAddGetMethod:()Z", 1);
    }

    @Test
    public void checkSizeMethod() {
        checkMethod("checkSizeMethod:()Z", 1);
    }

    @Test
    public void checkClearMethod() {
        checkMethod("checkClearMethod:()Z", 1);
    }

    @Test
    public void checkAddWithIndexMethod() {
        checkMethod("checkAddWithIndexMethod:()Z", 1);
    }

    @Test
    public void checkSetMethod() {
        checkMethod("checkAddWithIndexMethod:()Z", 1);
    }

    @Test
    public void checkRemoveMethod() {
        checkMethod("checkRemoveMethod:()Z", 1);
    }

    @Test
    public void checkIsEmptyMethod() {
        checkMethod("checkIsEmptyMethod:()Z", 1);
    }

    @Test
    public void checkContainsMethod() {
        checkMethod("checkContainsMethod:()Z", 1);
    }

    @Test
    public void checkContainsIntMethod() {
        checkMethod("checkContainsIntMethod:()Z", 1);
    }

    @Test
    public void checkRemoveObjectMethod() {
        checkMethod("checkRemoveObjectMethod:()Z", 1);
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
