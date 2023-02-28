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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.github.axolotlclient.installer.ProgressConsumer;

public final class Util {

    public static final String USER_AGENT = "AxolotlClient";

    public static Path checkParent(Path parent, Path path) {
        if (!parent.resolve(path).normalize().startsWith(parent.normalize()))
            throw new UnsupportedOperationException(path + " is not inside " + parent);

        return path;
    }

    public static Path getDotMinecraft() {
        switch (OperatingSystem.CURRENT) {
            case MAC:
                return Paths.get(System.getProperty("user.home"), "Library/Application Support/minecraft");
            case WINDOWS:
                return Paths.get(System.getenv("APPDATA"), ".minecraft");
            default:
                return Paths.get(System.getProperty("user.home"), ".minecraft");
        }
    }

    public static byte[] readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        int length;
        byte[] buffer = new byte[8192];
        while ((length = in.read(buffer)) != -1)
            out.write(buffer, 0, length);
    }

    public static void progressiveCopy(InputStream in, OutputStream out, int max, ProgressConsumer progress)
            throws IOException {
        long read = 0;
        int length;
        byte[] buffer = new byte[8192];
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
            read += length;
            progress.update((float) read / max);
        }
    }

    public static int[] parseSemVer(String version) {
        String[] parts = version.split("\\.", 3);
        if (parts.length < 3)
            return null;

        try {
            return new int[] { Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]) };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static InputStream openStream(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setRequestProperty("User-Agent", USER_AGENT);
        return connection.getInputStream();
    }
}
