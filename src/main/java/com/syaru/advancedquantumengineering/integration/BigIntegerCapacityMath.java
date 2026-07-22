package com.syaru.advancedquantumengineering.integration;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import java.math.BigInteger;
import java.util.Objects;

/** Checked arithmetic shared by the Quantum Computer capacity bridge. */
public final class BigIntegerCapacityMath {
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private BigIntegerCapacityMath() {
    }

    public static BigInteger checkedNonNegative(
            BigInteger value,
            String name,
            int maximumBits) {
        BigInteger checked = Objects.requireNonNull(value, name);
        // 0以下のbit設定は上限検査として成立しないため拒否する。
        if (maximumBits < 1) {
            throw new IllegalArgumentException("maximumBits must be positive");
        }
        // 設定bit上限に加え、16,384桁の厳密な共通上限も必ず適用する。
        if (checked.signum() < 0
                || checked.bitLength() > maximumBits
                || checked.compareTo(AQEConfig.MAX_BIG_INTEGER_VALUE) > 0) {
            throw new IllegalStateException(
                    name + " is negative or exceeds AQE's BigInteger safety limit");
        }
        return checked;
    }

    public static BigInteger add(
            BigInteger left,
            BigInteger right,
            String name,
            int maximumBits) {
        checkedNonNegative(left, name + " left operand", maximumBits);
        checkedNonNegative(right, name + " right operand", maximumBits);
        return checkedNonNegative(left.add(right), name, maximumBits);
    }

    public static BigInteger multiply(
            BigInteger left,
            BigInteger right,
            String name,
            int maximumBits) {
        checkedNonNegative(left, name + " left operand", maximumBits);
        checkedNonNegative(right, name + " right operand", maximumBits);
        return checkedNonNegative(left.multiply(right), name, maximumBits);
    }

    public static long saturatedLong(BigInteger value, int maximumBits) {
        BigInteger checked = checkedNonNegative(value, "long facade value", maximumBits);
        return checked.compareTo(LONG_MAX) >= 0 ? Long.MAX_VALUE : checked.longValueExact();
    }
}
