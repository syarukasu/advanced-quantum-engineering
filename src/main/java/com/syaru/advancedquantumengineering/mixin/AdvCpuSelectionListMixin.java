package com.syaru.advancedquantumengineering.mixin;

import appeng.client.Point;
import appeng.client.gui.Tooltip;
import appeng.core.localization.ButtonToolTips;
import com.syaru.advancedquantumengineering.integration.BigIntegerCpuDisplayMarker;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.pedroksl.advanced_ae.gui.quantumcomputer.AdvCpuSelectionList;
import net.pedroksl.advanced_ae.gui.quantumcomputer.QuantumComputerMenu.CraftingCpuListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AdvCpuSelectionList.class, remap = false)
public abstract class AdvCpuSelectionListMixin {
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

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void advancedQuantumEngineering$addExactStorageToTooltip(
            int mouseX,
            int mouseY,
            CallbackInfoReturnable<Tooltip> cir) {
        Tooltip tooltip = cir.getReturnValue();
        CraftingCpuListEntry entry = advancedQuantumEngineering$hitTestCpu(new Point(mouseX, mouseY));
        int decimalDigits = entry == null
                ? -1
                : BigIntegerCpuDisplayMarker.readDecimalDigits(entry.name());
        if (tooltip == null || decimalDigits < 0) {
            return;
        }

        var content = new ArrayList<>(tooltip.getContent());
        content.add(
                Math.min(1, content.size()),
                ButtonToolTips.CpuStatusStorage
                        .text(Component.literal(BigIntegerCpuDisplayMarker.formatCapacity(decimalDigits)))
                        .withStyle(ChatFormatting.GRAY));
        cir.setReturnValue(new Tooltip(content));
    }
}
