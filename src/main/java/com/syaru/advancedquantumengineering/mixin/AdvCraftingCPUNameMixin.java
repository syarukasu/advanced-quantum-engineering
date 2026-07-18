package com.syaru.advancedquantumengineering.mixin;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.integration.AQEBigIntegerCpuAccess;
import com.syaru.advancedquantumengineering.integration.BigIntegerCpuDisplayMarker;
import net.minecraft.network.chat.Component;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCluster;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AdvCraftingCPU.class, remap = false)
public abstract class AdvCraftingCPUNameMixin {
    @Shadow
    @Final
    private AdvCraftingCPUCluster cluster;

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void advancedQuantumEngineering$syncBigIntegerCapacityMarker(
            CallbackInfoReturnable<Component> cir) {
        if (!(cluster instanceof AQEBigIntegerCpuAccess access)
                || !access.aqe$hasBigIntegerQuantumCore()) {
            return;
        }

        Component name = cir.getReturnValue();
        if (name == null) {
            name = Component.translatable("gui.advanced_quantum_engineering.big_integer_cpu");
        }
        cir.setReturnValue(BigIntegerCpuDisplayMarker.mark(
                name,
                AQEConfig.getBigIntegerCoreStorageDecimalDigits()));
    }
}
