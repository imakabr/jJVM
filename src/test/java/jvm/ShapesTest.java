package jvm;

import jvm.heap.Heap;
import jvm.parser.Method;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;

public class ShapesTest {

    public static final String klass = "jvm/examples/shapes/Main";

    @Test
    @Ignore
    public void checkWriteToFile() {
        checkMethod("main:([Ljava/lang/String;)V", 0);
    }

    private void checkMethod(@Nonnull String methodName, long expected) {
        Main main = new Main(10000, 50, 10000);
        main.getKlassLoader().loadKlass(klass);
        Heap heap = main.getHeap();

        int methodIndex = heap.getMethodRepo().getIndexByName(klass + "." + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long actual = main.getEngine().invoke(method);
        assertEquals(expected, actual);
    }
}
