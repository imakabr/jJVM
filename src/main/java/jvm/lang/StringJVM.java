package jvm.lang;

public class StringJVM {

    private char[] value;

    public StringJVM() {

    }

    public StringJVM(String string) {
        char[] source = string.toCharArray();
        char[] dest = new char[source.length];
        arrayCopy(source, dest);
        this.value = dest;
    }

    public StringJVM(char[] value) {
        this.value = new char[value.length];
        arrayCopy(value, this.value);
    }

    private void arrayCopy(char[] source, char[] dest) {
        for (int i = 0; i < source.length; i++) {
            dest[i] = source[i];
        }
    }

    public char[] toCharArray() {
        char[] copy = new char[value.length];
        arrayCopy(this.value, copy);
        return copy;
    }

    public String replace(char oldChar, char newChar) {
        char[] newValue = new char[value.length];
        for (int i = 0 ; i < value.length; i++) {
            if (value[i] == oldChar) {
                newValue[i] = newChar;
            } else {
                newValue[i] = value[i];
            }
        }
        return new String(newValue);
    }
}
