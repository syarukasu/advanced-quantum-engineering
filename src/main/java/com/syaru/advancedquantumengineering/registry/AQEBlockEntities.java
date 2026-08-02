package com.syaru.advancedquantumengineering.registry;

import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import com.syaru.advancedquantumengineering.blockentity.ExperimentalQuantumCoreBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.BigIntegerQuantumCoreBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.ModifiedDataEntanglerBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumAcceleratorBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumCoreBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumMultiThreaderBlockEntity;
import com.syaru.advancedquantumengineering.blockentity.ModifiedQuantumStorageBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AQEBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AdvancedQuantumEngineering.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModifiedQuantumCoreBlockEntity>> MODIFIED_QUANTUM_CORE =
            BLOCK_ENTITIES.register("modified_quantum_core",
                    () -> BlockEntityType.Builder.of(
                            ModifiedQuantumCoreBlockEntity::new,
                            AQEBlocks.MODIFIED_QUANTUM_CORE.get()
                    ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModifiedQuantumStorageBlockEntity>> MODIFIED_QUANTUM_STORAGE =
            BLOCK_ENTITIES.register("modified_quantum_storage",
                    () -> BlockEntityType.Builder.of(
                            ModifiedQuantumStorageBlockEntity::new,
                            AQEBlocks.MODIFIED_QUANTUM_STORAGE.get()
                    ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModifiedQuantumAcceleratorBlockEntity>> MODIFIED_QUANTUM_ACCELERATOR =
            BLOCK_ENTITIES.register("modified_quantum_accelerator",
                    () -> BlockEntityType.Builder.of(
                            ModifiedQuantumAcceleratorBlockEntity::new,
                            AQEBlocks.MODIFIED_QUANTUM_ACCELERATOR.get()
                    ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModifiedQuantumMultiThreaderBlockEntity>> MODIFIED_QUANTUM_MULTI_THREADER =
            BLOCK_ENTITIES.register("modified_quantum_multi_threader",
                    () -> BlockEntityType.Builder.of(
                            ModifiedQuantumMultiThreaderBlockEntity::new,
                            AQEBlocks.MODIFIED_QUANTUM_MULTI_THREADER.get()
                    ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModifiedDataEntanglerBlockEntity>> MODIFIED_DATA_ENTANGLER =
            BLOCK_ENTITIES.register("modified_data_entangler",
                    () -> BlockEntityType.Builder.of(
                            ModifiedDataEntanglerBlockEntity::new,
                            AQEBlocks.MODIFIED_DATA_ENTANGLER.get()
                    ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ExperimentalQuantumCoreBlockEntity>> EXPERIMENTAL_QUANTUM_CORE =
            BLOCK_ENTITIES.register("experimental_quantum_core",
                    () -> BlockEntityType.Builder.of(
                            ExperimentalQuantumCoreBlockEntity::new,
                            AQEBlocks.EXPERIMENTAL_QUANTUM_CORE.get()
                    ).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BigIntegerQuantumCoreBlockEntity>> BIG_INTEGER_QUANTUM_CORE =
            BLOCK_ENTITIES.register("big_integer_quantum_core",
                    () -> BlockEntityType.Builder.of(
                            BigIntegerQuantumCoreBlockEntity::new,
                            AQEBlocks.BIG_INTEGER_QUANTUM_CORE.get()
                    ).build(null));

    private AQEBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}
