package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.jupiter.api.Test;

class OptionalAcoMetadataTest {
    @Test
    void runtimeContractMatchesOptionalDependencyMetadata() throws IOException {
        assertEquals("[1.5.9,1.6.0),[2.0.0-rc.1,3.0.0)",
                BigCraftingIntegration.SUPPORTED_ACO_VERSION_RANGE);

        String metadata;
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("META-INF/neoforge.mods.toml")) {
            if (input == null) {
                throw new AssertionError("processed neoforge.mods.toml was not available to the test runtime");
            }
            metadata = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        int dependency = metadata.indexOf("modId = \"ae2_crafting_optimizer\"");
        assertTrue(dependency >= 0, "optional ACO dependency must be declared");
        String acoSection = metadata.substring(dependency);
        assertTrue(acoSection.contains("type = \"optional\""), "ACO must remain optional");
        assertTrue(
                acoSection.contains("versionRange = \""
                        + BigCraftingIntegration.SUPPORTED_ACO_VERSION_RANGE + "\""),
                "runtime and metadata compatibility ranges must stay aligned");
        assertTrue(
                metadata.contains("ACO is an optional BigInteger backend"),
                "mod description must explain the optional ACO backend");
    }

    @Test
    void acceptsLegacyAndSnapshotAcoWithoutAdmittingUnsupportedMajorVersions() throws Exception {
        VersionRange supported = VersionRange.createFromVersionSpec(
                BigCraftingIntegration.SUPPORTED_ACO_VERSION_RANGE);
        for (String version : new String[] {
                "1.5.9", "1.5.33", "2.0.0-rc.4", "2.0.0-snapshot-refs.8", "2.0.0"
        }) {
            assertTrue(supported.containsVersion(new DefaultArtifactVersion(version)), version);
        }
        for (String version : new String[] {"1.5.8", "1.6.0", "3.0.0"}) {
            assertFalse(supported.containsVersion(new DefaultArtifactVersion(version)), version);
        }
    }
}
