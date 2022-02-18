package jvm.processing.heap_test;

import jvm.processing.heap_test.shapes.*;
import processing.core.PApplet;

class ShapeManager {

    private final Reader reader;
    private final PApplet pApplet;

    public ShapeManager(PApplet pApplet, Reader reader) {
        this.pApplet = pApplet;
        this.reader = reader;
    }

    public void checkShape(Shape[][] shapes) {
        String message = reader.getMessage();
//        pApplet.println(message);
        if (message != null) {
            String m = PApplet.trim(message);
            String[] str = PApplet.split(m, ' ');
            int y = Integer.parseInt(str[0]);
            int x = Integer.parseInt(str[1]);
            String shapeName = str[2];
            if ("null".equals(shapeName)) {
                Shape shape = shapes[y][x];
                if (shape != null) {
                    shape.kill();
                }
            } else if ("cleared".equals(shapeName)) {
                shapes[y][x] = null;
            } else {
                if (shapes[y][x] == null) {
                    String colorName = str[3];
                    int velocity = Integer.parseInt(str[4]);
                    String effect = str[5];
                    shapes[y][x] = createShape(x, y, shapeName, colorName, effect, velocity);
                }
            }
        }
    }

    private Shape createShape(int x, int y, String shapeName, String colorName, String effect, int velocity) {
        if ("square".equals(shapeName)) {
            return new Square(pApplet, x, y, getColor(colorName), new EffectController(pApplet, effect, velocity));
        } else if ("triangle".equals(shapeName)) {
            return new Triangle(pApplet, x, y, getColor(colorName), new EffectController(pApplet, effect, velocity));
        } else if ("diamond".equals(shapeName)) {
            return new Diamond(pApplet, x, y, getColor(colorName), new EffectController(pApplet, effect, velocity));
        } else if ("pentagon".equals(shapeName)) {
            return new Pentagon(pApplet, x, y, getColor(colorName), new EffectController(pApplet, effect, velocity));
        } else if ("cross".equals(shapeName)) {
            return new Cross(pApplet, x, y, getColor(colorName), new EffectController(pApplet, effect, velocity));
        } else if ("circle".equals(shapeName)) {
            return new Circle(pApplet, x, y, getColor(colorName), new EffectController(pApplet, effect, velocity));
        } else if ("star".equals(shapeName)) {
            return new Star(pApplet,x, y, getColor(colorName), new EffectController(pApplet, effect, velocity));
        } else {
            throw new RuntimeException("wrong shape name");
        }
    }

    private Rgb getColor(String name) {
        if ("red".equals(name)) {
            return new Rgb(255, 0, 0);
        } else if ("yellow".equals(name)) {
            return new Rgb(255, 255, 0);
        } else if ("purple".equals(name)) {
            return new Rgb(128, 0, 128);
        } else if ("blue".equals(name)) {
            return new Rgb(0, 0, 255);
        } else if ("green".equals(name)) {
            return new Rgb(50, 205, 50);
        } else {
            throw new RuntimeException("wrong color name");
        }
    }
}
