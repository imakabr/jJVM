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
        new Player(new Grid(parseInt(messages[1]) / 10, parseInt(messages[0]) / 10), service).run();
    }

    public static int parseInt(String str) {
        int number = 0;
        for (int i = 0; i < str.length(); i++) {
            number = number * 10 + parseChar(str.charAt(i));
        }
        return number;
    }

    private static int parseChar(char value) {
        if (value >= '0' && value <= '9') {
            return 9 - ('9' - value);
        }
        return -6666666;
    }
}


