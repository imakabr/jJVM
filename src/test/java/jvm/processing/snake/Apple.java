package jvm.processing.snake;

import processing.core.PImage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class Apple {
    @Nonnull
    private final PImage image;
    @Nonnull
    private final Point point;
    @Nonnull
    private final Sketch pApplet;
    @Nullable
    private SnakeManager snakeManager;

    public Apple(@Nonnull Sketch pApplet, @Nonnull String imageName) {
        this.pApplet = pApplet;
        this.image = pApplet.loadImage(imageName);
        this.point = new Point(-1, -1);
    }

    @Nonnull
    public Point getPoint() {
        return point;
    }

    public boolean isNotExist() {
        return point.x == -1 && point.y == -1;
    }

    public void draw() {
        pApplet.image(image, point.x, point.y);
    }

    public void create() {
        do {
            point.x = ((int) (Math.random() * (pApplet.width / 10))) * 10;
            point.y = ((int) (Math.random() * (pApplet.height / 10))) * 10;
        } while (Objects.requireNonNull(snakeManager).touchBody(point) /*|| pApplet.map.matchesBody(point)*/);
    }

    public void destroy() {
        point.x = -1;
        point.y = -1;
    }

    public void setSnakeService(@Nonnull SnakeManager snakeManager) {
        this.snakeManager = snakeManager;
    }
}
