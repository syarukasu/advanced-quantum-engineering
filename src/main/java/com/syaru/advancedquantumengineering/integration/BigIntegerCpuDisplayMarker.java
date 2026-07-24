package com.syaru.advancedquantumengineering.integration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** AE2既存のCPU名同期へ、固定長のサーバー権威容量スナップショットを埋め込む。 */
public final class BigIntegerCpuDisplayMarker {
    private static final String PREFIX = "aqe:big_integer_capacity_v3=";
    private static final String LEGACY_CAPACITY_PREFIX = "aqe:big_integer_capacity_v2=";
    private static final String LEGACY_DIGITS_PREFIX = "aqe:big_integer_capacity_digits=";
    private static final int MAX_COMPONENTS_TO_SCAN = 64;

    private BigIntegerCpuDisplayMarker() {
    }

    public static Component mark(Component name, BigIntegerCapacitySnapshot snapshot) {
        // CPU名が存在しない場合は、呼び出し側が用意する代替名を優先する。
        if (name == null) {
            return null;
        }
        String encoded = snapshot.encode();
        // 同じサーバースナップショットが既に付いていればComponentを増殖させない。
        if (readSnapshot(name).filter(snapshot::equals).isPresent()) {
            return name;
        }

        MutableComponent marked = name.copy();
        // AQEが直接付けた旧マーカーを除去し、容量更新のたびに兄弟Componentが増えないようにする。
        marked.getSiblings().removeIf(BigIntegerCpuDisplayMarker::isDirectCapacityMarker);
        marked.append(Component.empty().withStyle(style -> style.withInsertion(PREFIX + encoded)));
        return marked;
    }

    public static Optional<BigIntegerCapacitySnapshot> readSnapshot(Component component) {
        // マーカーがない通常CPUは、AQE側で表示を上書きしない。
        if (component == null) {
            return Optional.empty();
        }

        Deque<Component> pending = new ArrayDeque<>();
        pending.add(component);
        int scanned = 0;
        Optional<BigIntegerCapacitySnapshot> latest = Optional.empty();
        // 悪意ある深いComponentツリーでクライアント表示処理が無制限にならないよう上限を設ける。
        while (!pending.isEmpty() && scanned++ < MAX_COMPONENTS_TO_SCAN) {
            Component current = pending.removeFirst();
            String insertion = current.getStyle().getInsertion();
            // 重複マーカーが残る旧Componentでは、後から追加された最新の正常値を採用する。
            String payload = capacityPayload(insertion);
            // v3または互換v2マーカーだけを固定長Snapshotとして復号する。
            if (payload != null) {
                Optional<BigIntegerCapacitySnapshot> decoded =
                        BigIntegerCapacitySnapshot.decode(payload);
                if (decoded.isPresent()) {
                    latest = decoded;
                }
            }
            pending.addAll(current.getSiblings());
        }
        return latest;
    }

    public static MutableComponent formatValue(BigIntegerCapacitySnapshot.DisplayValue value) {
        // long範囲は二進単位へ繰り上げ、超過値は実際の先頭桁を使う指数表記へ切り替える。
        return Component.literal(CraftingStorageFormatter.format(value));
    }

    public static MutableComponent formatUsed(BigIntegerCapacitySnapshot snapshot) {
        return Component.translatable(
                "gui.advanced_quantum_engineering.capacity.used",
                formatValue(snapshot.used()));
    }

    public static MutableComponent formatCompactUsed(BigIntegerCapacitySnapshot snapshot) {
        return Component.translatable(
                "gui.advanced_quantum_engineering.capacity.used",
                formatCompactValue(snapshot.used()));
    }

    public static MutableComponent formatAvailable(BigIntegerCapacitySnapshot snapshot) {
        return Component.translatable(
                "gui.advanced_quantum_engineering.capacity.available",
                formatValue(snapshot.available()));
    }

    public static MutableComponent formatLiveSummary(BigIntegerCapacitySnapshot snapshot) {
        return Component.translatable(
                "gui.advanced_quantum_engineering.capacity.live_summary",
                formatValue(snapshot.used()),
                formatValue(snapshot.total()),
                snapshot.activeJobs(),
                snapshot.bigJobs());
    }

    public static MutableComponent formatJobs(BigIntegerCapacitySnapshot snapshot) {
        return Component.translatable(
                "gui.advanced_quantum_engineering.capacity.jobs",
                snapshot.activeJobs(),
                snapshot.bigJobs());
    }

    private static MutableComponent formatCompactValue(BigIntegerCapacitySnapshot.DisplayValue value) {
        // 通常単位と指数表記のどちらもCPU一覧へ収まる固定長なので、Tooltipと同じ表記を使う。
        return formatValue(value);
    }

    private static boolean isDirectCapacityMarker(Component component) {
        String insertion = component.getStyle().getInsertion();
        return insertion != null
                && (insertion.startsWith(PREFIX)
                        || insertion.startsWith(LEGACY_CAPACITY_PREFIX)
                        || insertion.startsWith(LEGACY_DIGITS_PREFIX));
    }

    private static String capacityPayload(String insertion) {
        // 現行v3を優先し、v2はJob件数0としてBigIntegerCapacitySnapshot側で移行する。
        if (insertion != null && insertion.startsWith(PREFIX)) {
            return insertion.substring(PREFIX.length());
        }
        if (insertion != null && insertion.startsWith(LEGACY_CAPACITY_PREFIX)) {
            return insertion.substring(LEGACY_CAPACITY_PREFIX.length());
        }
        return null;
    }
}
