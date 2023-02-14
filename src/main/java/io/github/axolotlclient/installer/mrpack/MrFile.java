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

package io.github.axolotlclient.installer.mrpack;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.github.axolotlclient.installer.util.Util;
import io.toadlabs.jfgjds.data.JsonObject;
import io.toadlabs.jfgjds.data.JsonValue;

public final class MrFile {

    private final Path path;
    private final String sha1;
    private final MrEnvSpec env;
    private final List<String> urls;

    public MrFile(JsonObject object, String side) {
        path = Paths.get(object.get("path").getStringValue());
        sha1 = object.get("hashes").asObject().get("sha1").getStringValue();
        // :p
        env = object.getOpt("env").map(JsonValue::asObject).map((value) -> value.get(side))
                .map(JsonValue::getStringValue).map((string) -> string.toUpperCase(Locale.ROOT)).map(MrEnvSpec::valueOf)
                .orElse(MrEnvSpec.REQUIRED);
        urls = object.get("downloads").asArray().getList().stream().map(JsonValue::getStringValue)
                .collect(Collectors.toList());
    }

    public Path getPath() {
        return path;
    }

    public String getSha1() {
        return sha1;
    }

    public MrEnvSpec getEnv() {
        return env;
    }

    public void download(Path base) throws IOException {
        Path target = Util.checkParent(base, base.resolve(path));
        Iterator<String> iterator = urls.iterator();
        while (iterator.hasNext()) {
            try (InputStream in = new URL(iterator.next()).openStream()) {
                if (!Files.isDirectory(target.getParent()))
                    Files.createDirectories(target.getParent());

                Files.copy(in, target);
            } catch (IOException error) {
                if (!iterator.hasNext())
                    throw new IOException("All urls from " + urls + " could not be downloaded", error);

                System.err.printf("URL %s failed; trying next one");
                error.printStackTrace();
            }
        }
    }
}
