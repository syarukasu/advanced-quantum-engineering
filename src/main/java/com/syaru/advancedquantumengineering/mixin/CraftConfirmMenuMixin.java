package com.syaru.advancedquantumengineering.mixin;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.menu.me.crafting.CraftConfirmMenu;
import appeng.menu.me.crafting.CraftingCPURecord;
import com.syaru.advancedquantumengineering.integration.AQEBigIntegerCpuAccess;
import com.syaru.advancedquantumengineering.integration.BigIntegerCpuDisplayMarker;
import net.minecraft.network.chat.Component;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCluster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftConfirmMenu.class, remap = false)
public abstract class CraftConfirmMenuMixin {
    @Shadow
    public Component cpuName;

    @Inject(method = "onCPUSelectionChanged", at = @At("TAIL"))
    private void advancedQuantumEngineering$markBigIntegerCpu(
            CraftingCPURecord record,
            boolean hasCpu,
            CallbackInfo ci) {
        // CPU未選択時や同期前は、確認画面の既定状態を維持する。
        if (record == null || cpuName == null) {
            return;
        }

        ICraftingCPU cpu = ((CraftingCPURecordAccessor) (Object) record).aqe$getCpu();
        // 通常AE2 CPUにはAdvanced AEクラスタへの参照がないため対象外とする。
        if (!(cpu instanceof AdvCraftingCPU)) {
            return;
        }

        AdvCraftingCPUCluster cluster = ((AdvCraftingCPUAccessor) (Object) cpu).aqe$getCluster();
        // BigIntegerコアを含まないAdvanced AE CPUへ表示マーカーを付けない。
        if (!(cluster instanceof AQEBigIntegerCpuAccess access)
                || !access.aqe$hasBigIntegerQuantumCore()) {
            return;
        }

        cpuName = BigIntegerCpuDisplayMarker.mark(
                cpuName,
                access.aqe$getCapacityDisplaySnapshot());
    }
}
