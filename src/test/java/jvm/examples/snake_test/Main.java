package jvm.examples.snake_test;

import java.io.IOException;

public class Main {
    private final int port = 10005;

    public static void main(String[] args) throws IOException {
        new Main().main();
    }

    public void main() throws IOException {
        NetworkManager service = new NetworkManager(port);
        service.sendMessage("ready");
        String[] messages = service.getMessage().split(" ");
        new Player(new Grid(Integer.parseInt(messages[1]) / 10, Integer.parseInt(messages[0]) / 10), service).run();
    }
}


