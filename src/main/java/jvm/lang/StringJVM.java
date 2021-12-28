package jvm.lang;

public class StringJVM {

    private  char[] value;

    public StringJVM(StringJVM stringJVM) {
    }

    public StringJVM() {

    }

    public StringJVM(java.lang.String string) {
        this.value = string.toCharArray();
    }
}
