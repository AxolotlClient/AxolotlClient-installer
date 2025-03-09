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

package io.github.axolotlclient.installer.modrinth.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import io.github.axolotlclient.installer.util.Util;
import io.toadlabs.jfgjds.JsonDeserializer;
import io.toadlabs.jfgjds.data.JsonObject;
import io.toadlabs.jfgjds.data.JsonValue;

public class ProjectVersion {

    private static final String URL_FORMAT = "https://api.modrinth.com/v2/project/%s/version";

    private final List<String> gameVersions;
    private final List<ProjectFile> files;
    private final ReleaseChannel versionType;

    public static List<ProjectVersion> getFeatured(String slug) throws IOException {
        URL url = new URL(String.format(URL_FORMAT, slug));
        try (InputStream in = Util.openStream(url)) {
            return JsonDeserializer.read(in, StandardCharsets.UTF_8).asArray().stream().map(JsonValue::asObject)
                    .map(ProjectVersion::new).collect(Collectors.toList());
        }
    }

    public ProjectVersion(JsonObject obj) {
        this.gameVersions = obj.get("game_versions").asArray().stream().map(JsonValue::getStringValue)
                .collect(Collectors.toList());
        this.files = obj.get("files").asArray().stream().map(JsonValue::asObject).map(ProjectFile::new)
                .collect(Collectors.toList());
        this.versionType = ReleaseChannel.parse(obj.get("version_type").getStringValue());
    }

    public List<String> getGameVersions() {
        return gameVersions;
    }

    public List<ProjectFile> getFiles() {
        return files;
    }

    public ReleaseChannel getVersionType() {
        return versionType;
    }
}
