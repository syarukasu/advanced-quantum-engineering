package com.syaru.advancedquantumengineering.integration;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;

/**
 * 量子コンピュータの容量Ledgerを、桁数に比例しない固定長の表示情報へ変換する。
 * クラフト判定には使わず、サーバーからクライアントへの表示同期専用とする。
 */
public record BigIntegerCapacitySnapshot(
        DisplayValue total,
        DisplayValue used,
        DisplayValue available) {
    private static final String VALUE_SEPARATOR = ";";

    public BigIntegerCapacitySnapshot {
        Objects.requireNonNull(total, "total");
        Objects.requireNonNull(used, "used");
        Objects.requireNonNull(available, "available");
    }

    public static BigIntegerCapacitySnapshot capture(
            BigInteger total,
            BigInteger used,
            BigInteger available) {
        return new BigIntegerCapacitySnapshot(
                DisplayValue.capture(total),
                DisplayValue.capture(used),
                DisplayValue.capture(available));
    }

    public static BigIntegerCapacitySnapshot zero() {
        DisplayValue zero = DisplayValue.capture(BigInteger.ZERO);
        return new BigIntegerCapacitySnapshot(zero, zero, zero);
    }

    public String encode() {
        return total.encode()
                + VALUE_SEPARATOR
                + used.encode()
                + VALUE_SEPARATOR
                + available.encode();
    }

    public static Optional<BigIntegerCapacitySnapshot> decode(String encoded) {
        // 表示マーカー以外の挿入文字列や、欠損した古いマーカーは拒否する。
        if (encoded == null || encoded.isBlank()) {
            return Optional.empty();
        }

        String[] values = encoded.split(VALUE_SEPARATOR, -1);
        // 総容量・使用中・空き容量の三値がそろった場合だけ採用する。
        if (values.length != 3) {
            return Optional.empty();
        }

        Optional<DisplayValue> total = DisplayValue.decode(values[0]);
        Optional<DisplayValue> used = DisplayValue.decode(values[1]);
        Optional<DisplayValue> available = DisplayValue.decode(values[2]);
        // どれか一つでも壊れていれば、部分的な値を画面へ出さずAE2表示へ戻す。
        if (total.isEmpty() || used.isEmpty() || available.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new BigIntegerCapacitySnapshot(
                total.orElseThrow(),
                used.orElseThrow(),
                available.orElseThrow()));
    }

    /** 最大19桁だけを保持し、long範囲は正確に、それ以上は固定長で表す。 */
    public record DisplayValue(int decimalDigits, String leadingDigits) {
        private static final String FIELD_SEPARATOR = ",";
        private static final int MAX_LEADING_DIGITS = 19;

        public DisplayValue {
            // AQEが扱えるBigInteger上限外の表示値は、パケットへ載せない。
            if (decimalDigits < 1
                    || decimalDigits > AQEConfig.MAX_EFFECTIVE_BIG_INTEGER_DECIMAL_DIGITS) {
                throw new IllegalArgumentException("capacity display digit count is outside AQE's limit");
            }
            Objects.requireNonNull(leadingDigits, "leadingDigits");
            int expectedLength = expectedLeadingLength(decimalDigits);
            // 桁数と先頭値の長さが一致しないデータは、切り詰め位置を復元できない。
            if (leadingDigits.length() != expectedLength
                    || !leadingDigits.chars().allMatch(Character::isDigit)) {
                throw new IllegalArgumentException("capacity display leading digits are malformed");
            }
            // 0以外の値で先頭0を許すと、表示桁数と実際の桁数が食い違う。
            if (decimalDigits > 1 && leadingDigits.charAt(0) == '0') {
                throw new IllegalArgumentException("capacity display value has a leading zero");
            }
        }

        public static DisplayValue capture(BigInteger value) {
            BigInteger checked = Objects.requireNonNull(value, "value");
            // 容量Ledgerは非負値だけを扱うため、負数は表示で隠さず拒否する。
            if (checked.signum() < 0) {
                throw new IllegalArgumentException("capacity display value is negative");
            }

            String decimal = checked.toString();
            // 巨大な保存値が表示経路だけを通って上限を越えないようにする。
            if (decimal.length() > AQEConfig.MAX_EFFECTIVE_BIG_INTEGER_DECIMAL_DIGITS) {
                throw new IllegalArgumentException("capacity display value exceeds AQE's limit");
            }
            int leadingLength = expectedLeadingLength(decimal.length());
            return new DisplayValue(decimal.length(), decimal.substring(0, leadingLength));
        }

        public boolean isExact() {
            return decimalDigits <= MAX_LEADING_DIGITS;
        }

        public String encode() {
            return decimalDigits + FIELD_SEPARATOR + leadingDigits;
        }

        public static Optional<DisplayValue> decode(String encoded) {
            // 区切りが一つだけの正規形以外は、曖昧に解釈しない。
            if (encoded == null) {
                return Optional.empty();
            }
            int separator = encoded.indexOf(FIELD_SEPARATOR);
            if (separator <= 0 || separator != encoded.lastIndexOf(FIELD_SEPARATOR)) {
                return Optional.empty();
            }

            try {
                int digits = Integer.parseInt(encoded.substring(0, separator));
                return Optional.of(new DisplayValue(digits, encoded.substring(separator + 1)));
            } catch (IllegalArgumentException ignored) {
                return Optional.empty();
            }
        }

        public String groupedLeadingDigits() {
            StringBuilder grouped = new StringBuilder(leadingDigits.length() + 8);
            int firstGroupLength = decimalDigits % 3;
            // 3桁区切りが値全体の位置と一致するよう、先頭グループだけ長さを調整する。
            if (firstGroupLength == 0) {
                firstGroupLength = 3;
            }

            for (int index = 0; index < leadingDigits.length(); index++) {
                // 値全体での3桁境界に到達した時だけカンマを追加する。
                if (index > 0 && (index - firstGroupLength) % 3 == 0) {
                    grouped.append(',');
                }
                grouped.append(leadingDigits.charAt(index));
            }
            return grouped.toString();
        }

        public String firstGroupedDigits() {
            int firstGroupLength = decimalDigits % 3;
            // 3の倍数桁では、先頭グループも通常どおり3桁になる。
            if (firstGroupLength == 0) {
                firstGroupLength = 3;
            }
            return leadingDigits.substring(0, Math.min(firstGroupLength, leadingDigits.length()));
        }

        private static int expectedLeadingLength(int decimalDigits) {
            // long範囲を含む19桁以内は、値を省略せずそのまま同期する。
            if (decimalDigits <= MAX_LEADING_DIGITS) {
                return decimalDigits;
            }
            int firstGroupLength = decimalDigits % 3;
            // 省略値は末尾が3桁グループの途中にならない長さへそろえる。
            if (firstGroupLength == 0) {
                firstGroupLength = 3;
            }
            return firstGroupLength
                    + ((MAX_LEADING_DIGITS - firstGroupLength) / 3) * 3;
        }
    }
}
