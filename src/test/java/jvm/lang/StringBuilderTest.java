package jvm.lang;

import jvm.Main;
import jvm.engine.ExecutionEngine;
import jvm.heap.Heap;
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
        Main main = new Main(500, 50, 10000);
        Heap heap = main.getHeap();
        main.getKlassLoader().loadKlass(klass);

        int methodIndex = heap.getMethodRepo().getIndexByName(klass + "." + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long actual = main.getEngine().invoke(method);
        assertEquals(expected, actual);
    }

}