package jvm.processing.snake;

import processing.core.PImage;
import processing.core.PVector;

import javax.annotation.Nonnull;

public class Snake {

    private int currentSize = 10;
    private int sizeRecord = currentSize;
    private int headPointer = currentSize - 1;
    private int tailPointer = 0;
    private final Point[] data;
    private Point[] body;
    public final Point shift;
    private final int size = 1000;
    private final PImage bodyImg;
    private final PImage headStraightImg;
    private final PImage headAsideImg;
    private final Sketch pApplet;
    private Apple apple;
    private ParticleSystemManage manager;

    public Snake(@Nonnull Sketch pApplet, @Nonnull String bodyImg, @Nonnull String headStraightImg, @Nonnull String headAsideImg) {
        this.pApplet = pApplet;
        this.bodyImg = pApplet.loadImage(bodyImg);
        this.headStraightImg = pApplet.loadImage(headStraightImg);
        this.headAsideImg = pApplet.loadImage(headAsideImg);
        data = new Point[size];
        body = new Point[0];
        shift = new Point(0, -10);
        init();
    }

    public void setApple(Apple apple) {
        this.apple = apple;
    }

    public void setManager(ParticleSystemManage manager) {
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

    public void draw() {
        body = new Point[currentSize];
        int pointer = 1;
        if (headPointer >= tailPointer) {
            for (int i = tailPointer; i <= headPointer; i++) {
                if (i == headPointer) {
                    body[0] = new Point(data[headPointer].x, data[headPointer].y);
                    drawHead();
                } else {
                    body[pointer] = new Point(data[i].x, data[i].y);
                    pApplet.image(bodyImg, data[i].x, data[i].y);
                }
                pointer = i == headPointer ? pointer : pointer + 1;
            }
        } else {
            for (int i = 0; i <= headPointer; i++) {
                if (i == headPointer) {
                    body[0] = new Point(data[headPointer].x, data[headPointer].y);
                    drawHead();
                } else {
                    body[pointer] = new Point(data[i].x, data[i].y);
                    pApplet.image(bodyImg, data[i].x, data[i].y);
                }
                pointer = i == headPointer ? pointer : pointer + 1;
            }
            for (int i = tailPointer; i < size; i++) {
                body[pointer++] = new Point(data[i].x, data[i].y);
                pApplet.image(bodyImg, data[i].x, data[i].y);
            }
        }
        pApplet.text("Size: " + currentSize, 0, 10);
        pApplet.text("Size record: " + sizeRecord, 60, 10);
    }

    private void drawHead() {
        pApplet.image(pApplet.direct == 'u' || pApplet.direct == 'd' ? headStraightImg : headAsideImg, data[headPointer].x, data[headPointer].y);
    }

    public void move() {
        int x = (pApplet.width + (data[headPointer].x + shift.x) % pApplet.width) % pApplet.width;
        int y = (pApplet.height + (data[headPointer].y + shift.y) % pApplet.height) % pApplet.height;
        if (matchesBody(new Point(x, y))) {
            pApplet.rect(0, 0, 640, 360); // bang
            manager.add(new ParticleSystemBuilder(pApplet).count(currentSize).images(new PVector(x, y), bodyImg));
            manager.add(new ParticleSystemBuilder(pApplet).count(400).bloods(new PVector(x, y)));
            currentSize = 1;
            destroy();
        }
//    if (map.matchesBody(x, y)) {
//      map.ruin(x, y, 0);
//      parSys.add(new ParticleSystemBuilder().bricks(new PVector(x, y)));
//      parSys.add(new ParticleSystemBuilder().count(70).bloods(new PVector(x, y)));
//      currentLife--;
//    }

        if (ateFood(x, y)) {
            apple.destroy();
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

    public boolean matchesBody(Point point) {
        for (Point value : body) {
            if (value.x == point.x && value.y == point.y) {
                return true;
            }
        }
        return false;
    }

}
