package com.syaru.advancedquantumengineering.integration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/** 1.21.1向けACO 1.5.9のAPI v3がある場合だけReflectionで読み込む。 */
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
    private final Method registerExternalPlanConsumer;
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
        this.registerExternalPlanConsumer = optionalMethod(
                apiType,
                "registerExternalBigIntegerPlanConsumer");
        this.unregister = registryType.getMethod("unregister", Object.class);
        Class<?> registrationType = Class.forName(
                "com.syaru.ae2craftingoptimizer.api.big.BigCraftingHostRegistration",
                false,
                loader);
        this.registrationClose = registrationType.getMethod("close");
        this.runtimeMethods = new RuntimeMethods(hostType);
        // AQEがBigInteger提出境界を所有することをACOへ能力として通知する。
        if (registerExternalPlanConsumer != null) {
            invoke(registerExternalPlanConsumer, null);
        }
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
            Object owner,
            BigInteger physicalCapacity,
            CompoundTag savedState) {
        Objects.requireNonNull(owner, "owner");
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
        Object registration = invoke(register, null, owner, runtime);
        return new Host(owner, runtime, registration, registrationClose, unregister, runtimeMethods);
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
            Method save,
            SnapshotMethods snapshot) {
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
                    hostType.getMethod("save"),
                    SnapshotMethods.find(hostType));
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

    private record SnapshotMethods(
            Method snapshot,
            Method physicalCapacity,
            Method reserved,
            Method available,
            Method standardJobCount,
            Method bigJobCount,
            Method managedChildJobCount,
            Method overcommitted,
            Method backendState,
            Method activeState) {
        private static SnapshotMethods find(Class<?> hostType) {
            try {
                Class<?> stateType = Class.forName(
                        "com.syaru.ae2craftingoptimizer.api.big.BigCraftingHostBackendState",
                        false,
                        hostType.getClassLoader());
                Class<?> snapshotType = Class.forName(
                        "com.syaru.ae2craftingoptimizer.api.big.BigCraftingHostSnapshot",
                        false,
                        hostType.getClassLoader());
                return new SnapshotMethods(
                        hostType.getMethod("snapshot", long.class, stateType),
                        snapshotType.getMethod("physicalCapacity"),
                        snapshotType.getMethod("reserved"),
                        snapshotType.getMethod("available"),
                        snapshotType.getMethod("standardJobCount"),
                        snapshotType.getMethod("bigJobCount"),
                        snapshotType.getMethod("managedChildJobCount"),
                        snapshotType.getMethod("overcommitted"),
                        snapshotType.getMethod("backendState"),
                        stateType.getMethod("valueOf", String.class));
            } catch (ReflectiveOperationException unsupportedOlderAco) {
                return null;
            }
        }
    }

    private static final class Host implements AQEBigCraftingHost {
        private final Object owner;
        private final Object runtime;
        private final Object registration;
        private final Method registrationClose;
        private final Method unregister;
        private final RuntimeMethods methods;
        private boolean closed;

        private Host(
                Object owner,
                Object runtime,
                Object registration,
                Method registrationClose,
                Method unregister,
                RuntimeMethods methods) {
            this.owner = owner;
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
        public synchronized AQEHostSnapshot snapshot(long revision) {
            SnapshotMethods snapshot = methods.snapshot();
            if (snapshot == null) {
                return AQEBigCraftingHost.super.snapshot(revision);
            }
            Object activeState = invoke(snapshot.activeState(), null, "ACTIVE");
            Object raw = invoke(snapshot.snapshot(), runtime, revision, activeState);
            return new AQEHostSnapshot(
                    revision,
                    (BigInteger) invoke(snapshot.physicalCapacity(), raw),
                    (BigInteger) invoke(snapshot.reserved(), raw),
                    (BigInteger) invoke(snapshot.available(), raw),
                    ((Number) invoke(snapshot.standardJobCount(), raw)).longValue(),
                    ((Number) invoke(snapshot.bigJobCount(), raw)).longValue(),
                    ((Number) invoke(snapshot.managedChildJobCount(), raw)).longValue(),
                    (boolean) invoke(snapshot.overcommitted(), raw),
                    ((Enum<?>) invoke(snapshot.backendState(), raw)).name());
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
                    invoke(unregister, null, owner);
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
