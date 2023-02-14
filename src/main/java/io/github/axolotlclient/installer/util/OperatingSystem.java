package io.github.axolotlclient.installer.util;

public enum OperatingSystem {
    LINUX, MAC, WINDOWS, UNKNOWN;

    public static final OperatingSystem CURRENT;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac"))
            CURRENT = MAC;
        else if (os.contains("win"))
            CURRENT = WINDOWS;
        else if (os.contains("linux"))
            CURRENT = LINUX;
        else
            CURRENT = UNKNOWN;
    }
}
