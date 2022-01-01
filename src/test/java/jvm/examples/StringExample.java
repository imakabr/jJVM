package jvm.examples;

public class StringExample {

    public int checkStringToUpperCase() {
        String actual = "hEllo woRld!";
        char[] expected = "HELLO WORLD!".toCharArray();
        char[] result = actual.toUpperCase().toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != result[i]) {
                return 0;
            }
        }
        return 1;
    }

    public int checkStringToLowerCase() {
        String actual = "HElLo WoRLd!11";
        char[] expected = "hello world!11".toCharArray();
        char[] result = actual.toLowerCase().toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != result[i]) {
                return 0;
            }
        }
        return 1;
    }

    public int checkStringCharAt() {
        char[] expected = {'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };
        String actual = "hello world";
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual.charAt(i)) {
                return 0;
            }
        }
        return 1;
    }

    public int checkStringToCharArray() {
        String hello = "hello world";
        char[] expected = {'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };
        char[] actual = hello.toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (actual[i] != expected[i]) {
                return 0;
            }
        }
        return 1;
    }

    public int checkStringReplace() {
        String hello = "hello to all";
        String str = new String(hello);
        String result = str.replace('l', 'r');
        char[] actual = result.toCharArray();
        char[] expected = "herro to arr".toCharArray();
        for (int i = 0; i < actual.length; i++) {
            if (actual[i] != expected[i]) {
                return 0;
            }
        }
        return 1;
    }

    public boolean checkStringEqualsTrue() {
        char[] expected = {'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };
        String first = new String(expected);
        String second = "hello world";
        return first.equals(second);
    }

    public boolean checkStringEqualsFalse() {
        char[] expected = {'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };
        String first = new String(expected);
        String second = "hallo world";
        return first.equals(second);
    }

    public int checkStringHashCode() {
        String actual = new String("hello world");
        return actual.hashCode();
    }

    public int checkStringConcat() {
        String hello = new String(new char[]{'h', 'e', 'l', 'l', 'o'});
        String world = new String(new char[]{'w', 'o', 'r', 'l', 'd'});
        String helloWorld = hello + " " + world + "!";
        char[] expected = "hello world!".toCharArray();
        char[] actual = helloWorld.toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return 0;
            }
        }
        return 1;
    }

    public int checkStringConcatMethod() {
        String hello = new String(new char[]{'h', 'e', 'l', 'l', 'o'});
        String world = new String(new char[]{'w', 'o', 'r', 'l', 'd'});
        String helloWorld = hello.concat(" ").concat(world).concat("!");
        char[] expected = "hello world!".toCharArray();
        char[] actual = helloWorld.toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return 0;
            }
        }
        return 1;
    }

}
