package com.syaru.advancedquantumengineering.block;

import com.syaru.advancedquantumengineering.blockentity.BigIntegerQuantumCoreBlockEntity;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.blocks.AAEAbstractCraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitType;
import org.jetbrains.annotations.Nullable;

public final class BigIntegerQuantumCoreBlock
        extends AAEAbstractCraftingUnitBlock<BigIntegerQuantumCoreBlockEntity> {
    public BigIntegerQuantumCoreBlock() {
        super(AAECraftingUnitBlock.getProps(AAECraftingUnitType.QUANTUM_CORE, false),
                AAECraftingUnitType.QUANTUM_CORE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AQEBlockEntities.BIG_INTEGER_QUANTUM_CORE.get().create(pos, state);
    }
}
