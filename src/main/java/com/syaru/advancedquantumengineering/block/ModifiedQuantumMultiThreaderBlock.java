package com.syaru.advancedquantumengineering.block;

import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumMultiThreaderBlockEntity;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.blocks.AAEAbstractCraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitType;
import org.jetbrains.annotations.Nullable;

public class ModifiedQuantumMultiThreaderBlock extends AAEAbstractCraftingUnitBlock<ModifiedQuantumMultiThreaderBlockEntity> {
    public ModifiedQuantumMultiThreaderBlock() {
        super(AAECraftingUnitBlock.getProps(AAECraftingUnitType.MULTI_THREADER, false), AAECraftingUnitType.MULTI_THREADER);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AQEBlockEntities.MODIFIED_QUANTUM_MULTI_THREADER.get().create(pos, state);
    }
}
