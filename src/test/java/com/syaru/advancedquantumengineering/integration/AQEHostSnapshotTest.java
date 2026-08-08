package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class AQEHostSnapshotTest {
    @Test
    void oneSnapshotKeepsTheCapacityLedgerAtomic() {
        AQEHostSnapshot snapshot = new AQEHostSnapshot(
                4L,
                BigInteger.valueOf(100),
                BigInteger.valueOf(25),
                BigInteger.valueOf(75),
                2L,
                1L,
                3L,
                false,
                "ACTIVE");

        assertEquals(BigInteger.valueOf(75), snapshot.available());
        assertEquals(3L, snapshot.managedChildJobCount());
    }

    @Test
    void rejectsAnInconsistentSnapshot() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AQEHostSnapshot(
                        1L,
                        BigInteger.TEN,
                        BigInteger.valueOf(3),
                        BigInteger.valueOf(3),
                        0L,
                        0L,
                        0L,
                        false,
                        "ACTIVE"));
    }
}
