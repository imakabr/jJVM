package jvm.processing.snake;

import processing.core.PImage;

public class Apple {
    private final PImage image;
    private final Point point;
    private final Snake snake;
    private final Sketch pApplet;

    public Apple(Sketch pApplet, String imageName, Snake snake) {
        this.pApplet = pApplet;
        this.image = pApplet.loadImage(imageName);
        this.point = new Point(-1, -1);
        this.snake = snake;
    }

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
        } while (snake.matchesBody(point) /*|| pApplet.map.matchesBody(point)*/);
    }

    public void destroy() {
        point.x = -1;
        point.y = -1;
    }
}
