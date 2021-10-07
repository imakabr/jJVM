package jvm.engine;

import jvm.JVMValue;
import jvm.engine.ExecutionEngine;
import jvm.heap.HeapHolder;
import jvm.parser.Method;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static jvm.engine.Opcode.*;


public class ArithmeticTest {

    private static ExecutionEngine engine;
    
    @BeforeClass
    public static void setup() {
        engine = new ExecutionEngine(HeapHolder.getHeap());
    }

    @Test
    public void divide() {
        byte[] byteCode = {ICONST_5.B(), ICONST_3.B(), IDIV.B(), IRETURN.B()};
        JVMValue res = engine.invoke(new Method("", "", "", 0, byteCode, 2, 0));
        assertEquals(1, (int) res.value);

        byte[] byteCode2 = {ICONST_2.B(), ICONST_2.B(), IDIV.B(), IRETURN.B()};
        res = engine.invoke(new Method("", "", "main:()V", 0, byteCode2, 2, 0));
        assertEquals(1, (int) res.value);

        byte[] byteCode3 = {BIPUSH.B(), (byte)17, BIPUSH.B(), (byte)5, IREM.B(), IRETURN.B()};
        res = engine.invoke(new Method("", "", "main:()V", 0, byteCode3, 2, 0));
        assertEquals(2, (int) res.value);
    }

    @Test
    public void addAndMultiply() {

        byte[] byteCode = {ICONST_1.B(), ICONST_1.B(), IADD.B(), IRETURN.B()};
        JVMValue res = engine.invoke(new Method("", "", "", 0, byteCode, 2, 0));
        assertEquals(2, (int) res.value);

        byte[] byteCode2 = {ICONST_1.B(), ICONST_M1.B(), IADD.B(), IRETURN.B()};
        res = engine.invoke(new Method("", "", "", 0, byteCode2, 2, 0));
        assertEquals(0, (int) res.value);

        byte[] byteCode3 = {ICONST_2.B(), ICONST_M1.B(), IMUL.B(), IRETURN.B()};
        res = engine.invoke(new Method("", "", "", 0, byteCode3, 2, 0));
        assertEquals( -2, (int) res.value);
    }

    @Test
    public void increment() {
        byte[] byteCode = {ICONST_1.B(), ISTORE.B(), (byte) 5, IINC.B(), (byte) 5, (byte) 41, ILOAD.B(), (byte) 5, IRETURN.B()};
        JVMValue res = engine.invoke(new Method("", "", "", 0, byteCode, 1, 6));

        assertEquals(42, (int) res.value);
    }

    @Test
    public void duplicate() {
        byte[] byteCode = {ICONST_1.B(), DUP.B(), IADD.B(), IRETURN.B()};
        JVMValue res = engine.invoke(new Method("", "", "", 0, byteCode, 2, 0));
        assertEquals( 2, (int) res.value);

        byte[] byteCode2 = {ICONST_1.B(), DUP.B(), IADD.B(), DUP.B(), IADD.B(), IRETURN.B()};
        res = engine.invoke(new Method("", "", "", 0, byteCode2, 2, 0));
        assertEquals(4, (int) res.value);
    }

    @Test
    public void dupNopPop() {
        byte[] byteCode = {ICONST_1.B(), DUP.B(), NOP.B(), POP.B(), IRETURN.B()};
        JVMValue res = engine.invoke(new Method("", "", "", 0, byteCode, 2, 0));
        assertEquals(1, (int) res.value);

        byte[] byteCode2 = {ICONST_1.B(), DUP.B(), NOP.B(), POP.B(), POP.B(), RETURN.B()};
        res = engine.invoke(new Method("", "", "", 0, byteCode2, 2, 0));
        assertNull(res);
    }

    @Test
    public void dupX1() {
        byte[] byteCode = {ICONST_1.B(), ICONST_2.B(), DUP_X1.B(), IADD.B(), IADD.B(), IRETURN.B()};
        JVMValue res = engine.invoke(new Method("", "", "", 0, byteCode, 3, 0));
        assertEquals(5, (int) res.value);

        byte[] byteCode2 = {ICONST_1.B(), ICONST_2.B(), DUP_X1.B(), IADD.B(), DUP_X1.B(), IADD.B(), IADD.B(), IRETURN.B()};
        res = engine.invoke(new Method("", "", "", 0, byteCode2, 3, 0));
        assertEquals( 8, (int) res.value);
    }

    @Test
    public void IfEqual() {
        byte[] byteCode = {ICONST_1.B(), ICONST_1.B(), IADD.B(), ICONST_2.B(), IF_ICMPEQ.B(), (byte) 0, (byte) 11, ICONST_4.B(), GOTO.B(), (byte) 0, (byte) 12, ICONST_3.B(), IRETURN.B()};
        JVMValue res = engine.invoke(new Method("", "", "", 0, byteCode, 2, 0));
        assertEquals(3, ((int) res.value));

        byte[] byteCode2 = {ICONST_1.B(), ICONST_1.B(), IADD.B(), ICONST_3.B(), IF_ICMPEQ.B(), (byte) 0, (byte) 11, ICONST_4.B(), GOTO.B(), (byte) 0, (byte) 12, ICONST_3.B(), IRETURN.B()};
        res = engine.invoke(new Method("", "", "", 0, byteCode2, 2, 0));
        assertEquals(4, ((int) res.value));
    }

}
