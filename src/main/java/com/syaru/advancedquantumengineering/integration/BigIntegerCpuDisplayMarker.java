package com.syaru.advancedquantumengineering.integration;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/** Carries the server-authoritative BigInteger digit count through AE2's existing CPU-name sync. */
public final class BigIntegerCpuDisplayMarker {
    private static final String PREFIX = "aqe:big_integer_capacity_digits=";
    private static final int MAX_COMPONENTS_TO_SCAN = 64;

    private BigIntegerCpuDisplayMarker() {
    }

    public static Component mark(Component name, int decimalDigits) {
        if (name == null) {
            return null;
        }
        int checkedDigits = checkedDigits(decimalDigits);
        if (readDecimalDigits(name) == checkedDigits) {
            return name;
        }

        MutableComponent marked = name.copy();
        marked.append(Component.empty().withStyle(style -> style.withInsertion(PREFIX + checkedDigits)));
        return marked;
    }

    public static int readDecimalDigits(Component component) {
        if (component == null) {
            return -1;
        }

        Deque<Component> pending = new ArrayDeque<>();
        pending.add(component);
        int scanned = 0;
        while (!pending.isEmpty() && scanned++ < MAX_COMPONENTS_TO_SCAN) {
            Component current = pending.removeFirst();
            String insertion = current.getStyle().getInsertion();
            if (insertion != null && insertion.startsWith(PREFIX)) {
                try {
                    return checkedDigits(Integer.parseInt(insertion.substring(PREFIX.length())));
                } catch (IllegalArgumentException ignored) {
                    return -1;
                }
            }
            pending.addAll(current.getSiblings());
        }
        return -1;
    }

    public static String formatCapacity(int decimalDigits) {
        return "10^" + checkedDigits(decimalDigits) + " - 1 B";
    }

    private static int checkedDigits(int decimalDigits) {
        if (decimalDigits < AQEConfig.MIN_BIG_INTEGER_DECIMAL_DIGITS
                || decimalDigits > AQEConfig.MAX_BIG_INTEGER_DECIMAL_DIGITS) {
            throw new IllegalArgumentException("BigInteger capacity digit count is outside AQE's supported range");
        }
        return decimalDigits;
    }
}
