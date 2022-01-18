package jvm.io;

public class InputStreamReaderJVM extends ReaderJVM{

    private InputStreamJVM in;

    public InputStreamReaderJVM(InputStreamJVM inputStream) {
        this.in = inputStream;
        initInputStreamReaderJVM(inputStream);
    }

    private native void initInputStreamReaderJVM(InputStreamJVM in);
}
