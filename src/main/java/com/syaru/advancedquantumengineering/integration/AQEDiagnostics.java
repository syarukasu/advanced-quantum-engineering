package com.syaru.advancedquantumengineering.integration;

import appeng.block.crafting.ICraftingUnitType;
import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.registry.AQEBlocks;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitType;
import net.pedroksl.advanced_ae.common.blocks.AAEAbstractCraftingUnitBlock;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCluster;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

public final class AQEDiagnostics {
    private static final long TIB = 1024L * 1024L * 1024L * 1024L;
    private static final long HIGH_STORAGE_WARNING_BYTES = 256L * TIB;
    private static final int HIGH_COPROCESSOR_WARNING = 262_144;

    private AQEDiagnostics() {
    }

    public static void runStartupChecks() {
        boolean ok = true;

        logDetectedVersions();
        ok &= checkAdvancedAeApi();
        ok &= checkRegisteredUnitTypes();
        logConfiguredPerformance();

        if (!ok) {
            String message = "Advanced Quantum Engineering detected an Advanced AE integration mismatch. "
                    + "The modified Quantum Computer parts may not form or may report wrong values.";
            if (AQEConfig.failFastOnIntegrationMismatch()) {
                throw new IllegalStateException(message);
            }
            AdvancedQuantumEngineering.LOGGER.error(message);
        }
    }

    private static void logDetectedVersions() {
        AdvancedQuantumEngineering.LOGGER.info("Detected AE2 version: {}", getVersion("ae2"));
        AdvancedQuantumEngineering.LOGGER.info("Detected Advanced AE version: {}", getVersion(AdvancedAEIntegration.MODID));
        AdvancedQuantumEngineering.LOGGER.info("Detected AE2 Omni Cells version: {}", getVersion(OmniCellsIntegration.MODID));
    }

    private static String getVersion(String modId) {
        return ModList.get().getModContainerById(modId)
                .map(container -> container.getModInfo().getVersion().toString())
                .orElse("not loaded");
    }

    private static boolean checkAdvancedAeApi() {
        boolean ok = true;
        ok &= hasMethod(AdvCraftingBlockEntity.class, "getStorageBytes");
        ok &= hasMethod(AdvCraftingBlockEntity.class, "getStorageMultiplier");
        ok &= hasMethod(AdvCraftingBlockEntity.class, "getAcceleratorThreads");
        ok &= hasMethod(AdvCraftingBlockEntity.class, "getAccelerationMultiplier");
        ok &= hasDeclaredMethod(AdvCraftingCPUCluster.class, "addBlockEntity", AdvCraftingBlockEntity.class);
        ok &= hasUnitType("QUANTUM_CORE");
        ok &= hasUnitType("STORAGE_256M");
        ok &= hasUnitType("STORAGE_MULTIPLIER");
        ok &= hasUnitType("QUANTUM_ACCELERATOR");
        ok &= hasUnitType("MULTI_THREADER");
        return ok;
    }

    private static boolean hasMethod(Class<?> owner, String name) {
        try {
            owner.getMethod(name);
            return true;
        } catch (NoSuchMethodException e) {
            AdvancedQuantumEngineering.LOGGER.error("Missing expected method: {}.{}()", owner.getName(), name);
            return false;
        }
    }

