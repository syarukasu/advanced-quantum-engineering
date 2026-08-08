package com.syaru.advancedquantumengineering.integration;

import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/**
 * Fail-closed host for a payload that cannot currently be proven restorable.
 * It intentionally never decodes the backend payload and never frees capacity.
 */
final class PausedOpaqueBigCraftingHost implements AQEBigCraftingHost {
    enum FailureCategory {
        UNKNOWN_SCHEMA,
        MISSING_FIELD,
        NON_CANONICAL_COUNT,
        OVERSIZED_PAYLOAD,
        ACO_ABSENT,
        ACO_API_MISMATCH,
        BACKEND_DISABLED,
        RUNTIME_RESTORE_FAILURE,
        LINKAGE_ERROR
    }

    private final BigInteger physicalCapacity;
    private final CompoundTag originalPayload;
    private final FailureCategory category;
    private final BigInteger safeReservedProjection;
    private final String backendHint;
    private BigInteger standardReserved = BigInteger.ZERO;

    PausedOpaqueBigCraftingHost(
            BigInteger physicalCapacity,
            CompoundTag originalPayload,
            FailureCategory category) {
        this.physicalCapacity = Objects.requireNonNull(physicalCapacity, "physical capacity");
        this.originalPayload = Objects.requireNonNull(originalPayload, "original payload").copy();
        this.category = Objects.requireNonNull(category, "failure category");
        this.safeReservedProjection = AQEBigCraftingHostState
                .safeReservedProjection(this.originalPayload)
                .orElse(null);
        this.backendHint = AQEBigCraftingHostState.safeBackendHint(this.originalPayload);
    }

    @Override
    public synchronized void reconcile(
            BigInteger replacementCapacity,
            Map<UUID, BigInteger> standardJobReservations) {
        Objects.requireNonNull(replacementCapacity, "physical capacity");
        Objects.requireNonNull(standardJobReservations, "standard reservations");
        BigInteger total = BigInteger.ZERO;
        for (var entry : standardJobReservations.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue().signum() < 0) {
                continue;
            }
            total = total.add(entry.getValue());
        }
        standardReserved = total;
    }

    @Override
    public BigInteger physicalCapacity() {
        return physicalCapacity;
    }

    @Override
    public synchronized BigInteger reserved() {
        BigInteger preserved = safeReservedProjection == null
                ? physicalCapacity
                : safeReservedProjection;
        return preserved.max(standardReserved).max(physicalCapacity);
    }

    @Override
    public BigInteger available() {
        return BigInteger.ZERO;
    }

    @Override
    public long availableAsSaturatedLong() {
        return 0L;
    }

    @Override
    public synchronized AQEHostSnapshot snapshot(long revision) {
        return new AQEHostSnapshot(
                revision,
                physicalCapacity,
                reserved(),
                BigInteger.ZERO,
                0L,
                0L,
                0L,
                true,
                backendId());
    }

    @Override
    public String backendId() {
        return "aqe:paused_opaque/" + backendHint;
    }

    @Override
    public boolean hasPersistentState() {
        return true;
    }

    @Override
    public CompoundTag save() {
        return originalPayload.copy();
    }

    @Override
    public boolean isPaused() {
        return true;
    }

    @Override
    public String stateHint() {
        return category == FailureCategory.ACO_ABSENT
                || category == FailureCategory.ACO_API_MISMATCH
                || category == FailureCategory.BACKEND_DISABLED
                ? "PAUSED_INCOMPATIBLE"
                : "PAUSED_CORRUPT";
    }

    FailureCategory category() {
        return category;
    }

    @Override
    public void close() {
        // Opaque state remains owned by the block entity and is never discarded here.
    }
}
