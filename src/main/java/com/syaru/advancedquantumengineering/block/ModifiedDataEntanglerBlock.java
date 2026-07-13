package com.syaru.advancedquantumengineering.block;

import com.syaru.advancedquantumengineering.blockentity.ModifiedDataEntanglerBlockEntity;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.blocks.AAEAbstractCraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitType;
import org.jetbrains.annotations.Nullable;

public class ModifiedDataEntanglerBlock extends AAEAbstractCraftingUnitBlock<ModifiedDataEntanglerBlockEntity> {
    public ModifiedDataEntanglerBlock() {
        super(AAECraftingUnitBlock.getProps(AAECraftingUnitType.STORAGE_MULTIPLIER, false), AAECraftingUnitType.STORAGE_MULTIPLIER);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AQEBlockEntities.MODIFIED_DATA_ENTANGLER.get().create(pos, state);
    }
}
