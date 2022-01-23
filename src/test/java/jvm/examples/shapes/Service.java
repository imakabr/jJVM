package jvm.examples.shapes;


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
    private final Random random;

    public Service(Random random, int port) throws IOException {
        Socket clientSocket = new Socket("127.0.0.1", port);
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.random = random;
    }

    public String receive() throws IOException {
        return in.readLine();
    }

    public void send(String message) {
        out.println(message);
    }

    public String getMoves() {
        return moves[random.nextInt(moves.length)];
    }

}
