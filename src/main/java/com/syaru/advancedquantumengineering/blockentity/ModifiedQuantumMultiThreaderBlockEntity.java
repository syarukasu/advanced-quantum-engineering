package com.syaru.advancedquantumengineering.blockentity;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import com.syaru.advancedquantumengineering.registry.AQEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;

public class ModifiedQuantumMultiThreaderBlockEntity extends AdvCraftingBlockEntity {
    public ModifiedQuantumMultiThreaderBlockEntity(BlockPos pos, BlockState state) {
        super(AQEBlockEntities.MODIFIED_QUANTUM_MULTI_THREADER.get(), pos, state);
    }

    @Override
    public int getAccelerationMultiplier() {
        return AQEConfig.getMultiThreaderMultiplier();
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return AQEItems.MODIFIED_QUANTUM_MULTI_THREADER.get();
    }
}
