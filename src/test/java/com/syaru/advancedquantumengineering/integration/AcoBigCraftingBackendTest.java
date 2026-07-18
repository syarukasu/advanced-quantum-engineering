package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.syaru.ae2craftingoptimizer.api.big.BigCraftingHostRegistry;
import com.syaru.ae2craftingoptimizer.api.big.BigCraftingHostRuntime;
import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

class AcoBigCraftingBackendTest {
    @Test
    void bindsOnlyThroughTheReflectedOptionalApi() throws ReflectiveOperationException {
        AcoBigCraftingBackend backend = new AcoBigCraftingBackend();
        Object owner = new Object();
        BigInteger capacity = BigInteger.TEN.pow(64);
        AQEBigCraftingHost host = backend.create(owner, capacity, new CompoundTag());
        BigCraftingHostRuntime runtime = BigCraftingHostRegistry.find(owner);

        assertTrue(backend.isAvailable());
        assertEquals("aco:big_crafting_v3", backend.id());
        assertSame(runtime, BigCraftingHostRegistry.find(owner));

        host.reconcile(capacity, Map.of(
                UUID.randomUUID(), BigInteger.valueOf(Long.MAX_VALUE),
                UUID.randomUUID(), BigInteger.valueOf(Long.MAX_VALUE)));
        assertEquals(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TWO), host.reserved());
        assertEquals(Long.MAX_VALUE, host.availableAsSaturatedLong());

        host.close();
        assertNull(BigCraftingHostRegistry.find(owner));
    }

    @Test
    void restoresOpaqueAcoPayloadWithoutLosingBigReservation()
            throws ReflectiveOperationException {
        AcoBigCraftingBackend backend = new AcoBigCraftingBackend();
        Object firstOwner = new Object();
        BigInteger capacity = BigInteger.TEN.pow(50);
        AQEBigCraftingHost first = backend.create(firstOwner, capacity, new CompoundTag());
        BigInteger bigReservation = BigInteger.TEN.pow(30);
        BigCraftingHostRegistry.find(firstOwner).setBigReserved(bigReservation);
        CompoundTag saved = first.save();
        first.close();

        Object restoredOwner = new Object();
        AQEBigCraftingHost restored = backend.create(restoredOwner, capacity, saved);
        restored.reconcile(capacity, Map.of(UUID.randomUUID(), BigInteger.valueOf(250)));

        assertEquals(bigReservation.add(BigInteger.valueOf(250)), restored.reserved());
        AQEBigCraftingHostState.Decoded envelope = AQEBigCraftingHostState.decode(restored.save());
        assertEquals("aco:big_crafting_v3", envelope.backend());
        assertEquals(bigReservation, envelope.backendReserved());
        restored.close();
    }
}
