package com.syaru.advancedquantumengineering.integration;

import java.math.BigInteger;

/** Implemented on Advanced AE Quantum Computer clusters by AQE's version-pinned mixin. */
public interface AQEBigIntegerCpuAccess {
    boolean aqe$hasBigIntegerQuantumCore();

    BigInteger aqe$getPhysicalCraftingCapacity();

    BigInteger aqe$getReservedCraftingCapacity();

    BigInteger aqe$getAvailableCraftingCapacity();

    BigIntegerCapacitySnapshot aqe$getCapacityDisplaySnapshot();

    String aqe$getBigIntegerBackendId();
}
