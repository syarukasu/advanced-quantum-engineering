package com.syaru.advancedquantumengineering.integration;

import com.syaru.advancedquantumengineering.block.ExperimentalQuantumCoreBlock;
import com.syaru.advancedquantumengineering.block.BigIntegerQuantumCoreBlock;
import com.syaru.advancedquantumengineering.block.ModifiedQuantumCoreBlock;
import com.syaru.advancedquantumengineering.block.ModifiedQuantumAcceleratorBlock;
import com.syaru.advancedquantumengineering.block.ModifiedQuantumMultiThreaderBlock;
import com.syaru.advancedquantumengineering.block.ModifiedQuantumStorageBlock;
import com.syaru.advancedquantumengineering.block.ModifiedDataEntanglerBlock;
import com.syaru.advancedquantumengineering.blockentity.ExperimentalQuantumCoreBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.BigIntegerQuantumCoreBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumCoreBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumAcceleratorBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumMultiThreaderBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumStorageBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.ModifiedDataEntanglerBlockEntity;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import com.syaru.advancedquantumengineering.registry.AQEBlocks;

public final class AdvancedAEIntegration {
    public static final String MODID = "advanced_ae";
    public static final String ORIGINAL_QUANTUM_CORE = "advanced_ae:quantum_core";

    private AdvancedAEIntegration() {
    }

    public static void bindBlockEntity() {
        ModifiedQuantumCoreBlock core = AQEBlocks.MODIFIED_QUANTUM_CORE.get();
        core.setBlockEntity(
                ModifiedQuantumCoreBlockEntity.class,
                AQEBlockEntities.MODIFIED_QUANTUM_CORE.get(),
                null,
                null
        );
        ModifiedQuantumStorageBlock storage = AQEBlocks.MODIFIED_QUANTUM_STORAGE.get();
        storage.setBlockEntity(
                ModifiedQuantumStorageBlockEntity.class,
                AQEBlockEntities.MODIFIED_QUANTUM_STORAGE.get(),
                null,
                null
        );
        ModifiedQuantumAcceleratorBlock accelerator = AQEBlocks.MODIFIED_QUANTUM_ACCELERATOR.get();
        accelerator.setBlockEntity(
                ModifiedQuantumAcceleratorBlockEntity.class,
                AQEBlockEntities.MODIFIED_QUANTUM_ACCELERATOR.get(),
                null,
                null
        );
        ModifiedQuantumMultiThreaderBlock multiThreader = AQEBlocks.MODIFIED_QUANTUM_MULTI_THREADER.get();
        multiThreader.setBlockEntity(
                ModifiedQuantumMultiThreaderBlockEntity.class,
                AQEBlockEntities.MODIFIED_QUANTUM_MULTI_THREADER.get(),
                null,
                null
        );
        ModifiedDataEntanglerBlock entangler = AQEBlocks.MODIFIED_DATA_ENTANGLER.get();
        entangler.setBlockEntity(
                ModifiedDataEntanglerBlockEntity.class,
                AQEBlockEntities.MODIFIED_DATA_ENTANGLER.get(),
                null,
                null
        );
        ExperimentalQuantumCoreBlock experimentalCore = AQEBlocks.EXPERIMENTAL_QUANTUM_CORE.get();
        experimentalCore.setBlockEntity(
                ExperimentalQuantumCoreBlockEntity.class,
                AQEBlockEntities.EXPERIMENTAL_QUANTUM_CORE.get(),
                null,
                null
        );
        BigIntegerQuantumCoreBlock bigIntegerCore = AQEBlocks.BIG_INTEGER_QUANTUM_CORE.get();
        bigIntegerCore.setBlockEntity(
                BigIntegerQuantumCoreBlockEntity.class,
                AQEBlockEntities.BIG_INTEGER_QUANTUM_CORE.get(),
                null,
                null
        );
    }
}
