/*
 * Copyright © 2023-2023 moehreag <moehreag@gmail.com>, TheKodeToad <TheKodeToad@proton.me> & Contributors
 *
 * This file is part of AxolotlClient Installer.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.installer.util;

import java.util.Comparator;
import java.util.regex.Pattern;

public class MinecraftVersionComparator implements Comparator<String> {

    public static final MinecraftVersionComparator INSTANCE = new MinecraftVersionComparator();

    private static final Pattern ALPHABET_PATTERN = Pattern.compile("[a-zA-Z]+");
    private static final String SNAPSHOT_INDICATOR = "w";
    private static final String PRERELEASE_INDICATOR = "-pre";
    private static final String RELEASE_CANDIDATE_INDICATOR = "-rc";

    @Override
    public int compare(String o1, String o2) {

        boolean o1e = o1.isEmpty();
        boolean o2e = o2.isEmpty();
        if (o1e && !o2e) {
            return -1;
        } else if (!o1e && o2e) {
            return 1;
        }

        if (o1.indexOf(SNAPSHOT_INDICATOR) == 2 || o2.indexOf(SNAPSHOT_INDICATOR) == 2) {
            boolean o1c = o1.contains(SNAPSHOT_INDICATOR);
            boolean o2c = o2.contains(SNAPSHOT_INDICATOR);
            if (o1c && !o2c) {
                return -1;
            } else if (!o1c && o2c) {
                return 1;
            }

            // probably a snapshot version

            String[] parts1 = o1.split(SNAPSHOT_INDICATOR);
            String[] parts2 = o2.split(SNAPSHOT_INDICATOR);
            if (Integer.parseInt(parts1[0]) < Integer.parseInt(parts2[0])) {
                return -1;
            } else if (Integer.parseInt(parts1[0]) > Integer.parseInt(parts2[0])) {
                return 1;
            }
            int number1 = Integer.parseInt(ALPHABET_PATTERN.matcher(parts1[1]).replaceAll(""));
            int number2 = Integer.parseInt(ALPHABET_PATTERN.matcher(parts2[1]).replaceAll(""));
            if (number1 < number2) {
                return -1;
            } else if (number1 > number2) {
                return 1;
            }

            String ab1 = parts1[1].replace(String.valueOf(number1), "");
            String ab2 = parts2[1].replace(String.valueOf(number2), "");

            return ab1.compareTo(ab2);

        } else if (o1.contains(PRERELEASE_INDICATOR) || o2.contains(PRERELEASE_INDICATOR)) {

            // Pre-release version

            return compareWithSubstring(PRERELEASE_INDICATOR, o1, o2);

        } else if (o1.contains(RELEASE_CANDIDATE_INDICATOR) || o2.contains(RELEASE_CANDIDATE_INDICATOR)){

            // Release Candidates

            return compareWithSubstring(RELEASE_CANDIDATE_INDICATOR, o1, o2);
        }

        // Probably a stable version. Or something else. Could also be some random semver-like version.
        // Doesn't really matter though as long as it can be parsed.

        int[] a = Util.parseVersion(o1);
        int[] b = Util.parseVersion(o2);

        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            if (a[i] > b[i]) {
                return 1;
            } else if (a[i] < b[i]) {
                return -1;
            }
        }

        return 0;
    }

    private int compareWithSubstring(String sub, String o1, String o2){
        String[] parts1;
        if (o1.contains(sub)) {
            parts1 = o1.split(sub);
        } else {
            parts1 = new String[]{o1, "99"};
        }
        String[] parts2;
        if (o2.contains(sub)) {
            parts2 = o2.split(sub);
        } else {
            parts2 = new String[]{o2, "99"};
        }

        int i;
        if ((i = compare(parts1[0], parts2[0])) != 0) {
            return i;
        }

        return Integer.compare(Integer.parseInt(parts1[1]), Integer.parseInt(parts2[1]));
    }
}
