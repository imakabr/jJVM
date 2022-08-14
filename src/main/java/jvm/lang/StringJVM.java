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

    public String concat(String str) {
        int oldLen = value.length;
        int addedLen = str.length();
        char[] buf = new char[oldLen + addedLen];
        arrayCopy(value, buf);
        char[] strChars = str.toCharArray();
        for (int i = 0; i < addedLen; i++) {
            buf[i + oldLen] = strChars[i];
        }
        return new String(buf);
    }

    public char[] toCharArray() {
        char[] copy = new char[value.length];
        arrayCopy(this.value, copy);
        return copy;
    }

    public String replace(char oldChar, char newChar) {
        char[] newValue = new char[value.length];
        for (int i = 0; i < value.length; i++) {
            if (value[i] == oldChar) {
                newValue[i] = newChar;
            } else {
                newValue[i] = value[i];
            }
        }
        return new String(newValue);
    }

    private boolean isSmallLetter(char a) {
        return a >= 'a' && a <= 'z';
    }

    private boolean isCapitalLetter(char a) {
        return a >= 'A' && a <= 'Z';
    }

    public String toUpperCase() {
        char[] result = new char[value.length];
        for (int i = 0; i < value.length; i++) {
            if (isSmallLetter(value[i])) {
                result[i] = (char) ('A' + value[i] - 'a');
            } else {
                result[i] = value[i];
            }
        }
        return new String(result);
    }

    public String toLowerCase() {
        char[] result = new char[value.length];
        for (int i = 0; i < value.length; i++) {
            if (isCapitalLetter(value[i])) {
                result[i] = (char) ('a' + value[i] - 'A');
            } else {
                result[i] = value[i];
            }
        }
        return new String(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (o instanceof String) {
            String string = (String) o;
            char[] array = string.toCharArray();
            if (value.length != array.length) {
                return false;
            }
            for (int i = 0; i < value.length; i++) {
                if (value[i] != array[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public char charAt(int index) {
        return value[index];
    }

    public int length() {
        return value.length;
    }

    @Override
    public int hashCode() {
        if (value == null) {
            return 0;
        }
        int result = 1;
        for (char element : value) {
            result = 31 * result + element;
        }
        return result;
    }

    public StringJVM toStringJVM() {
        return this;
    }

    public native String intern();

}
