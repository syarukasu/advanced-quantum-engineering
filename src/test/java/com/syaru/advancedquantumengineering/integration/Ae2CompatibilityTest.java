package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class Ae2CompatibilityTest {
    @Test
    void recognizesThePublishedUelmVersionWithoutInventingAnotherModId() {
        assertTrue(Ae2Compatibility.isUelmVersion("15.5.0-uelm"));
        assertTrue(Ae2Compatibility.isUelmVersion("15.5.0-uelm+build.1"));
        assertTrue(Ae2Compatibility.isUelmVersion("15.5.0-uelm.1"));
        assertEquals("ae2", Ae2Compatibility.AE2_MOD_ID);
    }

    @Test
    void doesNotMisclassifyUpstreamOrUnrelatedVersions() {
        assertFalse(Ae2Compatibility.isUelmVersion("15.4.10"));
        assertFalse(Ae2Compatibility.isUelmVersion("15.5.0"));
        assertFalse(Ae2Compatibility.isUelmVersion("15.5.0-uelm-dev"));
        assertFalse(Ae2Compatibility.isUelmVersion(null));
    }

    @Test
    void labelsBothSupportedImplementations() {
        assertEquals("AE2-UELM", Ae2Compatibility.implementationLabel("15.5.0-uelm"));
        assertEquals("AE2 upstream-compatible", Ae2Compatibility.implementationLabel("15.4.10"));
    }

    @Test
    void acceptsOnlyTheForge47_4RuntimeLine() throws IOException {
        try (InputStream metadata = getClass().getResourceAsStream("/META-INF/mods.toml")) {
            assertNotNull(metadata);
            String normalized = new String(metadata.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("\r\n", "\n");
            assertTrue(normalized.contains(
                    "modId = \"forge\"\n"
                            + "mandatory = true\n"
                            + "versionRange = \"[47.4,47.5)\""));
        }
    }
}
