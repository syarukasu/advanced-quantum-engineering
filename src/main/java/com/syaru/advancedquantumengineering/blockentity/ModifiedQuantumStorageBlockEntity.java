package com.syaru.advancedquantumengineering.blockentity;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import com.syaru.advancedquantumengineering.registry.AQEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;

public class ModifiedQuantumStorageBlockEntity extends AdvCraftingBlockEntity {
    public ModifiedQuantumStorageBlockEntity(BlockPos pos, BlockState state) {
        super(AQEBlockEntities.MODIFIED_QUANTUM_STORAGE.get(), pos, state);
    }

    @Override
    public long getStorageBytes() {
        return AQEConfig.getStorageBlockBytes();
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return AQEItems.MODIFIED_QUANTUM_STORAGE.get();
    }
}
