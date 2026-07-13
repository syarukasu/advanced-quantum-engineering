package com.syaru.advancedquantumengineering.blockentity;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import com.syaru.advancedquantumengineering.registry.AQEItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;

public class ModifiedDataEntanglerBlockEntity extends AdvCraftingBlockEntity {
    public ModifiedDataEntanglerBlockEntity(BlockPos pos, BlockState state) {
        super(AQEBlockEntities.MODIFIED_DATA_ENTANGLER.get(), pos, state);
    }

    @Override
    public int getStorageMultiplier() {
        return AQEConfig.getDataEntanglerMultiplier();
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return AQEItems.MODIFIED_DATA_ENTANGLER.get();
    }
}
