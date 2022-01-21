package jvm.examples.shapes;


import java.util.Random;

public class Controller {

    private static final String[] moves = {"left", "right", "scale"};

    public Controller() {
    }

    public String draw(String name, String color, int velocity, Random random) {
        int move = random.nextInt(moves.length);
        return new StringBuilder()
                .append(name)
                .append(" ")
                .append(color)
                .append(" ")
                .append(velocity)
                .append(" ")
                .append(moves[move])
                .toString();
    }
}
