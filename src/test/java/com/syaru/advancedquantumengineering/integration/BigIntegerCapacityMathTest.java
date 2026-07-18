package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class BigIntegerCapacityMathTest {
    @Test
    void keepsExactCapacityBeyondLongAcrossStorageMultiplier() {
        BigInteger core = BigInteger.TEN.pow(64).subtract(BigInteger.ONE);
        BigInteger total = BigIntegerCapacityMath.multiply(core, BigInteger.valueOf(8), "capacity", 256);

        assertEquals(core.multiply(BigInteger.valueOf(8)), total);
        assertEquals(Long.MAX_VALUE, BigIntegerCapacityMath.saturatedLong(total, 256));
    }

    @Test
    void keepsSmallLongFacadeExact() {
        assertEquals(4_096L, BigIntegerCapacityMath.saturatedLong(BigInteger.valueOf(4_096), 64));
    }

    @Test
    void rejectsNegativeAndOversizedIntermediates() {
        assertThrows(
                IllegalStateException.class,
                () -> BigIntegerCapacityMath.checkedNonNegative(BigInteger.valueOf(-1), "capacity", 64));
        assertThrows(
                IllegalStateException.class,
                () -> BigIntegerCapacityMath.multiply(
                        BigInteger.ONE.shiftLeft(63), BigInteger.TWO, "capacity", 64));
    }
}
