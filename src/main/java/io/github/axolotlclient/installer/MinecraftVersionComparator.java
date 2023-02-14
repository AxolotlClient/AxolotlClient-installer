package io.github.axolotlclient.installer;

import java.util.Comparator;

public class MinecraftVersionComparator implements Comparator<String> {

    public static final MinecraftVersionComparator INSTANCE = new MinecraftVersionComparator();

    @Override
    public int compare(String o1, String o2) {
        int[] a = parseSemVer(o1);
        int[] b = parseSemVer(o2);

        if (a[0] > b[0])
            return 1;
        if (a[0] < b[0])
            return -1;
        if (a[1] > b[1])
            return 1;
        if (a[1] < b[1])
            return -1;
        if (a[2] > b[2])
            return 1;
        if (a[2] < b[2])
            return -1;

        return 0;
    }

    private static int[] parseSemVer(String version) {
        String[] parts = version.split("\\.", 3);
        if (parts.length < 3)
            return null;

        try {
            return new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]) };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
