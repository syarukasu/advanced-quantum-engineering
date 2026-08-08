package com.syaru.advancedquantumengineering.integration;

import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;

/** Releases optional ACO hosts at the same lifecycle boundaries as the server. */
public final class AQEHostLifecycleEvents {
    private AQEHostLifecycleEvents() {
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        AQEHostRegistrationRegistry.closeForLifecycle(event.getLevel());
    }

    public static void onServerStopping(ServerStoppingEvent event) {
        AQEHostRegistrationRegistry.closeAll("server stopping");
    }
}
