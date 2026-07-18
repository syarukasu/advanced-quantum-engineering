package com.syaru.advancedquantumengineering.blockentity;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.integration.BigIntegerStorageProvider;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import com.syaru.advancedquantumengineering.registry.AQEItems;
import java.math.BigInteger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;

public final class BigIntegerQuantumCoreBlockEntity extends AdvCraftingBlockEntity
        implements BigIntegerStorageProvider {
    public BigIntegerQuantumCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AQEBlockEntities.BIG_INTEGER_QUANTUM_CORE.get(), pos, state);
    }

    /** Safe facade used before AQE's BigInteger cluster calculation replaces the long aggregate. */
    @Override
    public long getStorageBytes() {
        return AQEConfig.MAX_UNIT_STORAGE_BYTES;
    }

    @Override
    public BigInteger getBigIntegerStorageBytes() {
        return AQEConfig.getBigIntegerCoreStorage();
    }

    @Override
    public int getAcceleratorThreads() {
        return AQEConfig.getBigIntegerCoreCoprocessors();
    }

    @Override
    protected Item getItemFromBlockEntity() {
        return AQEItems.BIG_INTEGER_QUANTUM_CORE.get();
    }
}
