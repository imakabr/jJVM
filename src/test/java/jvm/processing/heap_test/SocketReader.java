package jvm.processing.heap_test;

import processing.core.PApplet;
import processing.net.*;

class SocketReader implements Reader {

    private final Server server;

    public SocketReader(PApplet mainClass, int port) {
        server = new Server(mainClass, port);
    }

    public String getMessage() {
        Client thisClient = server.available();
        if (thisClient != null) {
            String message = thisClient.readString();
            System.out.println(thisClient.ip() + "\t" + message);
            server.write("next\n");
            return message;
        }
        return null;
    }
}
