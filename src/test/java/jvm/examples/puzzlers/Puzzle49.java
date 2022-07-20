package jvm.examples.puzzlers;

public class Puzzle49 {
    public static int test() {
        return Elvis.INSTANCE.beltSize();
    }

}

class Elvis {
    public static final Elvis INSTANCE = new Elvis();
    private final int beltSize;
    private static final int CURRENT_YEAR = new Year().getYear();
    private Elvis() {
        beltSize = CURRENT_YEAR - 1930;
    }
    public int beltSize() {
        return beltSize;
    }
    public static void main(String[] args) {
        System.out.println("Elvis wears a size " +
                INSTANCE.beltSize() + " belt.");
    }
}

class Year {
    private final int year;

    public Year() {
        year = 2022;
    }

    public int getYear() {
        return year;
    }
}
