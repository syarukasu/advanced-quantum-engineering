package com.syaru.advancedquantumengineering.mixin;

import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.integration.AQEBigCraftingHost;
import com.syaru.advancedquantumengineering.integration.AQEBigIntegerCpuAccess;
import com.syaru.advancedquantumengineering.integration.BigCraftingIntegration;
import com.syaru.advancedquantumengineering.integration.BigIntegerCapacityMath;
import com.syaru.advancedquantumengineering.integration.BigIntegerStorageProvider;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPUCluster;
import net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AdvCraftingCPUCluster.class, remap = false)
public abstract class AdvCraftingCPUClusterMixin implements AQEBigIntegerCpuAccess {
    @Unique
    private static final String AQE_BIG_HOST_NBT = "aqeBigCraftingHost";

    @Shadow
    private int accelerator;

    @Shadow
    private int acceleratorMultiplier;

    @Shadow
    @Final
    private List<AdvCraftingBlockEntity> blockEntities;

    @Shadow
    private long storage;

    @Shadow
    private long storageMultiplier;

    @Shadow
    private long remainingStorage;

    @Shadow
    @Final
    private HashMap<UUID, AdvCraftingCPU> activeCpus;

    @Unique
    private AQEBigCraftingHost aqe$bigHost;

    @Unique
    private BigInteger aqe$physicalCapacity = BigInteger.ZERO;

    @Unique
    private BigInteger aqe$availableCapacity = BigInteger.ZERO;

    @ModifyConstant(method = "addBlockEntity", constant = @Constant(intValue = 16))
    private int advancedQuantumEngineering$raiseSingleUnitThreadLimit(int original) {
        return Math.max(original, AQEConfig.getMaxSingleUnitCoprocessors());
    }

    @Inject(method = "addBlockEntity", at = @At("TAIL"))
    private void advancedQuantumEngineering$repairOverflowedState(
            AdvCraftingBlockEntity blockEntity,
            CallbackInfo ci) {
        advancedQuantumEngineering$recalculateCoprocessorState();
        advancedQuantumEngineering$recalculateStorageState();
    }

    @Inject(method = "getCoProcessors", at = @At("HEAD"), cancellable = true)
    private void advancedQuantumEngineering$clampEffectiveCoprocessors(
            CallbackInfoReturnable<Integer> cir) {
        long effective = advancedQuantumEngineering$recalculateCoprocessorState();
        cir.setReturnValue((int) effective);
    }

