package jvm;

import jvm.heap.api.Heap;
import jvm.parser.Method;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;

public class PuzzlersTest {

    public static final String path = "jvm/examples/puzzlers/";

    @Test
    public void puzzle47Test() {
        checkMethod("Puzzle47", "test:()I", 10);
    }

    @Test
    public void puzzle48Test() {
        checkMethod("Puzzle48", "test:()Z", 1);

    }

    @Test
    public void puzzle49Test() {
        checkMethod("Puzzle49", "test:()I", -1930);
    }

    @Test
    public void puzzle51Test() {
        checkMethod("Puzzle51", "test:()Z", 1);
    }

    @Test
    public void puzzle52Test() {
        checkMethod("Puzzle52", "test:()I", 9900);
    }

    @Test
    public void puzzle66Test() {
        checkMethod("Puzzle66", "test:()Z", 1);
    }

    @Test
    public void puzzle67Test() {
        checkMethod("Puzzle67", "test:()Z", 1);
    }

    public void checkMethod(@Nonnull String klass, @Nonnull String methodName, int expected) {
        String className = path + klass;
        VirtualMachine virtualMachine = new VirtualMachine(500, 50, 10000, false);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.getKlassLoader().loadKlass(className);
        int methodIndex = heap.getMethodRepo().getIndexByName(className + "." + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long actual = virtualMachine.getEngine().invoke(method);
        assertEquals(expected, actual);
    }
}
