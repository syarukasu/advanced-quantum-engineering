package com.syaru.advancedquantumengineering.item;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.integration.BigCraftingIntegration;
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

public final class BigIntegerQuantumCoreItem extends BlockItem {
    public BigIntegerQuantumCoreItem(Block block) {
        super(block, new Item.Properties().rarity(Rarity.EPIC));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable(
                "tooltip.advanced_quantum_engineering.big_integer_quantum_core.storage",
                "10^" + AQEConfig.getBigIntegerCoreStorageDecimalDigits() + " - 1 B")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable(
                "tooltip.advanced_quantum_engineering.big_integer_quantum_core.coprocessors",
                AQEConfig.getBigIntegerCoreCoprocessors())
                .withStyle(ChatFormatting.RED));
        boolean acoActive = BigCraftingIntegration.isAcoBackendActive();
        tooltip.add(Component.translatable(
                acoActive
                        ? "tooltip.advanced_quantum_engineering.big_integer_quantum_core.aco_active"
                        : "tooltip.advanced_quantum_engineering.big_integer_quantum_core.long_fallback")
                .withStyle(acoActive
                        ? ChatFormatting.GREEN
                        : ChatFormatting.GOLD));
        tooltip.add(Component.translatable(
                "tooltip.advanced_quantum_engineering.big_integer_quantum_core.description.1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable(
                "tooltip.advanced_quantum_engineering.big_integer_quantum_core.description.2")
                .withStyle(ChatFormatting.GRAY));
    }
}
