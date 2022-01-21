package jvm.io;

public class BufferedReaderJVM {

    private ReaderJVM reader;

    public BufferedReaderJVM(ReaderJVM reader) {
        this.reader = reader;
        initBufferedReaderJVM(reader);
    }

    private native void initBufferedReaderJVM(ReaderJVM reader);

    public native String readLine();
}
