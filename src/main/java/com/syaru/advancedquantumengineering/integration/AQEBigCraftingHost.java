package com.syaru.advancedquantumengineering.integration;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/** AQE-owned boundary; implementations may be local or supplied by optional ACO integration. */
public interface AQEBigCraftingHost extends AutoCloseable {
    void reconcile(BigInteger physicalCapacity, Map<UUID, BigInteger> standardJobReservations);

    BigInteger physicalCapacity();

    BigInteger reserved();

    BigInteger available();

    long availableAsSaturatedLong();

    /** Reads all capacity and job counters from one accounting revision. */
    default AQEHostSnapshot snapshot(long revision) {
        BigInteger total = physicalCapacity();
        BigInteger used = reserved();
        BigInteger available = available();
        return new AQEHostSnapshot(
                revision,
                total,
                used,
                available,
                0L,
                Math.max(0, bigJobCount()),
                Math.max(0, managedChildJobCount()),
                used.compareTo(total) > 0,
                backendId());
    }

    /** ACOが所有するBigInteger親Job数。任意Backendが未対応なら0を返す。 */
    default int bigJobCount() {
        return 0;
    }

    /** Big親JobのためにAdvanced AEへ委譲中の子Window数。 */
    default int managedChildJobCount() {
        return 0;
    }

    String backendId();

    boolean hasPersistentState();

    CompoundTag save();

    @Override
    void close();
}
