package com.syaru.advancedquantumengineering.integration;

import java.util.concurrent.atomic.LongAdder;

/** Low-overhead counters for proving revision-based CPU/structure behavior. */
public final class AQERevisionMetrics {
    private static final LongAdder STRUCTURE_FULL_SCANS = new LongAdder();
    private static final LongAdder RESERVATION_FULL_REBUILDS = new LongAdder();
    private static final LongAdder HOST_SNAPSHOT_READS = new LongAdder();
    private static final LongAdder HOST_RECONCILES = new LongAdder();
    private static final LongAdder COPROCESSOR_HOT_PATH_REUSES = new LongAdder();
    private static final LongAdder PRESENTATION_REUSES = new LongAdder();

    private AQERevisionMetrics() {
    }

    public static void recordStructureScan() { STRUCTURE_FULL_SCANS.increment(); }
    public static void recordReservationRebuild() { RESERVATION_FULL_REBUILDS.increment(); }
    public static void recordHostSnapshotRead() { HOST_SNAPSHOT_READS.increment(); }
    public static void recordHostReconcile() { HOST_RECONCILES.increment(); }
    public static void recordCoprocessorHotPathReuse() { COPROCESSOR_HOT_PATH_REUSES.increment(); }
    public static void recordPresentationReuse() { PRESENTATION_REUSES.increment(); }

    public static String summary() {
        return "structure scans=" + STRUCTURE_FULL_SCANS.sum()
                + ", reservation rebuilds=" + RESERVATION_FULL_REBUILDS.sum()
                + ", host snapshots=" + HOST_SNAPSHOT_READS.sum()
                + ", host reconciles=" + HOST_RECONCILES.sum()
                + ", coprocessor reuses=" + COPROCESSOR_HOT_PATH_REUSES.sum()
                + ", presentation reuses=" + PRESENTATION_REUSES.sum();
    }
}
