/*
 * Copyright © 2023 TheKodeToad <TheKodeToad@proton.me> & Contributors
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
