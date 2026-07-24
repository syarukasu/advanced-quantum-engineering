package com.syaru.advancedquantumengineering.integration;

import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import com.syaru.advancedquantumengineering.config.AQEConfig;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fml.ModList;

/** Optional ACO integration loader with no eager reference to ACO classes. */
public final class BigCraftingIntegration {
    public static final String ACO_MODID = "ae2_crafting_optimizer";
    public static final String SUPPORTED_ACO_VERSION_RANGE = "[1.3.0,1.6.0)";
    private static final String ADAPTER_CLASS =
            "com.syaru.advancedquantumengineering.integration.AcoBigCraftingBackend";

    private static volatile boolean initialized;
    private static volatile boolean acoBackendSelected;
    private static volatile AQEBigCraftingBackend backend = new LocalBackend();

    private BigCraftingIntegration() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        if (!ModList.get().isLoaded(ACO_MODID)) {
            AdvancedQuantumEngineering.LOGGER.info(
                    "ACO is not installed; AQE BigInteger cores use the saturated-long fallback");
            return;
        }

        String installed = ModList.get().getModContainerById(ACO_MODID)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("unknown");
        // Forge enforces the optional dependency range from mods.toml when ACO is present.
        // The adapter additionally verifies API_VERSION and every reflected method below.
        AdvancedQuantumEngineering.LOGGER.info(
                "Detected optional ACO {} in supported range {}; validating BigInteger API v3",
                installed,
                SUPPORTED_ACO_VERSION_RANGE);

        try {
            Class<?> adapterType = Class.forName(
                    ADAPTER_CLASS, true, BigCraftingIntegration.class.getClassLoader());
            AQEBigCraftingBackend candidate = (AQEBigCraftingBackend)
                    adapterType.getDeclaredConstructor().newInstance();
            backend = candidate;
            acoBackendSelected = true;
            if (candidate.isAvailable()) {
                AdvancedQuantumEngineering.LOGGER.info(
                        "AQE optional BigInteger backend selected and enabled: {}", candidate.id());
            } else {
                AdvancedQuantumEngineering.LOGGER.warn(
                        "ACO {} is installed, but its BigInteger backend is currently disabled; "
                                + "AQE will use the local fallback until it is enabled",
                        installed);
            }
        } catch (LinkageError | ReflectiveOperationException failure) {
            backend = new LocalBackend();
            acoBackendSelected = false;
            Throwable cause = failure instanceof InvocationTargetException invocation
                    && invocation.getCause() != null
                            ? invocation.getCause()
                            : failure;
            failOrFallback(
                    "Failed to initialize optional ACO BigInteger integration for " + installed,
                    cause);
        } catch (RuntimeException failure) {
            backend = new LocalBackend();
            acoBackendSelected = false;
            failOrFallback(
                    "Failed to initialize optional ACO BigInteger integration for " + installed,
                    failure);
        }
    }

    public static AQEBigCraftingHost createHost(
            Object owner,
            BigInteger physicalCapacity,
            CompoundTag savedState) {
        initialize();
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(physicalCapacity, "physicalCapacity");
        CompoundTag safeSaved = savedState == null ? new CompoundTag() : savedState.copy();
        try {
            if (acoBackendSelected && !backend.isAvailable()) {
                return new LocalBigCraftingHost(physicalCapacity, safeSaved);
            }
            return backend.create(owner, physicalCapacity, safeSaved);
        } catch (RuntimeException | LinkageError failure) {
            if (AQEConfig.failFastOnIntegrationMismatch()) {
                throw new IllegalStateException(
                        "Failed to restore AQE's optional BigInteger crafting backend. "
                                + "The saved state was not discarded.",
                        failure);
            }
            AdvancedQuantumEngineering.LOGGER.error(
                    "Optional BigInteger backend failed; preserving its state in long fallback",
                    failure);
            return new LocalBigCraftingHost(physicalCapacity, safeSaved);
        }
    }

    public static boolean isAcoBackendActive() {
        initialize();
        try {
            return acoBackendSelected && backend.isAvailable();
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }

    public static String backendId() {
        initialize();
        return backend.id();
    }

    private static void failOrFallback(String message, Throwable failure) {
        if (AQEConfig.failFastOnIntegrationMismatch()) {
            throw new IllegalStateException(message, failure);
        }
        if (failure == null) {
            AdvancedQuantumEngineering.LOGGER.error(message);
        } else {
            AdvancedQuantumEngineering.LOGGER.error(message, failure);
        }
    }

    private static final class LocalBackend implements AQEBigCraftingBackend {
        @Override
        public boolean isAvailable() {
            return true;
        }

        @Override
        public String id() {
            return "aqe:long_fallback";
        }

        @Override
        public AQEBigCraftingHost create(
                Object owner,
                BigInteger physicalCapacity,
                CompoundTag savedState) {
            return new LocalBigCraftingHost(physicalCapacity, savedState);
        }
    }
}
