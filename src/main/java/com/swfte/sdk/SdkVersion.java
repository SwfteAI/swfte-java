package com.swfte.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The SDK version, read from {@code sdk-version.properties} which Maven filters
 * from {@code ${project.version}}, so the pom is the only place it is written.
 */
public final class SdkVersion {

    public static final String VERSION = load();

    private SdkVersion() {
    }

    private static String load() {
        try (InputStream in = SdkVersion.class.getResourceAsStream("/com/swfte/sdk/sdk-version.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                String v = props.getProperty("version");
                if (v != null && !v.isEmpty() && !v.startsWith("${")) {
                    return v;
                }
            }
        } catch (IOException e) {
            // fall through
        }
        return "unknown";
    }
}
