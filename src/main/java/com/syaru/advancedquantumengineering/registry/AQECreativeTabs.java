package com.syaru.advancedquantumengineering.registry;

import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class AQECreativeTabs {
    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AdvancedQuantumEngineering.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.advanced_quantum_engineering"))
                    .icon(() -> new ItemStack(AQEItems.MODIFIED_QUANTUM_CORE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(AQEItems.MODIFIED_QUANTUM_CORE.get());
                        output.accept(AQEItems.MODIFIED_QUANTUM_STORAGE.get());
                        output.accept(AQEItems.MODIFIED_DATA_ENTANGLER.get());
                        output.accept(AQEItems.MODIFIED_QUANTUM_ACCELERATOR.get());
                        output.accept(AQEItems.MODIFIED_QUANTUM_MULTI_THREADER.get());
                        output.accept(AQEItems.EXPERIMENTAL_QUANTUM_CORE.get());
                        output.accept(AQEItems.BIG_INTEGER_QUANTUM_CORE.get());
                    })
                    .build());

    private AQECreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
