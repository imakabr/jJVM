package jvm.processing.snake;

import processing.core.PImage;

class GameMap {
    PImage image;
    int[][] map;
    int height;
    int width;

    GameMap(int height, int width, String imageName) {
//    image = loadImage(imageName);
        this.height = height;
        this.width = width;
        map = new int[width][height];
    }

//  void loadFromFile(String fileName) {
//    String[] file = loadStrings(fileName);
//    for (int i = 0; i<file.length; i++) {
//      String str[] = split(file[i], ' ');
//      put(int(str[0]), int(str[1]));
//    }
//  }
//
//  void draw() {
//    for (int i = 0; i<map.length; i++) {
//      for (int j = 0; j<map[0].length; j++) {
//        if (map[i][j] == 1) {
//          image(image, i*10, j*10);
//        }
//      }
//    }
//  }
//
//  void ruin(int pointX, int pointY, int count) {
//    pointX/=10;
//    pointY/=10;
//    for (int i = pointX-count; i<=pointX+count; i++) {
//      for (int j = pointY-count; j<=pointY+count; j++) {
//        if (i >= 0 && i<map.length && j >= 0 && j<map[0].length) {
//          map[i][j] = 0;
//        }
//      }
//    }
//  }
//
//  void drawBang(int pointX, int pointY) {
//  pointX/=10;
//  pointY/=10;
//  for (int i = pointX-1; i<=pointX+1; i++) {
//    for (int j = pointY-1; j<=pointY+1; j++) {
//      if (i >= 0 && i<width && j >= 0 && j<height) {
//        if (i == pointX && j == pointY) {
//          parSys.add(new ParticleSystemBuilder().bricks(new PVector(i*10, j*10)));
//          parSys.add(new ParticleSystemBuilder().bang(new PVector(i*10, j*10)));
//        }
//        fill(255, 240, 100);
//        //rect(i*10, j*10, 10, 10);
//        ellipse(i*10, j*10, 10, 10);
//        fill(255, 255, 255);
//      }
//    }
//  }
//}
//
//  void put(int x, int y) {
//    map[x/10][y/10] = 1;
//  }
//
//  boolean matchesBody(int pointX, int pointY) {
//    return map[pointX/10][pointY/10] == 1;
//  }
}
