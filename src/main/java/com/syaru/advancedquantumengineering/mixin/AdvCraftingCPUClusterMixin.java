package com.syaru.advancedquantumengineering.mixin;

import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import com.syaru.advancedquantumengineering.config.AQEConfig;
import com.syaru.advancedquantumengineering.integration.AQEBigCraftingHost;
import com.syaru.advancedquantumengineering.integration.AQEBigIntegerCpuAccess;
import com.syaru.advancedquantumengineering.integration.BigCraftingIntegration;
import com.syaru.advancedquantumengineering.integration.BigIntegerCapacitySnapshot;
import com.syaru.advancedquantumengineering.integration.BigIntegerCapacityMath;
import com.syaru.advancedquantumengineering.integration.BigIntegerStorageProvider;
import com.syaru.advancedquantumengineering.integration.AQEHostSnapshot;
import com.syaru.advancedquantumengineering.integration.AQERevisionMetrics;
import com.syaru.advancedquantumengineering.integration.AQEHostOwnerToken;
import com.syaru.advancedquantumengineering.integration.AQEHostRegistration;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.IGrid;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
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
    public abstract Level getLevel();

    @Shadow
    @Final
    private HashMap<UUID, AdvCraftingCPU> activeCpus;

    @Unique
    private AQEBigCraftingHost aqe$bigHost;

    @Unique
    private AQEHostRegistration aqe$hostRegistration;

    @Unique
    private long aqe$hostGeneration;

    @Unique
    private BigInteger aqe$physicalCapacity = BigInteger.ZERO;

    @Unique
    private BigInteger aqe$availableCapacity = BigInteger.ZERO;

    @Unique
    private BigInteger aqe$displayTotal = BigInteger.ZERO;

    @Unique
    private BigInteger aqe$displayUsed = BigInteger.ZERO;

    @Unique
    private int aqe$displayActiveJobs;

    @Unique
    private int aqe$displayBigJobs;

    @Unique
    private BigIntegerCapacitySnapshot aqe$displaySnapshot = BigIntegerCapacitySnapshot.zero();

    @Unique
    private long aqe$structureRevision = 1L;

    @Unique
    private long aqe$activeCpuRevision = 1L;

    @Unique
    private long aqe$hostRevision = 1L;

    @Unique
    private long aqe$cachedStructureRevision;

    @Unique
    private long aqe$reconciledStructureRevision;

    @Unique
    private long aqe$reconciledCpuRevision;

    @Unique
    private boolean aqe$structureDirty = true;

    @Unique
    private BigInteger aqe$cachedStorageContributions = BigInteger.ZERO;

    @Unique
    private BigInteger aqe$cachedStorageMultipliers = BigInteger.ZERO;

    @Unique
    private BigInteger aqe$cachedPhysicalCapacity = BigInteger.ZERO;

    @Unique
    private long aqe$cachedEffectiveCoprocessors;

    @Unique
    private boolean aqe$cachedHasBigCore;

    @Unique
    private AQEHostSnapshot aqe$latestHostSnapshot;

    @Unique
    private boolean aqe$reportedOverbookedDisplayLedger;

    @ModifyConstant(method = "addBlockEntity", constant = @Constant(intValue = 16))
    private int advancedQuantumEngineering$raiseSingleUnitThreadLimit(int original) {
        return Math.max(original, AQEConfig.getMaxSingleUnitCoprocessors());
    }

    @Inject(method = "addBlockEntity", at = @At("TAIL"))
    private void advancedQuantumEngineering$repairOverflowedState(
            AdvCraftingBlockEntity blockEntity,
            CallbackInfo ci) {
        advancedQuantumEngineering$markStructureDirty();
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
    private void advancedQuantumEngineering$saveBigIntegerHost(
            CompoundTag data,
            HolderLookup.Provider registries,
            CallbackInfo ci) {
        AQEBigCraftingHost host = aqe$bigHost;
        if (host != null && host.hasPersistentState()) {
            data.put(AQE_BIG_HOST_NBT, host.save());
        }
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void advancedQuantumEngineering$loadBigIntegerHost(
            CompoundTag data,
            HolderLookup.Provider registries,
            CallbackInfo ci) {
        advancedQuantumEngineering$markStructureDirty();
        advancedQuantumEngineering$markActiveCpuDirty();
        CompoundTag saved = data.contains(AQE_BIG_HOST_NBT, Tag.TAG_COMPOUND)
                ? data.getCompound(AQE_BIG_HOST_NBT).copy()
                : new CompoundTag();
        advancedQuantumEngineering$replaceHost(saved, aqe$calculatePhysicalCapacity());
        advancedQuantumEngineering$recalculateStorageState();
    }

    @Inject(method = "destroy", at = @At("HEAD"))
    private void advancedQuantumEngineering$closeHostOnDestroy(CallbackInfo ci) {
        advancedQuantumEngineering$closeHost();
    }

    @Inject(method = "breakCluster", at = @At("HEAD"))
    private void advancedQuantumEngineering$closeHostOnBreak(CallbackInfo ci) {
        advancedQuantumEngineering$closeHost();
    }

    @Override
    public boolean aqe$hasBigIntegerQuantumCore() {
        advancedQuantumEngineering$ensureStructureAggregates();
        return aqe$cachedHasBigCore;
    }

    @Override
    public BigInteger aqe$getPhysicalCraftingCapacity() {
        advancedQuantumEngineering$ensureStructureAggregates();
        return aqe$cachedPhysicalCapacity;
    }

    @Override
    public BigInteger aqe$getReservedCraftingCapacity() {
        advancedQuantumEngineering$recalculateStorageState();
        return aqe$latestHostSnapshot == null
                ? BigInteger.ZERO
                : aqe$latestHostSnapshot.reserved();
    }

    @Override
    public BigInteger aqe$getAvailableCraftingCapacity() {
        advancedQuantumEngineering$recalculateStorageState();
        return aqe$latestHostSnapshot == null
                ? aqe$availableCapacity
                : aqe$latestHostSnapshot.available();
    }

    @Override
    public synchronized BigIntegerCapacitySnapshot aqe$getCapacityDisplaySnapshot() {
        advancedQuantumEngineering$recalculateStorageState();
        AQEHostSnapshot hostSnapshot = aqe$latestHostSnapshot;
        BigInteger total = hostSnapshot == null ? aqe$physicalCapacity : hostSnapshot.physicalCapacity();
        BigInteger reserved = hostSnapshot == null ? BigInteger.ZERO : hostSnapshot.reserved();
        BigInteger used = reserved;
        // 一度取得した使用中容量から空きを導出し、三値が別時点になる競合を避ける。
        BigInteger available = total.subtract(used);
        if (available.signum() < 0) {
            /*
             * Backendの過剰予約を負の空き容量として同期するとGUI側の三値が破損する。
             * 内部Ledgerには触れず、表示だけを物理上限へ固定し、一度だけ原因を記録する。
             */
            if (!aqe$reportedOverbookedDisplayLedger) {
                aqe$reportedOverbookedDisplayLedger = true;
                AdvancedQuantumEngineering.LOGGER.error(
                        "AQE capacity display detected reservations above physical capacity: total={}, reserved={}, backend={}",
                        total,
                        used,
                        hostSnapshot == null ? "aqe:uninitialized" : hostSnapshot.backendState());
            }
            used = total;
            available = BigInteger.ZERO;
        }
        long standardJobsLong = hostSnapshot == null ? activeCpus.size() : hostSnapshot.standardJobCount();
        long bigJobsLong = hostSnapshot == null ? 0L : hostSnapshot.bigJobCount();
        int bigJobs = clampDisplayCount(bigJobsLong);
        // ACO子WindowはBig親Jobの内部実装なので、通常Jobとの二重表示から除外する。
        long combinedJobs = safeDisplayAdd(Math.max(0L, standardJobsLong), bigJobsLong);
        // 異常な外部Backend件数でも表示用intをoverflowさせず、容量会計には影響させない。
        int activeJobs = combinedJobs >= Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : (int) combinedJobs;

        // 容量が変化した時だけ最大16,384桁の10進変換を行い、通常の画面更新を軽く保つ。
        if (!total.equals(aqe$displayTotal)
                || !reserved.equals(aqe$displayUsed)
                || activeJobs != aqe$displayActiveJobs
                || bigJobs != aqe$displayBigJobs) {
            aqe$displaySnapshot = BigIntegerCapacitySnapshot.capture(
                    total, used, available, activeJobs, bigJobs);
            aqe$displayTotal = total;
            // 比較用にはBackendの生予約値を保持し、同じ破損値で毎画面再変換しない。
            aqe$displayUsed = reserved;
            aqe$displayActiveJobs = activeJobs;
            aqe$displayBigJobs = bigJobs;
        } else {
            AQERevisionMetrics.recordPresentationReuse();
        }
        return aqe$displaySnapshot;
    }

    @Override
    public String aqe$getBigIntegerBackendId() {
        AQEBigCraftingHost host = aqe$bigHost;
        return host == null ? "aqe:uninitialized" : host.backendId();
    }

    @Unique
    private void advancedQuantumEngineering$recalculateStorageState() {
        advancedQuantumEngineering$ensureStructureAggregates();
        BigInteger physicalCapacity = aqe$cachedPhysicalCapacity;

        if (aqe$bigHost == null) {
            advancedQuantumEngineering$replaceHost(new CompoundTag(), physicalCapacity);
        }
        if (aqe$reconciledStructureRevision != aqe$structureRevision
                || aqe$reconciledCpuRevision != aqe$activeCpuRevision) {
            Map<UUID, BigInteger> reservations = new LinkedHashMap<>();
            for (var entry : activeCpus.entrySet()) {
                AdvCraftingCPU cpu = entry.getValue();
                if (cpu != null && cpu.getAvailableStorage() > 0L) {
                    reservations.put(entry.getKey(), BigInteger.valueOf(cpu.getAvailableStorage()));
                }
            }
            aqe$bigHost.reconcile(physicalCapacity, reservations);
            aqe$reconciledStructureRevision = aqe$structureRevision;
            aqe$reconciledCpuRevision = aqe$activeCpuRevision;
            aqe$hostRevision = nextRevision(aqe$hostRevision);
            AQERevisionMetrics.recordReservationRebuild();
            AQERevisionMetrics.recordHostReconcile();
        }
        this.storage = BigIntegerCapacityMath.saturatedLong(
                aqe$cachedStorageContributions, AQEConfig.MAX_BIG_INTEGER_BITS);
        this.storageMultiplier = BigIntegerCapacityMath.saturatedLong(
                aqe$cachedStorageMultipliers, AQEConfig.MAX_BIG_INTEGER_BITS);
        AQERevisionMetrics.recordHostSnapshotRead();
        aqe$latestHostSnapshot = aqe$bigHost.snapshot(aqe$hostRevision);
        this.remainingStorage = BigIntegerCapacityMath.saturatedLong(
                aqe$latestHostSnapshot.available(), AQEConfig.MAX_BIG_INTEGER_BITS);
        this.aqe$physicalCapacity = aqe$latestHostSnapshot.physicalCapacity();
        this.aqe$availableCapacity = aqe$latestHostSnapshot.available();
    }

    @Unique
    private synchronized void advancedQuantumEngineering$replaceHost(
            CompoundTag savedState,
            BigInteger physicalCapacity) {
        AQEHostOwnerToken owner = new AQEHostOwnerToken(
                ++aqe$hostGeneration,
                advancedQuantumEngineering$lifecycleOwner());
        AQEBigCraftingHost replacement = BigCraftingIntegration.createHost(
                owner, physicalCapacity, savedState);
        AQEHostRegistration replacementRegistration = AQEHostRegistration.open(owner, replacement);
        AQEHostRegistration previousRegistration = aqe$hostRegistration;
        aqe$bigHost = replacement;
        aqe$hostRegistration = replacementRegistration;
        if (previousRegistration != null) {
            previousRegistration.closeForReplacement();
        }
    }

    @Unique
    private synchronized void advancedQuantumEngineering$closeHost() {
        AQEHostRegistration registration = aqe$hostRegistration;
        aqe$hostRegistration = null;
        aqe$bigHost = null;
        if (registration != null) {
            registration.close();
        }
    }

    @Unique
    private Level advancedQuantumEngineering$lifecycleOwner() {
        if (!blockEntities.isEmpty()) {
            Level level = blockEntities.get(0).getLevel();
            if (level != null) {
                return level;
            }
        }
        return getLevel();
    }

    @Unique
    private BigInteger aqe$calculatePhysicalCapacity() {
        advancedQuantumEngineering$ensureStructureAggregates();
        return aqe$cachedPhysicalCapacity;
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
        advancedQuantumEngineering$ensureStructureAggregates();
        return aqe$cachedEffectiveCoprocessors;
    }

    @Unique
    private synchronized void advancedQuantumEngineering$markStructureDirty() {
        aqe$structureRevision = nextRevision(aqe$structureRevision);
        aqe$structureDirty = true;
    }

    @Unique
    private synchronized void advancedQuantumEngineering$markActiveCpuDirty() {
        aqe$activeCpuRevision = nextRevision(aqe$activeCpuRevision);
    }

    @Unique
    private synchronized void advancedQuantumEngineering$ensureStructureAggregates() {
        if (!aqe$structureDirty && aqe$cachedStructureRevision == aqe$structureRevision) {
            AQERevisionMetrics.recordCoprocessorHotPathReuse();
            return;
        }
        BigInteger storageTotal = BigInteger.ZERO;
        BigInteger multiplierTotal = BigInteger.ZERO;
        long summedAccelerators = 0L;
        long summedMultipliers = 0L;
        boolean hasBigCore = false;
        for (AdvCraftingBlockEntity blockEntity : blockEntities) {
            hasBigCore |= blockEntity instanceof BigIntegerStorageProvider;
            storageTotal = BigIntegerCapacityMath.add(
                    storageTotal,
                    BigIntegerCapacityMath.checkedNonNegative(
                            blockEntity instanceof BigIntegerStorageProvider provider
                                    ? provider.getBigIntegerStorageBytes()
                                    : BigInteger.valueOf(Math.max(0L, blockEntity.getStorageBytes())),
                            "AQE storage contribution",
                            AQEConfig.MAX_BIG_INTEGER_BITS),
                    "AQE summed storage",
                    AQEConfig.MAX_BIG_INTEGER_BITS);
            int multiplier = Math.max(0, blockEntity.getStorageMultiplier());
            if (multiplier > 0) {
                multiplierTotal = BigIntegerCapacityMath.add(
                        multiplierTotal,
                        BigInteger.valueOf(multiplier),
                        "AQE summed storage multiplier",
                        AQEConfig.MAX_BIG_INTEGER_BITS);
            }
            summedAccelerators = safeAddCoprocessors(
                    summedAccelerators, Math.max(0L, blockEntity.getAcceleratorThreads()));
            summedMultipliers = safeAddCoprocessors(
                    summedMultipliers, Math.max(0L, blockEntity.getAccelerationMultiplier()));
        }
        BigInteger physical = multiplierTotal.signum() > 0
                ? BigIntegerCapacityMath.multiply(
                        storageTotal,
                        multiplierTotal,
                        "AQE effective storage",
                        AQEConfig.MAX_BIG_INTEGER_BITS)
                : storageTotal;
        long effective = summedMultipliers > 0
                ? safeMultiplyCoprocessors(summedAccelerators, summedMultipliers)
                : summedAccelerators;
        this.accelerator = (int) summedAccelerators;
        this.acceleratorMultiplier = (int) summedMultipliers;
        aqe$cachedStorageContributions = storageTotal;
        aqe$cachedStorageMultipliers = multiplierTotal;
        aqe$cachedPhysicalCapacity = physical;
        aqe$cachedEffectiveCoprocessors = effective;
        aqe$cachedHasBigCore = hasBigCore;
        aqe$cachedStructureRevision = aqe$structureRevision;
        aqe$structureDirty = false;
        AQERevisionMetrics.recordStructureScan();
    }

    @Unique
    private static long nextRevision(long revision) {
        return revision == Long.MAX_VALUE ? 1L : revision + 1L;
    }

    @Unique
    private static int clampDisplayCount(long value) {
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.max(0L, value);
    }

    @Unique
    private static long safeDisplayAdd(long left, long right) {
        if (left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }

    @Inject(method = "submitJob", at = @At("TAIL"), require = 0)
    private void advancedQuantumEngineering$markCpuAdded(
            IGrid grid,
            ICraftingPlan plan,
            IActionSource source,
            ICraftingRequester requester,
            CallbackInfoReturnable<ICraftingSubmitResult> cir) {
        advancedQuantumEngineering$markActiveCpuDirty();
    }

    @Inject(method = "cancelJobs", at = @At("TAIL"), require = 0)
    private void advancedQuantumEngineering$markAllCpusChanged(CallbackInfo ci) {
        advancedQuantumEngineering$markActiveCpuDirty();
    }

    @Inject(method = "cancelJob", at = @At("TAIL"), require = 0)
    private void advancedQuantumEngineering$markCpuCancelled(UUID jobId, CallbackInfo ci) {
        advancedQuantumEngineering$markActiveCpuDirty();
    }

    @Inject(method = "deactivate", at = @At("TAIL"), require = 0)
    private void advancedQuantumEngineering$markCpuDeactivated(UUID jobId, CallbackInfo ci) {
        advancedQuantumEngineering$markActiveCpuDirty();
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
