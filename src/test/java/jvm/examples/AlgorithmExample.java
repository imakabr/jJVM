package jvm.examples;

public class AlgorithmExample {

    public boolean checkBubbleSorting() {
        int[] array = {5, 34, 56, 567, 23, 89, 73 ,345 ,765 ,14 ,234};
        int[] result = {5, 14, 23, 34, 56, 73, 89, 234, 345, 567, 765};
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array.length; j++) {
                if (array[i] < array[j]) {
                    int temp = array[j];
                    array[j] = array[i];
                    array[i] = temp;
                }
            }
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i] != result[i]) {
                return false;
            }
        }
        return true;
    }
}
