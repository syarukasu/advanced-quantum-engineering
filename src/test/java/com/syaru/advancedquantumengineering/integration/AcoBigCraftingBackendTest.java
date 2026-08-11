package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class AcoBigCraftingBackendTest {
    @Test
    void bindsOnlyThroughTheReflectedOptionalApi() throws ReflectiveOperationException {
        assumeAcoApiIsPresent();
        AcoBigCraftingBackend backend = new AcoBigCraftingBackend();
        assumeAcoRuntimeIsLoaded(backend);
        Object owner = new Object();
        BigInteger capacity = BigInteger.TEN.pow(64);
        AQEBigCraftingHost host = backend.create(owner, capacity, new CompoundTag());
        assertTrue(backend.isAvailable());
        assertEquals("aco:big_crafting_v3", backend.id());
        Object registration = findRegistration(owner);
        assertTrue(registration.getClass().getMethod("ownerIdentity").invoke(registration) == owner);
        assertFalse((boolean) registration.getClass().getMethod("isClosed").invoke(registration));

        host.reconcile(capacity, Map.of(
                UUID.randomUUID(), BigInteger.valueOf(Long.MAX_VALUE),
                UUID.randomUUID(), BigInteger.valueOf(Long.MAX_VALUE)));
        assertEquals(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.TWO), host.reserved());
        assertEquals(Long.MAX_VALUE, host.availableAsSaturatedLong());

        host.close();
        assertTrue(findRegistrationOptional(owner).isEmpty());
        assertTrue((boolean) registration.getClass().getMethod("isClosed").invoke(registration));
    }

    @Test
    void restoresOpaqueAcoPayloadWithoutLosingBigReservation()
            throws ReflectiveOperationException {
        assumeAcoApiIsPresent();
        AcoBigCraftingBackend backend = new AcoBigCraftingBackend();
        assumeAcoRuntimeIsLoaded(backend);
        Object firstOwner = new Object();
        BigInteger capacity = BigInteger.TEN.pow(50);
        AQEBigCraftingHost first = backend.create(firstOwner, capacity, new CompoundTag());
        first.reconcile(capacity, Map.of(UUID.randomUUID(), BigInteger.TEN.pow(30)));
        BigInteger bigReservation = BigInteger.TEN.pow(30);
        CompoundTag saved = first.save();
        first.close();

        Object restoredOwner = new Object();
        AQEBigCraftingHost restored = backend.create(restoredOwner, capacity, saved);
        assertEquals(bigReservation, restored.reserved());
        restored.reconcile(capacity, Map.of(UUID.randomUUID(), BigInteger.valueOf(250)));

        assertEquals(BigInteger.valueOf(250), restored.reserved());
        AQEBigCraftingHostState.Decoded envelope = AQEBigCraftingHostState.decode(restored.save());
        assertEquals("aco:big_crafting_v3", envelope.backend());
        assertEquals(BigInteger.ZERO, envelope.backendReserved());
        restored.close();
    }

    /** 公開RegistryだけをReflectionで読み、AQEの本番コードと同じ任意依存境界を検証する。 */
    private static Object findRegistration(Object owner) throws ReflectiveOperationException {
        return findRegistrationOptional(owner).orElseThrow();
    }

    private static Optional<?> findRegistrationOptional(Object owner)
            throws ReflectiveOperationException {
        Class<?> registry = Class.forName(
                "com.syaru.ae2craftingoptimizer.api.big.BigCraftingHostRegistry");
        Method findRegistration = registry.getMethod("findRegistration", Object.class);
        return (Optional<?>) findRegistration.invoke(null, owner);
    }

    /** 任意依存をGradleの単体テストへ同梱しない構成では、この連携試験をSkipする。 */
    private static void assumeAcoApiIsPresent() {
        try {
            Class.forName("com.syaru.ae2craftingoptimizer.api.big.BigCraftingEngineApi");
        } catch (ClassNotFoundException missingOptionalDependency) {
            Assumptions.assumeTrue(false, "ACO runtime API is not on the unit-test classpath");
        }
    }

    /** ForgeのModListとACO設定が揃う実環境だけで任意連携の状態試験を実行する。 */
    private static void assumeAcoRuntimeIsLoaded(AcoBigCraftingBackend backend) {
        boolean available;
        try {
            available = backend.isAvailable();
        } catch (IllegalStateException notLoadedYet) {
            available = false;
        }
        Assumptions.assumeTrue(
                available,
                "ACO runtime integration requires a loaded Forge config and compatible host mod");
    }
}
