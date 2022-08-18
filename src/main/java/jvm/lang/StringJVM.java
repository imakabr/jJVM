package jvm.lang;

import java.util.ArrayList;

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

    public StringJVM(char[] value, int offset, int count) {
        char[] newValue = new char[count];
        for (int i = 0; i < count; i++) {
            newValue[i] = value[i + offset];
        }
        this.value = newValue;
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
        if (o instanceof StringJVM) {
            StringJVM string = (StringJVM) o;
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

    public StringJVM[] split(String regex) {
        char ch;
        if (regex.length() == 1 &&
                ".$|()[{^?*+\\".indexOf(ch = regex.charAt(0), 0) == -1) {
            int off = 0;
            int next = 0;
            ArrayList<StringJVM> list = new ArrayList<>();
            while ((next = indexOf(ch, off)) != -1) {
                list.add(substring(off, next));
                off = next + 1;
            }
            // If no match was found, return this
            if (off == 0)
                return new StringJVM[]{this};

            // Add remaining segment
            list.add(substring(off, value.length));

            // Construct result
            int resultSize = list.size();
            while (resultSize > 0 && list.get(resultSize - 1).length() == 0) {
                resultSize--;
            }

            StringJVM[] result = new StringJVM[resultSize];
            for (int i = 0; i < resultSize; i++) {
                result[i] = list.get(i);
            }
            return result;
        } else {
            throw new RuntimeExceptionJVM("regex " + regex + " does not support");
        }
    }

    public StringJVM substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsExceptionJVM("Begin index = " + beginIndex);
        }
        if (endIndex > value.length) {
            throw new IndexOutOfBoundsExceptionJVM("End index = " + endIndex);
        }
        int subLen = endIndex - beginIndex;
        if (subLen < 0) {
            throw new StringIndexOutOfBoundsException("Sub length = " + subLen);
        }
        return ((beginIndex == 0) && (endIndex == value.length)) ? this
                : new StringJVM(value, beginIndex, subLen);
    }

    public int indexOf(int ch, int fromIndex) {
        final int max = value.length;
        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= max) {
            return -1;
        }

        final char[] value = this.value;
        for (int i = fromIndex; i < max; i++) {
            if (value[i] == ch) {
                return i;
            }
        }
        return -1;
    }

}
