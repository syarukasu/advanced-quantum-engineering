package com.syaru.advancedquantumengineering.item;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class ExperimentalQuantumCoreItem extends BlockItem {
    public ExperimentalQuantumCoreItem(Block block) {
        super(block, new Item.Properties().rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.advanced_quantum_engineering.experimental_quantum_core.storage",
                formatBytes(AQEConfig.getExperimentalCoreStorage())
        ).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable(
                "tooltip.advanced_quantum_engineering.experimental_quantum_core.coprocessors",
                AQEConfig.getExperimentalCoreCoprocessors()
        ).withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.advanced_quantum_engineering.experimental_quantum_core.warning")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.advanced_quantum_engineering.experimental_quantum_core.description.1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.advanced_quantum_engineering.experimental_quantum_core.description.2")
                .withStyle(ChatFormatting.GRAY));
    }

    private static String formatBytes(long bytes) {
        String[] units = {"B", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB"};
        double value = bytes;
        int unit = 0;
        while (value >= 1024.0D && unit < units.length - 1) {
            value /= 1024.0D;
            unit++;
        }
        if (Math.abs(value - Math.rint(value)) < 0.01D) {
            return String.format("%.0f %s", value, units[unit]);
        }
        return String.format("%.2f %s", value, units[unit]);
    }
}
