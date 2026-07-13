package com.syaru.advancedquantumengineering.block;

import com.syaru.advancedquantumengineering.blockentity.ExperimentalQuantumCoreBlockEntity;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.blocks.AAEAbstractCraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitType;
import org.jetbrains.annotations.Nullable;

public class ExperimentalQuantumCoreBlock extends AAEAbstractCraftingUnitBlock<ExperimentalQuantumCoreBlockEntity> {
    public ExperimentalQuantumCoreBlock() {
        super(AAECraftingUnitBlock.getProps(AAECraftingUnitType.QUANTUM_CORE, false), AAECraftingUnitType.QUANTUM_CORE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AQEBlockEntities.EXPERIMENTAL_QUANTUM_CORE.get().create(pos, state);
    }
}

