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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AQEBlocks {
    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, AdvancedQuantumEngineering.MODID);

    public static final RegistryObject<ModifiedQuantumCoreBlock> MODIFIED_QUANTUM_CORE =
            BLOCKS.register("modified_quantum_core", ModifiedQuantumCoreBlock::new);
    public static final RegistryObject<ModifiedQuantumStorageBlock> MODIFIED_QUANTUM_STORAGE =
            BLOCKS.register("modified_quantum_storage", ModifiedQuantumStorageBlock::new);
    public static final RegistryObject<ModifiedQuantumAcceleratorBlock> MODIFIED_QUANTUM_ACCELERATOR =
            BLOCKS.register("modified_quantum_accelerator", ModifiedQuantumAcceleratorBlock::new);
    public static final RegistryObject<ModifiedQuantumMultiThreaderBlock> MODIFIED_QUANTUM_MULTI_THREADER =
            BLOCKS.register("modified_quantum_multi_threader", ModifiedQuantumMultiThreaderBlock::new);
    public static final RegistryObject<ModifiedDataEntanglerBlock> MODIFIED_DATA_ENTANGLER =
            BLOCKS.register("modified_data_entangler", ModifiedDataEntanglerBlock::new);
    public static final RegistryObject<ExperimentalQuantumCoreBlock> EXPERIMENTAL_QUANTUM_CORE =
            BLOCKS.register("experimental_quantum_core", ExperimentalQuantumCoreBlock::new);
    public static final RegistryObject<BigIntegerQuantumCoreBlock> BIG_INTEGER_QUANTUM_CORE =
            BLOCKS.register("big_integer_quantum_core", BigIntegerQuantumCoreBlock::new);

    private AQEBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
