package com.kahzerx.kcp.utils;

import java.util.Locale;

public class OSUtils {
    public enum OS {
        WINDOWS("windows"),
        MACOS("darwin"),
        LINUX("linux"),
        INVALID("invalid");

        private final String value;

        OS(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum Arch {
        ARMV5("arm5"),
        ARMV6("arm6"),
        ARMV7("arm7"),
        ARM64("arm64"),
        A386("386"),
        AMD64("amd64"),
        INVALID("invalid");

        private final String value;

        Arch(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static OS getOSType() {
        OS detected;
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ROOT);
        if (os.contains("mac") || os.contains("darwin")) {
            detected = OS.MACOS;
        } else if (os.contains("win") || os.startsWith("msys") || os.startsWith("mingw")) {
            detected = OS.WINDOWS;
        } else if (os.contains("nux")) {
            detected = OS.LINUX;
        } else {
            detected = OS.INVALID;
        }
        return detected;
    }

    public static Arch getArchType() {
        Arch detected;
        String arch = System.getProperty("os.arch", "generic").toLowerCase();
        if (arch.startsWith("armv5") || arch.startsWith("arm5")) {
            detected = Arch.ARMV5;
        } else if (arch.startsWith("armv6") || arch.startsWith("arm6")) {
            detected = Arch.ARMV6;
        } else if (arch.startsWith("armv7") || arch.startsWith("arm7")) {
            detected = Arch.ARMV7;
        } else if (arch.contains("arm64") || arch.contains("aarch64")) {
            detected = Arch.ARM64;
        } else if (arch.contains("x86_64") || arch.contains("amd64")) {
            detected = Arch.AMD64;
        } else if (arch.contains("x86") || arch.contains("i686") || arch.contains("386")) {
            detected = Arch.A386;
        } else {
            detected = Arch.INVALID;
        }
        return detected;
    }

    public static String getOSName() {
        return getOSType().getValue();
    }

    public static String getArchName() {
        return getArchType().getValue();
    }
}
