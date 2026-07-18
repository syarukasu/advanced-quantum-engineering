package com.syaru.ae2craftingoptimizer.api.big;

import com.syaru.ae2craftingoptimizer.engine.BigCraftingKeyCodec;
import java.math.BigInteger;
import net.minecraft.nbt.CompoundTag;

/** Test-only optional API fixture. */
public final class BigCraftingEngineApi {
    public static final int API_VERSION = 3;

    private BigCraftingEngineApi() {
    }

    public static boolean isEnabled() {
        return true;
    }

    public static <K> BigCraftingHostRuntime createHost(
            BigInteger capacity,
            BigCraftingKeyCodec<K> keyCodec) {
        return new BigCraftingHostRuntime(capacity, BigInteger.ZERO);
    }

    public static <K> BigCraftingHostRuntime loadHost(
            CompoundTag saved,
            BigInteger currentCapacity,
            BigCraftingKeyCodec<K> keyCodec) {
        return BigCraftingHostRuntime.load(saved, currentCapacity);
    }
}
