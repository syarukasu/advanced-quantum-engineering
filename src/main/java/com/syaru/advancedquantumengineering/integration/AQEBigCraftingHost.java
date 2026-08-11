package com.syaru.advancedquantumengineering.integration;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/**
 * AQE-owned boundary; implementations may be local or supplied by optional ACO integration.
 *
 * <p>{@link #close()}は新しい会計変更と外部Registry登録を終了する。停止処理中の
 * 最終BlockEntity保存を壊さないよう、容量getter、{@link #snapshot(long)}、
 * {@link #save()}はclose後も最後の状態を読み取れる必要がある。</p>
 */
public interface AQEBigCraftingHost extends AutoCloseable {
    void reconcile(BigInteger physicalCapacity, Map<UUID, BigInteger> standardJobReservations);

    BigInteger physicalCapacity();

    BigInteger reserved();

    BigInteger available();

    long availableAsSaturatedLong();

    /** 同一会計世代の容量とジョブ件数を一つの表示用Snapshotへまとめる。 */
    default AQEHostSnapshot snapshot(long revision) {
        BigInteger total = physicalCapacity();
        BigInteger used = reserved();
        BigInteger available = available();
        return new AQEHostSnapshot(
                revision,
                total,
                used,
                available,
                0L,
                Math.max(0, bigJobCount()),
                Math.max(0, managedChildJobCount()),
                used.compareTo(total) > 0,
                backendId());
    }

    /** ACOが所有するBigInteger親Job数。任意Backendが未対応なら0を返す。 */
    default int bigJobCount() {
        return 0;
    }

    /** Big親JobのためにAdvanced AEへ委譲中の子Window数。 */
    default int managedChildJobCount() {
        return 0;
    }

    /** True while the host is preserving an unverified payload without accepting jobs. */
    default boolean isPaused() {
        return false;
    }

    /** Stable diagnostic state for UI and startup reports. */
    default String stateHint() {
        return isPaused() ? "PAUSED" : "ACTIVE";
    }

    String backendId();

    boolean hasPersistentState();

    CompoundTag save();

    @Override
    void close();
}
