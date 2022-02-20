package jvm.lang;

public class RuntimeExceptionJVM extends RuntimeException{

    public RuntimeExceptionJVM(String message) {
        super(message);
    }

    public RuntimeExceptionJVM(Throwable cause) {
        super(cause);
    }

    public RuntimeExceptionJVM() {
    }
}
