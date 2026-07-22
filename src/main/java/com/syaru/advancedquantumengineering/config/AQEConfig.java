package com.syaru.advancedquantumengineering.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public final class AQEConfig {
    public static final String CONFIG_FILE_NAME = "advanced_quantum_engineering.toml";
    public static final String LEGACY_CONFIG_FILE_NAME = "advanced_quantum_engineering-server.toml";
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
    /** ACOと共有する、構造全体の実効容量の10進桁上限。 */
    public static final int MAX_EFFECTIVE_BIG_INTEGER_DECIMAL_DIGITS = 16_384;
    /** 16,384桁を越えない構造全体の厳密な最大容量。 */
    public static final BigInteger MAX_BIG_INTEGER_VALUE = BigInteger.TEN
            .pow(MAX_EFFECTIVE_BIG_INTEGER_DECIMAL_DIGITS)
            .subtract(BigInteger.ONE);
    /** 厳密な最大容量を表現するために必要なbit数。 */
    public static final int MAX_BIG_INTEGER_BITS = MAX_BIG_INTEGER_VALUE.bitLength();
    public static final int MIN_BIG_INTEGER_DECIMAL_DIGITS = 20;
    /** Advanced AEの加算とData Entangler乗算へ12桁分を残す。 */
    public static final int BIG_INTEGER_STRUCTURE_HEADROOM_DECIMAL_DIGITS = 12;
    public static final int MAX_BIG_INTEGER_DECIMAL_DIGITS =
            MAX_EFFECTIVE_BIG_INTEGER_DECIMAL_DIGITS
                    - BIG_INTEGER_STRUCTURE_HEADROOM_DECIMAL_DIGITS;
    public static final int DEFAULT_BIG_INTEGER_DECIMAL_DIGITS = 64;
    public static final int DEFAULT_BIG_INTEGER_CORE_COPROCESSORS = MAX_SAFE_EFFECTIVE_COPROCESSORS;

    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.LongValue CORE_STORAGE;
    private static final ForgeConfigSpec.LongValue STORAGE_BLOCK_BYTES;
    private static final ForgeConfigSpec.IntValue BASE_COPROCESSORS;
    private static final ForgeConfigSpec.IntValue ACCELERATOR_THREADS;
    private static final ForgeConfigSpec.IntValue MULTI_THREADER_MULTIPLIER;
    private static final ForgeConfigSpec.IntValue DATA_ENTANGLER_MULTIPLIER;
    private static final ForgeConfigSpec.LongValue EXPERIMENTAL_CORE_STORAGE;
    private static final ForgeConfigSpec.IntValue EXPERIMENTAL_CORE_COPROCESSORS;
    private static final ForgeConfigSpec.IntValue BIG_INTEGER_CORE_DECIMAL_DIGITS;
    private static final ForgeConfigSpec.IntValue BIG_INTEGER_CORE_COPROCESSORS;
    private static final ForgeConfigSpec.BooleanValue FAIL_FAST_ON_INTEGRATION_MISMATCH;
    private static final ForgeConfigSpec.BooleanValue WARN_ON_EXTREME_CONFIG_VALUES;
    private static final ForgeConfigSpec.IntValue DIAGNOSTIC_MODIFIED_ACCELERATOR_COUNT;
    private static volatile int cachedBigIntegerDigits = -1;
    private static volatile BigInteger cachedBigIntegerStorage = BigInteger.ZERO;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("通常量子コンピュータの性能設定 / Standard Quantum Computer tuning")
                .push("quantum_computer");
        CORE_STORAGE = builder
                .comment("改造量子コアの基本ストレージ量（byte）")
                .defineInRange("core_storage_bytes", DEFAULT_CORE_STORAGE, MIN_STORAGE_BYTES, MAX_UNIT_STORAGE_BYTES);
        STORAGE_BLOCK_BYTES = builder
                .comment("改造量子ストレージ1個の容量（byte）")
                .defineInRange("storage_block_bytes", DEFAULT_STORAGE_BLOCK_BYTES, MIN_STORAGE_BYTES, MAX_UNIT_STORAGE_BYTES);
        BASE_COPROCESSORS = builder
                .comment("改造量子コアの基本コプロセッサ数")
                .defineInRange("core_coprocessors", DEFAULT_BASE_COPROCESSORS, MIN_COPROCESSORS, MAX_BASE_COPROCESSORS);
        ACCELERATOR_THREADS = builder
                .comment("改造量子アクセラレータ1個の処理スレッド数")
                .defineInRange("accelerator_threads", DEFAULT_ACCELERATOR_THREADS, MIN_COPROCESSORS, MAX_ACCELERATOR_THREADS);
        MULTI_THREADER_MULTIPLIER = builder
                .comment("改造マルチスレッダーのコプロセッサ倍率")
                .defineInRange("multi_threader_multiplier", DEFAULT_MULTI_THREADER_MULTIPLIER, MIN_COPROCESSORS, MAX_MULTI_THREADER_MULTIPLIER);
        DATA_ENTANGLER_MULTIPLIER = builder
                .comment("改造データエンタングラーのストレージ倍率")
                .defineInRange("data_entangler_multiplier", DEFAULT_DATA_ENTANGLER_MULTIPLIER, MIN_DATA_ENTANGLER_MULTIPLIER, MAX_DATA_ENTANGLER_MULTIPLIER);
        builder.pop();

        builder.comment("終盤量子コアの性能設定 / Endgame Quantum Core tuning")
                .push("endgame_cores");
        EXPERIMENTAL_CORE_STORAGE = builder
                .comment("long型量子コアの容量（byte）")
                .defineInRange("long_core_storage_bytes", DEFAULT_EXPERIMENTAL_CORE_STORAGE, MIN_STORAGE_BYTES, MAX_SAFE_EFFECTIVE_STORAGE_BYTES);
        EXPERIMENTAL_CORE_COPROCESSORS = builder
                .comment("long型量子コアのコプロセッサ数")
                .defineInRange("long_core_coprocessors", DEFAULT_EXPERIMENTAL_CORE_COPROCESSORS, MIN_COPROCESSORS, MAX_SAFE_EFFECTIVE_COPROCESSORS);
        BIG_INTEGER_CORE_DECIMAL_DIGITS = builder
                .comment(
                        "BigInteger量子コア容量の桁数。容量は 10^digits - 1 byte",
                        "構造全体の上限16384桁に対し、加算・Data Entangler用の12桁を予約")
                .defineInRange(
                        "big_integer_storage_digits",
                        DEFAULT_BIG_INTEGER_DECIMAL_DIGITS,
                        MIN_BIG_INTEGER_DECIMAL_DIGITS,
                        MAX_BIG_INTEGER_DECIMAL_DIGITS);
        BIG_INTEGER_CORE_COPROCESSORS = builder
                .comment("BigInteger量子コアのコプロセッサ数")
                .defineInRange(
                        "big_integer_coprocessors",
                        DEFAULT_BIG_INTEGER_CORE_COPROCESSORS,
                        MIN_COPROCESSORS,
                        MAX_SAFE_EFFECTIVE_COPROCESSORS);
        builder.pop();

        builder.comment("互換性検査と診断ログ / Safety and diagnostics")
                .push("safety_and_diagnostics");
        FAIL_FAST_ON_INTEGRATION_MISMATCH = builder
                .comment("Advanced AEの互換性不一致を検出した場合、起動を停止する")
                .define("fail_fast_on_integration_mismatch", true);
        WARN_ON_EXTREME_CONFIG_VALUES = builder
                .comment("極端な容量・コプロセッサ設定をログへ警告する")
                .define("warn_on_extreme_values", true);
        DIAGNOSTIC_MODIFIED_ACCELERATOR_COUNT = builder
                .comment("起動診断で計算に使う改造アクセラレータ数。ゲーム性能には影響しない")
                .defineInRange("diagnostic_accelerator_count", 121, 0, 512);
        builder.pop();

        SPEC = builder.build();
    }

    private AQEConfig() {
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, CONFIG_FILE_NAME);
    }

    public static boolean migrateLegacyServerConfig(MinecraftServer server) {
        Path legacyPath = server.getWorldPath(LevelResource.ROOT)
                .resolve("serverconfig")
                .resolve(LEGACY_CONFIG_FILE_NAME);
        if (!Files.isRegularFile(legacyPath)) {
            return false;
        }

        try (CommentedFileConfig legacy = CommentedFileConfig.of(legacyPath)) {
            legacy.load();
            migrateLong(legacy, "modifiedQuantumCore.coreStorage", CORE_STORAGE);
            migrateLong(legacy, "modifiedQuantumStorage.storageBlockBytes", STORAGE_BLOCK_BYTES);
            migrateInt(legacy, "modifiedQuantumCore.baseCoprocessors", BASE_COPROCESSORS);
            migrateInt(legacy, "modifiedQuantumAccelerator.acceleratorThreads", ACCELERATOR_THREADS);
            migrateInt(legacy, "modifiedQuantumMultiThreader.multiThreaderMultiplier", MULTI_THREADER_MULTIPLIER);
            migrateInt(legacy, "modifiedDataEntangler.dataEntanglerMultiplier", DATA_ENTANGLER_MULTIPLIER);
            migrateLong(legacy, "experimentalQuantumCore.experimentalCoreStorage", EXPERIMENTAL_CORE_STORAGE);
            migrateInt(legacy, "experimentalQuantumCore.experimentalCoreCoprocessors", EXPERIMENTAL_CORE_COPROCESSORS);
            migrateInt(legacy, "bigIntegerQuantumCore.storageDecimalDigits", BIG_INTEGER_CORE_DECIMAL_DIGITS);
            migrateInt(legacy, "bigIntegerQuantumCore.coprocessors", BIG_INTEGER_CORE_COPROCESSORS);
            migrateBoolean(legacy, "diagnostics.failFastOnIntegrationMismatch", FAIL_FAST_ON_INTEGRATION_MISMATCH);
            migrateBoolean(legacy, "diagnostics.warnOnExtremeConfigValues", WARN_ON_EXTREME_CONFIG_VALUES);
            migrateInt(legacy, "diagnostics.diagnosticModifiedAcceleratorCount", DIAGNOSTIC_MODIFIED_ACCELERATOR_COUNT);
        } catch (RuntimeException exception) {
            AdvancedQuantumEngineering.LOGGER.error("Failed to read legacy AQE config {}", legacyPath, exception);
            return false;
        }

        try {
            SPEC.save();
            Path migratedPath = legacyPath.resolveSibling(LEGACY_CONFIG_FILE_NAME + ".migrated");
            Files.move(legacyPath, migratedPath, StandardCopyOption.REPLACE_EXISTING);
            AdvancedQuantumEngineering.LOGGER.info("Migrated legacy AQE config to config/{}", CONFIG_FILE_NAME);
            return true;
        } catch (Exception exception) {
            AdvancedQuantumEngineering.LOGGER.error("AQE values were loaded, but the migrated config could not be saved", exception);
            return false;
        }
    }

    private static void migrateLong(CommentedFileConfig legacy, String path, ForgeConfigSpec.LongValue target) {
        Object value = legacy.get(path);
        if (value instanceof Number number) {
            target.set(number.longValue());
        }
    }

    private static void migrateInt(CommentedFileConfig legacy, String path, ForgeConfigSpec.IntValue target) {
        Object value = legacy.get(path);
        if (value instanceof Number number) {
            target.set(number.intValue());
        }
    }

    private static void migrateBoolean(CommentedFileConfig legacy, String path, ForgeConfigSpec.BooleanValue target) {
        Object value = legacy.get(path);
        if (value instanceof Boolean booleanValue) {
            target.set(booleanValue);
        }
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

    public static int getBigIntegerCoreStorageDecimalDigits() {
        return Math.min(
                MAX_BIG_INTEGER_DECIMAL_DIGITS,
                Math.max(MIN_BIG_INTEGER_DECIMAL_DIGITS, BIG_INTEGER_CORE_DECIMAL_DIGITS.get()));
    }

    public static BigInteger getBigIntegerCoreStorage() {
        int digits = getBigIntegerCoreStorageDecimalDigits();
        BigInteger cached = cachedBigIntegerStorage;
        if (cachedBigIntegerDigits == digits && cached.signum() > 0) {
            return cached;
        }
        synchronized (AQEConfig.class) {
            if (cachedBigIntegerDigits != digits || cachedBigIntegerStorage.signum() <= 0) {
                cachedBigIntegerStorage = BigInteger.TEN.pow(digits).subtract(BigInteger.ONE);
                cachedBigIntegerDigits = digits;
            }
            return cachedBigIntegerStorage;
        }
    }

    public static int getBigIntegerCoreCoprocessors() {
        return Math.min(
                MAX_SAFE_EFFECTIVE_COPROCESSORS,
                Math.max(MIN_COPROCESSORS, BIG_INTEGER_CORE_COPROCESSORS.get()));
    }

    public static int getMaxSingleUnitCoprocessors() {
        return Math.max(
                getBigIntegerCoreCoprocessors(),
                Math.max(
                        getExperimentalCoreCoprocessors(),
                        Math.max(getBaseCoprocessors(), getAcceleratorThreads())));
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
