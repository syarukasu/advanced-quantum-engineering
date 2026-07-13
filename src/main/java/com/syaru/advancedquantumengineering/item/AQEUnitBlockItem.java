package com.syaru.advancedquantumengineering.item;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.registry.AQEBlocks;
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

public class AQEUnitBlockItem extends BlockItem {
    public AQEUnitBlockItem(Block block) {
        super(block, new Item.Properties().rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Block block = getBlock();
        if (block == AQEBlocks.MODIFIED_QUANTUM_STORAGE.get()) {
            tooltip.add(Component.translatable(
                    "tooltip.advanced_quantum_engineering.unit.storage",
                    formatBytes(AQEConfig.getStorageBlockBytes())
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        } else if (block == AQEBlocks.MODIFIED_QUANTUM_ACCELERATOR.get()) {
            tooltip.add(Component.translatable(
                    "tooltip.advanced_quantum_engineering.unit.coprocessors",
                    AQEConfig.getAcceleratorThreads()
            ).withStyle(ChatFormatting.AQUA));
        } else if (block == AQEBlocks.MODIFIED_QUANTUM_MULTI_THREADER.get()) {
            tooltip.add(Component.translatable(
                    "tooltip.advanced_quantum_engineering.unit.thread_multiplier",
                    AQEConfig.getMultiThreaderMultiplier()
            ).withStyle(ChatFormatting.AQUA));
        } else if (block == AQEBlocks.MODIFIED_DATA_ENTANGLER.get()) {
            tooltip.add(Component.translatable(
                    "tooltip.advanced_quantum_engineering.unit.multiplier",
                    AQEConfig.getDataEntanglerMultiplier()
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(Component.translatable("tooltip.advanced_quantum_engineering.unit.astral_scale")
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
