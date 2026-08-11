package com.syaru.advancedquantumengineering.integration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/** 互換範囲内の任意ACO 1.3.xから1.5.x API v3がある場合だけReflectionで読み込む。 */
public final class AcoBigCraftingBackend implements AQEBigCraftingBackend {
    private static final int REQUIRED_API_VERSION = 3;
    private static final String API_CLASS =
            "com.syaru.ae2craftingoptimizer.api.big.BigCraftingEngineApi";
    private static final String CODEC_CLASS =
            "com.syaru.ae2craftingoptimizer.api.big.AeKeyBigCraftingCodec";
    private static final String KEY_CODEC_CLASS =
            "com.syaru.ae2craftingoptimizer.engine.BigCraftingKeyCodec";
    private static final String HOST_CLASS =
            "com.syaru.ae2craftingoptimizer.api.big.BigCraftingHostRuntime";
    private static final String REGISTRY_CLASS =
            "com.syaru.ae2craftingoptimizer.api.big.BigCraftingHostRegistry";

    private final Object keyCodec;
    private final Method isEnabled;
    private final Method isCalculationProfileActive;
    private final Method createHost;
    private final Method loadHost;
    private final Method register;
    private final Method unregister;
    private final Method registrationClose;
    private final RuntimeMethods runtimeMethods;

    public AcoBigCraftingBackend() throws ReflectiveOperationException {
        ClassLoader loader = AcoBigCraftingBackend.class.getClassLoader();
        Class<?> apiType = Class.forName(API_CLASS, false, loader);
        Class<?> codecType = Class.forName(CODEC_CLASS, false, loader);
        Class<?> keyCodecType = Class.forName(KEY_CODEC_CLASS, false, loader);
        Class<?> hostType = Class.forName(HOST_CLASS, false, loader);
        Class<?> registryType = Class.forName(REGISTRY_CLASS, false, loader);

        Field apiVersion = apiType.getField("API_VERSION");
        int detectedApiVersion = apiVersion.getInt(null);
        if (detectedApiVersion != REQUIRED_API_VERSION) {
            throw new IllegalStateException(
                    "AQE requires ACO BigInteger API " + REQUIRED_API_VERSION
                            + ", found " + detectedApiVersion);
        }

        this.keyCodec = codecType.getField("INSTANCE").get(null);
        if (!keyCodecType.isInstance(keyCodec)) {
            throw new IllegalStateException("ACO AEKey codec does not implement its advertised API");
        }
        this.isEnabled = apiType.getMethod("isEnabled");
        // 計算プロファイル照会は新しいACOで追加されたため、古いAPIでは任意メソッドとして扱う。
        this.isCalculationProfileActive = optionalMethod(apiType, "isCalculationProfileActive");
        this.createHost = apiType.getMethod("createHost", BigInteger.class, keyCodecType);
        this.loadHost = apiType.getMethod(
                "loadHost", CompoundTag.class, BigInteger.class, keyCodecType);
        this.register = registryType.getMethod("register", Object.class, hostType);
        this.unregister = registryType.getMethod("unregister", Object.class);
        Class<?> registrationType = Class.forName(
                "com.syaru.ae2craftingoptimizer.api.big.BigCraftingHostRegistration",
                false,
                loader);
        this.registrationClose = registrationType.getMethod("close");
        this.runtimeMethods = new RuntimeMethods(hostType);
    }

    @Override
    public boolean isAvailable() {
        // サーバー設定でBigInteger機能が無効なら、AQE側もホストを選択しない。
        if (!(boolean) invoke(isEnabled, null)) {
            return false;
        }
        // 厳密なAE2計算プロファイルが無効なら、long互換経路を二重所有しない。
        return isCalculationProfileActive == null
                || (boolean) invoke(isCalculationProfileActive, null);
    }

    private static Method optionalMethod(Class<?> owner, String name) {
        try {
            return owner.getMethod(name);
        } catch (NoSuchMethodException unsupportedOlderApi) {
            // 旧ACO APIには照会メソッドがないため、isEnabled()だけで互換判定する。
            return null;
        }
    }

    @Override
    public String id() {
        return "aco:big_crafting_v3";
    }

    @Override
    public AQEBigCraftingHost create(
            Object lifecycleOwner,
            Object backendRegistryOwner,
            BigInteger physicalCapacity,
            CompoundTag savedState) {
        Objects.requireNonNull(lifecycleOwner, "lifecycleOwner");
        Objects.requireNonNull(backendRegistryOwner, "backendRegistryOwner");
        Object runtime;
        if (AQEBigCraftingHostState.isPresent(savedState)) {
            AQEBigCraftingHostState.Decoded decoded = AQEBigCraftingHostState.decode(savedState);
            if (!id().equals(decoded.backend())) {
                throw new IllegalArgumentException(
                        "saved AQE backend " + decoded.backend() + " cannot be loaded by " + id());
            }
            runtime = invoke(loadHost, null, decoded.payload(), physicalCapacity, keyCodec);
        } else {
            runtime = invoke(createHost, null, physicalCapacity, keyCodec);
        }
        /*
         * ACO looks up a host with AdvCraftingCPUCluster identity during
         * submitJob. Register under that real cluster object rather than the
         * AQE generation token used by AQE's separate lifecycle registry.
         */
        Object registration = invoke(register, null, backendRegistryOwner, runtime);
        return new Host(
                lifecycleOwner,
                backendRegistryOwner,
                runtime,
                registration,
                registrationClose,
                unregister,
                runtimeMethods);
    }

