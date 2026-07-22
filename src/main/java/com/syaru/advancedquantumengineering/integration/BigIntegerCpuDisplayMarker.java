package com.syaru.advancedquantumengineering.integration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** AE2既存のCPU名同期へ、固定長のサーバー権威容量スナップショットを埋め込む。 */
public final class BigIntegerCpuDisplayMarker {
    private static final String PREFIX = "aqe:big_integer_capacity_v2=";
    private static final String LEGACY_PREFIX = "aqe:big_integer_capacity_digits=";
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
            if (insertion != null && insertion.startsWith(PREFIX)) {
                Optional<BigIntegerCapacitySnapshot> decoded = BigIntegerCapacitySnapshot.decode(
                        insertion.substring(PREFIX.length()));
                if (decoded.isPresent()) {
                    latest = decoded;
                }
            }
            pending.addAll(current.getSiblings());
        }
        return latest;
    }

    public static MutableComponent formatValue(BigIntegerCapacitySnapshot.DisplayValue value) {
        // 19桁以内は省略せず、サーバーが扱っている値をそのまま表示する。
        if (value.isExact()) {
            return Component.translatable(
                    "gui.advanced_quantum_engineering.capacity.exact",
                    value.groupedLeadingDigits());
        }
        return Component.translatable(
                "gui.advanced_quantum_engineering.capacity.truncated",
                value.groupedLeadingDigits(),
                value.decimalDigits());
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
                formatValue(snapshot.total()));
    }

    private static MutableComponent formatCompactValue(BigIntegerCapacitySnapshot.DisplayValue value) {
        // 一覧内で12桁までの値は、読める長さを保ったまま正確に表示する。
        if (value.isExact() && value.decimalDigits() <= 12) {
            return formatValue(value);
        }
        return Component.translatable(
                "gui.advanced_quantum_engineering.capacity.truncated",
                value.firstGroupedDigits(),
                value.decimalDigits());
    }

    private static boolean isDirectCapacityMarker(Component component) {
        String insertion = component.getStyle().getInsertion();
        return insertion != null
                && (insertion.startsWith(PREFIX) || insertion.startsWith(LEGACY_PREFIX));
    }
}
