package jvm;

import jvm.heap.Heap;
import jvm.parser.Method;

public class Main {

    public static String entryPoint = ".main:([Ljava/lang/String;)V";

    public static void main(String[] args) {
        VirtualMachine virtualMachine = new VirtualMachine();
        virtualMachine.getKlassLoader().loadKlass(args[0]);
        Heap heap = virtualMachine.getHeap();

        int methodIndex = heap.getMethodRepo().getIndexByName(args[0] + entryPoint);
        Method method = heap.getMethodRepo().getMethod(methodIndex);
        long actual = virtualMachine.getEngine().invoke(method);
    }
}
