package jvm;

import jvm.heap.Heap;
import jvm.parser.Method;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashSet;


public class SnakeTest {

    public static final String klass = "jvm/examples/snake_test/Main";

    @Test
    @Ignore
    public void runPlayer() {
        checkMethod("main:([Ljava/lang/String;)V");
    }

    private void checkMethod(@Nonnull String methodName) {
        VirtualMachine virtualMachine = new VirtualMachine(200000, 50, 10000, true);
        virtualMachine.getKlassLoader().loadKlass(klass);
        Heap heap = virtualMachine.getHeap();
        virtualMachine.runHeapMonitor(new HashSet<>(Arrays.asList("java/lang/String",
                "java/lang/StringBuilder",
                "jvm/examples/snake_test/Node",
                "Collections { " +
                        "java/util/HashMap, " +
                        "jvm/util/HashMapNode, " +
                        "java/util/HashSet, " +
                        "java/util/ArrayList, " +
                        "java/util/ArrayDeque, " +
                        "}")));

        int methodIndex = heap.getMethodRepo().getIndexByName(klass + "." + methodName);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        virtualMachine.getEngine().invoke(method);
    }
}
