package com.syaru.advancedquantumengineering;

import com.mojang.logging.LogUtils;
import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.integration.AdvancedAEIntegration;
import com.syaru.advancedquantumengineering.integration.AQEDiagnostics;
import com.syaru.advancedquantumengineering.integration.OmniCellsIntegration;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import com.syaru.advancedquantumengineering.registry.AQEBlocks;
import com.syaru.advancedquantumengineering.registry.AQECreativeTabs;
import com.syaru.advancedquantumengineering.registry.AQEItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AdvancedQuantumEngineering.MODID)
public final class AdvancedQuantumEngineering {
    public static final String MODID = "advanced_quantum_engineering";
    public static final String MOD_NAME = "Advanced Quantum Engineering";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AdvancedQuantumEngineering() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        AQEConfig.register();
        AQEBlocks.register(modBus);
        AQEItems.register(modBus);
        AQEBlockEntities.register(modBus);
        AQECreativeTabs.register(modBus);

        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AdvancedAEIntegration.bindBlockEntity();
            AQEDiagnostics.runStartupChecks();
        });

        LOGGER.info("{} initialized", MOD_NAME);
        LOGGER.info("Advanced AE integration enabled: {}", ModList.get().isLoaded(AdvancedAEIntegration.MODID));
        LOGGER.info("AE2 Omni Cells integration enabled: {}", ModList.get().isLoaded(OmniCellsIntegration.MODID));
        LOGGER.info("Modified Quantum Core storage: {}", AQEConfig.getCoreStorage());
        LOGGER.info("Modified Quantum Core base threads: {}", AQEConfig.getBaseCoprocessors());
        LOGGER.info("Modified Quantum Storage bytes: {}", AQEConfig.getStorageBlockBytes());
        LOGGER.info("Modified Quantum Accelerator threads: {}", AQEConfig.getAcceleratorThreads());
        LOGGER.info("Modified Quantum Multi-Threader multiplier: {}", AQEConfig.getMultiThreaderMultiplier());
        LOGGER.info("Modified Data Entangler multiplier: {}", AQEConfig.getDataEntanglerMultiplier());
        LOGGER.info("Experimental Quantum Core storage: {}", AQEConfig.getExperimentalCoreStorage());
        LOGGER.info("Experimental Quantum Core co-processors: {}", AQEConfig.getExperimentalCoreCoprocessors());
        LOGGER.info("Using Omni Cells component: {}", OmniCellsIntegration.QUANTUM_OMNI_CELL_COMPONENT_64M);
        LOGGER.info("Integration method: subclass");
    }

    private void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("AQE server config active - Modified Quantum Core storage: {}", AQEConfig.getCoreStorage());
        LOGGER.info("AQE server config active - Modified Quantum Core base threads: {}", AQEConfig.getBaseCoprocessors());
        LOGGER.info("AQE server config active - Experimental Quantum Core storage: {}", AQEConfig.getExperimentalCoreStorage());
        LOGGER.info("AQE server config active - Experimental Quantum Core co-processors: {}", AQEConfig.getExperimentalCoreCoprocessors());
    }
}
