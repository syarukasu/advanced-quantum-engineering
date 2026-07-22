package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class OptionalAcoMetadataTest {
    @Test
    void runtimeContractMatchesOptionalDependencyMetadata() throws IOException {
        assertEquals("[1.3.0,1.5.0)", BigCraftingIntegration.SUPPORTED_ACO_VERSION_RANGE);

        String metadata;
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("META-INF/mods.toml")) {
            if (input == null) {
                throw new AssertionError("processed mods.toml was not available to the test runtime");
            }
            metadata = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        int dependency = metadata.indexOf("modId = \"ae2_crafting_optimizer\"");
        assertTrue(dependency >= 0, "optional ACO dependency must be declared");
        String acoSection = metadata.substring(dependency);
        assertTrue(acoSection.contains("mandatory = false"), "ACO must remain optional");
        assertTrue(
                acoSection.contains("versionRange = \""
                        + BigCraftingIntegration.SUPPORTED_ACO_VERSION_RANGE + "\""),
                "runtime and metadata compatibility ranges must stay aligned");
    }
}
