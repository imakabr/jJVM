package jvm.processing.snake;

import processing.core.PApplet;
import processing.net.Client;
import processing.net.Server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SocketManager {

    @Nonnull
    PApplet pApplet;
    @Nonnull
    private final Server server;
    @Nonnull
    private final Map<Integer, Socket> sockets;
    private int port;

    public SocketManager(@Nonnull PApplet mainClass, int port) {
        this.pApplet = mainClass;
        this.server = new Server(mainClass, port);
        this.sockets = new HashMap<>();
        this.port = port + 1;
    }

    public boolean checkInitMessage() {
        Client thisClient = server.available();
        if (thisClient != null) {
            String message = PApplet.trim(thisClient.readString());
            if (message != null) {
                return "init".equals(message);
            }
        }
        return false;
    }

    public void initConnection(@Nonnull Snake snake) {
        sockets.put(snake.getNumber(), new Socket(pApplet, port));
        server.write(port + "\n");
        port++;
    }

    @Nullable
    public String getMessage(@Nonnull Snake snake) {
        return sockets.get(snake.getNumber()).getMessage();
    }

    public void sendMessage(@Nonnull String message, @Nonnull Snake snake) {
        sockets.get(snake.getNumber()).sendMessage(message);
    }
}
