package com.syaru.advancedquantumengineering.integration;

import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;

/** Releases optional ACO hosts at the same lifecycle boundaries as the server. */
public final class AQEHostLifecycleEvents {
    private AQEHostLifecycleEvents() {
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        AQEHostRegistrationRegistry.closeForLifecycle(event.getLevel());
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        /*
         * ServerStoppingEventの後にもBlockEntityのNBT保存が行われる。
         * 最終保存より前にHostを閉じると、ACOの返却処理とAQEの保存処理が
         * 閉鎖済みHostを参照するため、全World保存が完了した境界で解放する。
         */
        AQEHostRegistrationRegistry.closeAll("server stopped");
    }
}
