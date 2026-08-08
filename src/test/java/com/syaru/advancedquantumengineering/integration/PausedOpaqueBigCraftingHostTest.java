package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import java.math.BigInteger;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class PausedOpaqueBigCraftingHostTest {
    @Test
    void preservesMalformedPayloadAndNeverReleasesCapacity() {
        CompoundTag original = new CompoundTag();
        original.putString("backend", "aco:big_crafting_v3");
        original.putByteArray("backendReserved", BigInteger.valueOf(1234).toByteArray());
        CompoundTag malformedPayload = new CompoundTag();
        malformedPayload.putString("sentinel", "do-not-discard");
        original.put("payload", malformedPayload);

        PausedOpaqueBigCraftingHost host = new PausedOpaqueBigCraftingHost(
                BigInteger.valueOf(1000),
                original,
                PausedOpaqueBigCraftingHost.FailureCategory.UNKNOWN_SCHEMA);
        host.reconcile(BigInteger.valueOf(1000), Map.of());

        assertTrue(host.isPaused());
        assertEquals("PAUSED_CORRUPT", host.stateHint());
        assertEquals(BigInteger.ZERO, host.available());
        assertEquals(0L, host.availableAsSaturatedLong());
        assertEquals(BigInteger.valueOf(1234), host.reserved());
        assertEquals(original, host.save());
        assertEquals(original, host.save());
    }

    @Test
    void rejectsNonCanonicalOrOversizedOuterReservationProjection() {
        CompoundTag nonCanonical = new CompoundTag();
        nonCanonical.putByteArray("backendReserved", new byte[] {0, 1});
        assertFalse(AQEBigCraftingHostState.safeReservedProjection(nonCanonical).isPresent());

        CompoundTag oversized = new CompoundTag();
        oversized.putByteArray("backendReserved", new byte[AQEConfig.MAX_BIG_INTEGER_BITS / 8 + 2]);
        assertFalse(AQEBigCraftingHostState.safeReservedProjection(oversized).isPresent());
    }

    @Test
    void incompatibleBackendUsesIncompatibleDiagnostic() {
        PausedOpaqueBigCraftingHost host = new PausedOpaqueBigCraftingHost(
                BigInteger.TEN,
                new CompoundTag(),
                PausedOpaqueBigCraftingHost.FailureCategory.ACO_API_MISMATCH);
        assertEquals("PAUSED_INCOMPATIBLE", host.stateHint());
    }
}
