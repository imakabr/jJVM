package jvm.processing.snake;

import javax.annotation.Nonnull;

public class GameController {

    @Nonnull
    private final Sketch pApplet;
    @Nonnull
    private final SocketManager socketManager;
    @Nonnull
    private final Apple apple;
    @Nonnull
    private final ParticleSystemManager particleSystem;
    @Nonnull
    private final SnakeManager snakeManager;
    private int playerCount;
    private boolean ready;


    public GameController(@Nonnull Sketch pApplet, @Nonnull SocketManager socketManager, @Nonnull Apple apple,
                          @Nonnull ParticleSystemManager particleSystem, @Nonnull SnakeManager snakeManager) {
        this.pApplet = pApplet;
        this.socketManager = socketManager;
        this.apple = apple;
        this.particleSystem = particleSystem;
        this.snakeManager = snakeManager;
    }

    private void checkNewPlayer() {
        if (socketManager.checkInitMessage()) {
            Snake snake = createNewSnake();
            socketManager.initConnection(snake);
            snakeManager.add(snake);
            ready = true;
        }
    }

    public void draw() {
        checkNewPlayer();
        snakeManager.draw();
        if (apple.isNotExist()) {
            apple.create();
        }
        apple.draw();
        particleSystem.draw();
        if (!ready) {
            drawTitle();
        }
    }

    private void drawTitle() {
        pApplet.text("Crazy Snake game", pApplet.width / 3f, pApplet.height / 2f);
        pApplet.text("Waiting for players", pApplet.width / 3f, pApplet.height / 2f + 10);
    }

    @Nonnull
    private Snake createNewSnake() {
        Snake snake = new Snake(pApplet, snakeManager, playerCount++, "jvm.processing.snake/body",
                "jvm.processing.snake/head",
                "jvm.processing.snake/head_");
        snake.setApple(apple);
        snake.setManager(particleSystem);
        return snake;
    }
}
