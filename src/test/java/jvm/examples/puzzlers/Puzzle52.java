package jvm.examples.puzzlers;

public class Puzzle52 {
    public static int test() {
        return Cache.getSum();
    }
}

class Cache {
    static {
        initializeIfNecessary();
    }
    private static int sum;
    public static int getSum() {
        initializeIfNecessary();
        return sum;
    }
    private static boolean initialized = false;
    private static synchronized void initializeIfNecessary() {
        if (!initialized) {
            for (int i = 0; i < 100; i++)
                sum += i;
            initialized = true;
        }
    }
}
class Client {
    public static void main(String[] args) {
        System.out.println(Cache.getSum());
    }
}
