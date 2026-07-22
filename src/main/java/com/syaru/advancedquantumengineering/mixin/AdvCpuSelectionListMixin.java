package com.syaru.advancedquantumengineering.mixin;

import appeng.client.Point;
import appeng.client.gui.Tooltip;
import appeng.core.localization.Tooltips;
import com.syaru.advancedquantumengineering.integration.BigIntegerCapacitySnapshot;
import com.syaru.advancedquantumengineering.integration.BigIntegerCpuDisplayMarker;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.pedroksl.advanced_ae.gui.quantumcomputer.AdvCpuSelectionList;
import net.pedroksl.advanced_ae.gui.quantumcomputer.QuantumComputerMenu.CraftingCpuListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AdvCpuSelectionList.class, remap = false)
public abstract class AdvCpuSelectionListMixin {
    @Invoker("hitTestCpu")
    protected abstract CraftingCpuListEntry advancedQuantumEngineering$hitTestCpu(Point point);

    @Inject(method = "formatStorage", at = @At("HEAD"), cancellable = true)
    private void advancedQuantumEngineering$showLiveStorageInList(
            CraftingCpuListEntry entry,
            CallbackInfoReturnable<String> cir) {
        BigIntegerCapacitySnapshot snapshot = BigIntegerCpuDisplayMarker
                .readSnapshot(entry.name())
                .orElse(null);
        // BigInteger CPUだけ、一覧の数値を現在予約中の容量へ差し替える。
        if (snapshot != null) {
            cir.setReturnValue(BigIntegerCpuDisplayMarker.formatCompactUsed(snapshot).getString());
        }
    }

    @Redirect(
            method = "getTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lappeng/core/localization/Tooltips;ofBytes(J)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent advancedQuantumEngineering$showPhysicalStorageInTooltip(
            long storage,
            int mouseX,
            int mouseY) {
        BigIntegerCapacitySnapshot snapshot = advancedQuantumEngineering$snapshotAt(mouseX, mouseY);
        return snapshot != null
                ? BigIntegerCpuDisplayMarker.formatValue(snapshot.total())
                : Tooltips.ofBytes(storage);
    }

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void advancedQuantumEngineering$addLiveCapacityToTooltip(
            int mouseX,
            int mouseY,
            CallbackInfoReturnable<Tooltip> cir) {
        Tooltip tooltip = cir.getReturnValue();
        BigIntegerCapacitySnapshot snapshot = advancedQuantumEngineering$snapshotAt(mouseX, mouseY);
        // CPU行の外側や通常CPUでは、Advanced AE本来のTooltipをそのまま返す。
        if (tooltip == null || snapshot == null) {
            return;
        }

        var content = new ArrayList<>(tooltip.getContent());
        content.add(BigIntegerCpuDisplayMarker.formatUsed(snapshot).withStyle(ChatFormatting.GRAY));
        content.add(BigIntegerCpuDisplayMarker.formatAvailable(snapshot).withStyle(ChatFormatting.GRAY));
        cir.setReturnValue(new Tooltip(content));
    }

    private BigIntegerCapacitySnapshot advancedQuantumEngineering$snapshotAt(int mouseX, int mouseY) {
        CraftingCpuListEntry entry = advancedQuantumEngineering$hitTestCpu(new Point(mouseX, mouseY));
        // マウス下にCPU行がなければ、表示マーカーを探さない。
        if (entry == null) {
            return null;
        }
        return BigIntegerCpuDisplayMarker.readSnapshot(entry.name()).orElse(null);
    }
}
