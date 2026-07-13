package com.syaru.advancedquantumengineering.registry;

import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import com.syaru.advancedquantumengineering.item.AQEUnitBlockItem;
import com.syaru.advancedquantumengineering.item.ExperimentalQuantumCoreItem;
import com.syaru.advancedquantumengineering.item.ModifiedQuantumCoreItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AQEItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AdvancedQuantumEngineering.MODID);

    public static final RegistryObject<Item> MODIFIED_QUANTUM_CORE =
            ITEMS.register("modified_quantum_core", () -> new ModifiedQuantumCoreItem(AQEBlocks.MODIFIED_QUANTUM_CORE.get()));
    public static final RegistryObject<Item> MODIFIED_QUANTUM_STORAGE =
            ITEMS.register("modified_quantum_storage", () -> new AQEUnitBlockItem(AQEBlocks.MODIFIED_QUANTUM_STORAGE.get()));
    public static final RegistryObject<Item> MODIFIED_QUANTUM_ACCELERATOR =
            ITEMS.register("modified_quantum_accelerator", () -> new AQEUnitBlockItem(AQEBlocks.MODIFIED_QUANTUM_ACCELERATOR.get()));
    public static final RegistryObject<Item> MODIFIED_QUANTUM_MULTI_THREADER =
            ITEMS.register("modified_quantum_multi_threader", () -> new AQEUnitBlockItem(AQEBlocks.MODIFIED_QUANTUM_MULTI_THREADER.get()));
    public static final RegistryObject<Item> MODIFIED_DATA_ENTANGLER =
            ITEMS.register("modified_data_entangler", () -> new AQEUnitBlockItem(AQEBlocks.MODIFIED_DATA_ENTANGLER.get()));
    public static final RegistryObject<Item> EXPERIMENTAL_QUANTUM_CORE =
            ITEMS.register("experimental_quantum_core", () -> new ExperimentalQuantumCoreItem(AQEBlocks.EXPERIMENTAL_QUANTUM_CORE.get()));

    private AQEItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
