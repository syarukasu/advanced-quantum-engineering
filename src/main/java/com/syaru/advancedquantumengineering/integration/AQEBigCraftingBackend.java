package com.syaru.advancedquantumengineering.integration;

import java.math.BigInteger;
import net.minecraft.nbt.CompoundTag;

interface AQEBigCraftingBackend {
    boolean isAvailable();

    String id();

    AQEBigCraftingHost create(Object owner, BigInteger physicalCapacity, CompoundTag savedState);
}
