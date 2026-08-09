package com.syaru.advancedquantumengineering.integration;

import java.util.Locale;
import java.util.Optional;
import net.minecraftforge.fml.ModList;

/**
 * Runtime compatibility information for the AE2 replacement forks supported by AQE.
 *
 * <p>AE2-UELM deliberately keeps the upstream mod id ({@code ae2}), so it must
 * never be treated as a second AE2 installation or as a separate dependency.</p>
 */
public final class Ae2Compatibility {
    public static final String AE2_MOD_ID = "ae2";
    public static final String UELM_VERSION = "15.5.0-uelm";

    private Ae2Compatibility() {
    }

    public static Optional<String> detectedVersion() {
        return ModList.get().getModContainerById(AE2_MOD_ID)
                .map(container -> container.getModInfo().getVersion().toString());
    }

    public static boolean isUelmVersion(String version) {
        if (version == null) {
            return false;
        }

        String normalized = version.trim().toLowerCase(Locale.ROOT);
        return normalized.equals(UELM_VERSION)
                || normalized.startsWith(UELM_VERSION + "+")
                || normalized.startsWith(UELM_VERSION + ".");
    }

    public static boolean isUelmLoaded() {
        return detectedVersion().map(Ae2Compatibility::isUelmVersion).orElse(false);
    }

    public static String implementationLabel(String version) {
        return isUelmVersion(version) ? "AE2-UELM" : "AE2 upstream-compatible";
    }
}
