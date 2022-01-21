package jvm.examples.shapes;

import java.io.*;
import java.net.Socket;
import java.util.Random;

public class Main {

    String[] shapes = {"square", "triangle", "circle", "cross", "diamond", "pentagon", "star"};
    String[] colors = {"red", "yellow", "purple", "blue", "green"};
    int count = 35;
    int port = 10003;

    PrintWriter out;
    Socket clientSocket;
    BufferedReader in;

    public Main() throws IOException {
        clientSocket = new Socket("127.0.0.1", port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        new Main().main();
    }

    private void sendToServer(String message) throws IOException {
        out.println(message);
        if (!"next".equals(in.readLine())) {
            System.out.println("something wrong");
            System.exit(-1);
        }
    }


    public void main() throws InterruptedException, IOException {
        Shape[][] shapes = new Shape[count][count];
        while (true) {
            for (int y = 0; y < shapes.length; y++) {
                for (int x = 0; x < shapes[0].length; x++) {
                    if (shapes[y][x] == null) {
                        shapes[y][x] = createNewShape(x, y);
                    }
                    checkTime(shapes);
                }
            }
        }
    }

    private void print(String str) throws IOException {
        System.out.println(str);
        sendToServer(str);
    }

    private void checkTime(Shape[][] shapes) throws InterruptedException, IOException {
        for (int y = 0; y < shapes.length; y++) {
            for (int x = 0; x < shapes[0].length; x++) {
                Shape shape = shapes[y][x];
                if (shape != null && shape.isDead()) {
                    print(x + " " + y + " null");
                    shapes[y][x] = null;
                }
            }
        }
    }

    public Shape createNewShape(int x, int y) throws IOException {
        Random random = new Random();
        int shapeIndex = random.nextInt(shapes.length);
        int color = random.nextInt(colors.length);
        int velocity = random.nextInt(6) + 1;
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
