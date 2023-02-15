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

package io.github.axolotlclient.installer.mrpack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.github.axolotlclient.installer.ProgressConsumer;
import io.github.axolotlclient.installer.util.Util;
import io.toadlabs.jfgjds.JsonDeserializer;
import io.toadlabs.jfgjds.data.JsonObject;

public final class MrPack {

    private final Map<String, String> dependencies;
    private final List<MrFile> files;

    // due to technical limitations, it's best to extract the pack as it's read
    public static MrPack extract(InputStream in, String side, Path directory) throws IOException {
        ZipInputStream zipIn = new ZipInputStream(in);
        ZipEntry entry;
        MrPack pack = null;
        Set<Path> sideExtracted = new HashSet<>();
        while ((entry = zipIn.getNextEntry()) != null) {
            boolean sideOverride = entry.getName().startsWith(side + "-overrides/");

            if (entry.getName().equals("modrinth.index.json"))
                pack = new MrPack(JsonDeserializer.read(zipIn, StandardCharsets.UTF_8).asObject(), side);
            else if (entry.getName().startsWith("overrides/")) {
                Path path = Util.checkParent(directory,
                        directory.resolve(entry.getName().substring(entry.getName().indexOf('/') + 1)));
                if (!sideExtracted.add(path) && !sideOverride)
                    // the side-specific takes priority!
                    continue;

                if (entry.isDirectory())
                    continue;

                if (Files.isRegularFile(path))
                    Files.deleteIfExists(path);
                if (Files.isDirectory(path))
                    continue;
                if (!Files.isDirectory(path.getParent()))
                    Files.createDirectories(path.getParent());

                Files.copy(zipIn, path);
            }
        }

        if (pack == null)
            throw new MrPackException("Not a valid mrpack");

        return pack;
    }

    private MrPack(JsonObject object, String side) {
        dependencies = new HashMap<>();
        files = new ArrayList<>();

        object.get("dependencies").asObject().forEach((key, value) -> dependencies.put(key, value.getStringValue()));
        object.get("files").asArray().forEach((file) -> files.add(new MrFile(file.asObject(), side)));
    }

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public List<MrFile> getFiles() {
        return files;
    }

    public void installMods(Path base, Predicate<MrFile> optionalModHandling, ProgressConsumer progress) {
        float percent = 0;
        float percentStep = 1F / files.size();

        for (MrFile file : files) {
            try {
                if (file.getEnv() == MrEnvSpec.UNSUPPORTED)
                    continue;
                if (file.getEnv() == MrEnvSpec.OPTIONAL && !optionalModHandling.test(file))
                    continue;

                file.download(base, progress.subprogress(percent, percent + percentStep));
            } catch (IOException e) {
                System.err.println("Failed to download file");
                e.printStackTrace();
            }
            percent += percentStep;
        }
    }
}
