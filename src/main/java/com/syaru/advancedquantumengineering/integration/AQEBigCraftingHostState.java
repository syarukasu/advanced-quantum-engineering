package com.syaru.advancedquantumengineering.integration;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

/** Versioned envelope that AQE can preserve even when the optional backend is absent. */
final class AQEBigCraftingHostState {
    static final int SCHEMA_VERSION = 1;

    private AQEBigCraftingHostState() {
    }

    static CompoundTag encode(String backend, BigInteger backendReserved, CompoundTag payload) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", SCHEMA_VERSION);
        tag.putString("backend", requireBackend(backend));
        putCount(tag, "backendReserved", backendReserved);
        tag.put("payload", Objects.requireNonNull(payload, "payload").copy());
        return tag;
    }

    static Decoded decode(CompoundTag tag) {
        Objects.requireNonNull(tag, "tag");
        if (tag.getInt("schema") != SCHEMA_VERSION
                || !tag.contains("payload", Tag.TAG_COMPOUND)) {
            throw new IllegalArgumentException("unsupported AQE BigInteger host state schema");
        }
        return new Decoded(
                requireBackend(tag.getString("backend")),
                readCount(tag, "backendReserved"),
                tag.getCompound("payload").copy());
    }

    static boolean isPresent(CompoundTag tag) {
        return tag != null && !tag.isEmpty();
    }

    private static void putCount(CompoundTag tag, String key, BigInteger value) {
        BigInteger checked = checkedCount(value, key);
        tag.putByteArray(key, checked.toByteArray());
    }

    private static BigInteger readCount(CompoundTag tag, String key) {
        if (!tag.contains(key, Tag.TAG_BYTE_ARRAY)) {
            throw new IllegalArgumentException("missing AQE BigInteger count " + key);
        }
        byte[] encoded = tag.getByteArray(key);
        int maximumBytes = (AQEConfig.MAX_BIG_INTEGER_BITS + 8) / 8;
        if (encoded.length == 0 || encoded.length > maximumBytes) {
            throw new IllegalArgumentException("invalid AQE BigInteger count " + key);
        }
        BigInteger value = checkedCount(new BigInteger(encoded), key);
        if (!Arrays.equals(encoded, value.toByteArray())) {
            throw new IllegalArgumentException("non-canonical AQE BigInteger count " + key);
        }
        return value;
    }

    private static BigInteger checkedCount(BigInteger value, String name) {
        Objects.requireNonNull(value, name);
        // NBT復元時もbit境界だけでなく、16,384桁の厳密な最大値を検査する。
        if (value.signum() < 0
                || value.bitLength() > AQEConfig.MAX_BIG_INTEGER_BITS
                || value.compareTo(AQEConfig.MAX_BIG_INTEGER_VALUE) > 0) {
            throw new IllegalArgumentException(name + " is negative or exceeds AQE's BigInteger limit");
        }
        return value;
    }

    private static String requireBackend(String backend) {
        String checked = Objects.requireNonNull(backend, "backend").trim();
        if (checked.isEmpty() || checked.length() > 128) {
            throw new IllegalArgumentException("invalid AQE BigInteger backend id");
        }
        return checked;
    }

    record Decoded(String backend, BigInteger backendReserved, CompoundTag payload) {
        Decoded {
            requireBackend(backend);
            checkedCount(backendReserved, "backendReserved");
            Objects.requireNonNull(payload, "payload");
        }
    }
}
