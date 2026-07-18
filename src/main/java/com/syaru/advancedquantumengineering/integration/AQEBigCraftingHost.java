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

    String backendId();

    boolean hasPersistentState();

    CompoundTag save();

    @Override
    void close();
}
