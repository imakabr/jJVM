package jvm;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class Utils {

    public static byte[] getClassFileData(String fName) {
        File classFile;
        byte[] buffer;
        try {
            classFile = Paths.get(ClassLoader.getSystemResource(fName + ".class").toURI()).toFile();
        } catch (URISyntaxException e) {
            throw new RuntimeException("No such file");
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
    public static String checkSystemKlassName(@Nonnull String name) {
        if (name.startsWith("jvm/lang/")) {
            return name.replace("jvm", "java")
                    .replace("JVM", "");
        }
        return name;
    }
}
