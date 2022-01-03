package jvm.lang;

import jvm.parser.Klass;
import jvm.parser.Method;

import static jvm.engine.Opcode.*;
import static jvm.engine.Opcode.IRETURN;
import static jvm.heap.KlassLoader.ABSENCE;
import static jvm.parser.ParserConstants.ACC_NATIVE;
import static jvm.parser.ParserConstants.ACC_PUBLIC;

public class ObjectJVM {

    private static final byte[] JUST_RETURN = {RETURN.b()};
    private static final byte[] EQUALS_BYTECODE = {ALOAD_0.b(), ALOAD_1.b(), IF_ACMPNE.b(), (byte) 0, (byte) 7, ICONST_1.b(), GOTO.b(), (byte) 0, (byte) 4, ICONST_0.b(), IRETURN.b()};

    private static final Method INIT = new Method("java/lang/Object", "()V", "<init>:()V", ACC_PUBLIC, JUST_RETURN, 0, 1);
    private static final Method HASHCODE = new Method("java/lang/Object", "()I", "hashCode:()I", ACC_PUBLIC + ACC_NATIVE, JUST_RETURN, 0, 1);
    private static final Method EQUALS = new Method("java/lang/Object", "(Ljava/lang/Object;)Z", "equals:(Ljava/lang/Object;)Z", ACC_PUBLIC, EQUALS_BYTECODE, 2, 2);
    private static final Method TO_STRING = new Method("java/lang/Object", "()Ljava/lang/String;", "toString:()Ljava/lang/String;", ACC_PUBLIC + ACC_NATIVE, JUST_RETURN, 2, 1);
    private static final Method FINALIZE = new Method("java/lang/Object", "()V", "finalize:()V", ACC_PUBLIC, JUST_RETURN, 0, 1);

    public static Klass getObjectKlass() {
        Klass object = new Klass("java/lang/Object", ABSENCE);
        object.addMethods(new Method[]{INIT, HASHCODE, EQUALS, TO_STRING, FINALIZE});
        return object;
    }

}
