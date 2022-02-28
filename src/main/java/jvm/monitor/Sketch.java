package jvm.monitor;

import processing.core.PApplet;
import processing.core.PVector;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class Sketch extends PApplet {

    PVector origin;

    public static void main(String[] args) {
        PApplet.main("jvm.visual.Test2");
    }

    public void settings() {
        size(400, 800);
    }

    int[] angles = {100, 100, 100, 100, 100,100,100,100,100,100,};
//    int[] angles = {700, 300};
//        int[] angles = {120, 100, 50, 90, 640};
    Color[] colors = generateColors(angles.length);
//    Color[] colors = getColorsFromPalette(angles.length);
//    String[] classNames = {"jvm/lang/String", "jvm/lang/StringBuilder", "jvm/lang/Object", "jvm/lang/Shape", "jvm/lang/Triangle", "sdf", "sdf", "fdg", "sdfs", "qwe"};

    float leftBorder = 20f;
    float loadingHeapBarHeight = 10f;

//    SocketReader socketReader = new SocketReader(this, 10004);

    public void setup() {
        noStroke();
        frameRate(60);

    }


    public void draw() {
        background(20);
//        changeAngles();
        main();
    }

    int i = 2;
    int j = 1;

    public void changeAngles() {
        if (angles[0] > 200) {
            i = -1;
        } else if (angles[0] < 150) {
            i = 2;
        }

        if (angles[1] >= 200) {
            j = -2;
        } else if (angles[1] < 150) {
            j = 3;
        }

        angles[0] += i;
        angles[1] += j;

    }

    private List<String> classNames = Collections.singletonList("Empty");;
    private List<Integer> data = Collections.singletonList(100);
    private int heapSize = 100;
    private boolean firstMassage = true;
    public void main() {
        Message message = HeapMonitor.queue.poll();
        if (message != null) {
            if (firstMassage) {
                classNames = message.getClassNames();
                colors = getColorsFromPalette(classNames.size());
                heapSize = message.getHeapSize();
//                firstMassage = false;
            }
            data = message.getData();
        }
        drawObjects(data, heapSize);
        drawClassNames(classNames);
    }

    private void drawClassNames(@Nonnull List<String> classNames) {
        float markHeight = 15;
        float verticalShift = markHeight * 2;
        float shift = markHeight;
        for (int i = 0; i < classNames.size(); i++) {
            fill(colors[i].getRed(), colors[i].getGreen(), colors[i].getBlue());
            rect(leftBorder, verticalShift + height / 2f + loadingHeapBarHeight + shift, markHeight, markHeight);
            fill(255);
            textSize(markHeight);
            text(classNames.get(i), leftBorder + markHeight * 2, verticalShift + height / 2f + loadingHeapBarHeight + shift + markHeight);
            shift += markHeight * 2;
        }
    }


    void drawObjects(@Nonnull List<Integer> data, int heapSize) {
        float lastAngle = 0;
        int lastBorder = 0;
        for (int i = 0; i < data.size(); i++) {
            int partOfAngle = (int) map(data.get(i), 0, heapSize, 0, 360);
            drawLoadingHeapEllipseGradient(colors[i], height / 3f, lastAngle, lastAngle + radians(partOfAngle), partOfAngle);
            lastAngle += radians(partOfAngle);

            float partOfWidth = map(data.get(i), 0, heapSize, 0, width - leftBorder * 2);
            drawLoadingHeapBar(colors[i], leftBorder + lastBorder, height / 2f, partOfWidth, loadingHeapBarHeight);
            lastBorder += partOfWidth;
        }
    }

    public Color[] generateColors(int n) {
        Color[] cols = new Color[n];
        for (int i = 0; i < n - 1; i++) {
            cols[i] = Color.getHSBColor((i + 1f) / (float) n, 0.9f, 0.95f);
        }
        cols[n - 1] = Color.GRAY;
        return cols;
    }

    public Color[] getColorsFromPalette(int n) {
        Color[] colors = {new Color(0, 85, 63), new Color(0, 132, 98), new Color(0, 180, 132),
                new Color(0, 227, 167), new Color(19, 255, 193), new Color(66, 255, 205),
                new Color(113, 255, 218), new Color(161, 255, 230), new Color(208, 255, 243), new Color(255, 255, 255)};
        Color[] colors2 = {new Color(250, 250, 110),
                new Color(204, 243, 91),
                new Color(151, 235, 74),
                new Color(94, 226, 58),
                new Color(44, 214, 51),
                new Color(45, 187, 88),
                new Color(47, 160, 111),
                new Color(47, 134, 119),
                new Color(45, 105, 110),
                new Color(42, 72, 88)
        };
        Color[] cols = new Color[n];
        for (int i = 0; i < n; i++) {
            cols[i] = colors2[(int) map(i, 0, n, 0, colors2.length)];
        }
//        System.arraycopy(colors2, 0, cols, 0, n - 1);
//        cols[n - 1] = Color.GRAY;
        return cols;
    }

    void drawLoadingHeapEllipseGradient(Color rgb, float diameter, float lastShapeStartAngle, float endShapeAngle, int degrees) {
        float startAngle = lastShapeStartAngle;
        float endAngle = lastShapeStartAngle + radians(1f);
        for (int i = 0; i < degrees * 1; i++) {
            float alpha = map(i, 0, degrees * 1, 255, 100);
            fill(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
            arc(width / 2f, height / 4f, diameter, diameter, startAngle, endAngle);
            startAngle = endAngle;
            endAngle = startAngle + radians(1f);
            if (endAngle == endShapeAngle) {
                return;
            }
        }
    }

    void drawLoadingHeapEllipse(Color rgb, float diameter, float lastShapeStartAngle, float endShapeAngle, int degrees) {
        beginShape();
        float startAngle = lastShapeStartAngle;
        float endAngle = lastShapeStartAngle + radians(1f);
        fill(rgb.getRed(), rgb.getGreen(), rgb.getBlue()/*, 150*/);
        noStroke();
        arc(width / 2f, height / 4f, diameter, diameter, lastShapeStartAngle, endShapeAngle);
        endShape();
    }


    void drawLoadingHeapBar(Color rgb, float x, float y, float width, float height) {
        float start = x;
        for (int i = 0; i < width; i++) {
            float alpha = map(i, 0, width, 255, 100);
            fill(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
            rect(start++, y, 1, height);

//            stroke(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
//            line(start, y, start + 1, y + height);
//            start++;
        }
    }

}
