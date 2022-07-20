package jvm.examples.puzzlers;

public class Puzzle47 {

    public static void main(String[] args) {
        test();
    }

    public static int test() {
        Dog[] dogs = {new Dog(), new Dog()};
        for (Dog dog : dogs) {
            dog.woof();
        }
        Cat[] cats = {new Cat(), new Cat(), new Cat()};
        for (Cat cat : cats) {
            cat.meow();
        }
        System.out.print(Dog.getCount() + " woofs and ");
        System.out.println(Cat.getCount() + " meows");
        return Dog.getCount() + Cat.getCount();
    }
}

class Counter {
    private static int count;

    public static void increment() {
        count++;
    }

    public static int getCount() {
        return count;
    }
}

class Dog extends Counter {
    public Dog() {
    }

    public void woof() {
        increment();
    }
}

class Cat extends Counter {
    public Cat() {
    }

    public void meow() {
        increment();
    }
}
