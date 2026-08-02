package com.syaru.advancedquantumengineering.block;

import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumCoreBlockEntity;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.blocks.AAEAbstractCraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitType;
import org.jetbrains.annotations.Nullable;

public class ModifiedQuantumCoreBlock extends AAEAbstractCraftingUnitBlock<ModifiedQuantumCoreBlockEntity> {
    public ModifiedQuantumCoreBlock() {
        super(metalProps()
                        .lightLevel(state -> state.getValue(LIGHT_LEVEL))
                        .noOcclusion(),
                AAECraftingUnitType.QUANTUM_CORE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AQEBlockEntities.MODIFIED_QUANTUM_CORE.get().create(pos, state);
    }
}
