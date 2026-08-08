package com.syaru.advancedquantumengineering.integration;

import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.nbt.CompoundTag;

/** Process-wide lifecycle ledger for AQE hosts. */
public final class AQEHostRegistrationRegistry {
    private static final Map<AQEHostOwnerToken, AQEHostRegistration> ACTIVE = new ConcurrentHashMap<>();
    private static final Map<java.util.UUID, CompoundTag> QUARANTINED = new ConcurrentHashMap<>();
    private static final AtomicLong REGISTERED = new AtomicLong();
    private static final AtomicLong CLOSED = new AtomicLong();
    private static final AtomicLong STALE_CLOSES = new AtomicLong();
    private static final AtomicLong PENDING_AT_CLOSE = new AtomicLong();

    private AQEHostRegistrationRegistry() {
    }

    static void register(AQEHostRegistration registration) {
        AQEHostRegistration previous = ACTIVE.putIfAbsent(registration.owner(), registration);
        if (previous != null) {
            throw new IllegalStateException("AQE host owner token was reused: " + registration.owner());
        }
        REGISTERED.incrementAndGet();
    }

    static void closed(AQEHostRegistration registration, int pendingJobs) {
        ACTIVE.remove(registration.owner(), registration);
        CLOSED.incrementAndGet();
        PENDING_AT_CLOSE.addAndGet(pendingJobs);
    }

    static void recordStaleClose() {
        STALE_CLOSES.incrementAndGet();
    }

    static void quarantine(AQEHostOwnerToken owner, CompoundTag saved, int pendingJobs) {
        CompoundTag copy = saved == null ? new CompoundTag() : saved.copy();
        if (!copy.isEmpty() || pendingJobs > 0) {
            QUARANTINED.put(owner.identity(), copy);
        }
    }

    public static int activeCount() {
        return ACTIVE.size();
    }

    public static long registeredCount() {
        return REGISTERED.get();
    }

    public static long closedCount() {
        return CLOSED.get();
    }

    public static long staleCloseCount() {
        return STALE_CLOSES.get();
    }

    public static long pendingJobsAtClose() {
        return PENDING_AT_CLOSE.get();
    }

    public static int quarantineCount() {
        return QUARANTINED.size();
    }

    public static void closeForLifecycle(Object lifecycleOwner) {
        if (lifecycleOwner == null) {
            return;
        }
        List<AQEHostRegistration> matches = ACTIVE.entrySet().stream()
                .filter(entry -> entry.getKey().lifecycleOwner() == lifecycleOwner)
                .map(Map.Entry::getValue)
                .toList();
        matches.forEach(AQEHostRegistration::close);
        logSummary("lifecycle unload");
    }

    public static void closeAll(String reason) {
        new ArrayList<>(ACTIVE.values()).forEach(AQEHostRegistration::close);
        logSummary(reason);
    }

    static void resetForTests() {
        ACTIVE.clear();
        QUARANTINED.clear();
        REGISTERED.set(0L);
        CLOSED.set(0L);
        STALE_CLOSES.set(0L);
        PENDING_AT_CLOSE.set(0L);
    }

    private static void logSummary(String reason) {
        AdvancedQuantumEngineering.LOGGER.info(
                "AQE host lifecycle {}: active={}, registered={}, closed={}, staleCloses={}, pendingJobsAtClose={}, quarantined={}",
                reason,
                activeCount(),
                registeredCount(),
                closedCount(),
                staleCloseCount(),
                pendingJobsAtClose(),
                quarantineCount());
    }
}
