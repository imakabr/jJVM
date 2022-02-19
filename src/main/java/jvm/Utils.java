package jvm;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Paths;

public class Utils {

    public static byte[] getClassFileData(String fName) {
        File classFile;
        byte[] buffer;
        try {
            classFile = Paths.get(ClassLoader.getSystemResource(fName + ".class").toURI()).toFile();
        } catch (URISyntaxException | FileSystemNotFoundException e) {
            throw new RuntimeException("No such class file - " + fName);
        }
        try (final FileInputStream stream = new FileInputStream(classFile)) {
            buffer = new byte[stream.available()];
            stream.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Something wrong while read: %s", classFile));
        }
        return buffer;
    }

    @Nonnull
    public static String changeJVMKlassNameToSystemKlassName(@Nonnull String name) {
        if (name.contains("JVM")) {
            return name.replace("jvm", "java")
                    .replace("JVM", "");
        }
        return name;
    }

    @Nonnull
    public static String changeSystemKlassNameToJVMKlassName(@Nonnull String name) {
        if (name.contains("java/lang/") || name.contains("java/io/") || name.contains("java/net/") || name.contains("java/util/")) {
            return name.replace("java", "jvm") + "JVM";
        }
        return name;
    }

    @Nonnull
    public static String toString(long[] array, int size) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            int type = getValueType(array[i]);
            int value = getPureValue(array[i]);
            if (type == getType("Z")) {
                builder.append("Bool:");
            } else if (type == getType("C")) {
                builder.append("Char:");
            } else if (type == getType("I")) {
                builder.append("Int:");
            } else if (type == getType("A")) {
                builder.append("Ref:");
                if (value == 0) {
                    builder.append("null ");
                    continue;
                }
            }
            builder.append(value)
                    .append(" ");
        }
        return builder.toString();
    }

    @Nonnull
    public static String toStringFromCharArray(long[] array) {
        char[] chars = new char[array.length];
        for (int i = 0; i < array.length; i++) {
            chars[i] = (char) getPureValue(array[i]);
        }
        return String.valueOf(chars);
    }

    private static int getType(@Nonnull String type) {
        return JVMType.valueOf(type).ordinal();
    }

    private static int getPureValue(long value) {
        return (int) value;
    }

    private static int getValueType(long value) {
        int type = (int) (value >> 32);
        return type >>> 31 == 1 ? ~type : type; // if 'type >>> 31 == 1' (negative sign) type was inverted
    }
}
