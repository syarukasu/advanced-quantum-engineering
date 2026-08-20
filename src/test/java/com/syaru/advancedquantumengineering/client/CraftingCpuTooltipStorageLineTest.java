package com.syaru.advancedquantumengineering.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appeng.core.localization.ButtonToolTips;
import com.syaru.advancedquantumengineering.integration.BigIntegerCapacitySnapshot;
import com.syaru.advancedquantumengineering.integration.BigIntegerCpuDisplayMarker;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;

class CraftingCpuTooltipStorageLineTest {
    private static final BigInteger EXACT_CAPACITY = BigInteger.TEN.pow(64).subtract(BigInteger.ONE);
    private static final BigIntegerCapacitySnapshot SNAPSHOT = BigIntegerCapacitySnapshot.capture(
            EXACT_CAPACITY,
            BigInteger.ZERO,
            EXACT_CAPACITY);

    @Test
    void replacesForgeStorageLineWithoutChangingOtherLines() {
        Component name = Component.literal("CPU #1");
        Component storage = storageLine(Component.literal("old"));
        Component processors = ButtonToolTips.CpuStatusCoProcessors.text(Component.literal("64"));
        var lines = new ArrayList<>(List.of(name, storage, processors));

        assertTrue(CraftingCpuTooltipStorageLine.replace(lines, SNAPSHOT));
        assertEquals(List.of(name, storageLine(BigIntegerCpuDisplayMarker.formatValue(SNAPSHOT.total())), processors), lines);
    }

    @Test
    void replacesNeoForgeStorageLineAfterCoprocessors() {
        Component name = Component.literal("CPU #1");
        Component processors = ButtonToolTips.CpuStatusCoProcessors.text(Component.literal("64"));
        Component storage = storageLine(Component.literal("old"));
        var lines = new ArrayList<>(List.of(name, processors, storage));

        assertTrue(CraftingCpuTooltipStorageLine.replace(lines, SNAPSHOT));
        assertEquals(List.of(name, processors, storageLine(BigIntegerCpuDisplayMarker.formatValue(SNAPSHOT.total()))), lines);
    }

    @Test
    void leavesTooltipWithoutStorageLineUntouched() {
        List<Component> lines = new ArrayList<>(List.of(Component.literal("CPU #1")));
        var before = List.copyOf(lines);

        assertFalse(CraftingCpuTooltipStorageLine.replace(lines, SNAPSHOT));
        assertEquals(before, lines);
    }

    private static Component storageLine(Component value) {
        return ButtonToolTips.CpuStatusStorage.text(value).withStyle(ChatFormatting.GRAY);
    }
}
