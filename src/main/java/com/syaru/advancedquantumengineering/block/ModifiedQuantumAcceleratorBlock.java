package com.syaru.advancedquantumengineering.block;

import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumAcceleratorBlockEntity;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.blocks.AAEAbstractCraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitType;
import org.jetbrains.annotations.Nullable;

public class ModifiedQuantumAcceleratorBlock extends AAEAbstractCraftingUnitBlock<ModifiedQuantumAcceleratorBlockEntity> {
    public ModifiedQuantumAcceleratorBlock() {
        super(AAECraftingUnitBlock.getProps(AAECraftingUnitType.QUANTUM_ACCELERATOR, false), AAECraftingUnitType.QUANTUM_ACCELERATOR);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AQEBlockEntities.MODIFIED_QUANTUM_ACCELERATOR.get().create(pos, state);
    }
}
