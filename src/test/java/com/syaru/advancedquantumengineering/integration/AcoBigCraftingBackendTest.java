package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.electronwill.nightconfig.core.CommentedConfig;
import java.math.BigInteger;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class AcoBigCraftingBackendTest {
    @Test
    void bindsOnlyThroughTheReflectedOptionalApi() throws ReflectiveOperationException {
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

    @Test
    void closedHostRemainsReadableForFinalWorldSave()
            throws ReflectiveOperationException {
        loadAcoConfigDefaults();
        AcoBigCraftingBackend backend = new AcoBigCraftingBackend();
        Object owner = new Object();
        BigInteger capacity = BigInteger.TEN.pow(64);
        BigInteger reservation = BigInteger.TEN.pow(32);
        AQEBigCraftingHost host = backend.create(owner, capacity, new CompoundTag());
        host.reconcile(capacity, Map.of(UUID.randomUUID(), reservation));
        CompoundTag savedBeforeClose = host.save();

        host.close();

        assertEquals(capacity, host.physicalCapacity());
        assertEquals(reservation, host.reserved());
        assertEquals(capacity.subtract(reservation), host.available());
        assertEquals(capacity, host.snapshot(1L).physicalCapacity());
        assertEquals(savedBeforeClose, host.save());
        assertThrows(
                IllegalStateException.class,
                () -> host.reconcile(capacity, Map.of()));
    }

    /** Minecraftを起動しないJUnitでも、ACOのNeoForge Config既定値だけを有効にする。 */
    private static void loadAcoConfigDefaults() throws ReflectiveOperationException {
        Class<?> configClass = Class.forName(
                "com.syaru.ae2craftingoptimizer.config.ACOConfig");
        Field specField = configClass.getDeclaredField("SPEC");
        specField.setAccessible(true);
        ModConfigSpec spec = (ModConfigSpec) specField.get(null);
        CommentedConfig defaults = CommentedConfig.inMemory();
        spec.correct(defaults);
        Class<?> loadedConfigType = Class.forName("net.neoforged.fml.config.LoadedConfig");
        Constructor<?> constructor = loadedConfigType.getDeclaredConstructor(
                CommentedConfig.class, Path.class, ModConfig.class);
        constructor.setAccessible(true);
        Object loadedConfig = constructor.newInstance(defaults, null, null);
        Class<?> loadedConfigContract = Class.forName(
                "net.neoforged.fml.config.IConfigSpec$ILoadedConfig");
        Method acceptConfig = ModConfigSpec.class.getMethod("acceptConfig", loadedConfigContract);
        acceptConfig.invoke(spec, loadedConfig);
    }

    /** 公開RegistryだけをReflectionで読み、AQEの任意依存境界を実装JARで検証する。 */
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

    /** NeoForgeのModListとACO設定が揃う実環境だけで任意連携の状態試験を実行する。 */
    private static void assumeAcoRuntimeIsLoaded(AcoBigCraftingBackend backend) {
        boolean available;
        try {
            available = backend.isAvailable();
        } catch (RuntimeException notLoadedYet) {
            // JUnit単体環境にはNeoForgeのModListと実Configロードがないため、任意連携試験を飛ばす。
            available = false;
        }
        Assumptions.assumeTrue(
                available,
                "ACO runtime integration requires a loaded NeoForge config and compatible host mod");
    }
}
