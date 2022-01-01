package jvm.lang;

import jvm.engine.ExecutionEngine;
import jvm.heap.Heap;
import jvm.parser.Method;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.*;

public class StringBuilderTest {

    public static final String klass = "jvm/examples/StringBuilderExample";

    @Test
    public void checkStringBuilder() {
        checkMethod(".checkStringBuilder:()I", 1);
    }

    @Test
    public void checkStringBuilderAppend() {
        checkMethod(".checkStringBuilderAppend:()I", 1);
    }

    @Test
    public void checkStringBuilderEquals() {
        checkMethod(".checkStringBuilderEquals:()Z", 1);
    }

    @Test
    public void checkStringBuilderHashCode() {
        checkMethod(".checkStringBuilderHashCode:()I", -510690210);
    }

    private void checkMethod(@Nonnull String methodName, long expected) {
        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(klass);

        int methodIndex = heap.getMethodRepo().getIndexByName(klass + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long actual = new ExecutionEngine(heap).invoke(method);
        assertEquals(expected, actual);
    }

}