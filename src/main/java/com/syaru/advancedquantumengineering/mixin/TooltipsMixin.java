package com.syaru.advancedquantumengineering.mixin;

import appeng.core.localization.Tooltips;
import java.util.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Tooltips.class, remap = false)
public abstract class TooltipsMixin {
    private static final long TEBIBYTE = 1024L * 1024L * 1024L * 1024L;
    private static final long PEBIBYTE = TEBIBYTE * 1024L;
    private static final long EXBIBYTE = PEBIBYTE * 1024L;

    @Inject(method = "getByteAmount", at = @At("HEAD"), cancellable = true)
    private static void advancedQuantumEngineering$formatHugeByteAmounts(
            long amount,
            CallbackInfoReturnable<Tooltips.Amount> cir) {
        if (amount >= TEBIBYTE) {
            cir.setReturnValue(formatHugeByteAmount(amount));
        }
    }

    private static Tooltips.Amount formatHugeByteAmount(long amount) {
        if (amount >= EXBIBYTE) {
            return new Tooltips.Amount(format(amount, EXBIBYTE), "E");
        }
        if (amount >= PEBIBYTE) {
            return new Tooltips.Amount(format(amount, PEBIBYTE), "P");
        }
        return new Tooltips.Amount(format(amount, TEBIBYTE), "T");
    }

    private static String format(long amount, long divisor) {
        double value = (double) amount / (double) divisor;
        String text;
        if (value < 10.0D) {
            text = String.format(Locale.ROOT, "%.3f", value);
        } else if (value < 100.0D) {
            text = String.format(Locale.ROOT, "%.2f", value);
        } else if (value < 1000.0D) {
            text = String.format(Locale.ROOT, "%.1f", value);
        } else {
            text = String.format(Locale.ROOT, "%.0f", value);
        }

        while (text.indexOf('.') >= 0 && text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.endsWith(".")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
}
