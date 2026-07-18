package com.syaru.advancedquantumengineering.mixin;

import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCluster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AdvCraftingCPU.class, remap = false)
public interface AdvCraftingCPUAccessor {
    @Accessor("cluster")
    AdvCraftingCPUCluster aqe$getCluster();
}
