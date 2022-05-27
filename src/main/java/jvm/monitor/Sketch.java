package jvm.monitor;

import processing.core.PApplet;
import processing.core.PVector;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.Collections;
import java.util.List;

public class Sketch extends PApplet {

    public void settings() {
        size(400, 800);
    }

    private Color[] colors;
    private final float leftBorder = 20f;
    private final float loadingHeapBarHeight = 10f;
    private List<String> classNames = Collections.singletonList("Empty");
    private List<Integer> data = Collections.singletonList(100);
    private int heapSize = 100;

    public void setup() {
        noStroke();
        frameRate(60);
    }

    public void draw() {
        background(20);
        main();
    }

    public void main() {
        Message message = HeapMonitor.queue.poll();
        if (message != null) {
            classNames = message.getClassNames();
            colors = getColorsFromPalette(classNames.size());
            heapSize = message.getHeapSize();
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

    private void drawObjects(@Nonnull List<Integer> data, int heapSize) {
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

    private Color[] generateColors(int n) {
        Color[] cols = new Color[n];
        for (int i = 0; i < n - 1; i++) {
            cols[i] = Color.getHSBColor((i + 1f) / (float) n, 0.9f, 0.95f);
        }
        cols[n - 1] = Color.GRAY;
        return cols;
    }

    private Color[] getColorsFromPalette(int n) {
        Color[] colors = {new Color(250, 250, 110),
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
            cols[i] = colors[(int) map(i, 0, n, 0, colors.length)];
        }
        return cols;
    }

    private void drawLoadingHeapEllipseGradient(Color rgb, float diameter, float lastShapeStartAngle, float endShapeAngle, int degrees) {
        float startAngle = lastShapeStartAngle;
        float endAngle = lastShapeStartAngle + radians(1f);
        for (int i = 0; i < degrees; i++) {
            float alpha = map(i, 0, degrees, 255, 100);
            fill(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
            arc(width / 2f, height / 4f, diameter, diameter, startAngle, endAngle);
            startAngle = endAngle;
            endAngle = startAngle + radians(1f);
            if (endAngle == endShapeAngle) {
                return;
            }
        }
    }

    private void drawLoadingHeapEllipse(Color rgb, float diameter, float lastShapeStartAngle, float endShapeAngle, int degrees) {
        beginShape();
        fill(rgb.getRed(), rgb.getGreen(), rgb.getBlue()/*, 150*/);
        noStroke();
        arc(width / 2f, height / 4f, diameter, diameter, lastShapeStartAngle, endShapeAngle);
        endShape();
    }


    private void drawLoadingHeapBar(Color rgb, float x, float y, float width, float height) {
        float start = x;
        for (int i = 0; i < width; i++) {
            float alpha = map(i, 0, width, 255, 100);
            fill(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
            rect(start++, y, 1, height);
        }
    }

}
