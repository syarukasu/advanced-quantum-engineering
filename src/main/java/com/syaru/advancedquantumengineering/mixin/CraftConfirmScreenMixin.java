package com.syaru.advancedquantumengineering.mixin;

import appeng.client.gui.me.crafting.CraftConfirmScreen;
import appeng.core.localization.GuiText;
import appeng.menu.me.crafting.CraftConfirmMenu;
import com.syaru.advancedquantumengineering.integration.BigIntegerCpuDisplayMarker;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CraftConfirmScreen.class, remap = false)
public abstract class CraftConfirmScreenMixin {
    @Inject(method = "updateBeforeRender", at = @At("TAIL"))
    private void advancedQuantumEngineering$showExactBigIntegerCapacity(CallbackInfo ci) {
        CraftConfirmScreen screen = (CraftConfirmScreen) (Object) this;
        CraftConfirmMenu menu = screen.getMenu();
        int decimalDigits = BigIntegerCpuDisplayMarker.readDecimalDigits(menu.cpuName);
        if (decimalDigits < 0) {
            return;
        }

        Component exactCapacity = Component.literal(
                BigIntegerCpuDisplayMarker.formatCapacity(decimalDigits));
        Component status = GuiText.ConfirmCraftCpuStatus.text(
                exactCapacity,
                menu.getCpuCoProcessors());
        ((AEBaseScreenAccessor) screen).aqe$setTextContent("cpu_status", status);
    }
}
