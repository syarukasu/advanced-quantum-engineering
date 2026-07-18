package com.syaru.advancedquantumengineering.mixin;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.menu.me.crafting.CraftingCPURecord;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CraftingCPURecord.class, remap = false)
public interface CraftingCPURecordAccessor {
    @Accessor("cpu")
    ICraftingCPU aqe$getCpu();
}
