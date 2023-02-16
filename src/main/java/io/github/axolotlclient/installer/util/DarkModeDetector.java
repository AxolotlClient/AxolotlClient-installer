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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class DarkModeDetector {

    private static final String LINUX_COMMAND = "dbus-send --session --print-reply=literal --dest=org.freedesktop.portal.Desktop /org/freedesktop/portal/desktop org.freedesktop.portal.Settings.Read string:org.freedesktop.appearance string:color-scheme";
    private static final String MAC_COMMAND = "defaults read -g AppleInterfaceStyle";

    public static boolean detect() {
        try {
            switch (OperatingSystem.CURRENT) {
                case LINUX:
                    return linuxImpl();
                case MAC:
                    return macImpl();
                // TODO windows
                default:
                    return false;
            }
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean linuxImpl() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Runtime.getRuntime().exec(LINUX_COMMAND).getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            return line.substring(line.lastIndexOf(' ') + 1).equals("1");
        }
    }

    private static boolean macImpl() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Runtime.getRuntime().exec(MAC_COMMAND).getInputStream(), StandardCharsets.UTF_8))) {
            return reader.readLine().equals("Dark");
        }
    }
}