    /**
     * Keeps the pre-owner-split test and integration call shape working.
     * Older callers used one identity for both lifecycle and ACO lookup.
     */
    public AQEBigCraftingHost create(
            Object owner,
            BigInteger physicalCapacity,
            CompoundTag savedState) {
        return create(owner, owner, physicalCapacity, savedState);
    }

    private static Object invoke(Method method, Object target, Object... arguments) {
        try {
            return method.invoke(target, arguments);
        } catch (IllegalAccessException failure) {
            throw new IllegalStateException("ACO API method became inaccessible: " + method, failure);
        } catch (InvocationTargetException failure) {
            Throwable cause = failure.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException("ACO API invocation failed: " + method, cause);
        }
    }

    private record RuntimeMethods(
            Method resizePhysicalCapacity,
            Method replaceExternalReservations,
            Method physicalCapacity,
            Method reserved,
            Method available,
            Method availableAsSaturatedLong,
            Method bigReserved,
            Method bigJobCount,
            Method managedChildJobCount,
            Method save) {
        private RuntimeMethods(Class<?> hostType) throws NoSuchMethodException {
            this(
                    hostType.getMethod("resizePhysicalCapacity", BigInteger.class),
                    hostType.getMethod("replaceExternalReservations", Map.class),
                    hostType.getMethod("physicalCapacity"),
                    hostType.getMethod("reserved"),
                    hostType.getMethod("available"),
                    hostType.getMethod("availableAsSaturatedLong"),
                    hostType.getMethod("bigReserved"),
                    optionalMethod(hostType, "bigJobCount"),
                    optionalMethod(hostType, "managedChildJobCount"),
                    hostType.getMethod("save"));
        }

        private static Method optionalMethod(Class<?> owner, String name) {
            try {
                return owner.getMethod(name);
            } catch (NoSuchMethodException unsupportedOlderApi) {
                // ACO API v3初期版には件数getterがないため、容量連携を壊さず表示だけ0へ戻す。
                return null;
            }
        }
    }

    private static final class Host implements AQEBigCraftingHost {
        private final Object owner;
        private final Object backendRegistryOwner;
        private final Object runtime;
        private final Object registration;
        private final Method registrationClose;
        private final Method unregister;
        private final RuntimeMethods methods;
        private boolean closed;

        private Host(
                Object owner,
                Object backendRegistryOwner,
                Object runtime,
                Object registration,
                Method registrationClose,
                Method unregister,
                RuntimeMethods methods) {
            this.owner = owner;
            this.backendRegistryOwner = backendRegistryOwner;
            this.runtime = runtime;
            this.registration = registration;
            this.registrationClose = registrationClose;
            this.unregister = unregister;
            this.methods = methods;
        }

        @Override
        public synchronized void reconcile(
                BigInteger physicalCapacity,
                Map<UUID, BigInteger> standardJobReservations) {
            ensureOpen();
            synchronized (runtime) {
                invoke(methods.resizePhysicalCapacity(), runtime, physicalCapacity);
                invoke(methods.replaceExternalReservations(), runtime, standardJobReservations);
            }
        }

        @Override
        public BigInteger physicalCapacity() {
            return (BigInteger) invoke(methods.physicalCapacity(), runtime);
        }

        @Override
        public BigInteger reserved() {
            return (BigInteger) invoke(methods.reserved(), runtime);
        }

        @Override
        public BigInteger available() {
            return (BigInteger) invoke(methods.available(), runtime);
        }

        @Override
        public long availableAsSaturatedLong() {
            return ((Number) invoke(methods.availableAsSaturatedLong(), runtime)).longValue();
        }

        @Override
        public int bigJobCount() {
            Method method = methods.bigJobCount();
            // 古いACO API v3では件数同期を持たないため、容量機能を維持して0件表示にする。
            return method == null ? 0 : ((Number) invoke(method, runtime)).intValue();
        }

        @Override
        public int managedChildJobCount() {
            Method method = methods.managedChildJobCount();
            // 子Window件数も任意拡張なので、旧Backendでは通常Jobとの区別を行わない。
            return method == null ? 0 : ((Number) invoke(method, runtime)).intValue();
        }

        @Override
        public String backendId() {
            return "aco:big_crafting_v3";
        }

        @Override
        public boolean hasPersistentState() {
            return true;
        }

        @Override
        public CompoundTag save() {
            // ACO Runtimeはclose後も読み取りとNBT保存を保証するため、最終World保存を許可する。
            synchronized (runtime) {
                return AQEBigCraftingHostState.encode(
                        backendId(),
                        (BigInteger) invoke(methods.bigReserved(), runtime),
                        (CompoundTag) invoke(methods.save(), runtime));
            }
        }

        @Override
        public synchronized void close() {
            if (!closed) {
                if (registration != null) {
                    invoke(registrationClose, registration);
                } else {
                    // Compatibility with an older API that returned void from register.
                    invoke(unregister, null, backendRegistryOwner);
                }
                closed = true;
            }
        }

        private void ensureOpen() {
            if (closed) {
                throw new IllegalStateException("AQE BigInteger host is closed");
            }
        }
    }
}
