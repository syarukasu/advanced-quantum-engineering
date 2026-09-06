package com.syaru.advancedquantumengineering.mixin;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.me.service.CraftingService;
import com.syaru.advancedquantumengineering.AdvancedQuantumEngineering;
import com.syaru.advancedquantumengineering.integration.AqeExactCpuAccess;
import com.syaru.advancedquantumengineering.integration.AqePhysicalExecution;
import com.syaru.advancedquantumengineering.integration.AqeExecutionProgress;
import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.pedroksl.advanced_ae.common.cluster.AdvCraftingCPU;
import net.pedroksl.advanced_ae.common.logic.AdvCraftingCPULogic;
import net.pedroksl.advanced_ae.common.logic.ElapsedTimeTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** AQE owns its exact CPU lifecycle; normal Advanced AE jobs remain untouched. */
@Mixin(value = AdvCraftingCPULogic.class, remap = false, priority = 1200)
public abstract class AdvCraftingExactLogicMixin implements AqeExactCpuAccess {
    @Shadow @Final private AdvCraftingCPU cpu;
    @Shadow protected abstract void finishJob(boolean successful);
    @Shadow protected abstract void postChange(AEKey key);
    @Shadow public abstract appeng.api.networking.crafting.ICraftingLink getLastLink();
    @Unique private BigInteger aqe$reservation;
    @Unique private AqePhysicalExecution aqe$physical;
    @Unique private CompoundTag aqe$saved;
    @Unique private AqeExecutionProgress aqe$progress;
    @Unique private String aqe$lastState;
    @Unique private Map<AEKey, BigInteger> aqe$stored = Map.of();
    @Unique private Map<AEKey, BigInteger> aqe$waiting = Map.of();
    @Unique private Map<AEKey, BigInteger> aqe$pending = Map.of();

    @Override public BigInteger aqe$exactReservation() { return aqe$reservation; }
    @Override public boolean aqe$hasPhysicalExecution() { return aqe$saved != null; }

    @Override
    public void aqe$installExactSubmission(AqePhysicalExecution.Submission submission) {
        aqe$reservation = submission.bytes();
        aqe$physical = submission.physical();
        if (aqe$physical != null) {
            aqe$saved = aqe$physical.save();
            aqe$progress = new AqeExecutionProgress(0L);
            aqe$refreshStatus();
        }
        cpu.markDirty();
    }

    @Inject(method = "tickCraftingLogic", at = @At("HEAD"), cancellable = true)
    private void aqe$advanceExactJob(IEnergyService energy, CraftingService crafting, CallbackInfo ci) {
        if (!aqe$hasPhysicalExecution()) return;
        ci.cancel();
        if (aqe$physical == null || !cpu.isActive() || cpu.getGrid() == null) return;
        var link = getLastLink();
        if (link != null && link.isCanceled()) aqe$physical.cancel();
        aqe$physical.advance(cpu.getGrid(), cpu.getLevel(), cpu.getSrc());
        aqe$saved = aqe$physical.save();
        aqe$progress.update(aqe$physical);
        aqe$refreshStatus();
        cpu.markDirty();
        String state = aqe$physical.state();
        String diagnostic = state + ": " + aqe$physical.detail();
        if (!diagnostic.equals(aqe$lastState)) {
            AdvancedQuantumEngineering.LOGGER.info("AQE exact job {}: {} ({})",
                    aqe$physical.jobId(), state, aqe$physical.detail());
            aqe$lastState = diagnostic;
        }
        if (state.equals("COMPLETE") || state.equals("CANCELLED")) {
            // Receipt return is complete before the original CPU closes its link.
            finishJob(state.equals("COMPLETE"));
            cpu.updateOutput(null);
            cpu.deactivate();
        }
    }

    @Unique
    private void aqe$refreshStatus() {
        var stored = aqe$physical.stored();
        var waiting = aqe$physical.waiting();
        var pending = aqe$physical.pending();
        if (stored.equals(aqe$stored) && waiting.equals(aqe$waiting) && pending.equals(aqe$pending)) return;
        Set<AEKey> changed = new java.util.HashSet<>(aqe$stored.keySet());
        changed.addAll(aqe$waiting.keySet());
        changed.addAll(aqe$pending.keySet());
        changed.addAll(stored.keySet());
        changed.addAll(waiting.keySet());
        changed.addAll(pending.keySet());
        aqe$stored = stored;
        aqe$waiting = waiting;
        aqe$pending = pending;
        changed.forEach(this::postChange);
    }

    @Inject(method = "cancel", at = @At("HEAD"), cancellable = true)
    private void aqe$requestExactCancellation(CallbackInfo ci) {
        if (!aqe$hasPhysicalExecution()) return;
        ci.cancel();
        if (aqe$physical != null) {
            aqe$physical.cancel();
            aqe$saved = aqe$physical.save();
            cpu.markDirty();
        }
    }

