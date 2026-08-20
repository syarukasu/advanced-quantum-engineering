package com.syaru.advancedquantumengineering.client;

import appeng.core.localization.ButtonToolTips;
import com.syaru.advancedquantumengineering.integration.BigIntegerCapacitySnapshot;
import com.syaru.advancedquantumengineering.integration.BigIntegerCpuDisplayMarker;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/** 既存MODが構築したCPU Tooltipを壊さず、容量行だけを正確なBigInteger表示へ差し替える。 */
public final class CraftingCpuTooltipStorageLine {
    private CraftingCpuTooltipStorageLine() {
    }

    public static boolean replace(List<Component> lines, BigIntegerCapacitySnapshot snapshot) {
        String storageTranslationKey = ButtonToolTips.CpuStatusStorage.getTranslationKey();
        Component exactStorage = ButtonToolTips.CpuStatusStorage
                .text(BigIntegerCpuDisplayMarker.formatValue(snapshot.total()))
                .withStyle(ChatFormatting.GRAY);

        // AE2の版や他MODによる行順変更へ対応するため、翻訳キーで容量行だけを探す。
        for (int index = 0; index < lines.size(); index++) {
            Component line = lines.get(index);
            // CPU名、コプロセッサ、実行状況など容量以外の行は変更しない。
            if (!isStorageLine(line, storageTranslationKey)) {
                continue;
            }
            lines.set(index, exactStorage);
            return true;
        }
        return false;
    }

    private static boolean isStorageLine(Component line, String storageTranslationKey) {
        // 翻訳Component以外はAE2の容量ラベルではないため対象外にする。
        if (!(line.getContents() instanceof TranslatableContents translatable)) {
            return false;
        }
        return storageTranslationKey.equals(translatable.getKey());
    }
}
