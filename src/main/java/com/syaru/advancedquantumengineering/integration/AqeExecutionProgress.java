package com.syaru.advancedquantumengineering.integration;

import net.pedroksl.advanced_ae.common.logic.ElapsedTimeTracker;

/** UI projection of completed physical work; never used for quantity accounting. */
public final class AqeExecutionProgress extends ElapsedTimeTracker {
    private long elapsed;
    private long last;
    private int completed;
    private int total = 1;

    public AqeExecutionProgress(long elapsed) { this.elapsed = Math.max(0L, elapsed); }

    public void update(AqePhysicalExecution execution) {
        long now = System.nanoTime();
        if (last != 0L) elapsed += Math.max(0L, now - last);
        last = now;
        completed = execution.numerator();
        total = execution.denominator();
    }

    @Override public long getElapsedTime() { return elapsed; }
    @Override public float getProgress() { return (float) completed / total; }
    @Override public long getRemainingItemCount() { return total - completed; }
    @Override public long getStartItemCount() { return total; }
}
