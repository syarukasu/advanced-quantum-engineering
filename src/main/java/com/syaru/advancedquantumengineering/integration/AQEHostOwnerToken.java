package com.syaru.advancedquantumengineering.integration;

import java.util.UUID;

/** A unique ACO owner for one AQE host generation. */
public final class AQEHostOwnerToken {
    private final long generation;
    private final Object lifecycleOwner;
    private final UUID identity = UUID.randomUUID();

    public AQEHostOwnerToken(long generation, Object lifecycleOwner) {
        this.generation = generation;
        this.lifecycleOwner = lifecycleOwner;
    }

    public long generation() {
        return generation;
    }

    public Object lifecycleOwner() {
        return lifecycleOwner;
    }

    public UUID identity() {
        return identity;
    }

    @Override
    public String toString() {
        return "AQEHostOwnerToken{" + generation + "," + identity + '}';
    }
}
