package jvm.parser;

public class ParserConstants {
    public static final int ACC_PUBLIC = 0x0001;      // Declared public; may be accessed from outside its package.
    public static final int ACC_PRIVATE = 0x0002;      // Declared private; usable only within the defining class.
    public static final int ACC_PROTECTED = 0x0004;      // Declared protected; may be accessed within subclasses.
    public static final int ACC_STATIC = 0x0008;      // Declared static
    public static final int ACC_FINAL = 0x0010;       // Declared final; no subclasses allowed.
    public static final int ACC_SUPER = 0x0020;       // (Class) Treat superclass methods specially when invoked by the invokespecial instruction.
    public static final int ACC_VOLATILE = 0x0040;       // (Field) Declared volatile; cannot be cached.
    public static final int ACC_TRANSIENT = 0x0080;       // (Field) Declared transient; not written or read by a persistent object manager.
    public static final int ACC_INTERFACE = 0x0200;   // (Class) Is an interface, not a class.
    public static final int ACC_ABSTRACT = 0x0400;    // (Class) Declared abstract; must not be instantiated.
    public static final int ACC_SYNTHETIC = 0x1000;   // Declared synthetic; not present in the source code.
    public static final int ACC_ANNOTATION = 0x2000;  // Declared as an annotation type.
    public static final int ACC_ENUM = 0x4000; 	      // Declared as an enum type.
    // Method-only constants
    public static final int ACC_SYNCHRONIZED = 0x0020;       // (Method) Declared synchronized; invocation is wrapped by a monitor use.
    public static final int ACC_BRIDGE = 0x0040;       // (Method) A bridge, generated by the compiler.
    public static final int ACC_VARARGS = 0x0080;       // (Method) Declared with variable number of arguments.
    public static final int ACC_NATIVE = 0x0100;       // (Method) Declared native; implemented in a language other than Java.
    public static final int ACC_ABSTRACT_M = 0x0400;       // (Method) Declared abstract; no implementation is provided.
    public static final int ACC_STRICT = 0x0800;       // (Method) Declared strictfp; floating-point mode is FP-strict.
}
