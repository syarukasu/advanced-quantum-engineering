package com.syaru.advancedquantumengineering.registry;

import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import com.syaru.advancedquantumengineering.block.ExperimentalQuantumCoreBlock;
import com.syaru.advancedquantumengineering.block.BigIntegerQuantumCoreBlock;
import com.syaru.advancedquantumengineering.block.ModifiedDataEntanglerBlock;
import com.syaru.advancedquantumengineering.block.ModifiedQuantumAcceleratorBlock;
import com.syaru.advancedquantumengineering.block.ModifiedQuantumCoreBlock;
import com.syaru.advancedquantumengineering.block.ModifiedQuantumMultiThreaderBlock;
import com.syaru.advancedquantumengineering.block.ModifiedQuantumStorageBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public final class AQEBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, AdvancedQuantumEngineering.MODID);

    public static final DeferredHolder<Block, ModifiedQuantumCoreBlock> MODIFIED_QUANTUM_CORE =
            BLOCKS.register("modified_quantum_core", ModifiedQuantumCoreBlock::new);
    public static final DeferredHolder<Block, ModifiedQuantumStorageBlock> MODIFIED_QUANTUM_STORAGE =
            BLOCKS.register("modified_quantum_storage", ModifiedQuantumStorageBlock::new);
    public static final DeferredHolder<Block, ModifiedQuantumAcceleratorBlock> MODIFIED_QUANTUM_ACCELERATOR =
            BLOCKS.register("modified_quantum_accelerator", ModifiedQuantumAcceleratorBlock::new);
    public static final DeferredHolder<Block, ModifiedQuantumMultiThreaderBlock> MODIFIED_QUANTUM_MULTI_THREADER =
            BLOCKS.register("modified_quantum_multi_threader", ModifiedQuantumMultiThreaderBlock::new);
    public static final DeferredHolder<Block, ModifiedDataEntanglerBlock> MODIFIED_DATA_ENTANGLER =
            BLOCKS.register("modified_data_entangler", ModifiedDataEntanglerBlock::new);
    public static final DeferredHolder<Block, ExperimentalQuantumCoreBlock> EXPERIMENTAL_QUANTUM_CORE =
            BLOCKS.register("experimental_quantum_core", ExperimentalQuantumCoreBlock::new);
    public static final DeferredHolder<Block, BigIntegerQuantumCoreBlock> BIG_INTEGER_QUANTUM_CORE =
            BLOCKS.register("big_integer_quantum_core", BigIntegerQuantumCoreBlock::new);

    private AQEBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
