package com.syaru.advancedquantumengineering.integration;

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
        if (maximumBits < 1) {
            throw new IllegalArgumentException("maximumBits must be positive");
        }
        if (checked.signum() < 0 || checked.bitLength() > maximumBits) {
            throw new IllegalStateException(
                    name + " is negative or exceeds the " + maximumBits + "-bit safety limit");
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
