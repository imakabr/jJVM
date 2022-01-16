package jvm.net;

import java.io.InputStream;
import java.io.OutputStream;

public class SocketJVM {

    public SocketJVM(String ipAddress, int port) {
        initSocket(ipAddress, port);
    }

    private native void initSocket(String ipAddress, int port);

    public native InputStream getInputStream();

    public native OutputStream getOutputStream();
}
