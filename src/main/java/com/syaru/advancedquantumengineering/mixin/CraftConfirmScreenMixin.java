package com.syaru.advancedquantumengineering.mixin;

import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftConfirmMenu;
import com.syaru.advancedquantumengineering.integration.BigIntegerCapacitySnapshot;
import com.syaru.advancedquantumengineering.integration.BigIntegerCpuDisplayMarker;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftConfirmScreen.class, remap = false)
public abstract class CraftConfirmScreenMixin {
    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void advancedQuantumEngineering$showLiveBigIntegerCapacity(CallbackInfo ci) {
        CraftConfirmScreen screen = (CraftConfirmScreen) (Object) this;
        CraftConfirmMenu menu = screen.getMenu();
        BigIntegerCapacitySnapshot snapshot = BigIntegerCpuDisplayMarker
                .readSnapshot(menu.cpuName)
                .orElse(null);
        // 通常CPUや壊れたマーカーは、AE2本来の確認画面表示を維持する。
        if (snapshot == null) {
            return;
        }

        Component status = GuiText.ConfirmCraftCpuStatus.text(
                BigIntegerCpuDisplayMarker.formatLiveSummary(snapshot),
                menu.getCpuCoProcessors());
        ((AEBaseScreenAccessor) screen).aqe$setTextContent("cpu_status", status);
    }
}
