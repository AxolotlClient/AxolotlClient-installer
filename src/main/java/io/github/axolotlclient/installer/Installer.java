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

package io.github.axolotlclient.installer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import io.github.axolotlclient.installer.mrpack.MrPack;
import masecla.modrinth4j.client.agent.UserAgent;
import masecla.modrinth4j.endpoints.version.GetProjectVersions.GetProjectVersionsRequest;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.version.ProjectVersion;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectFile;

/**
 * The installer backend.
 */
public final class Installer {

    private static final String SLUG = "axolotlclient-modpack";

    private final ModrinthAPI api;
    private final List<String> availableGameVers;
    private final Map<String, ProjectVersion> modVerFromGameVer = new HashMap<>();

    public Installer() throws InterruptedException, ExecutionException {
        api = ModrinthAPI.rateLimited(UserAgent.builder().projectName("AxolotlClient").build(), "");
        api.versions().getProjectVersions(SLUG, GetProjectVersionsRequest.builder().featured(true).build()).get()
                .forEach((version) -> {
                    for (String game : version.getGameVersions())
                        this.modVerFromGameVer.put(game, version);
                });
        availableGameVers = modVerFromGameVer.keySet().stream().sorted(MinecraftVersionComparator.INSTANCE.reversed())
                .collect(Collectors.toList());
    }

    public void install(ProjectVersion version, Path directory) throws IOException {
        ProjectFile file = version.getFiles().stream().filter(ProjectFile::isPrimary).findFirst()
                .orElseThrow(() -> new IllegalStateException("No primary file found"));
        try (InputStream in = new URL(file.getUrl()).openStream()) {
            MrPack pack = MrPack.extract(in, "client", directory);
            pack.installMods(directory, ignored -> false);
        }
    }

    public ProjectVersion getModVerForGameVer(String game) {
        return modVerFromGameVer.get(game);
    }

    public List<String> getAvailableGameVers() {
        return availableGameVers;
    }
}
