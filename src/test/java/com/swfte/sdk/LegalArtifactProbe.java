package com.swfte.sdk;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/** Run after package against all three saved artifacts; never infer legal bytes from the pom. */
public final class LegalArtifactProbe {
    public static void main(String[] paths) throws Exception {
        if (paths.length != 3) throw new AssertionError("provide main, sources and javadoc jars");
        byte[] expected = Files.readAllBytes(Paths.get("LICENSE"));
        String[] suffixes = {".jar", "-sources.jar", "-javadoc.jar"};
        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            String expectedName = "swfte-sdk-" + SdkVersion.VERSION + suffixes[i];
            if (!Paths.get(path).getFileName().toString().equals(expectedName)) throw new AssertionError("expected " + expectedName);
            try (JarFile jar = new JarFile(path)) {
                JarEntry legal = jar.getJarEntry("META-INF/LICENSE");
                if (legal == null) legal = jar.getJarEntry("LICENSE");
                if (legal == null || legal.isDirectory()) throw new AssertionError(path + " is missing LICENSE");
                try (InputStream input = jar.getInputStream(legal)) {
                    if (!Arrays.equals(expected, input.readAllBytes())) throw new AssertionError(path + " has different license bytes");
                }
            }
        }
        System.out.println("JAVA_ALL_THREE_ARTIFACT_LICENSES_MATCH");
    }
}
