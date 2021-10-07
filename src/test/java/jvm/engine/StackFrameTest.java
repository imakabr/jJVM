package jvm.engine;

import jvm.engine.StackFrame;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class StackFrameTest {

    @Test
    public void stackOperandOutOfBoundByPushTest() {
        StackFrame stack = new StackFrame(0, 3);
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertThrows(IndexOutOfBoundsException.class, () -> stack.push(4));
    }

    @Test
    public void stackOperandOutOfBoundByPushTest2() {
        StackFrame stack = new StackFrame(3, 3);
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertThrows(IndexOutOfBoundsException.class, () -> stack.push(4));
    }

    @Test
    public void stackOperandOutOfBoundByPopTest() {
        StackFrame stack = new StackFrame(0, 3);
        assertThrows(IndexOutOfBoundsException.class, stack::pop);
    }

    @Test
    public void pushPopTest() {
        StackFrame stack = new StackFrame(0, 2);
        stack.push(1);
        stack.push(2);
        assertThrows(IndexOutOfBoundsException.class, () -> stack.push(3)); // check overflow
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
        assertThrows(IndexOutOfBoundsException.class, stack::pop); // check stack pointer on the top of stack operand
    }

    @Test
    public void invokeMethodAndReturnValue() {
        StackFrame stack = new StackFrame(0, 3);
        // push local variables to new method
        stack.push(1);
        stack.push(2);
        stack.programCounter = 555;
        // invoke new method
        stack.initNewMethodStack(2, 2, 1);
        // check local variables
        assertEquals(1, stack.getLocalVar(0));
        assertEquals(2, stack.getLocalVar(1));
        stack.push(4); // push return value
        stack.programCounter = 666;
        // return to source method
        stack.destroyCurrentMethodStack(0, 3, true);
        assertEquals(4, stack.pop()); // check return value
        assertEquals(555, stack.programCounter);
    }

    @Test
    public void invokeMethodWithoutReturnValue() {
        StackFrame stack = new StackFrame(0, 3);
        // push local variables to new method
        stack.push(1);
        stack.push(2);
        stack.programCounter = 555;
        // invoke new method
        stack.initNewMethodStack(2, 2, 1);
        // check local variables
        assertEquals(1, stack.getLocalVar(0));
        assertEquals(2, stack.getLocalVar(1));
        stack.programCounter = 666;
        // return to source method
        stack.destroyCurrentMethodStack(0, 3, false);
        assertEquals(555, stack.programCounter);
        assertThrows(IndexOutOfBoundsException.class, stack::pop); // check operand stack is empty
    }

    @Test
    public void invokeManyMethods() {
        StackFrame stack = new StackFrame(0, 3);
        stack.push(1);
        stack.push(2);
        stack.programCounter = 111;

        stack.initNewMethodStack(2, 4, 2);
        // check local variables
        assertEquals(1, stack.getLocalVar(0));
        assertEquals(2, stack.getLocalVar(1));
        stack.push(333);
        stack.pop();
        stack.push(3);
        stack.setLocalVar(2, 4);
        stack.push(5);
        stack.setLocalVar(3, 6);
        stack.programCounter = 222;

        stack.initNewMethodStack(2, 3, 5);
        // check local variables
        assertEquals(3, stack.getLocalVar(0));
        assertEquals(5, stack.getLocalVar(1));
        stack.setLocalVar(2, 7);
        stack.push(8); // push return value
        stack.programCounter = 333;

        stack.destroyCurrentMethodStack(4, 2, true);
        assertEquals(8, stack.pop()); // check return value
        assertEquals(222, stack.programCounter);
        // check local variables
        assertEquals(1, stack.getLocalVar(0));
        assertEquals(2, stack.getLocalVar(1));
        assertEquals(4, stack.getLocalVar(2));
        assertEquals(6, stack.getLocalVar(3));
        assertThrows(IndexOutOfBoundsException.class, stack::pop); // check operand stack is empty
        stack.push(9); // push return value

        stack.destroyCurrentMethodStack(0, 3, true);
        assertEquals(9, stack.pop()); // check return value
        assertEquals(111, stack.programCounter);
        assertThrows(IndexOutOfBoundsException.class, stack::pop); // check operand stack is empty

        stack.initNewMethodStack(0, 2, 1);
        stack.programCounter = 777;
        stack.setLocalVar(0, 10);
        stack.setLocalVar(1, 11);

        stack.destroyCurrentMethodStack(0, 3, false);
        assertEquals(111, stack.programCounter);
        assertThrows(IndexOutOfBoundsException.class, stack::pop); // check operand stack is empty
    }


}