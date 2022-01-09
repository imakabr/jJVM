package jvm.examples.shapes;

import jvm.lang.StringBuilder;

import javax.annotation.Nonnull;
import java.util.Random;

public class Controller {

    private static final String[] moves = {"left", "right", "scale"};

    public Controller() {
    }

    @Nonnull
    public String draw(@Nonnull String name, @Nonnull String color, int velocity, @Nonnull Random random) {
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
