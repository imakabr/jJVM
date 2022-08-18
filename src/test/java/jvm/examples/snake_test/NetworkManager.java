package jvm.examples.snake_test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static jvm.examples.snake_test.Main.parseInt;

public class NetworkManager {

    private final BufferedReader startIn;
    private final PrintWriter startOut;
    private final PrintWriter playOut;
    private final BufferedReader playIn;

    public NetworkManager(int port) throws IOException {
        Socket clientSocket = new Socket("127.0.0.1", port);
        this.startOut = new PrintWriter(clientSocket.getOutputStream(), true);
        this.startIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientSocket = new Socket("127.0.0.1", getPlayPort());
        this.playOut = new PrintWriter(clientSocket.getOutputStream(), true);
        this.playIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    private int getPlayPort() throws IOException {
        startOut.println("init");
        int playPort = parseInt(startIn.readLine());
//        startIn.close();
//        startOut.close();
        return playPort;
    }

    public String getMessage() throws IOException {
        return playIn.readLine();
    }

    public void sendMessage(String message) {
        playOut.println(message);
    }


}
