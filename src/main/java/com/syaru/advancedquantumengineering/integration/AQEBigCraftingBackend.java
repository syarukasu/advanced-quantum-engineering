package com.syaru.advancedquantumengineering.integration;

import java.math.BigInteger;
import net.minecraft.nbt.CompoundTag;

interface AQEBigCraftingBackend {
    boolean isAvailable();

    String id();

    /**
     * Creates a host with separate lifecycle and backend registry owners.
     *
     * <p>AQE uses a generation token for its own replacement and quarantine
     * bookkeeping, while ACO indexes the host by the actual Advanced AE CPU
     * cluster instance. Keeping those identities separate avoids a false
     * CPU_TOO_SMALL result when the physical capacity is valid.</p>
     */
    AQEBigCraftingHost create(
            Object lifecycleOwner,
            Object backendRegistryOwner,
            BigInteger physicalCapacity,
            CompoundTag savedState);
}
