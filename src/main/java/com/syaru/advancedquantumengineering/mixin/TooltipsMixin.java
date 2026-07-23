package com.syaru.advancedquantumengineering.mixin;

import appeng.core.localization.Tooltips;
import com.syaru.advancedquantumengineering.integration.CraftingStorageFormatter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Tooltips.class, remap = false)
public abstract class TooltipsMixin {
    private static final long TEBIBYTE = 1024L * 1024L * 1024L * 1024L;

    @Inject(method = "getByteAmount", at = @At("HEAD"), cancellable = true)
    private static void advancedQuantumEngineering$formatHugeByteAmounts(
            long amount,
            CallbackInfoReturnable<Tooltips.Amount> cir) {
        if (amount >= TEBIBYTE) {
            cir.setReturnValue(formatHugeByteAmount(amount));
        }
    }

    private static Tooltips.Amount formatHugeByteAmount(long amount) {
        CraftingStorageFormatter.UnitAmount formatted = CraftingStorageFormatter.binaryAmount(amount);
        return new Tooltips.Amount(formatted.number(), formatted.unit());
    }
}
