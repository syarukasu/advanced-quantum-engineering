package com.syaru.advancedquantumengineering.registry;

import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import com.syaru.advancedquantumengineering.item.AQEUnitBlockItem;
import com.syaru.advancedquantumengineering.item.BigIntegerQuantumCoreItem;
import com.syaru.advancedquantumengineering.item.ExperimentalQuantumCoreItem;
import com.syaru.advancedquantumengineering.item.ModifiedQuantumCoreItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AQEItems {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, AdvancedQuantumEngineering.MODID);

    public static final DeferredHolder<Item, Item> MODIFIED_QUANTUM_CORE =
            ITEMS.register("modified_quantum_core", () -> new ModifiedQuantumCoreItem(AQEBlocks.MODIFIED_QUANTUM_CORE.get()));
    public static final DeferredHolder<Item, Item> MODIFIED_QUANTUM_STORAGE =
            ITEMS.register("modified_quantum_storage", () -> new AQEUnitBlockItem(AQEBlocks.MODIFIED_QUANTUM_STORAGE.get()));
    public static final DeferredHolder<Item, Item> MODIFIED_QUANTUM_ACCELERATOR =
            ITEMS.register("modified_quantum_accelerator", () -> new AQEUnitBlockItem(AQEBlocks.MODIFIED_QUANTUM_ACCELERATOR.get()));
    public static final DeferredHolder<Item, Item> MODIFIED_QUANTUM_MULTI_THREADER =
            ITEMS.register("modified_quantum_multi_threader", () -> new AQEUnitBlockItem(AQEBlocks.MODIFIED_QUANTUM_MULTI_THREADER.get()));
    public static final DeferredHolder<Item, Item> MODIFIED_DATA_ENTANGLER =
            ITEMS.register("modified_data_entangler", () -> new AQEUnitBlockItem(AQEBlocks.MODIFIED_DATA_ENTANGLER.get()));
    public static final DeferredHolder<Item, Item> EXPERIMENTAL_QUANTUM_CORE =
            ITEMS.register("experimental_quantum_core", () -> new ExperimentalQuantumCoreItem(AQEBlocks.EXPERIMENTAL_QUANTUM_CORE.get()));
    public static final DeferredHolder<Item, Item> BIG_INTEGER_QUANTUM_CORE =
            ITEMS.register("big_integer_quantum_core", () -> new BigIntegerQuantumCoreItem(AQEBlocks.BIG_INTEGER_QUANTUM_CORE.get()));

    private AQEItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
