package jvm.examples.lang;

import jvm.examples.SimpleObject;

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

    public boolean checkToString() {
        String hello = new String(new char[]{'h', 'e', 'l', 'l', 'o'});
        String dup = hello.toString();
        return hello == dup;
    }

    public int checkPrintlnString() {
        String hello = "hello world";
        System.out.println(hello);
        System.out.println("With great power comes great responsibility");
        return 1;
    }

    public int checkPrintString() {
        System.out.print("hello ");
        System.out.print("word");
        System.out.print("!");
        return 1;
    }

    public int checkPrintChar() {
        char[] hello = {'h', 'e', 'l', 'l', 'o', ' ', 'w', 'o', 'r', 'l', 'd' };
        for (char letter : hello) {
            System.out.print(letter);
        }
        return 1;
    }

    public int checkPrintlnChar() {
        char[] hello = {'h', 'e', 'l', 'l', 'o'};
        for (char letter : hello) {
            System.out.println(letter);
        }
        return 1;
    }

    public int checkPrintInt() {
        int[] digits = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        for (int digit : digits) {
            System.out.print(digit);
        }
        return 1;
    }

    public int checkPrintlnInt() {
        int[] digits = {1, 2, 3, 4, 5};
        for (int digit : digits) {
            System.out.println(digit);
        }
        return 1;
    }

    public int checkPrintlnSimpleObject() {
        SimpleObject simpleObject = new SimpleObject(1, 2, 3);
        System.out.println(simpleObject);
        return 1;
    }

    public int checkPrintlnObject() {
        StringExample stringExample = new StringExample();
        System.out.println(stringExample);
        return 1;
    }

    public int checkObjectToString() {
        StringExample stringExample = new StringExample();
        StringExample stringExample2 = new StringExample();
        char[] actual = stringExample.toString().toCharArray();
        char[] expected = stringExample2.toString().toCharArray();
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != actual[i]) {
                return 0;
            }
        }
        return 1;
    }

    public boolean checkStringWithInternEqual() {
        String expected = "Hello world!";
        String actual = new String("Hello world!").intern();
        return expected == actual;
    }

}
