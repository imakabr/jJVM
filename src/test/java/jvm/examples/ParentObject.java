package jvm.examples;

public class ParentObject {
    int a;
    int b;

    ParentObject() {
        a = 1;
        b = 2;
    }
}

class ChildObject extends ParentObject {
    int c;

    ChildObject() {
        a = 2;
        this.c = 3;
    }
}

class ChildChildObject extends ChildObject {
    int d;

    ChildChildObject() {
        super.a = 11;
        super.b = 22;
        c = 33;
        d = 44;
    }

    public void m() {
        ChildChildObject object = new ChildChildObject();
    }

    public static void main(String[] args) {
        ParentObject object = new ChildChildObject();
        ChildObject parentObject = (ChildObject) object;
        System.out.println(object.a);
    }
}


