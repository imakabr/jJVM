package jvm.processing.snake;

import processing.core.PApplet;

public class Sketch extends PApplet {

    private final int screenWidth = 400;
    private final int screenHeight = 400;
    private GameController manager;

    public void settings() {
        size(screenWidth, screenHeight);
    }

    public void setup() {
        noStroke();
        background(0);
        frameRate(30);
        Apple apple = new Apple(this, "jvm.processing.snake/apple.png");
        SocketManager socketManager = new SocketManager(this, 10005);
        SnakeManager snakeManager = new SnakeManager(apple, socketManager, screenWidth, screenHeight);
        apple.setSnakeService(snakeManager);
        manager = new GameController(this, socketManager, apple, new ParticleSystemManager(), snakeManager);
    }

    public void draw() {
        background(25, 25, 25);
        manager.draw();
    }
}
