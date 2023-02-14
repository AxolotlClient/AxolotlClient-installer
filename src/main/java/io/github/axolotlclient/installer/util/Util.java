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

import java.nio.file.Path;
import java.nio.file.Paths;

public class Util {

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
}
