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

    private static final String DEFAULT = "en_us";
    private static final Map<String, String> DATA = new HashMap<>();

    static {
        Locale locale = Locale.getDefault();
        try {
            InputStream in = Translate.class.getResourceAsStream("/lang/" + DEFAULT + ".json");
            if (in != null)
                load(in);
            else
                System.err.printf("Could not find default language file (%s)%n", DEFAULT);
        } catch (IOException e) {
            System.err.printf("Could not load default language file (%s)%n", DEFAULT);
            e.printStackTrace();
        }

        String system = locale.getLanguage() + '_' + locale.getCountry().toLowerCase();
        try {
            InputStream in = Translate.class.getResourceAsStream("/lang/" + system + ".json");
            if (in != null)
                load(in);
        } catch (IOException e) {
            System.err.printf("Could not load system language (%s)%n", system);
            e.printStackTrace();
        }
    }

    private static void load(InputStream in) throws IOException {
        JsonDeserializer.read(new InputStreamReader(in, StandardCharsets.UTF_8)).asObject()
                .forEach((key, value) -> DATA.put(key, value.getStringValue()));
    }

    public static String tr(String key) {
        return DATA.getOrDefault(key, key);
    }

    public static String tr(String key, Object... args) {
        return String.format(tr(key), args);
    }
}
