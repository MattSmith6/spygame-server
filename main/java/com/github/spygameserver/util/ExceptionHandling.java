package com.github.spygameserver.util;

// Inspiration taken from Apache Commons IOUtils#closeQuietly
// but applied to AutoCloseable's as opposed to Closeable's
public class ExceptionHandling {

    public static void closeQuietly(AutoCloseable autoCloseable) {
        if (autoCloseable == null) {
            return;
        }

        try {
            autoCloseable.close();
        } catch (Exception e) {
            // ignored
        }
    }

    public static void closeQuietly(AutoCloseable... autoCloseables) {
        for (AutoCloseable autoCloseable : autoCloseables) {
            closeQuietly(autoCloseable);
        }
    }
}
