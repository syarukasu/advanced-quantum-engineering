package com.syaru.advancedquantumengineering.integration;

import java.math.BigInteger;
import java.util.Objects;

/** One atomic capacity/accounting view supplied by ACO or the local fallback. */
public record AQEHostSnapshot(
        long revision,
        BigInteger physicalCapacity,
        BigInteger reserved,
        BigInteger available,
        long standardJobCount,
        long bigJobCount,
        long managedChildJobCount,
        boolean overcommitted,
        String backendState) {
    public AQEHostSnapshot {
        if (revision < 0L) {
            throw new IllegalArgumentException("host snapshot revision must not be negative");
        }
        Objects.requireNonNull(physicalCapacity, "physicalCapacity");
        Objects.requireNonNull(reserved, "reserved");
        Objects.requireNonNull(available, "available");
        Objects.requireNonNull(backendState, "backendState");
        if (physicalCapacity.signum() < 0 || reserved.signum() < 0 || available.signum() < 0
                || standardJobCount < 0L || bigJobCount < 0L || managedChildJobCount < 0L) {
            throw new IllegalArgumentException("host snapshot contains a negative value");
        }
        if (overcommitted) {
            if (available.signum() != 0 || reserved.compareTo(physicalCapacity) <= 0) {
                throw new IllegalArgumentException("invalid overcommitted host snapshot");
            }
        } else if (!physicalCapacity.equals(reserved.add(available))) {
            throw new IllegalArgumentException("inconsistent host snapshot ledger");
        }
    }
}
