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
