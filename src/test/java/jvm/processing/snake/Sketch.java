package jvm.processing.snake;

import processing.core.PApplet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Sketch extends PApplet {

    char direct = 'u';
    private final int screenWidth = 800;
    private final int screenHeight = 800;
    private final int port = 10005;
    private Socket socket;
    private boolean ready;

    private Snake snake;
    private Map map;
    private ParticleSystemManage parSys;
    private Apple apple;

    public void settings() {
        size(screenWidth, screenHeight);
    }

    public void setup() {
        noStroke();
        background(0);
        frameRate(100);

        snake = new Snake(this, "jvm.processing.snake/body.png", "jvm.processing.snake/head.png", "jvm.processing.snake/head2.png");
        apple = new Apple(this, "jvm.processing.snake/apple.png", snake);
        parSys = new ParticleSystemManage();
        snake.setApple(apple);
        snake.setManager(parSys);
        socket = new Socket(this, port);
//        map = new Map(hight / 10, width / 10, "brick.png");
//        map.loadFromFile("m-show2.txt");
    }

    public void draw() {
        background(25, 25, 25);
        if (!ready) {
            init();
        } else {
            char direction = parseDirection(socket.getMessage());
            if (direction != '0') {
                changeDirection(direction);
                snake.move();
                snake.draw();
                socket.sendMessage(getLocationData());
            }

        }
        if (apple.isNotExist()) {
            apple.create();
        }
        apple.draw();
        parSys.draw();
    }

    private void init() {
        snake.draw();
        String message = PApplet.trim(socket.getMessage());
        if (message != null) {
            if ("ready".equals(message)) {
                ready = true;
                socket.sendMessage(width + " " + height);
                socket.sendMessage(getLocationData());
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
    private String getLocationData() {
        Point applePoint = apple.getPoint();
        StringBuilder builder = new StringBuilder()
                .append(applePoint.x)
                .append(" ")
                .append(applePoint.y)
                .append("|");
        Point[] body = snake.getBody();
        for (int i = 0; i < body.length; i++) {
            builder.append(body[i].x)
                    .append(" ")
                    .append(body[i].y);
            if (i != body.length - 1) {
                builder.append("|");
            }
        }
        return builder.toString();
    }

    public void changeDirection(char direction) {
        if (direction == 'u' && snake.shift.y != 10) {
            snake.shift.y = -10;
            snake.shift.x = 0;
            direct = 'u';
        } else if (direction == 'd' && snake.shift.y != -10) {
            snake.shift.y = 10;
            snake.shift.x = 0;
            direct = 'd';
        } else if (direction == 'l' && snake.shift.x != 10) {
            snake.shift.x = -10;
            snake.shift.y = 0;
            direct = 'l';
        } else if (direction == 'r' && snake.shift.x != -10) {
            snake.shift.x = 10;
            snake.shift.y = 0;
            direct = 'r';
        }
    }


    public void keyPressed() {
        if (key == CODED) {
            if (keyCode == UP && snake.shift.y != 10) {
                snake.shift.y = -10;
                snake.shift.x = 0;
                direct = 'u';
            } else if (keyCode == DOWN && snake.shift.y != -10) {
                snake.shift.y = 10;
                snake.shift.x = 0;
                direct = 'd';
            } else if (keyCode == LEFT && snake.shift.x != 10) {
                snake.shift.x = -10;
                snake.shift.y = 0;
                direct = 'l';
            } else if (keyCode == RIGHT && snake.shift.x != -10) {
                snake.shift.x = 10;
                snake.shift.y = 0;
                direct = 'r';
            }
        }
    }
}
