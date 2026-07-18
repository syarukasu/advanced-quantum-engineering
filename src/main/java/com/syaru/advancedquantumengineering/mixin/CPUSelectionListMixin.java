package com.syaru.advancedquantumengineering.mixin;

import appeng.client.Point;
import appeng.client.gui.widgets.CPUSelectionList;
import appeng.core.localization.Tooltips;
import appeng.menu.me.crafting.CraftingStatusMenu.CraftingCpuListEntry;
import com.syaru.advancedquantumengineering.integration.BigIntegerCpuDisplayMarker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CPUSelectionList.class, remap = false)
public abstract class CPUSelectionListMixin {
    @Invoker("hitTestCpu")
    protected abstract CraftingCpuListEntry advancedQuantumEngineering$hitTestCpu(Point point);

    @Inject(method = "formatStorage", at = @At("HEAD"), cancellable = true)
    private void advancedQuantumEngineering$showExactStorageInList(
            CraftingCpuListEntry entry,
            CallbackInfoReturnable<String> cir) {
        int decimalDigits = BigIntegerCpuDisplayMarker.readDecimalDigits(entry.name());
        if (decimalDigits >= 0) {
            cir.setReturnValue(BigIntegerCpuDisplayMarker.formatCapacity(decimalDigits));
        }
    }

    @Redirect(
            method = "getTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/localization/Tooltips;ofBytes(J)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent advancedQuantumEngineering$showExactStorageInTooltip(
            long storage,
            int mouseX,
            int mouseY) {
        CraftingCpuListEntry entry = advancedQuantumEngineering$hitTestCpu(new Point(mouseX, mouseY));
        int decimalDigits = entry == null
                ? -1
                : BigIntegerCpuDisplayMarker.readDecimalDigits(entry.name());
        return decimalDigits >= 0
                ? Component.literal(BigIntegerCpuDisplayMarker.formatCapacity(decimalDigits))
                : Tooltips.ofBytes(storage);
    }
}
