package jvm.examples.heap_test;

import java.io.*;
import java.util.Random;

public class Main {

    private final int port = 10003;
    private final static int count = 18;

    private final String[] shapes = {"square", "triangle", "circle", "cross", "diamond", "pentagon", "star"};
    private final String[] colors = {"red", "yellow", "purple", "blue", "green"};

    private final Service service;
    private final Random random;
    public final static boolean[][] inHeap = new boolean[count][count];

    public Main() throws IOException {
        random = new Random();
        service = new Service(random, port);
    }

    public static void main(String[] args) throws IOException {
        new Main().main();
    }

    public void main() throws IOException {
        Shape[][] shapes = new Shape[count][count];
        while (true) {
            for (int y = 0; y < shapes.length; y++) {
                for (int x = 0; x < shapes[0].length; x++) {
                    if (shapes[y][x] == null && !inHeap[y][x]) {
                        Shape shape = createNewShape(x, y);
                        shapes[y][x] = shape;
                        inHeap[y][x] = true;
                        shape.draw();
                    }
                    checkTime(shapes);
                }
            }
        }
    }

    private void checkTime(Shape[][] shapes) throws IOException {
        for (int y = 0; y < shapes.length; y++) {
            for (int x = 0; x < shapes[0].length; x++) {
                Shape shape = shapes[y][x];
                if (shape != null && shape.timesUp()) {
                    shape.kill(y, x);
                    shapes[y][x] = null;
                }
            }
        }
    }

    private Shape createNewShape(int x, int y) {
        int shapeIndex = random.nextInt(shapes.length);
        int color = random.nextInt(colors.length);
        int velocity = random.nextInt(6) + 1;
        int time = random.nextInt(200);
        Shape shape = null;
        if (shapeIndex == 0) {
            shape = new Square(x, y, colors[color], velocity, time, service);
        } else if (shapeIndex == 1) {
            shape = new Triangle(x, y, colors[color], velocity, time, service);
        } else if (shapeIndex == 2) {
            shape = new Circle(x, y, colors[color], velocity, time, service);
        } else if (shapeIndex == 3) {
            shape = new Cross(x, y, colors[color], velocity, time, service);
        } else if (shapeIndex == 4) {
            shape = new Diamond(x, y, colors[color], velocity, time, service);
        } else if (shapeIndex == 5) {
            shape = new Pentagon(x, y, colors[color], velocity, time, service);
        } else if (shapeIndex == 6) {
            shape = new Star(x, y, colors[color], velocity, time, service);
        } else {
            System.out.println("There is no such shape");
            System.exit(-1);
        }
        return shape;
    }
}
