package com.syaru.advancedquantumengineering.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AQEHostRegistrationLifecycleTest {
    @AfterEach
    void cleanRegistry() {
        AQEHostRegistrationRegistry.resetForTests();
    }

    @Test
    void repeatedFormDestroyDoesNotRetainRegistrations() {
        for (int i = 0; i < 1_000; i++) {
            FakeHost host = new FakeHost(1);
            AQEHostRegistration registration = AQEHostRegistration.open(
                    new AQEHostOwnerToken(i + 1L, "level"), host);
            registration.close();
            assertEquals(1, host.closeCount);
        }

        assertEquals(0, AQEHostRegistrationRegistry.activeCount());
        assertEquals(1_000L, AQEHostRegistrationRegistry.registeredCount());
        assertEquals(1_000L, AQEHostRegistrationRegistry.closedCount());
        assertEquals(1_000L, AQEHostRegistrationRegistry.pendingJobsAtClose());
        assertEquals(1_000, AQEHostRegistrationRegistry.quarantineCount());
    }

    @Test
    void oldGenerationCannotCloseNewGeneration() {
        FakeHost oldHost = new FakeHost(1);
        FakeHost newHost = new FakeHost(1);
        AQEHostRegistration oldRegistration = AQEHostRegistration.open(
                new AQEHostOwnerToken(1L, "level"), oldHost);
        AQEHostRegistration newRegistration = AQEHostRegistration.open(
                new AQEHostOwnerToken(2L, "level"), newHost);

        oldRegistration.closeForReplacement();

        assertEquals(1, oldHost.closeCount);
        assertEquals(0, newHost.closeCount);
        assertEquals(1, AQEHostRegistrationRegistry.activeCount());
        newRegistration.close();
        assertTrue(AQEHostRegistrationRegistry.activeCount() == 0);
    }

    private static final class FakeHost implements AQEBigCraftingHost {
        private final int pendingJobs;
        private int closeCount;

        private FakeHost(int pendingJobs) {
            this.pendingJobs = pendingJobs;
        }

        @Override
        public void reconcile(BigInteger physicalCapacity, Map<UUID, BigInteger> standardJobReservations) {
        }

        @Override
        public BigInteger physicalCapacity() {
            return BigInteger.TEN;
        }

        @Override
        public BigInteger reserved() {
            return BigInteger.ONE;
        }

        @Override
        public BigInteger available() {
            return BigInteger.valueOf(9);
        }

        @Override
        public long availableAsSaturatedLong() {
            return 9;
        }

        @Override
        public int bigJobCount() {
            return pendingJobs;
        }

        @Override
        public String backendId() {
            return "test";
        }

        @Override
        public boolean hasPersistentState() {
            return true;
        }

        @Override
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("test", "preserved");
            return tag;
        }

        @Override
        public void close() {
            closeCount++;
        }
    }
}
