package com.syaru.advancedquantumengineering.integration;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/** Saturating long-compatible fallback used when ACO is absent or disabled. */
final class LocalBigCraftingHost implements AQEBigCraftingHost {
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private BigInteger physicalCapacity;
    private BigInteger standardReserved = BigInteger.ZERO;
    private final BigInteger deferredBackendReserved;
    private final CompoundTag deferredState;

    LocalBigCraftingHost(BigInteger physicalCapacity, CompoundTag savedState) {
        this.physicalCapacity = checked(physicalCapacity, "physical capacity");
        if (AQEBigCraftingHostState.isPresent(savedState)) {
            AQEBigCraftingHostState.Decoded decoded = AQEBigCraftingHostState.decode(savedState);
            this.deferredBackendReserved = decoded.backendReserved();
            this.deferredState = savedState.copy();
        } else {
            this.deferredBackendReserved = BigInteger.ZERO;
            this.deferredState = null;
        }
    }

    @Override
    public synchronized void reconcile(
            BigInteger replacementCapacity,
            Map<UUID, BigInteger> standardJobReservations) {
        physicalCapacity = checked(replacementCapacity, "physical capacity");
        Objects.requireNonNull(standardJobReservations, "standardJobReservations");
        BigInteger total = BigInteger.ZERO;
        for (var entry : standardJobReservations.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "standard job id");
            BigInteger amount = checked(entry.getValue(), "standard reservation");
            total = checked(total.add(amount), "standard reservation total");
        }
        standardReserved = total;
    }

    @Override
    public synchronized BigInteger physicalCapacity() {
        return physicalCapacity;
    }

    @Override
    public synchronized BigInteger reserved() {
        return checked(standardReserved.add(deferredBackendReserved), "reserved capacity");
    }

    @Override
    public synchronized BigInteger available() {
        BigInteger available = physicalCapacity.subtract(reserved());
        return available.signum() < 0 ? BigInteger.ZERO : available;
    }

    @Override
    public synchronized long availableAsSaturatedLong() {
        BigInteger available = available();
        return available.compareTo(LONG_MAX) >= 0 ? Long.MAX_VALUE : available.longValueExact();
    }

    @Override
    public String backendId() {
        return deferredState == null ? "aqe:long_fallback" : "aqe:paused_optional_backend";
    }

    @Override
    public boolean hasPersistentState() {
        return deferredState != null;
    }

    @Override
    public CompoundTag save() {
        return deferredState == null ? new CompoundTag() : deferredState.copy();
    }

    @Override
    public void close() {
    }

    private static BigInteger checked(BigInteger value, String name) {
        Objects.requireNonNull(value, name);
        // ACO未導入時も、AQEとACOで共有する16,384桁上限を越える値は保持しない。
        if (value.signum() < 0
                || value.bitLength() > AQEConfig.MAX_BIG_INTEGER_BITS
                || value.compareTo(AQEConfig.MAX_BIG_INTEGER_VALUE) > 0) {
            throw new IllegalArgumentException(name + " is negative or exceeds AQE's BigInteger limit");
        }
        return value;
    }
}
