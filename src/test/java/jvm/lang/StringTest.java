package jvm.lang;

import jvm.engine.ExecutionEngine;
import jvm.heap.Heap;
import jvm.parser.Method;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.*;

public class StringTest {

    public static final String klass = "jvm/examples/StringExample";

    @Test
    public void checkStringToCharArray() {
        checkMethod("checkStringToCharArray:()I", 1);
    }

    @Test
    public void checkStringReplace() {
        checkMethod("checkStringReplace:()I", 1);
    }

    @Test
    public void checkStringEqualsTrue() {
        checkMethod("checkStringEqualsTrue:()Z", 1);
    }

    @Test
    public void checkStringEqualsFalse() {
        checkMethod("checkStringEqualsFalse:()Z", 0);
    }

    @Test
    public void checkStringHashCode() {
        checkMethod("checkStringHashCode:()I", 1923188771);
    }

    @Test
    public void checkStringToUpperCase() {
        checkMethod("checkStringToUpperCase:()I", 1);
    }

    @Test
    public void checkStringCharAt() {
        checkMethod("checkStringCharAt:()I", 1);
    }

    @Test
    public void checkStringToLowerCase() {
        checkMethod("checkStringToLowerCase:()I", 1);
    }

    @Test
    public void checkStringConcat() {
        checkMethod("checkStringConcat:()I", 1);
    }

    @Test
    public void checkStringConcatMethod() {
        checkMethod("checkStringConcatMethod:()I", 1);
    }

    private void checkMethod(@Nonnull String methodName, long expected) {
        Heap heap = new Heap(500, 50);
        heap.getKlassLoader().loadKlass(klass);

        int methodIndex = heap.getMethodRepo().getIndexByName(klass + "." + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long actual = new ExecutionEngine(heap).invoke(method);
        assertEquals(expected, actual);
    }
}