    private static boolean hasDeclaredMethod(Class<?> owner, String name, Class<?>... parameterTypes) {
        try {
            Method method = owner.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);
            return true;
        } catch (NoSuchMethodException e) {
            AdvancedQuantumEngineering.LOGGER.error("Missing expected method: {}.{}(...)", owner.getName(), name);
            return false;
        }
    }

    private static boolean hasUnitType(String name) {
        try {
            AAECraftingUnitType.valueOf(name);
            return true;
        } catch (IllegalArgumentException e) {
            AdvancedQuantumEngineering.LOGGER.error("Missing expected Advanced AE crafting unit type: {}", name);
            return false;
        }
    }

    private static boolean checkRegisteredUnitTypes() {
        boolean ok = true;
        ok &= checkUnitType("modified_quantum_core", AQEBlocks.MODIFIED_QUANTUM_CORE.get(), AAECraftingUnitType.QUANTUM_CORE);
        ok &= checkUnitType("modified_quantum_storage", AQEBlocks.MODIFIED_QUANTUM_STORAGE.get(), AAECraftingUnitType.STORAGE_256M);
        ok &= checkUnitType("modified_quantum_accelerator", AQEBlocks.MODIFIED_QUANTUM_ACCELERATOR.get(), AAECraftingUnitType.QUANTUM_ACCELERATOR);
        ok &= checkUnitType("modified_quantum_multi_threader", AQEBlocks.MODIFIED_QUANTUM_MULTI_THREADER.get(), AAECraftingUnitType.MULTI_THREADER);
        ok &= checkUnitType("modified_data_entangler", AQEBlocks.MODIFIED_DATA_ENTANGLER.get(), AAECraftingUnitType.STORAGE_MULTIPLIER);
        ok &= checkUnitType("experimental_quantum_core", AQEBlocks.EXPERIMENTAL_QUANTUM_CORE.get(), AAECraftingUnitType.QUANTUM_CORE);
        return ok;
    }

    private static boolean checkUnitType(String name, AAEAbstractCraftingUnitBlock<?> block, ICraftingUnitType expected) {
        if (block.type == expected) {
            AdvancedQuantumEngineering.LOGGER.info("AQE unit type check passed: {} -> {}", name, expected);
            return true;
        }

        AdvancedQuantumEngineering.LOGGER.error(
                "AQE unit type mismatch: {} expected {}, got {}",
                name,
                expected,
                block.type
        );
        return false;
    }

    private static void logConfiguredPerformance() {
        long storage = safeMultiply(
                safeAdd(AQEConfig.getCoreStorage(), AQEConfig.getStorageBlockBytes()),
                AQEConfig.getDataEntanglerMultiplier()
        );
        long baseThreads = AQEConfig.getBaseCoprocessors();
        long acceleratorThreads = safeMultiply(
                AQEConfig.getAcceleratorThreads(),
                AQEConfig.getDiagnosticModifiedAcceleratorCount()
        );
        long effectiveThreads = safeMultiply(
                safeAdd(baseThreads, acceleratorThreads),
                AQEConfig.getMultiThreaderMultiplier()
        );

        AdvancedQuantumEngineering.LOGGER.info(
                "AQE diagnostic estimate: one modified core + one modified storage + one modified Data Entangler = {} bytes",
                storage
        );
        AdvancedQuantumEngineering.LOGGER.info(
                "AQE diagnostic estimate: modified core + {} modified accelerators + x{} modified Multi-Threader = {} co-processors",
                AQEConfig.getDiagnosticModifiedAcceleratorCount(),
                AQEConfig.getMultiThreaderMultiplier(),
                effectiveThreads
        );
        AdvancedQuantumEngineering.LOGGER.info(
                "AQE experimental Quantum Core: {} bytes, {} co-processors before Advanced AE multipliers",
                AQEConfig.getExperimentalCoreStorage(),
                AQEConfig.getExperimentalCoreCoprocessors()
        );
        AdvancedQuantumEngineering.LOGGER.info(
                "AQE effective co-processor values are clamped to {} to avoid AE2 int overflow",
                AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS
        );

        if (AQEConfig.warnOnExtremeConfigValues()) {
            warnIfExtreme(storage, Math.max(effectiveThreads, AQEConfig.getExperimentalCoreCoprocessors()));
        }
    }

    private static void warnIfExtreme(long storage, long effectiveThreads) {
        if (storage >= HIGH_STORAGE_WARNING_BYTES) {
            AdvancedQuantumEngineering.LOGGER.warn(
                    "AQE storage estimate is at or above 256 TiB. This is supported by AQE's bounds, but GUI/sync behavior should be tested before live use."
            );
        }
        if (effectiveThreads >= HIGH_COPROCESSOR_WARNING) {
            AdvancedQuantumEngineering.LOGGER.warn(
                    "AQE co-processor estimate is at or above 262,144. Large jobs may put pressure on AE2/Advanced AE progress sync even if TPS remains acceptable."
            );
        }
    }

    private static long safeAdd(long left, long right) {
        if (Long.MAX_VALUE - left < right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    private static long safeMultiply(long left, long right) {
        if (left <= 0 || right <= 0) {
            return 0;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }
}
