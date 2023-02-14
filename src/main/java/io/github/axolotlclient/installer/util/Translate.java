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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.toadlabs.jfgjds.JsonDeserializer;

/**
 * Utilities for translation.
 */
public final class Translate {

    private static final Map<String, String> DATA = new HashMap<>();

    static {
        Locale locale = Locale.getDefault();
        tryLoad("en_us");
        tryLoad((locale.getLanguage() + '_' + locale.getCountry()).toLowerCase());
    }

    private static void tryLoad(String name) {
        try {
            load(name);
        } catch (Throwable error) {
            System.err.printf("Could not load language file '%s':%n", name);
            error.printStackTrace();
        }
    }

    private static void load(String name) throws IOException {
        try (InputStream in = Translate.class.getResourceAsStream("/lang/" + name + ".json")) {
            if (in == null)
                throw new FileNotFoundException("Resource not present");

            JsonDeserializer.read(new InputStreamReader(in, StandardCharsets.UTF_8)).asObject()
                    .forEach((key, value) -> DATA.put(key, value.getStringValue()));
        }
    }

    public static String tr(String key) {
        return DATA.getOrDefault(key, key);
    }

    public static String tr(String key, Object... args) {
        return String.format(tr(key), args);
    }
}
