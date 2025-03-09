package io.github.axolotlclient.installer.modrinth.api;

import java.util.Locale;

public enum ReleaseChannel {
    RELEASE,
    BETA,
    ALPHA;

    public static ReleaseChannel parse(String value) {
        switch (value) {
            case "release":
                return RELEASE;
            case "beta":
                return BETA;
            case "alpha":
                return ALPHA;
        }

        throw new IllegalArgumentException(value);
    }

}
