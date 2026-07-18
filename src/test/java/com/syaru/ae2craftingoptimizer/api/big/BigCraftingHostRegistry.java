package com.syaru.ae2craftingoptimizer.api.big;

import java.util.IdentityHashMap;
import java.util.Map;

/** Test-only optional API fixture. */
public final class BigCraftingHostRegistry {
    private static final Map<Object, BigCraftingHostRuntime> HOSTS = new IdentityHashMap<>();

    private BigCraftingHostRegistry() {
    }

    public static synchronized void register(Object owner, BigCraftingHostRuntime runtime) {
        HOSTS.put(owner, runtime);
    }

    public static synchronized void unregister(Object owner) {
        HOSTS.remove(owner);
    }

    public static synchronized BigCraftingHostRuntime find(Object owner) {
        return HOSTS.get(owner);
    }
}
