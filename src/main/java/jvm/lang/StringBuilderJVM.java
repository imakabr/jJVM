package jvm.lang;

public class StringBuilderJVM {

    private char[] value;
    private int count;


    public StringBuilderJVM() {
        this.value = new char[16];
    }

    public StringBuilderJVM(String str) {
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

    public StringBuilderJVM append(String str) {
        if (str == null) {
            append("null");
            return this;
        }
        char[] buf = str.toCharArray();
        int length = str.length();
        checkCapacity(length);
        for (int i = 0; i < length; i++) {
            value[count + i] = buf[i];
        }
        count += length;
        return this;
    }

    public StringBuilderJVM append(char letter) {
        checkCapacity(1);
        value[count] = letter;
        count++;
        return this;
    }

    public StringBuilderJVM append(int number) {
        if (number == 0) {
            checkCapacity(1);
            value[count++] = '0';
            return this;
        }
        int d = number;
        int length = 0;
        int div1 = 1;
        int div2 = 1;
        while (d != 0) {
            d /= 10;
            length++;
            div1 *= 10;
            div2 *= 10;
        }
        checkCapacity(length);
        if (number >>> 31 == 1) { // if number is negative
            if (number == Integer.MIN_VALUE) {
                append("-2147483648");
                return this;
            }
            value[count++] = '-';
            number *= -1;
        }
        if (length == 10) { // if number >= 10^9
            div1 = div2 = 1000000000;
            value[count++] = (char) ('0' + number / div1);
            length--;
        }
        div2 /= 10;
        for (int i = 0; i < length; i++) {
            value[count++] = (char) ('0' + (number % div1 / div2));
            div1 /= 10;
            div2 /= 10;
        }
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
        StringBuilderJVM that = (StringBuilderJVM) o;
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
