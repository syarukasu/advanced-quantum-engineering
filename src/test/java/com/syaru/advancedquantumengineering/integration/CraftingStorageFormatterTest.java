package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class CraftingStorageFormatterTest {
    @Test
    void promotesLongValuesThroughExabytes() {
        assertEquals("0B", CraftingStorageFormatter.format(0L));
        assertEquals("1023B", CraftingStorageFormatter.format(1023L));
        assertEquals("1k", CraftingStorageFormatter.format(1L << 10));
        assertEquals("1M", CraftingStorageFormatter.format(1L << 20));
        assertEquals("1G", CraftingStorageFormatter.format(1L << 30));
        assertEquals("1T", CraftingStorageFormatter.format(1L << 40));
        assertEquals("1P", CraftingStorageFormatter.format(1L << 50));
        assertEquals("1E", CraftingStorageFormatter.format(1L << 60));
        assertEquals("7.999E", CraftingStorageFormatter.format(Long.MAX_VALUE));
    }

    @Test
    void keepsUsefulFractionalPrecisionWithoutRoundingUp() {
        assertEquals("1.5k", CraftingStorageFormatter.format(1536L));
        assertEquals("1.999M", CraftingStorageFormatter.format((2L << 20) - 1L));
    }

    @Test
    void switchesToScientificNotationAboveLongMax() {
        BigInteger justAboveLong = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE);
        BigInteger sixtyFourDigits = BigInteger.TEN.pow(64).subtract(BigInteger.ONE);

        assertEquals(
                "9.223 \u00D7 10^18 B",
                CraftingStorageFormatter.format(BigIntegerCapacitySnapshot.DisplayValue.capture(justAboveLong)));
        assertEquals(
                "9.999 \u00D7 10^63 B",
                CraftingStorageFormatter.format(BigIntegerCapacitySnapshot.DisplayValue.capture(sixtyFourDigits)));
    }

    @Test
    void rejectsNegativeLongCapacity() {
        assertThrows(IllegalArgumentException.class, () -> CraftingStorageFormatter.format(-1L));
    }
}
