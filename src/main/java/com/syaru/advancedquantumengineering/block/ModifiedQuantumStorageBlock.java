package com.syaru.advancedquantumengineering.block;

import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumStorageBlockEntity;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.blocks.AAEAbstractCraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitBlock;
import net.pedroksl.advanced_ae.common.blocks.AAECraftingUnitType;
import org.jetbrains.annotations.Nullable;

public class ModifiedQuantumStorageBlock extends AAEAbstractCraftingUnitBlock<ModifiedQuantumStorageBlockEntity> {
    public ModifiedQuantumStorageBlock() {
        super(AAECraftingUnitBlock.getProps(AAECraftingUnitType.STORAGE_256M, false), AAECraftingUnitType.STORAGE_256M);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AQEBlockEntities.MODIFIED_QUANTUM_STORAGE.get().create(pos, state);
    }
}
