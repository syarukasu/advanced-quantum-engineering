package com.syaru.advancedquantumengineering.integration;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

/** No compile-time ACO dependency; only versioned public APIs are accessed. */
public final class AqePhysicalExecution {
    private static final String API = "com.syaru.ae2craftingoptimizer.api.big.BigCraftingPhysicalExecution";
    private final Object delegate;
    private final Methods methods;

    private AqePhysicalExecution(Object delegate) throws ReflectiveOperationException {
        this.delegate = delegate;
        this.methods = new Methods(delegate.getClass());
    }

    public record Submission(BigInteger bytes, AqePhysicalExecution physical) { }

    public static Submission prepare(ICraftingPlan plan, UUID jobId, IGrid grid,
            Level level, IActionSource source) {
        if (!ModList.get().isLoaded(BigCraftingIntegration.ACO_MODID)) return null;
        // ACO 1.x still owns its old handoff. Never install a second executor over it.
        var version = ModList.get().getModContainerById(BigCraftingIntegration.ACO_MODID)
                .orElseThrow().getModInfo().getVersion();
        if (version.getMajorVersion() < 2) return null;
        try {
            Class<?> engine = Class.forName("com.syaru.ae2craftingoptimizer.api.big.BigCraftingEngineApi");
            Optional<?> view = (Optional<?>) call(engine.getMethod("inspectBigIntegerPlan", ICraftingPlan.class),
                    null, plan);
            if (view.isEmpty()) return null;
            Object exact = view.orElseThrow();
            BigInteger bytes = (BigInteger) call(exact.getClass().getMethod("exactBytes"), exact);
            Class<?> api = Class.forName(API);
            if (api.getField("API_VERSION").getInt(null) != 1) {
                throw new IllegalStateException("unsupported ACO physical API version");
            }
            if ((boolean) call(api.getMethod("validateNativePlan", ICraftingPlan.class, IGrid.class),
                    null, plan, grid)) return new Submission(bytes, null);
            Object prepared = call(api.getMethod("prepare", ICraftingPlan.class, UUID.class,
                    IGrid.class, Level.class, IActionSource.class), null, plan, jobId, grid, level, source);
            return new Submission(bytes, new AqePhysicalExecution(prepared));
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("AQE requires the ACO physical execution API for wide jobs", failure);
        }
    }

    public static AqePhysicalExecution load(CompoundTag saved) {
        try {
            Class<?> api = Class.forName(API);
            if (api.getField("API_VERSION").getInt(null) != 1) {
                throw new IllegalStateException("unsupported ACO physical API version");
            }
            return new AqePhysicalExecution(call(api.getMethod("load", CompoundTag.class), null, saved));
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("cannot restore AQE exact execution", failure);
        }
    }

    public void advance(IGrid grid, Level level, IActionSource source) { call(methods.advance, delegate, grid, level, source); }
    public void cancel() { call(methods.cancel, delegate); }
    public CompoundTag save() { return (CompoundTag) call(methods.save, delegate); }
    public String state() { return (String) call(methods.state, delegate); }
    public String detail() { return (String) call(methods.detail, delegate); }
    public int numerator() { return (int) call(methods.numerator, delegate); }
    public int denominator() { return (int) call(methods.denominator, delegate); }
    public BigInteger bytes() { return (BigInteger) call(methods.bytes, delegate); }
    public UUID jobId() { return (UUID) call(methods.jobId, delegate); }
    @SuppressWarnings("unchecked")
    public Map<AEKey, BigInteger> stored() { return (Map<AEKey, BigInteger>) call(methods.stored, delegate); }
    @SuppressWarnings("unchecked")
    public Map<AEKey, BigInteger> waiting() { return (Map<AEKey, BigInteger>) call(methods.waiting, delegate); }
    @SuppressWarnings("unchecked")
    public Map<AEKey, BigInteger> pending() { return (Map<AEKey, BigInteger>) call(methods.pending, delegate); }

    private static Object call(Method method, Object owner, Object... args) {
        try {
            return method.invoke(owner, args);
        } catch (InvocationTargetException failure) {
            if (failure.getCause() instanceof RuntimeException runtime) throw runtime;
            if (failure.getCause() instanceof Error error) throw error;
            throw new IllegalStateException("ACO physical API failed", failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("ACO physical API is inaccessible", failure);
        }
    }

    private static final class Methods {
        final Method advance, cancel, save, state, detail, numerator, denominator, bytes, jobId, stored, waiting, pending;
        Methods(Class<?> api) throws NoSuchMethodException {
            advance = api.getMethod("advance", IGrid.class, Level.class, IActionSource.class);
            cancel = api.getMethod("requestCancellation");
            save = api.getMethod("save");
            state = api.getMethod("state");
            detail = api.getMethod("detail");
            numerator = api.getMethod("progressNumerator");
            denominator = api.getMethod("progressDenominator");
            bytes = api.getMethod("reservedBytes");
            jobId = api.getMethod("jobId");
            stored = api.getMethod("storedItems");
            waiting = api.getMethod("waitingItems");
            pending = api.getMethod("pendingItems");
        }
    }
}
