package com.syaru.advancedquantumengineering.mixin;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCluster;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AdvCraftingCPUCluster.class, remap = false)
public abstract class AdvCraftingCPUClusterMixin {
    @Shadow
    private int accelerator;

    @Shadow
    private int acceleratorMultiplier;

    @Shadow
    private List<AdvCraftingBlockEntity> blockEntities;

    @Shadow
    private long storage;

    @Shadow
    private long storageMultiplier;

    @Shadow
    private long remainingStorage;

    @Shadow
    private HashMap<UUID, AdvCraftingCPU> activeCpus;

    @ModifyConstant(method = "addBlockEntity", constant = @Constant(intValue = 16))
    private int advancedQuantumEngineering$raiseSingleUnitThreadLimit(int original) {
        return Math.max(original, AQEConfig.getMaxSingleUnitCoprocessors());
    }

    @Inject(method = "addBlockEntity", at = @At("TAIL"))
    private void advancedQuantumEngineering$repairOverflowedCoprocessorState(
            AdvCraftingBlockEntity blockEntity,
            CallbackInfo ci
    ) {
        advancedQuantumEngineering$recalculateCoprocessorState();
    }

    @Inject(method = "getCoProcessors", at = @At("HEAD"), cancellable = true)
    private void advancedQuantumEngineering$clampEffectiveCoprocessors(CallbackInfoReturnable<Integer> cir) {
        long effective = advancedQuantumEngineering$recalculateCoprocessorState();
        cir.setReturnValue((int) effective);
    }

    @Inject(method = "recalculateRemainingStorage", at = @At("HEAD"), cancellable = true)
    private void advancedQuantumEngineering$clampEffectiveStorage(CallbackInfo ci) {
        long summedStorage = 0L;
        long summedMultiplier = 0L;
        for (AdvCraftingBlockEntity blockEntity : this.blockEntities) {
            summedStorage = safeAdd(summedStorage, Math.max(0L, blockEntity.getStorageBytes()));
            summedMultiplier = safeAdd(summedMultiplier, Math.max(0L, blockEntity.getStorageMultiplier()));
        }

        this.storage = summedStorage;
        this.storageMultiplier = summedMultiplier;

        long effectiveStorage = summedStorage;
        if (summedMultiplier > 0L) {
            effectiveStorage = safeMultiply(effectiveStorage, summedMultiplier);
        }

        long allocatedStorage = 0L;
        for (AdvCraftingCPU cpu : this.activeCpus.values()) {
            allocatedStorage = safeAdd(allocatedStorage, Math.max(0L, cpu.getAvailableStorage()));
        }

        this.remainingStorage = Math.max(0L, effectiveStorage - Math.min(effectiveStorage, allocatedStorage));
        ci.cancel();
    }

    private static long safeAdd(long left, long right) {
        if (left >= AQEConfig.MAX_SAFE_EFFECTIVE_STORAGE_BYTES || right <= 0L) {
            return left;
        }
        if (AQEConfig.MAX_SAFE_EFFECTIVE_STORAGE_BYTES - left < right) {
            return AQEConfig.MAX_SAFE_EFFECTIVE_STORAGE_BYTES;
        }
        return left + right;
    }

    private static long safeMultiply(long left, long right) {
        if (left <= 0L || right <= 0L) {
            return 0L;
        }
        if (left >= AQEConfig.MAX_SAFE_EFFECTIVE_STORAGE_BYTES) {
            return AQEConfig.MAX_SAFE_EFFECTIVE_STORAGE_BYTES;
        }
        if (left > AQEConfig.MAX_SAFE_EFFECTIVE_STORAGE_BYTES / right) {
            return AQEConfig.MAX_SAFE_EFFECTIVE_STORAGE_BYTES;
        }
        return left * right;
    }

    private long advancedQuantumEngineering$recalculateCoprocessorState() {
        long summedAccelerators = 0L;
        long summedMultipliers = 0L;
        for (AdvCraftingBlockEntity blockEntity : this.blockEntities) {
            summedAccelerators = safeAddCoprocessors(
                    summedAccelerators,
                    Math.max(0L, blockEntity.getAcceleratorThreads())
            );
            summedMultipliers = safeAddCoprocessors(
                    summedMultipliers,
                    Math.max(0L, blockEntity.getAccelerationMultiplier())
            );
        }

        this.accelerator = (int) summedAccelerators;
        this.acceleratorMultiplier = (int) summedMultipliers;

        long effective = summedAccelerators;
        if (summedMultipliers > 0L) {
            effective = safeMultiplyCoprocessors(effective, summedMultipliers);
        }
        return effective;
    }

    private static long safeAddCoprocessors(long left, long right) {
        if (left >= AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS || right <= 0L) {
            return left;
        }
        if (AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS - left < right) {
            return AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS;
        }
        return left + right;
    }

    private static long safeMultiplyCoprocessors(long left, long right) {
        if (left <= 0L || right <= 0L) {
            return 0L;
        }
        if (left >= AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS) {
            return AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS;
        }
        if (left > AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS / right) {
            return AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS;
        }
        return left * right;
    }
}
