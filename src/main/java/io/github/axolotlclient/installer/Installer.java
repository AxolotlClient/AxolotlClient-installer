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

import static io.github.axolotlclient.installer.util.Translate.tr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import io.github.axolotlclient.installer.mrpack.MrPack;
import io.github.axolotlclient.installer.util.MinecraftVersionComparator;
import io.github.axolotlclient.installer.util.Util;
import io.toadlabs.jfgjds.JsonDeserializer;
import io.toadlabs.jfgjds.JsonSerializer;
import io.toadlabs.jfgjds.data.JsonObject;
import masecla.modrinth4j.endpoints.version.GetProjectVersions.GetProjectVersionsRequest;
import masecla.modrinth4j.main.ModrinthAPI;
import masecla.modrinth4j.model.version.ProjectVersion;
import masecla.modrinth4j.model.version.ProjectVersion.ProjectFile;

/**
 * The installer backend.
 */
public final class Installer {

    private static final String MR_SLUG = "axolotlclient-modpack";
    private static final String QUILT_LOADER = "https://meta.quiltmc.org/v3/versions/loader/%s/%s/profile/json";
    private static final String FABRIC_LOADER = "https://meta.fabricmc.net/v2/versions/loader/%s/%s/profile/json";
    private static final String LEGACY_FABRIC_LOADER = "https://meta.legacyfabric.net/v2/versions/loader/%s/%s/profile/json";
    private static final String ICON;

    static {
        ICON = getIcon();
    }

    private final ModrinthAPI api;
    private final List<String> availableGameVers;
    private final Map<String, ProjectVersion> modVerFromGameVer = new HashMap<>();

    private static String getIcon() {
        try {
            return "data:image/png;base64," + Base64.getEncoder()
                    .encodeToString(Util.readBytes(Installer.class.getResourceAsStream("/icon.png")));
        } catch (IOException e) {
            System.err.println("Failed to load icon");
            e.printStackTrace();
            return "Furnace";
        }
    }

    public Installer() throws InterruptedException, ExecutionException {
        api = ModrinthAPI.rateLimited(Util.USER_AGENT, "");
        // create a mapping of game version to latest mod version
        api.versions().getProjectVersions(MR_SLUG, GetProjectVersionsRequest.builder().featured(true).build()).get()
                .forEach((version) -> {
                    for (String game : version.getGameVersions())
                        this.modVerFromGameVer.put(game, version);
                });
        // collect a sorted list of game versions
        availableGameVers = modVerFromGameVer.keySet().stream().sorted(MinecraftVersionComparator.INSTANCE.reversed())
                .collect(Collectors.toList());
    }

    public void install(ProjectVersion version, Path launcherDir, Path gameDir, ProgressConsumer progress)
            throws IOException {
        ProjectFile file = version.getFiles().stream().filter(ProjectFile::isPrimary).findFirst()
                .orElseThrow(() -> new IllegalStateException("No primary file found"));
        progress.update(tr("downloading_modpack"), -1);
        MrPack pack;
        try (InputStream in = Util.openStream(new URL(file.getUrl()))) {
            pack = MrPack.extract(in, "client", gameDir);
        }

        progress.update(tr("installing_mods"), 0);
        pack.installMods(gameDir, ignored -> false, progress);

        Path versions = launcherDir.resolve("versions");
        URL url;
        String versionName;

        // install the loader
        progress.update(tr("installing_loader"), -1);

        String gameVersion = pack.getDependencies().get("minecraft");
        if (pack.getDependencies().containsKey("quilt-loader")) {
            // install quilt!
            String quiltLoader = pack.getDependencies().get("quilt-loader");
            url = new URL(String.format(QUILT_LOADER, gameVersion, quiltLoader));
            versionName = "quilt-loader-" + quiltLoader;
        } else if (pack.getDependencies().containsKey("fabric-loader")) {
            // install fabric!
            String fabricLoader = pack.getDependencies().get("fabric-loader");
            int[] versionParts = Util.parseSemVer(gameVersion);

            versionName = "fabric-loader-" + fabricLoader;

            // heuristic for legacy fabric
            if (versionParts == null || versionParts[0] > 1 || versionParts[1] > 13)
                url = new URL(String.format(FABRIC_LOADER, gameVersion, fabricLoader));
            else
                url = new URL(String.format(LEGACY_FABRIC_LOADER, gameVersion, fabricLoader));
        } else
            throw new UnsupportedOperationException("Cannot find supported mod loader!");

        versionName += '-' + gameVersion;

        Path versionDir = versions.resolve(versionName);
        Path versionJson = versionDir.resolve(versionName + ".json");

        if (!Files.exists(versionJson)) {
            if (!Files.isDirectory(versionDir))
                Files.createDirectories(versionDir);

            try (InputStream in = url.openStream()) {
                Files.copy(in, versionJson);
            }
        }

        Path launcherProfiles = launcherDir.resolve("launcher_profiles.json");
        JsonObject profiles = null;
        if (Files.exists(launcherProfiles)) {
            try (InputStream in = Files.newInputStream(launcherProfiles)) {
                profiles = JsonDeserializer.read(in, StandardCharsets.UTF_8).asObject();
            } catch (IOException e) {
                System.err.println("Could not open profiles");
                e.printStackTrace();
            }
        }

        if (profiles == null)
            profiles = JsonObject.of("version", 3);

        JsonObject profilesMap = profiles.computeIfAbsent("profiles", JsonObject.DEFAULT_COMPUTION).asObject();
        profilesMap.put("axolotlclient-" + gameVersion,
                JsonObject.of("created", new Date(), "lastUsed", new Date(), "lastVersionId", versionName, "name",
                        "AxolotlClient " + gameVersion, "icon", ICON, "gameDir", gameDir.toAbsolutePath()));

        try (OutputStream out = Files.newOutputStream(launcherProfiles)) {
            JsonSerializer.write(profiles, out, StandardCharsets.UTF_8);
        }
    }

    public ProjectVersion getModVerForGameVer(String game) {
        return modVerFromGameVer.get(game);
    }

    public List<String> getAvailableGameVers() {
        return availableGameVers;
    }
}
