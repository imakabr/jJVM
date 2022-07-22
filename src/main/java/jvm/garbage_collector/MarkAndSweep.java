package jvm.garbage_collector;

import jvm.JVMType;
import jvm.engine.ExecutionEngine;
import jvm.engine.StackFrame;
import jvm.heap.Heap;
import jvm.heap.InstanceKlass;
import jvm.heap.InstanceObject;
import jvm.heap.ReferenceTable;
import jvm.parser.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static jvm.engine.ExecutionEngine.NULL;

public class MarkAndSweep implements GarbageCollector {

    @Nonnull
    private final StackFrame stackFrame;
    @Nullable
    private Heap heap;
    private int count;
    private boolean inProgress;
    private int startObjRef;

    public MarkAndSweep(@Nonnull StackFrame stackFrame) {
        this.stackFrame = stackFrame;
    }

    @Override
    public void setHeap(@Nonnull Heap heap) {
        this.heap = heap;
    }

    @Override
    public void run() {
        if (heap != null) {
            inProgress = true;
            HashSet<Integer> aliveObjects = new HashSet<>();
            Queue<Integer> queue = new ArrayDeque<>();

            collectObjectsFromInstanceKlassess(queue);
            findAliveObjects(queue, aliveObjects);

            collectObjectsFromStackFrame(queue);
            findAliveObjects(queue, aliveObjects);

            collectObjectsFromStringCache(queue);
            findAliveObjects(queue, aliveObjects);

            removeDeadObjectsFromHeap(aliveObjects);

//            System.out.println("GC completed " + count++);
            inProgress = false;
        }
    }

    private void collectObjectsFromStringCache(@Nonnull Queue<Integer> queue) {
        queue.addAll(Objects.requireNonNull(heap).getStringObjRef());
    }

    private void removeDeadObjectsFromHeap(@Nonnull Set<Integer> aliveObjects) {
        if (heap != null) {
            long startTime = System.currentTimeMillis();
            InstanceObject[] objects = heap.getInstanceObjects();
            ReferenceTable refTable = heap.getReferenceTable();
            int objRef = startObjRef;
            for (int i = 0; i < objects.length; i++) {
                int objIndex = refTable.getInstanceObjectIndex(objRef);
                if (objIndex != -1 && objects[objIndex] != null && !aliveObjects.contains(objRef)) {
                    InstanceObject object = objects[objIndex];
                    int klassIndex = !object.isArray() ? object.getKlassIndex() : -1;
                    if (klassIndex != -1) {
                        InstanceKlass klass = heap.getInstanceKlass(klassIndex);
                        Method method = heap.getMethodRepo().getMethod(
                                klass.getMethodIndex(
                                        klass.getIndexByVirtualMethodName("finalize:()V")));
                        StackFrame stackFrame = new StackFrame(method.getVarSize(), method.getOperandSize());
                        stackFrame.setLocalVar(0, setRefValueType(objRef));
                        new ExecutionEngine(heap, stackFrame).invoke(method);
                    }
                    objects[objIndex] = null;
                    heap.decrementInstanceObjectSize();
                    refTable.clearObjectIndex(objRef);
                }
                if (System.currentTimeMillis() - startTime > 400) {
                    startObjRef = objRef;
                    return;
                }
                objRef = (objRef + 1) % objects.length;
            }
            startObjRef = 0;
        }
    }

    private void collectObjectsFromStackFrame(@Nonnull Queue<Integer> queue) {
        long[] stack = stackFrame.getStack();
        int stackSize = stackFrame.getSize();
        for (int i = 0; i <= stackSize; i++) {
            long value = stack[i];
            if (getValueType(value) == JVMType.A.ordinal() && getPureValue(value) != NULL) {
                queue.add(getPureValue(value));
            }
        }
    }

    private void collectObjectsFromInstanceKlassess(@Nonnull Queue<Integer> queue) {
        if (heap != null) {
            for (InstanceKlass klass : heap.getInstanceKlasses()) {
                if (klass == null) {
                    break;
                }
                int objectRef = klass.getObjectRef();
                if (objectRef != -1) {
                    queue.add(objectRef);
                }
            }
        }
    }

    @Override
    public boolean isInProgress() {
        return inProgress;
    }

    private void findAliveObjects(@Nonnull Queue<Integer> queue, @Nonnull Set<Integer> visitedObjects) {
        if (heap != null) {
            ReferenceTable refTable = heap.getReferenceTable();
            while (!queue.isEmpty()) {
                int objectRef = queue.poll();
                if (!visitedObjects.contains(objectRef)) {
                    int objectIndex = refTable.getInstanceObjectIndex(objectRef);
                    if (objectIndex != -1) {
                        InstanceObject object = heap.getInstanceObject(objectRef);
                        for (long value : object.getFieldValues()) {
                            if (getValueType(value) == JVMType.A.ordinal() && getPureValue(value) != NULL) {
                                queue.add(getPureValue(value));
                            }
                        }
                        visitedObjects.add(objectRef);
                    }
                }
            }
        }
    }

    private int getValueType(long value) {
        int type = (int) (value >> 32);
        return type >>> 31 == 1 ? ~type : type; // if 'type >>> 31 == 1' (negative sign) type was inverted
    }

    private int getPureValue(long value) {
        return (int) value;
    }

    private long setRefValueType(int value) {
        return setValueType(JVMType.A.ordinal()) ^ value;
    }

    private long setValueType(int type) {
        return ((long) type << 32);
    }

}
