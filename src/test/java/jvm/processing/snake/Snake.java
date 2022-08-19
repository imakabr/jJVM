package jvm.processing.snake;

import processing.core.PImage;
import processing.core.PVector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Snake {

    public char direct = 'u';
    private int currentSize = 10;
    private int sizeRecord = currentSize;
    private int headPointer = currentSize - 1;
    private int tailPointer = 0;
    private final Point[] data;
    private Point[] body;
    @Nullable
    private Point head;
    @Nonnull
    public final Point shift;
    private final int size = 1000;
    @Nonnull
    private final PImage bodyImg;
    @Nonnull
    private final PImage headStraightImg;
    @Nonnull
    private final PImage headAsideImg;
    @Nonnull
    private final Sketch pApplet;
    private Apple apple;
    private ParticleSystemManager manager;
    @Nonnull
    private final SnakeManager snakeManager;
    private boolean ready;
    private final int number;

    public Snake(@Nonnull Sketch pApplet, @Nonnull SnakeManager snakeManager, int number,
                 @Nonnull String bodyImg, @Nonnull String headStraightImg, @Nonnull String headAsideImg) {
        this.pApplet = pApplet;
        this.snakeManager = snakeManager;
        this.number = number;
        this.bodyImg = pApplet.loadImage(bodyImg + number % 4 + ".png");
        this.headStraightImg = pApplet.loadImage(headStraightImg + number % 4 + ".png");
        this.headAsideImg = pApplet.loadImage(headAsideImg + number % 4 + ".png");
        data = new Point[size];
        body = new Point[0];
        shift = new Point(0, -10);
        init();
    }

    public int getNumber() {
        return number;
    }

    public boolean isNotReady() {
        return !ready;
    }

    public void setReady() {
        this.ready = true;
    }

    public void setApple(@Nonnull Apple apple) {
        this.apple = apple;
    }

    public void setManager(@Nonnull ParticleSystemManager manager) {
        this.manager = manager;
    }

    private void init() {
        int count = 0;
        for (int i = 0; i < size; i++) {
            data[i] = new Point();
            if (count < currentSize) {
                data[i].x = pApplet.width / 2;
                data[i].y = pApplet.height / 2 + i * 10;
                count++;
            }
        }
    }

    @Nonnull
    public Point[] getBody() {
        return body;
    }

    @Nullable
    public Point getHead() {
        return head;
    }

    public void draw() {
        body = new Point[currentSize - 1];
        int pointer = 0;
        if (headPointer >= tailPointer) {
            for (int i = tailPointer; i <= headPointer; i++) {
                if (i == headPointer) {
                    head = new Point(data[headPointer].x, data[headPointer].y);
                    drawHead();
                } else {
                    body[pointer++] = new Point(data[i].x, data[i].y);
                    pApplet.image(bodyImg, data[i].x, data[i].y);
                }
            }
        } else {
            for (int i = 0; i <= headPointer; i++) {
                if (i == headPointer) {
                    head = new Point(data[headPointer].x, data[headPointer].y);
                    drawHead();
                } else {
                    body[pointer++] = new Point(data[i].x, data[i].y);
                    pApplet.image(bodyImg, data[i].x, data[i].y);
                }
            }
            for (int i = tailPointer; i < size; i++) {
                body[pointer++] = new Point(data[i].x, data[i].y);
                pApplet.image(bodyImg, data[i].x, data[i].y);
            }
        }
        pApplet.text("Snake" + number + ": " + currentSize, 0, 10 * (number + 1));
        pApplet.text("Record: " + sizeRecord, 70, 10 * (number + 1));
    }

    private void drawHead() {
        pApplet.image(direct == 'u' || direct == 'd' ? headStraightImg : headAsideImg, data[headPointer].x, data[headPointer].y);
    }

    public void move(char direction) {
        changeDirection(direction);
        int x = (pApplet.width + (data[headPointer].x + shift.x) % pApplet.width) % pApplet.width;
        int y = (pApplet.height + (data[headPointer].y + shift.y) % pApplet.height) % pApplet.height;
        if (snakeManager.touchBody(new Point(x, y))) {
            manager.add(new ParticleSystemBuilder(pApplet).count(currentSize).images(new PVector(x, y), bodyImg));
            manager.add(new ParticleSystemBuilder(pApplet).count(400).bloods(new PVector(x, y), number % 4));
            currentSize = 1;
            destroy();
            do {
                x = ((int) (Math.random() * (pApplet.width / 10))) * 10;
                y = ((int) (Math.random() * (pApplet.height / 10))) * 10;
            } while (snakeManager.touchBody(new Point(x, y)));
        }

        if (ateFood(x, y)) {
            apple.destroy();
            apple.create();
            currentSize++;
            sizeRecord = Math.max(currentSize, sizeRecord);
        } else {
            data[tailPointer].x = 0;
            data[tailPointer].y = 0;
            tailPointer = (tailPointer + 1) % size;
        }
        headPointer = (headPointer + 1) % size;
        data[headPointer].x = x;
        data[headPointer].y = y;
    }

    private void changeDirection(char direction) {
        if (direction == 'u' && shift.y != 10) {
            shift.y = -10;
            shift.x = 0;
            direct = 'u';
        } else if (direction == 'd' && shift.y != -10) {
            shift.y = 10;
            shift.x = 0;
            direct = 'd';
        } else if (direction == 'l' && shift.x != 10) {
            shift.x = -10;
            shift.y = 0;
            direct = 'l';
        } else if (direction == 'r' && shift.x != -10) {
            shift.x = 10;
            shift.y = 0;
            direct = 'r';
        }
    }

    private void destroy() {
        for (int i = 0; i < data.length; i++) {
            data[i] = new Point();
        }
        headPointer = 0;
        tailPointer = 0;
    }

    private boolean ateFood(int x, int y) {
        return apple.getPoint().x == x && apple.getPoint().y == y;
    }

    public boolean touchBody(@Nonnull Point point) {
        if (point.equals(getHead())) {
            return true;
        }
        for (Point value : body) {
            if (value.equals(point)) {
                return true;
            }
        }
        return false;
    }

}
