package jvm.examples.heap_test;

import java.io.IOException;

public class Shape {

    protected final String name;
    protected final String color;
    private int time;
    protected final Service service;

    public Shape(String name, String color, int time, Service service) {
        this.name = name;
        this.color = color;
        this.service = service;
        this.time = time;
    }

    @Override
    public String toString() {
        return name + " " + color;
    }

    public void draw() throws IOException {
        print(toString());
    }

    public boolean timesUp() {
        time--;
        return time <= 0;
    }

    public void kill(int y, int x) throws IOException {
        print(y + " " + x + " null");
    }

    protected void print(String str) throws IOException {
        System.out.println(str);
        sendMessage(str);
    }

    private void sendMessage(String message) throws IOException {
//        synchronized (service) {
            service.send(message);
            if (!service.receive().equals("next")) {
                System.out.println("something wrong");
                System.exit(-1);
            }
//        }
    }
}
