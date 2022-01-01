package jvm.lang;

public class StringBuilder {

    private char[] value;
    private int count;


    public StringBuilder() {
        this.value = new char[16];
    }

    public StringBuilder(String str) {
        int length = str.length();
        char[] value = new char[length + 16];
        arrayCopy(str.toCharArray(), value, length);
        this.value = value;
        this.count = length;
    }

    private void arrayCopy(char[] source, char[] dest, int count) {
        for (int i = 0; i < count; i++) {
            dest[i] = source[i];
        }
    }

    public StringBuilder append(String str) {
        char[] buf = str.toCharArray();
        int length = str.length();
        checkCapacity(length);
        for (int i = 0; i < length; i++) {
            value[count + i] = buf[i];
        }
        count += length;
        return this;
    }

    public StringBuilder append(char letter) {
        checkCapacity(count + 1);
        value[count] = letter;
        count++;
        return this;
    }

    public int length() {
        return count;
    }

    private void checkCapacity(int length) {
        if (count + length > value.length) {
            char[] newValue = new char[value.length * 2 + length];
            arrayCopy(value, newValue, count);
            value = newValue;
        }
    }

    @Override
    public String toString() {
        char[] str = new char[count];
        arrayCopy(value, str, count);
        return new String(str);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        StringBuilder that = (StringBuilder) o;
        char[] thatValue = that.value;
        if (count != that.count) return false;
        for (int i = 0; i < count; i++) {
            if (value[i] != thatValue[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        if (count == 0) {
            return 0;
        }
        int result = 1;
        for (int i = 0; i < count; i++) {
            result = 31 * result + value[i];
        }
        return result;
    }

}
