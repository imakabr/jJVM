package jvm.lang;

import jvm.VirtualMachine;
import jvm.heap.Heap;
import jvm.parser.Method;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.*;

public class StringTest {

    public static final String klass = "jvm/examples/lang/StringExample";

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

    @Test
    public void checkToString() {
        checkMethod("checkToString:()Z", 1);
    }

    @Test
    public void checkPrintlnString() {
        checkMethod("checkPrintlnString:()I", 1);
    }

    @Test
    public void checkPrintString() {
        checkMethod("checkPrintString:()I", 1);
    }

    @Test
    public void checkPrintChar() {
        checkMethod("checkPrintChar:()I", 1);
    }

    @Test
    public void checkPrintlnChar() {
        checkMethod("checkPrintlnChar:()I", 1);
    }

    @Test
    public void checkPrintInt() {
        checkMethod("checkPrintInt:()I", 1);
    }

    @Test
    public void checkPrintlnInt() {
        checkMethod("checkPrintlnInt:()I", 1);
    }

    @Test
    public void checkPrintlnSimpleObject() {
        checkMethod("checkPrintlnSimpleObject:()I", 1);
    }

    @Test
    public void checkPrintlnObject() {
        checkMethod("checkPrintlnObject:()I", 1);
    }

    @Test
    public void checkObjectToString() {
        checkMethod("checkObjectToString:()I", 0);
    }

    @Test
    public void checkStringWithInternEqual() {
        checkMethod("checkStringWithInternEqual:()Z", 1);
    }

    @Test
    public void checkSplit() {
        checkMethod("checkSplit:()Z", 1);
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