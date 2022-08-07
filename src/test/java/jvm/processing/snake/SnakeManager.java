package jvm.processing.snake;

import processing.core.PApplet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SnakeManager {

    @Nonnull
    private final Apple apple;
    @Nonnull
    private final List<Snake> snakes;
    @Nonnull
    private final SocketManager socketManager;
    private final int width;
    private final int height;

    public SnakeManager(@Nonnull Apple apple, @Nonnull SocketManager socketManager, int width, int height) {
        this.apple = apple;
        this.snakes = new ArrayList<>();
        this.socketManager = socketManager;
        this.width = width;
        this.height = height;
    }

    public void add(@Nonnull Snake snake) {
        snakes.add(snake);
    }

    public void draw() {
        for (Snake snake : snakes) {
            if (snake.isNotReady()) {
                init(snake);
            } else {
                char direction = parseDirection(socketManager.getMessage(snake));
                if (direction != '0') {
                    snake.move(direction);
                    snake.draw();
                    socketManager.sendMessage(getLocationData(snake), snake);
                }
            }
        }
    }

    public boolean touchBody(@Nonnull Point point) {
        for (Snake snake : snakes) {
            if (snake.touchBody(point)) {
                return true;
            }
        }
        return false;
    }

    private void init(@Nonnull Snake snake) {
        snake.draw();
        String message = PApplet.trim(socketManager.getMessage(snake));
        if (message != null) {
            if ("ready".equals(message)) {
                snake.setReady();
                socketManager.sendMessage(width + " " + height, snake);
                socketManager.sendMessage(getLocationData(snake), snake);
            }
        }
    }

    private char parseDirection(@Nullable String message) {
        if (message != null) {
            return message.charAt(0);
        }
        return '0';
    }

    @Nonnull
    private String getLocationData(@Nonnull Snake snake) {
        StringBuilder builder = new StringBuilder();
        setLocationPoint(builder, apple.getPoint());
        setLocationPoint(builder, Objects.requireNonNull(snake.getHead()));
        setLocationPoints(builder, snake.getBody(), true);
        for (Snake currentSnake : snakes) {
            if (snake.getNumber() == currentSnake.getNumber() || currentSnake.isNotReady()) {
                continue;
            }
            setLocationPoint(builder, Objects.requireNonNull(currentSnake.getHead()));
            setLocationPoints(builder, currentSnake.getBody(), false);
        }
        return builder.toString();
    }

    private void setLocationPoints(@Nonnull StringBuilder builder, @Nonnull Point[] points, boolean lastSign) {
        for (int i = 0; i < points.length; i++) {
            builder.append(points[i].x)
                    .append(" ")
                    .append(points[i].y);
            if (i != points.length - 1 || lastSign) {
                builder.append("|");
            }
        }
    }

    private void setLocationPoint(@Nonnull StringBuilder builder, @Nonnull Point point) {
        builder.append(point.x)
                .append(" ")
                .append(point.y)
                .append("|");
    }

}
