package jvm.io;

public class PrintStreamJVM {

    public native void println(String x);

    public native void print(String x);

    public native void println(char x);

    public native void print(char x);

    public native void println(int x);

    public native void print(int x);

    public void println(Object x) {
        println(x.toString());
    }
    public void print(Object x) {
        print(x.toString());
    }
}