    @Inject(method = "insert", at = @At("HEAD"), cancellable = true)
    private void aqe$rejectUnreceiptedOutput(AEKey key, long amount, Actionable mode,
            CallbackInfoReturnable<Long> cir) {
        if (aqe$hasPhysicalExecution()) cir.setReturnValue(0L);
    }

    @Inject(method = "finishJob", at = @At("RETURN"))
    private void aqe$clearFinishedReservation(boolean successful, CallbackInfo ci) {
        aqe$reservation = null;
        aqe$physical = null;
        aqe$saved = null;
        aqe$lastState = null;
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void aqe$saveExactJob(CompoundTag owner, CallbackInfo ci) {
        if (aqe$reservation == null) return;
        owner.putString("aqeExactBytes", aqe$reservation.toString());
        if (aqe$saved != null) {
            owner.put("aqePhysicalExecution", aqe$saved.copy());
            if (aqe$progress != null) owner.putLong("aqeExactElapsed", aqe$progress.getElapsedTime());
        }
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void aqe$loadExactJob(CompoundTag owner, CallbackInfo ci) {
        aqe$reservation = null;
        aqe$physical = null;
        aqe$saved = owner.contains("aqePhysicalExecution", 10)
                ? owner.getCompound("aqePhysicalExecution").copy() : null;
        if (!owner.contains("aqeExactBytes", 8)) return;
        String bytes = owner.getString("aqeExactBytes");
        if (bytes.length() > 16384 || !bytes.matches("[1-9][0-9]*")) {
            throw new IllegalArgumentException("invalid AQE exact CPU reservation");
        }
        aqe$reservation = new BigInteger(bytes);
        if (aqe$saved != null) {
            try {
                if (getLastLink() == null) {
                    throw new IllegalStateException("the saved physical transaction has no CPU job link");
                }
                aqe$physical = AqePhysicalExecution.load(aqe$saved);
                if (!aqe$reservation.equals(aqe$physical.bytes())) {
                    throw new IllegalArgumentException("AQE reservation does not match the saved transaction");
                }
                aqe$progress = new AqeExecutionProgress(owner.getLong("aqeExactElapsed"));
                aqe$refreshStatus();
            } catch (RuntimeException | LinkageError failure) {
                aqe$physical = null;
                AdvancedQuantumEngineering.LOGGER.error("AQE exact job is paused; its saved transaction is retained", failure);
            }
        }
    }

    @Unique private static long aqe$display(BigInteger amount) {
        return amount.min(BigInteger.valueOf(Long.MAX_VALUE)).longValueExact();
    }
    @Inject(method = "getStored", at = @At("HEAD"), cancellable = true)
    private void aqe$getStored(AEKey key, CallbackInfoReturnable<Long> cir) {
        if (aqe$hasPhysicalExecution()) cir.setReturnValue(aqe$display(aqe$stored.getOrDefault(key, BigInteger.ZERO)));
    }
    @Inject(method = "getWaitingFor", at = @At("HEAD"), cancellable = true)
    private void aqe$getWaiting(AEKey key, CallbackInfoReturnable<Long> cir) {
        if (aqe$hasPhysicalExecution()) cir.setReturnValue(aqe$display(aqe$waiting.getOrDefault(key, BigInteger.ZERO)));
    }
    @Inject(method = "getPendingOutputs", at = @At("HEAD"), cancellable = true)
    private void aqe$getPending(AEKey key, CallbackInfoReturnable<Long> cir) {
        if (aqe$hasPhysicalExecution()) cir.setReturnValue(aqe$display(aqe$pending.getOrDefault(key, BigInteger.ZERO)));
    }
    @Inject(method = "getAllItems", at = @At("HEAD"), cancellable = true)
    private void aqe$getItems(KeyCounter items, CallbackInfo ci) {
        if (!aqe$hasPhysicalExecution()) return;
        ci.cancel();
        // The caller only needs the key union here; amount getters supply UI projections.
        Set<AEKey> keys = new java.util.HashSet<>(aqe$stored.keySet());
        keys.addAll(aqe$waiting.keySet());
        keys.addAll(aqe$pending.keySet());
        keys.forEach(key -> items.add(key, 1L));
    }
    @Inject(method = "getAllWaitingFor", at = @At("HEAD"), cancellable = true)
    private void aqe$getWaitingKeys(Set<AEKey> keys, CallbackInfo ci) {
        if (aqe$hasPhysicalExecution()) { ci.cancel(); keys.addAll(aqe$waiting.keySet()); }
    }
    @Inject(method = "getElapsedTimeTracker", at = @At("HEAD"), cancellable = true)
    private void aqe$getProgress(CallbackInfoReturnable<ElapsedTimeTracker> cir) {
        if (aqe$hasPhysicalExecution() && aqe$progress != null) cir.setReturnValue(aqe$progress);
    }
}
