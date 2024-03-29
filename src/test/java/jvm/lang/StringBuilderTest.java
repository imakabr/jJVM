package jvm.lang;

import jvm.VirtualMachine;
import jvm.heap.api.Heap;
import jvm.parser.Method;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.*;

public class StringBuilderTest {

    public static final String klass = "jvm/examples/lang/StringBuilderExample";

    @Test
    public void checkStringBuilder() {
        checkMethod("checkStringBuilder:()I", 1);
    }

    @Test
    public void checkStringBuilderAppend() {
        checkMethod("checkStringBuilderAppend:()I", 1);
    }

    @Test
    public void checkStringBuilderEquals() {
        checkMethod("checkStringBuilderEquals:()Z", 1);
    }

    @Test
    public void checkStringBuilderHashCode() {
        checkMethod("checkStringBuilderHashCode:()I", -510690210);
    }

    @Test
    public void checkStringBuilderAppendChar() {
        checkMethod("checkStringBuilderAppendChar:()I", 1);
    }

    @Test
    public void checkStringBuilderAppendInt() {
        checkMethod("checkStringBuilderAppendInt:()I", 1);
    }

    @Test
    public void checkStringBuilderAppendIntMax() {
        checkMethod("checkStringBuilderAppendIntMax:()I", 1);
    }

    @Test
    public void checkStringBuilderAppendIntMin() {
        checkMethod("checkStringBuilderAppendIntMin:()I", 1);
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