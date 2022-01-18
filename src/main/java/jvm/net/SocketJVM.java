package jvm.net;

import jvm.io.InputStreamJVM;
import jvm.io.OutputStreamJVM;

public class SocketJVM {

    public SocketJVM(String ipAddress, int port) {
        initSocket(ipAddress, port);
    }

    private native void initSocket(String ipAddress, int port);

    public native InputStreamJVM getInputStream();

    public native OutputStreamJVM getOutputStream();
}
