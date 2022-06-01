package jvm.examples.heap_test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class Service {

    private static final String[] moves = {"left", "right", "scale"};

    private final BufferedReader in;
    private final PrintWriter out;
    private final BufferedReader finalizeIn;
    private final PrintWriter finalizeOut;
    private final Random random;

    public Service(Random random, int port) throws IOException {
        Socket clientSocket = new Socket("127.0.0.1", port);
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientSocket = new Socket("127.0.0.1", port + 1);
        this.finalizeOut = new PrintWriter(clientSocket.getOutputStream(), true);
        this.finalizeIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.random = random;
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    public void send(String message) {
        out.println(message);
    }

    public void sendFinalize(String message) {
        finalizeOut.println(message);
    }

    public String receiveFinalize() throws IOException {
        return finalizeIn.readLine();
    }

    public String getMoves() {
        return moves[random.nextInt(moves.length)];
    }

}
