package com.syaru.ae2craftingoptimizer.api.big;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/** Test-only optional API fixture with the exact reflected 1.3.0 method surface. */
public final class BigCraftingHostRuntime {
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private BigInteger physicalCapacity;
    private final Map<UUID, BigInteger> externalReservations = new LinkedHashMap<>();
    private BigInteger bigReserved;

    public BigCraftingHostRuntime(BigInteger physicalCapacity, BigInteger bigReserved) {
        this.physicalCapacity = physicalCapacity;
        this.bigReserved = bigReserved;
    }

    public void resizePhysicalCapacity(BigInteger replacement) {
        physicalCapacity = replacement;
    }

    public void replaceExternalReservations(Map<UUID, BigInteger> replacement) {
        externalReservations.clear();
        externalReservations.putAll(replacement);
    }

    public BigInteger physicalCapacity() {
        return physicalCapacity;
    }

    public BigInteger reserved() {
        return externalReservations.values().stream().reduce(bigReserved, BigInteger::add);
    }

    public BigInteger available() {
        return physicalCapacity.subtract(reserved()).max(BigInteger.ZERO);
    }

    public long availableAsSaturatedLong() {
        BigInteger available = available();
        return available.compareTo(LONG_MAX) >= 0 ? Long.MAX_VALUE : available.longValueExact();
    }

    public BigInteger bigReserved() {
        return bigReserved;
    }

    public void setBigReserved(BigInteger replacement) {
        bigReserved = replacement;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putByteArray("physicalCapacity", physicalCapacity.toByteArray());
        tag.putByteArray("bigReserved", bigReserved.toByteArray());
        return tag;
    }

    public static BigCraftingHostRuntime load(CompoundTag tag, BigInteger currentCapacity) {
        return new BigCraftingHostRuntime(
                currentCapacity,
                new BigInteger(tag.getByteArray("bigReserved")));
    }
}
