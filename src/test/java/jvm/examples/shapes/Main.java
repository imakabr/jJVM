package jvm.examples.shapes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class Main {

    String[] shapes = {"square", "triangle", "circle", "cross", "diamond", "pentagon", "star"};
    String[] colors = {"red", "yellow", "purple", "blue", "green"};
    int count = 7;

    public static void main(String[] args) throws InterruptedException {
        new Main().main();
    }

    public void main() throws InterruptedException {
        Shape[][] shapes = new Shape[count][count];
        for (int y = 0; y < shapes.length; y++) {
            for (int x = 0; x < shapes[0].length; x++) {
                if (shapes[y][x] == null) {
                    shapes[y][x] = createNewShape(x, y);
                }
                Thread.sleep(200);
                checkTime(shapes);
            }
        }
    }

    private void print(@Nonnull String str) {
        System.out.println(str);
    }

    private void checkTime(@Nonnull Shape[][] shapes) throws InterruptedException {
        for (int y = 0; y < shapes.length; y++) {
            for (int x = 0; x < shapes[0].length; x++) {
                Shape shape = shapes[y][x];
                if (shape != null && shape.isDead()) {
                    print(x + " " + y + " null");
                    shapes[y][x] = null;
                    Thread.sleep(200);
                }
            }
        }
    }

    @Nullable
    public Shape createNewShape(int x, int y) {
        Random random = new Random();
        int shapeIndex = random.nextInt(shapes.length);
        int color = random.nextInt(colors.length);
        int velocity = random.nextInt(3) + 1;
        int time = random.nextInt(200);
        Shape shape;
        if (shapeIndex == 0) {
            shape = new Square(x, y, colors[color], velocity, time);
        } else if (shapeIndex == 1) {
            shape = new Triangle(x, y, colors[color], velocity, time);
        } else if (shapeIndex == 2) {
            shape = new Circle(x, y, colors[color], velocity, time);
        } else if (shapeIndex == 3) {
            shape = new Cross(x, y, colors[color], velocity, time);
        } else if (shapeIndex == 4) {
            shape = new Diamond(x, y, colors[color], velocity, time);
        } else if (shapeIndex == 5) {
            shape = new Pentagon(x, y, colors[color], velocity, time);
        } else if (shapeIndex == 6) {
            shape = new Star(x, y, colors[color], velocity, time);
        } else {
            return null;
        }
        print(shape.draw(random));
        return shape;
    }
}
