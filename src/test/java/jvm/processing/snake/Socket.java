package jvm.processing.snake;

import processing.core.PApplet;
import processing.net.Client;
import processing.net.Server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class Socket {

    private final Server server;

    public Socket(PApplet mainClass, int port) {
        server = new Server(mainClass, port);
    }

    @Nullable
    public String getMessage() {
        Client thisClient = server.available();
        if (thisClient != null) {
            String message = thisClient.readString();
//            System.out.println(thisClient.ip() + "\t" + message);
            return message;
        }
        return null;
    }

    public void sendMessage(@Nonnull String message) {
        server.write(message + "\n");
    }
}
