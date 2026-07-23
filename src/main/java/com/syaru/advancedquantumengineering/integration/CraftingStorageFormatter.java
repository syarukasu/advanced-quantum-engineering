package com.syaru.advancedquantumengineering.integration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Objects;

/** クラフトCPU容量を、long範囲では二進単位、超過後は指数表記へ整形する。 */
public final class CraftingStorageFormatter {
    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final int BINARY_UNIT_SHIFT = 10;
    private static final int BINARY_UNIT_COUNT = 7;
    private static final int BINARY_FRACTION_DIGITS = 3;
    private static final int SCIENTIFIC_SIGNIFICANT_DIGITS = 4;
    private static final String[] BINARY_UNITS = {"B", "k", "M", "G", "T", "P", "E"};

    private CraftingStorageFormatter() {
    }

    public static String format(long bytes) {
        UnitAmount amount = binaryAmount(bytes);
        return amount.number() + amount.unit();
    }

    public static String format(BigIntegerCapacitySnapshot.DisplayValue value) {
        BigIntegerCapacitySnapshot.DisplayValue checked = Objects.requireNonNull(value, "value");
        // 値全体が同期され、かつlongに収まる場合だけ高速な二進単位表示を使う。
        if (checked.isExact()) {
            BigInteger exact = new BigInteger(checked.leadingDigits());
            if (exact.compareTo(LONG_MAX) <= 0) {
                return format(exact.longValueExact());
            }
        }
        return scientific(checked);
    }

    public static UnitAmount binaryAmount(long bytes) {
        // 容量は非負値でなければならず、負値は上流のoverflowを示すため表示で隠さない。
        if (bytes < 0L) {
            throw new IllegalArgumentException("crafting storage cannot be negative");
        }

        int unitIndex = 0;
        long divisor = 1L;
        // 次の1024倍単位へ到達している間だけ、long範囲の最大Eまで繰り上げる。
        while (unitIndex + 1 < BINARY_UNIT_COUNT
                && bytes >= (divisor << BINARY_UNIT_SHIFT)) {
            divisor <<= BINARY_UNIT_SHIFT;
            unitIndex++;
        }

        // byte単位では小数化する必要がないため、元の整数をそのまま表示する。
        if (unitIndex == 0) {
            return new UnitAmount(Long.toString(bytes), BINARY_UNITS[unitIndex]);
        }

        BigDecimal scaled = BigDecimal.valueOf(bytes)
                .divide(BigDecimal.valueOf(divisor), BINARY_FRACTION_DIGITS, RoundingMode.DOWN)
                .stripTrailingZeros();
        return new UnitAmount(scaled.toPlainString(), BINARY_UNITS[unitIndex]);
    }

    private static String scientific(BigIntegerCapacitySnapshot.DisplayValue value) {
        String leading = value.leadingDigits();
        int significantLength = Math.min(SCIENTIFIC_SIGNIFICANT_DIGITS, leading.length());
        String significant = leading.substring(0, significantLength);
        String fractional = significant.substring(1);

        // 末尾0を除き、1.000のような不要な小数部をCPU一覧へ出さない。
        while (fractional.endsWith("0")) {
            fractional = fractional.substring(0, fractional.length() - 1);
        }

        String mantissa = fractional.isEmpty()
                ? significant.substring(0, 1)
                : significant.substring(0, 1) + "." + fractional;
        int exponent = value.decimalDigits() - 1;
        return mantissa + " \u00D7 10^" + exponent + " B";
    }

    public record UnitAmount(String number, String unit) {
        public UnitAmount {
            Objects.requireNonNull(number, "number");
            Objects.requireNonNull(unit, "unit");
        }
    }
}
