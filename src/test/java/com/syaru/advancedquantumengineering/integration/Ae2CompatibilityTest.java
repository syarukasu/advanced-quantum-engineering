package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
