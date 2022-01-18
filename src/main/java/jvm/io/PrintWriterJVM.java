package jvm.io;


public class PrintWriterJVM {

    private OutputStreamJVM out;

    public PrintWriterJVM(OutputStreamJVM outputStream, boolean autoFlush) {
        this.out = outputStream;
        initPrintWriterJVM(outputStream, autoFlush);
    }

    private native void initPrintWriterJVM(OutputStreamJVM outputStreamObjRef, boolean autoFlush);

    public native void println(String s);

}
