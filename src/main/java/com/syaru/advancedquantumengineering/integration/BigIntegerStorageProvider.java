package com.syaru.advancedquantumengineering.integration;

import java.math.BigInteger;

/** Storage contribution used by AQE's overflow-safe Advanced AE cluster calculation. */
public interface BigIntegerStorageProvider {
    BigInteger getBigIntegerStorageBytes();
}