    @Inject(method = "recalculateRemainingStorage", at = @At("HEAD"), cancellable = true)
    private void advancedQuantumEngineering$calculateBigIntegerStorage(CallbackInfo ci) {
        advancedQuantumEngineering$recalculateStorageState();
        ci.cancel();
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void advancedQuantumEngineering$saveBigIntegerHost(CompoundTag data, CallbackInfo ci) {
        AQEBigCraftingHost host = aqe$bigHost;
        if (host != null && host.hasPersistentState()) {
            data.put(AQE_BIG_HOST_NBT, host.save());
        }
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void advancedQuantumEngineering$loadBigIntegerHost(CompoundTag data, CallbackInfo ci) {
        CompoundTag saved = data.contains(AQE_BIG_HOST_NBT, Tag.TAG_COMPOUND)
                ? data.getCompound(AQE_BIG_HOST_NBT).copy()
                : new CompoundTag();
        if (aqe$bigHost != null) {
            aqe$bigHost.close();
        }
        aqe$bigHost = BigCraftingIntegration.createHost(this, aqe$calculatePhysicalCapacity(), saved);
        advancedQuantumEngineering$recalculateStorageState();
    }

    @Override
    public boolean aqe$hasBigIntegerQuantumCore() {
        for (AdvCraftingBlockEntity blockEntity : blockEntities) {
            if (blockEntity instanceof BigIntegerStorageProvider) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BigInteger aqe$getPhysicalCraftingCapacity() {
        return aqe$physicalCapacity;
    }

    @Override
    public BigInteger aqe$getReservedCraftingCapacity() {
        AQEBigCraftingHost host = aqe$bigHost;
        return host == null ? BigInteger.ZERO : host.reserved();
    }

    @Override
    public BigInteger aqe$getAvailableCraftingCapacity() {
        return aqe$availableCapacity;
    }

    @Override
    public String aqe$getBigIntegerBackendId() {
        AQEBigCraftingHost host = aqe$bigHost;
        return host == null ? "aqe:uninitialized" : host.backendId();
    }

    @Unique
    private void advancedQuantumEngineering$recalculateStorageState() {
        BigInteger physicalCapacity = aqe$calculatePhysicalCapacity();
        Map<UUID, BigInteger> reservations = new LinkedHashMap<>();
        for (var entry : activeCpus.entrySet()) {
            AdvCraftingCPU cpu = entry.getValue();
            if (cpu != null && cpu.getAvailableStorage() > 0L) {
                reservations.put(entry.getKey(), BigInteger.valueOf(cpu.getAvailableStorage()));
            }
        }

        if (aqe$bigHost == null) {
            aqe$bigHost = BigCraftingIntegration.createHost(this, physicalCapacity, new CompoundTag());
        }
        aqe$bigHost.reconcile(physicalCapacity, reservations);

        BigInteger summedStorage = aqe$sumStorageContributions();
        BigInteger summedMultiplier = aqe$sumStorageMultipliers();
        this.storage = BigIntegerCapacityMath.saturatedLong(
                summedStorage, AQEConfig.MAX_BIG_INTEGER_BITS);
        this.storageMultiplier = BigIntegerCapacityMath.saturatedLong(
                summedMultiplier, AQEConfig.MAX_BIG_INTEGER_BITS);
        this.remainingStorage = aqe$bigHost.availableAsSaturatedLong();
        this.aqe$physicalCapacity = physicalCapacity;
        this.aqe$availableCapacity = aqe$bigHost.available();
    }

    @Unique
    private BigInteger aqe$calculatePhysicalCapacity() {
        BigInteger storageTotal = aqe$sumStorageContributions();
        BigInteger multiplierTotal = aqe$sumStorageMultipliers();
        if (multiplierTotal.signum() > 0) {
            storageTotal = BigIntegerCapacityMath.multiply(
                    storageTotal,
                    multiplierTotal,
                    "AQE effective storage",
                    AQEConfig.MAX_BIG_INTEGER_BITS);
        }
        return storageTotal;
    }

    @Unique
    private BigInteger aqe$sumStorageContributions() {
        BigInteger total = BigInteger.ZERO;
        for (AdvCraftingBlockEntity blockEntity : blockEntities) {
            BigInteger contribution = blockEntity instanceof BigIntegerStorageProvider provider
                    ? provider.getBigIntegerStorageBytes()
                    : BigInteger.valueOf(Math.max(0L, blockEntity.getStorageBytes()));
            total = BigIntegerCapacityMath.add(
                    total,
                    BigIntegerCapacityMath.checkedNonNegative(
                            contribution,
                            "AQE storage contribution",
                            AQEConfig.MAX_BIG_INTEGER_BITS),
                    "AQE summed storage",
                    AQEConfig.MAX_BIG_INTEGER_BITS);
        }
        return total;
    }

    @Unique
    private BigInteger aqe$sumStorageMultipliers() {
        BigInteger total = BigInteger.ZERO;
        for (AdvCraftingBlockEntity blockEntity : blockEntities) {
            int multiplier = Math.max(0, blockEntity.getStorageMultiplier());
            if (multiplier > 0) {
                total = BigIntegerCapacityMath.add(
                        total,
                        BigInteger.valueOf(multiplier),
                        "AQE summed storage multiplier",
                        AQEConfig.MAX_BIG_INTEGER_BITS);
            }
        }
        return total;
    }

    @Unique
    private long advancedQuantumEngineering$recalculateCoprocessorState() {
        long summedAccelerators = 0L;
        long summedMultipliers = 0L;
        for (AdvCraftingBlockEntity blockEntity : this.blockEntities) {
            summedAccelerators = safeAddCoprocessors(
                    summedAccelerators,
                    Math.max(0L, blockEntity.getAcceleratorThreads()));
            summedMultipliers = safeAddCoprocessors(
                    summedMultipliers,
                    Math.max(0L, blockEntity.getAccelerationMultiplier()));
        }

        this.accelerator = (int) summedAccelerators;
        this.acceleratorMultiplier = (int) summedMultipliers;

        long effective = summedAccelerators;
        if (summedMultipliers > 0L) {
            effective = safeMultiplyCoprocessors(effective, summedMultipliers);
        }
        return effective;
    }

    @Unique
    private static long safeAddCoprocessors(long left, long right) {
        if (left >= AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS || right <= 0L) {
            return left;
        }
        if (AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS - left < right) {
            return AQEConfig.MAX_SAFE_EFFECTIVE_COPROCESSORS;
        }
        return left + right;
    }

    @Unique
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
