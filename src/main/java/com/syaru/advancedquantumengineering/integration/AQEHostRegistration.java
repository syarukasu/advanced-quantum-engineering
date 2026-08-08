package com.syaru.advancedquantumengineering.integration;

import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.nbt.CompoundTag;

/** Owns one AQE-to-ACO host generation and closes it exactly once. */
public final class AQEHostRegistration implements AutoCloseable {
    private final AQEHostOwnerToken owner;
    private final AQEBigCraftingHost host;
    private final AtomicBoolean closed = new AtomicBoolean();

    private AQEHostRegistration(AQEHostOwnerToken owner, AQEBigCraftingHost host) {
        this.owner = owner;
        this.host = host;
    }

    public static AQEHostRegistration open(AQEHostOwnerToken owner, AQEBigCraftingHost host) {
        AQEHostRegistration registration = new AQEHostRegistration(owner, host);
        AQEHostRegistrationRegistry.register(registration);
        return registration;
    }

    public AQEHostOwnerToken owner() {
        return owner;
    }

    public AQEBigCraftingHost host() {
        return host;
    }

    public long generation() {
        return owner.generation();
    }

    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() {
        closeInternal(true);
    }

    /** Replacements already hand the saved state to the next generation. */
    public void closeForReplacement() {
        closeInternal(false);
    }

    private void closeInternal(boolean quarantine) {
        if (!closed.compareAndSet(false, true)) {
            AQEHostRegistrationRegistry.recordStaleClose();
            return;
        }

        int pendingJobs = pendingJobs(host);
        CompoundTag saved = new CompoundTag();
        if (quarantine && host.hasPersistentState()) {
            try {
                saved = host.save();
            } catch (RuntimeException | LinkageError failure) {
                AdvancedQuantumEngineering.LOGGER.error(
                        "AQE could not snapshot host {} before close; keeping lifecycle quarantine marker",
                        owner,
                        failure);
            }
        }
        if (quarantine && (!saved.isEmpty() || pendingJobs > 0)) {
            AQEHostRegistrationRegistry.quarantine(owner, saved, pendingJobs);
        }

        try {
            host.close();
        } catch (RuntimeException | LinkageError failure) {
            // A teardown callback must not abort cluster destruction or server shutdown.
            AdvancedQuantumEngineering.LOGGER.error(
                    "AQE host {} failed while closing; registration is still retired",
                    owner,
                    failure);
        } finally {
            AQEHostRegistrationRegistry.closed(this, pendingJobs);
        }
    }

    private static int pendingJobs(AQEBigCraftingHost host) {
        try {
            long total = (long) Math.max(0, host.bigJobCount())
                    + Math.max(0, host.managedChildJobCount());
            return total >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) total;
        } catch (RuntimeException | LinkageError failure) {
            return Integer.MAX_VALUE;
        }
    }
}
