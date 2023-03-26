package jvm.engine;

import static jvm.Utils.*;

public class StackFrame {
    private final long[] stack;
    private int localVariable;
    public int programCounter;
    private int stackPointer;

    private int varSize;
    private int operandSize;
    public int invokeCount;

    public StackFrame(int varSize, int operandSize) {
        init(varSize, operandSize);
        stack = new long[1000];
    }

    public StackFrame(int size) {
        stack = new long[size];
    }

    public final void init(int varSize, int operandSize) {
        this.varSize = varSize;
        this.operandSize = operandSize;
        this.stackPointer = varSize;
    }

    public final long getObjectRefBeforeInvoke(int argSize) {
        return stack[stackPointer - 1 - argSize];
    }

    public final void initNewMethodStack(int argSize, int varSize, int operandSize) {
        this.varSize = varSize;
        this.operandSize = operandSize;
        int newLocalVariable = stackPointer - argSize;
        int addressPC = newLocalVariable + varSize;
        stack[addressPC] = programCounter;
        stack[addressPC + 1] = localVariable;
        localVariable = newLocalVariable;
        stackPointer = addressPC + 2;
        invokeCount++;
    }

    public final void destroyCurrentMethodStack(int varSize, int operandSize, boolean returnValue) {
        int addressPC = localVariable + this.varSize;
        programCounter = (int) stack[addressPC];
        stack[addressPC] = 0;
        int oldLocalVariable = localVariable;
        localVariable = (int) stack[addressPC + 1];
        stack[addressPC + 1] = 0;
        restoreReturnValueAndSP(oldLocalVariable, returnValue);
        this.varSize = varSize;
        this.operandSize = operandSize;
        invokeCount--;
    }

    private void restoreReturnValueAndSP(int oldLocalVariable, boolean returnValue) {
        if (returnValue) {
            stack[oldLocalVariable] = stack[stackPointer - 1]; // restore return value to new operand stack
        }
        stackPointer = returnValue ? oldLocalVariable + 1 : oldLocalVariable;
    }

    public final long getLocalVar(int index) {
        checkLocalVarBounds(index);
        return stack[index + localVariable];
    }

    public final void setLocalVar(int index, long value) {
        checkLocalVarBounds(index);
        stack[index + localVariable] = value;
    }

    public final long pop() {
        if (stackPointer <= localVariable + varSize + (invokeCount > 0 ? 2 : 0)) {
            throw new IndexOutOfBoundsException("wrong POP operation on the operand stack");
        }
        return stack[--stackPointer];
    }

    public final void push(long value) {
        if (stackPointer - (localVariable + varSize + (invokeCount > 0 ? 2 : 0)) >= operandSize) {
            throw new IndexOutOfBoundsException("wrong PUSH operation on the operand stack");
        }
        stack[stackPointer++] = value;
    }

    public final void dup() {
        push(stack[stackPointer - 1]);
    }

    public final void dupX1() {
        long first = pop();
        long second = pop();
        push(first);
        push(second);
        push(first);
    }

    private void checkLocalVarBounds(int index) {
        if (index < 0 || index >= varSize) {
            throw new IndexOutOfBoundsException("wrong index for local variables stack");
        }
    }

    public long[] getStack() {
        return stack;
    }

    public int getSize() {
        return localVariable + varSize + (invokeCount > 0 ? 2 : 0) + operandSize;
    }

    @Override
    public String toString() {
        int size = getSize();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (localVariable == i) {
                builder.append("LV->| "); // the beginning of Local Variables
            }
            if (localVariable + varSize == i) {
                builder.append("| Saved_Data->| "); // the beginning of Saved Data
            }
            if (localVariable + varSize + (invokeCount > 0 ? 2 : 0) == i) {
                builder.append("| OS->| "); // the beginning of Operand Stack
            }
            if (stackPointer == i) {
                builder.append("SP->"); // Stack Pointer
            }
            addValue(builder, stack[i]);
            if (localVariable + varSize + (invokeCount > 0 ? 2 : 0) + operandSize - 1 == i) {
                builder.append("|<-OS"); // the ending of Operand Stack
            }
        }
        return builder.length() == 0 ? "absence" : builder.toString();
    }
}
