package com.syaru.advancedquantumengineering.blockentity;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import com.syaru.advancedquantumengineering.registry.AQEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;

public class ModifiedQuantumCoreBlockEntity extends AdvCraftingBlockEntity {
    public ModifiedQuantumCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AQEBlockEntities.MODIFIED_QUANTUM_CORE.get(), pos, state);
    }

    @Override
    public long getStorageBytes() {
        return AQEConfig.getCoreStorage();
    }

    @Override
    public int getAcceleratorThreads() {
        return AQEConfig.getBaseCoprocessors();
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return AQEItems.MODIFIED_QUANTUM_CORE.get();
    }
}
