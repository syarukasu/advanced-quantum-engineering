package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class LocalBigCraftingHostTest {
    @Test
    void tracksManyLongJobsAgainstBigIntegerPhysicalCapacity() {
        BigInteger capacity = BigInteger.TEN.pow(64).subtract(BigInteger.ONE);
        LocalBigCraftingHost host = new LocalBigCraftingHost(capacity, new CompoundTag());
        host.reconcile(capacity, Map.of(
                UUID.randomUUID(), BigInteger.valueOf(Long.MAX_VALUE),
                UUID.randomUUID(), BigInteger.valueOf(Long.MAX_VALUE)));

        assertEquals(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TWO), host.reserved());
        assertEquals(capacity.subtract(host.reserved()), host.available());
        assertEquals(Long.MAX_VALUE, host.availableAsSaturatedLong());
    }

    @Test
    void preservesOpaqueAcoStateAndItsReservationWhileAcoIsMissing() {
        CompoundTag payload = new CompoundTag();
        payload.putString("sentinel", "keep-me");
        BigInteger pausedReservation = BigInteger.TEN.pow(30);
        CompoundTag saved = AQEBigCraftingHostState.encode(
                "aco:big_crafting_v3", pausedReservation, payload);
        LocalBigCraftingHost host = new LocalBigCraftingHost(BigInteger.TEN.pow(40), saved);
        host.reconcile(
                BigInteger.TEN.pow(40),
                Map.of(UUID.randomUUID(), BigInteger.valueOf(100)));

        assertEquals(pausedReservation.add(BigInteger.valueOf(100)), host.reserved());
        assertEquals("aqe:paused_optional_backend", host.backendId());
        CompoundTag roundTrip = host.save();
        assertEquals("keep-me", roundTrip.getCompound("payload").getString("sentinel"));
        assertArrayEquals(saved.getByteArray("backendReserved"), roundTrip.getByteArray("backendReserved"));
    }

    @Test
    void rejectsMalformedOrNonCanonicalCounts() {
        CompoundTag malformed = new CompoundTag();
        malformed.putInt("schema", AQEBigCraftingHostState.SCHEMA_VERSION);
        malformed.putString("backend", "aco:big_crafting_v3");
        malformed.putByteArray("backendReserved", new byte[] {0, 1});
        malformed.put("payload", new CompoundTag());

        assertThrows(
                IllegalArgumentException.class,
                () -> new LocalBigCraftingHost(BigInteger.TEN.pow(20), malformed));
    }
}
