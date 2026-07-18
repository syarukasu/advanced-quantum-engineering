package com.syaru.advancedquantumengineering.mixin;

import appeng.client.gui.AEBaseScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = AEBaseScreen.class, remap = false)
public interface AEBaseScreenAccessor {
    @Invoker("setTextContent")
    void aqe$setTextContent(String id, Component content);
}
