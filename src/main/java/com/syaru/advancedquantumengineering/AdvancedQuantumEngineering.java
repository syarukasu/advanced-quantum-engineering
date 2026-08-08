package com.syaru.advancedquantumengineering;

import com.mojang.logging.LogUtils;
import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.integration.AdvancedAEIntegration;
import com.syaru.advancedquantumengineering.integration.AQEDiagnostics;
import com.syaru.advancedquantumengineering.integration.BigCraftingIntegration;
import com.syaru.advancedquantumengineering.integration.AQEHostLifecycleEvents;
import com.syaru.advancedquantumengineering.integration.OmniCellsIntegration;
import com.syaru.advancedquantumengineering.registry.AQEBlockEntities;
import com.syaru.advancedquantumengineering.registry.AQEBlocks;
import com.syaru.advancedquantumengineering.registry.AQECreativeTabs;
import com.syaru.advancedquantumengineering.registry.AQEItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

@Mod(AdvancedQuantumEngineering.MODID)
public final class AdvancedQuantumEngineering {
    public static final String MODID = "advanced_quantum_engineering";
    public static final String MOD_NAME = "Advanced Quantum Engineering";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AdvancedQuantumEngineering(IEventBus modBus, ModContainer container) {
        AQEConfig.register(container);
        AQEBlocks.register(modBus);
        AQEItems.register(modBus);
        AQEBlockEntities.register(modBus);
        AQECreativeTabs.register(modBus);

        modBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(AQEHostLifecycleEvents::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(AQEHostLifecycleEvents::onServerStopping);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BigCraftingIntegration.initialize();
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
        LOGGER.info("BigInteger Quantum Core capacity: 10^{} - 1 bytes ({} bits)",
                AQEConfig.getBigIntegerCoreStorageDecimalDigits(),
                AQEConfig.getBigIntegerCoreStorage().bitLength());
        LOGGER.info("BigInteger Quantum Core co-processors: {}", AQEConfig.getBigIntegerCoreCoprocessors());
        LOGGER.info("Optional BigInteger backend: {}", BigCraftingIntegration.backendId());
        LOGGER.info("Experimental Quantum Core storage: {}", AQEConfig.getExperimentalCoreStorage());
        LOGGER.info("Experimental Quantum Core co-processors: {}", AQEConfig.getExperimentalCoreCoprocessors());
        LOGGER.info("Using Omni Cells component: {}", OmniCellsIntegration.QUANTUM_OMNI_CELL_COMPONENT_64M);
        LOGGER.info("Integration method: subclass");
    }

    private void onServerStarted(ServerStartedEvent event) {
        AQEConfig.migrateLegacyServerConfig(event.getServer());
        LOGGER.info("AQE global config active - Modified Quantum Core storage: {}", AQEConfig.getCoreStorage());
        LOGGER.info("AQE global config active - Modified Quantum Core base threads: {}", AQEConfig.getBaseCoprocessors());
        LOGGER.info("AQE global config active - Experimental Quantum Core storage: {}", AQEConfig.getExperimentalCoreStorage());
        LOGGER.info("AQE global config active - Experimental Quantum Core co-processors: {}", AQEConfig.getExperimentalCoreCoprocessors());
    }
}
