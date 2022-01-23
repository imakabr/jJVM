package jvm.engine;

import jvm.Main;
import jvm.heap.Heap;
import jvm.parser.Method;
import org.junit.BeforeClass;
import org.junit.Test;

import static jvm.engine.Opcode.*;
import static org.junit.Assert.assertEquals;


public class ArithmeticTest {

    private static ExecutionEngine engine;

    @BeforeClass
    public static void setup() {
        engine = new Main(500, 50, 10000).getEngine();
    }

    @Test
    public void divide() {
        byte[] byteCode = {ICONST_5.b(), ICONST_3.b(), IDIV.b(), IRETURN.b()};
        long res = engine.invoke(new Method("", "", "", 0, byteCode, 2, 0));
        assertEquals(1, res);

        byte[] byteCode2 = {ICONST_2.b(), ICONST_2.b(), IDIV.b(), IRETURN.b()};
        res = engine.invoke(new Method("", "", "main:()V", 0, byteCode2, 2, 0));
        assertEquals(1, res);

        byte[] byteCode3 = {BIPUSH.b(), (byte) 17, BIPUSH.b(), (byte) 5, IREM.b(), IRETURN.b()};
        res = engine.invoke(new Method("", "", "main:()V", 0, byteCode3, 2, 0));
        assertEquals(2, res);
    }

    @Test
    public void addAndMultiply() {

        byte[] byteCode = {ICONST_1.b(), ICONST_1.b(), IADD.b(), IRETURN.b()};
        long res = engine.invoke(new Method("", "", "", 0, byteCode, 2, 0));
        assertEquals(2, res);

        byte[] byteCode2 = {ICONST_1.b(), ICONST_M1.b(), IADD.b(), IRETURN.b()};
        res = engine.invoke(new Method("", "", "", 0, byteCode2, 2, 0));
        assertEquals(0, res);

        byte[] byteCode3 = {ICONST_2.b(), ICONST_M1.b(), IMUL.b(), IRETURN.b()};
        res = engine.invoke(new Method("", "", "", 0, byteCode3, 2, 0));
        assertEquals(-2, res);
    }

    @Test
    public void increment() {
        byte[] byteCode = {ICONST_1.b(), ISTORE.b(), (byte) 5, IINC.b(), (byte) 5, (byte) 41, ILOAD.b(), (byte) 5, IRETURN.b()};
        long res = engine.invoke(new Method("", "", "", 0, byteCode, 1, 6));

        assertEquals(42, res);
    }

    @Test
    public void duplicate() {
        byte[] byteCode = {ICONST_1.b(), DUP.b(), IADD.b(), IRETURN.b()};
        long res = engine.invoke(new Method("", "", "", 0, byteCode, 2, 0));
        assertEquals(2, res);

        byte[] byteCode2 = {ICONST_1.b(), DUP.b(), IADD.b(), DUP.b(), IADD.b(), IRETURN.b()};
        res = engine.invoke(new Method("", "", "", 0, byteCode2, 2, 0));
        assertEquals(4, res);
    }

    @Test
    public void dupNopPop() {
        byte[] byteCode = {ICONST_1.b(), DUP.b(), NOP.b(), POP.b(), IRETURN.b()};
        long res = engine.invoke(new Method("", "", "", 0, byteCode, 2, 0));
        assertEquals(1, res);

        byte[] byteCode2 = {ICONST_1.b(), DUP.b(), NOP.b(), POP.b(), POP.b(), RETURN.b()};
        res = engine.invoke(new Method("", "", "", 0, byteCode2, 2, 0));
        assertEquals(0, res);
    }

    @Test
    public void dupX1() {
        byte[] byteCode = {ICONST_1.b(), ICONST_2.b(), DUP_X1.b(), IADD.b(), IADD.b(), IRETURN.b()};
        long res = engine.invoke(new Method("", "", "", 0, byteCode, 3, 0));
        assertEquals(5, res);

        byte[] byteCode2 = {ICONST_1.b(), ICONST_2.b(), DUP_X1.b(), IADD.b(), DUP_X1.b(), IADD.b(), IADD.b(), IRETURN.b()};
        res = engine.invoke(new Method("", "", "", 0, byteCode2, 3, 0));
        assertEquals(8, res);
    }

    @Test
    public void IfEqual() {
        byte[] byteCode = {ICONST_1.b(), ICONST_1.b(), IADD.b(), ICONST_2.b(), IF_ICMPEQ.b(), (byte) 0, (byte) 7, ICONST_4.b(), GOTO.b(), (byte) 0, (byte) 4, ICONST_3.b(), IRETURN.b()};
        long res = engine.invoke(new Method("", "", "", 0, byteCode, 2, 0));
        assertEquals(3, (res));

        byte[] byteCode2 = {ICONST_1.b(), ICONST_1.b(), IADD.b(), ICONST_3.b(), IF_ICMPEQ.b(), (byte) 0, (byte) 7, ICONST_4.b(), GOTO.b(), (byte) 0, (byte) 4, ICONST_3.b(), IRETURN.b()};
        res = engine.invoke(new Method("", "", "", 0, byteCode2, 2, 0));
        assertEquals(4, (res));
    }

}
