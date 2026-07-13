package com.syaru.advancedquantumengineering.blockentity;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import com.syaru.advancedquantumengineering.registry.AQEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;

public class ExperimentalQuantumCoreBlockEntity extends AdvCraftingBlockEntity {
    public ExperimentalQuantumCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AQEBlockEntities.EXPERIMENTAL_QUANTUM_CORE.get(), pos, state);
    }

    @Override
    public long getStorageBytes() {
        return AQEConfig.getExperimentalCoreStorage();
    }

    @Override
    public int getAcceleratorThreads() {
        return AQEConfig.getExperimentalCoreCoprocessors();
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return AQEItems.EXPERIMENTAL_QUANTUM_CORE.get();
    }
}

