package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import java.math.BigInteger;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class BigIntegerCapacitySnapshotTest {
    @Test
    void preservesLongRangeValuesExactly() {
        BigIntegerCapacitySnapshot snapshot = BigIntegerCapacitySnapshot.capture(
                BigInteger.valueOf(Long.MAX_VALUE),
                BigInteger.valueOf(1_234_567_890L),
                BigInteger.valueOf(Long.MAX_VALUE - 1_234_567_890L));

        assertTrue(snapshot.total().isExact());
        assertEquals("9,223,372,036,854,775,807", snapshot.total().groupedLeadingDigits());
        assertEquals("1,234,567,890", snapshot.used().groupedLeadingDigits());
        assertEquals(snapshot, BigIntegerCapacitySnapshot.decode(snapshot.encode()).orElseThrow());
    }

    @Test
    void preservesActiveAndBigIntegerJobCounts() {
        BigIntegerCapacitySnapshot snapshot = BigIntegerCapacitySnapshot.capture(
                BigInteger.TEN.pow(64).subtract(BigInteger.ONE),
                BigInteger.valueOf(123L),
                BigInteger.TEN.pow(64).subtract(BigInteger.valueOf(124L)),
                7,
                3);

        assertEquals(7, snapshot.activeJobs());
        assertEquals(3, snapshot.bigJobs());
        assertEquals(snapshot, BigIntegerCapacitySnapshot.decode(snapshot.encode()).orElseThrow());
    }

    @Test
    void decodesLegacyCapacityMarkersWithZeroJobCounts() {
        BigIntegerCapacitySnapshot current = BigIntegerCapacitySnapshot.capture(
                BigInteger.valueOf(1_000L),
                BigInteger.valueOf(250L),
                BigInteger.valueOf(750L));
        String legacy = String.join(
                ";",
                current.total().encode(),
                current.used().encode(),
                current.available().encode());

        BigIntegerCapacitySnapshot decoded =
                BigIntegerCapacitySnapshot.decode(legacy).orElseThrow();

        assertEquals(0, decoded.activeJobs());
        assertEquals(0, decoded.bigJobs());
    }

    @Test
    void representsActualHugeValuesInsteadOfConfiguredExponent() {
        BigInteger rawCore = BigInteger.TEN.pow(64).subtract(BigInteger.ONE);
        BigInteger physical = rawCore.multiply(BigInteger.valueOf(8L));
        BigInteger used = new BigInteger("123456789012345678901234567890");
        BigIntegerCapacitySnapshot snapshot = BigIntegerCapacitySnapshot.capture(
                physical,
                used,
                physical.subtract(used));

        assertEquals(65, snapshot.total().decimalDigits());
        assertEquals("79,999,999,999,999,999", snapshot.total().groupedLeadingDigits());
        assertEquals("79", snapshot.total().firstGroupedDigits());
        assertEquals(30, snapshot.used().decimalDigits());
        assertFalse(snapshot.used().isExact());
    }

    @Test
    void keepsMaximumMarkerLengthBounded() {
        BigInteger maximum = AQEConfig.MAX_BIG_INTEGER_VALUE;
        BigInteger used = maximum.shiftRight(1);
        BigIntegerCapacitySnapshot snapshot = BigIntegerCapacitySnapshot.capture(
                maximum,
                used,
                maximum.subtract(used));

        assertEquals(AQEConfig.MAX_EFFECTIVE_BIG_INTEGER_DECIMAL_DIGITS, snapshot.total().decimalDigits());
        assertTrue(snapshot.encode().length() <= 90);
        assertEquals(snapshot, BigIntegerCapacitySnapshot.decode(snapshot.encode()).orElseThrow());
    }

    @Test
    void rejectsMalformedAndOutOfRangeDisplayValues() {
        assertTrue(BigIntegerCapacitySnapshot.decode("64,999;0,0").isEmpty());
        assertTrue(BigIntegerCapacitySnapshot.DisplayValue.decode("64,not-a-number").isEmpty());
        assertThrows(
                IllegalArgumentException.class,
                () -> BigIntegerCapacitySnapshot.capture(
                        BigInteger.TEN.pow(AQEConfig.MAX_EFFECTIVE_BIG_INTEGER_DECIMAL_DIGITS),
                        BigInteger.ZERO,
                        BigInteger.ZERO));
    }

    @Test
    void replacesOldCapacityMarkerWithoutReadingStaleState() {
        BigIntegerCapacitySnapshot idle = BigIntegerCapacitySnapshot.capture(
                BigInteger.valueOf(1_000L),
                BigInteger.ZERO,
                BigInteger.valueOf(1_000L));
        BigIntegerCapacitySnapshot busy = BigIntegerCapacitySnapshot.capture(
                BigInteger.valueOf(1_000L),
                BigInteger.valueOf(600L),
                BigInteger.valueOf(400L));

        Component first = BigIntegerCpuDisplayMarker.mark(Component.literal("Quantum CPU"), idle);
        Component second = BigIntegerCpuDisplayMarker.mark(first, busy);

        assertEquals(busy, BigIntegerCpuDisplayMarker.readSnapshot(second).orElseThrow());
        assertEquals(1, second.getSiblings().size());
    }
}
