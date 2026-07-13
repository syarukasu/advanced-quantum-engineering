package com.syaru.advancedquantumengineering.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class AQEConfig {
    public static final long MIN_STORAGE_BYTES = 1L;
    public static final int MIN_COPROCESSORS = 1;
    public static final int MIN_DATA_ENTANGLER_MULTIPLIER = 1;
    public static final int MAX_DATA_ENTANGLER_MULTIPLIER = 64;
    public static final long MAX_UNIT_STORAGE_BYTES = Long.MAX_VALUE / (MAX_DATA_ENTANGLER_MULTIPLIER * 2L) / 2_048L;
    public static final long MAX_SAFE_EFFECTIVE_STORAGE_BYTES = Long.MAX_VALUE - 1L;
    public static final int MAX_SAFE_EFFECTIVE_COPROCESSORS = Integer.MAX_VALUE - 1;
    public static final long DEFAULT_CORE_STORAGE = 268_435_456L;
    public static final long DEFAULT_STORAGE_BLOCK_BYTES = MAX_UNIT_STORAGE_BYTES;
    public static final int DEFAULT_BASE_COPROCESSORS = 4_096;
    public static final int MAX_BASE_COPROCESSORS = 65_536;
    public static final int DEFAULT_ACCELERATOR_THREADS = 512;
    public static final int MAX_ACCELERATOR_THREADS = 4_096;
    public static final int DEFAULT_MULTI_THREADER_MULTIPLIER = 8;
    public static final int MAX_MULTI_THREADER_MULTIPLIER = 64;
    public static final int DEFAULT_DATA_ENTANGLER_MULTIPLIER = 8;
    public static final long DEFAULT_EXPERIMENTAL_CORE_STORAGE = MAX_SAFE_EFFECTIVE_STORAGE_BYTES;
    public static final int DEFAULT_EXPERIMENTAL_CORE_COPROCESSORS = MAX_SAFE_EFFECTIVE_COPROCESSORS;

    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.LongValue CORE_STORAGE;
    private static final ForgeConfigSpec.LongValue STORAGE_BLOCK_BYTES;
    private static final ForgeConfigSpec.IntValue BASE_COPROCESSORS;
    private static final ForgeConfigSpec.IntValue ACCELERATOR_THREADS;
    private static final ForgeConfigSpec.IntValue MULTI_THREADER_MULTIPLIER;
    private static final ForgeConfigSpec.IntValue DATA_ENTANGLER_MULTIPLIER;
    private static final ForgeConfigSpec.LongValue EXPERIMENTAL_CORE_STORAGE;
    private static final ForgeConfigSpec.IntValue EXPERIMENTAL_CORE_COPROCESSORS;
    private static final ForgeConfigSpec.BooleanValue FAIL_FAST_ON_INTEGRATION_MISMATCH;
    private static final ForgeConfigSpec.BooleanValue WARN_ON_EXTREME_CONFIG_VALUES;
    private static final ForgeConfigSpec.IntValue DIAGNOSTIC_MODIFIED_ACCELERATOR_COUNT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("modifiedQuantumCore");
        CORE_STORAGE = builder
                .comment(
                        "Base crafting storage in bytes supplied by the modified core.",
                        "Default: " + DEFAULT_CORE_STORAGE + " bytes (256 MiB).",
                        "Range: " + MIN_STORAGE_BYTES + " - " + MAX_UNIT_STORAGE_BYTES + " bytes.",
                        "Use the modified storage block for bulk capacity.")
                .defineInRange("coreStorage", DEFAULT_CORE_STORAGE, MIN_STORAGE_BYTES, MAX_UNIT_STORAGE_BYTES);
        BASE_COPROCESSORS = builder
                .comment(
                        "Base Advanced AE crafting threads exposed by the modified core.",
                        "Default: " + DEFAULT_BASE_COPROCESSORS + ".",
                        "Range: " + MIN_COPROCESSORS + " - " + MAX_BASE_COPROCESSORS + ".",
                        "Higher values increase crafting calculation load; the default assumes one primary Astral-scale modified Quantum Computer per server.")
                .defineInRange("baseCoprocessors", DEFAULT_BASE_COPROCESSORS, MIN_COPROCESSORS, MAX_BASE_COPROCESSORS);
        builder.pop();

        builder.push("modifiedQuantumStorage");
        STORAGE_BLOCK_BYTES = builder
                .comment(
                        "Crafting storage in bytes supplied by the modified Quantum Storage block.",
                        "Default: " + DEFAULT_STORAGE_BLOCK_BYTES + " bytes, the safe per-unit ceiling.",
                        "Range: " + MIN_STORAGE_BYTES + " - " + MAX_UNIT_STORAGE_BYTES + " bytes.",
                        "The default gives about 256 TiB with one modified Data Entangler.")
                .defineInRange("storageBlockBytes", DEFAULT_STORAGE_BLOCK_BYTES, MIN_STORAGE_BYTES, MAX_UNIT_STORAGE_BYTES);
        builder.pop();

        builder.push("modifiedQuantumAccelerator");
        ACCELERATOR_THREADS = builder
                .comment(
                        "Advanced AE crafting threads supplied by one modified Quantum Accelerator block.",
                        "Default: " + DEFAULT_ACCELERATOR_THREADS + ".",
                        "Range: " + MIN_COPROCESSORS + " - " + MAX_ACCELERATOR_THREADS + ".",
                        "The default is tuned for Astral Mekanism's 256-process top tier without using int-max values.")
                .defineInRange("acceleratorThreads", DEFAULT_ACCELERATOR_THREADS, MIN_COPROCESSORS, MAX_ACCELERATOR_THREADS);
        builder.pop();

        builder.push("modifiedQuantumMultiThreader");
        MULTI_THREADER_MULTIPLIER = builder
                .comment(
                        "Co-processor multiplier supplied by one modified Quantum Computer Multi-Threader.",
                        "Default: x" + DEFAULT_MULTI_THREADER_MULTIPLIER + ".",
                        "Range: " + MIN_COPROCESSORS + " - " + MAX_MULTI_THREADER_MULTIPLIER + ".",
                        "This occupies Advanced AE's normal Multi-Threader structure slot and does not add an extra slot.")
                .defineInRange("multiThreaderMultiplier", DEFAULT_MULTI_THREADER_MULTIPLIER, MIN_COPROCESSORS, MAX_MULTI_THREADER_MULTIPLIER);
        builder.pop();

        builder.push("modifiedDataEntangler");
        DATA_ENTANGLER_MULTIPLIER = builder
                .comment(
                        "Storage multiplier supplied by the modified Quantum Data Entangler.",
                        "Default: " + DEFAULT_DATA_ENTANGLER_MULTIPLIER + ".",
                        "Range: " + MIN_DATA_ENTANGLER_MULTIPLIER + " - " + MAX_DATA_ENTANGLER_MULTIPLIER + ".")
                .defineInRange("dataEntanglerMultiplier", DEFAULT_DATA_ENTANGLER_MULTIPLIER, MIN_DATA_ENTANGLER_MULTIPLIER, MAX_DATA_ENTANGLER_MULTIPLIER);
        builder.pop();

        builder.push("experimentalQuantumCore");
        EXPERIMENTAL_CORE_STORAGE = builder
                .comment(
                        "Experimental core storage in bytes.",
                        "Default: " + DEFAULT_EXPERIMENTAL_CORE_STORAGE + " bytes (Long.MAX_VALUE - 1).",
                        "Range: " + MIN_STORAGE_BYTES + " - " + MAX_SAFE_EFFECTIVE_STORAGE_BYTES + " bytes.",
                        "AQE clamps Advanced AE's effective storage calculation to the same ceiling to avoid overflow after additions or Data Entangler multipliers.")
                .defineInRange("experimentalCoreStorage", DEFAULT_EXPERIMENTAL_CORE_STORAGE, MIN_STORAGE_BYTES, MAX_SAFE_EFFECTIVE_STORAGE_BYTES);
        EXPERIMENTAL_CORE_COPROCESSORS = builder
                .comment(
                        "Experimental core co-processors.",
                        "Default: " + DEFAULT_EXPERIMENTAL_CORE_COPROCESSORS + " (Integer.MAX_VALUE - 1).",
                        "Range: " + MIN_COPROCESSORS + " - " + MAX_SAFE_EFFECTIVE_COPROCESSORS + ".",
                        "This allows AE2 to add one execution slot without integer overflow.")
                .defineInRange("experimentalCoreCoprocessors", DEFAULT_EXPERIMENTAL_CORE_COPROCESSORS, MIN_COPROCESSORS, MAX_SAFE_EFFECTIVE_COPROCESSORS);
        builder.pop();

        builder.push("diagnostics");
        FAIL_FAST_ON_INTEGRATION_MISMATCH = builder
                .comment("Crash during common setup if AQE can detect that Advanced AE integration no longer matches the expected API. This avoids silently loading fake-looking blocks after an incompatible Advanced AE update.")
                .define("failFastOnIntegrationMismatch", true);
        WARN_ON_EXTREME_CONFIG_VALUES = builder
                .comment("Log warnings for very high storage or co-processor settings. This does not change gameplay values.")
                .define("warnOnExtremeConfigValues", true);
        DIAGNOSTIC_MODIFIED_ACCELERATOR_COUNT = builder
                .comment("Only used for startup diagnostics to estimate a full modified Quantum Computer. It does not change structure rules or performance.")
                .defineInRange("diagnosticModifiedAcceleratorCount", 121, 0, 512);
        builder.pop();

        SPEC = builder.build();
    }

    private AQEConfig() {
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC);
    }

    public static long getCoreStorage() {
        return Math.min(MAX_UNIT_STORAGE_BYTES, Math.max(MIN_STORAGE_BYTES, CORE_STORAGE.get()));
    }

    public static long getStorageBlockBytes() {
        return Math.min(MAX_UNIT_STORAGE_BYTES, Math.max(MIN_STORAGE_BYTES, STORAGE_BLOCK_BYTES.get()));
    }

    public static int getBaseCoprocessors() {
        return Math.min(MAX_BASE_COPROCESSORS, Math.max(MIN_COPROCESSORS, BASE_COPROCESSORS.get()));
    }

    public static int getAcceleratorThreads() {
        return Math.min(MAX_ACCELERATOR_THREADS, Math.max(MIN_COPROCESSORS, ACCELERATOR_THREADS.get()));
    }

    public static int getMultiThreaderMultiplier() {
        return Math.min(MAX_MULTI_THREADER_MULTIPLIER, Math.max(MIN_COPROCESSORS, MULTI_THREADER_MULTIPLIER.get()));
    }

    public static int getDataEntanglerMultiplier() {
        return Math.min(MAX_DATA_ENTANGLER_MULTIPLIER, Math.max(MIN_DATA_ENTANGLER_MULTIPLIER, DATA_ENTANGLER_MULTIPLIER.get()));
    }

    public static long getExperimentalCoreStorage() {
        return Math.min(MAX_SAFE_EFFECTIVE_STORAGE_BYTES, Math.max(MIN_STORAGE_BYTES, EXPERIMENTAL_CORE_STORAGE.get()));
    }

    public static int getExperimentalCoreCoprocessors() {
        return Math.min(MAX_SAFE_EFFECTIVE_COPROCESSORS, Math.max(MIN_COPROCESSORS, EXPERIMENTAL_CORE_COPROCESSORS.get()));
    }

    public static int getMaxSingleUnitCoprocessors() {
        return Math.max(getExperimentalCoreCoprocessors(), Math.max(getBaseCoprocessors(), getAcceleratorThreads()));
    }

    public static boolean failFastOnIntegrationMismatch() {
        return FAIL_FAST_ON_INTEGRATION_MISMATCH.get();
    }

    public static boolean warnOnExtremeConfigValues() {
        return WARN_ON_EXTREME_CONFIG_VALUES.get();
    }

    public static int getDiagnosticModifiedAcceleratorCount() {
        return Math.min(512, Math.max(0, DIAGNOSTIC_MODIFIED_ACCELERATOR_COUNT.get()));
    }
}
