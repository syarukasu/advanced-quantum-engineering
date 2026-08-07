# Advanced Quantum Engineering codebase map

> **Navigation only.** このMapはCodex・LLM・reviewerの探索量を減らすためのindexです。製品仕様の正本はREADMEと現行docsです。

## 使い方

1. [`../AGENTS.md`](../AGENTS.md)を読む。
2. 下のTask routeを1つ選ぶ。
3. `Read first`と`Source scope`だけを開き、symbol検索から始める。
4. compile/test結果が別package依存を示した場合だけscopeを広げる。

初期読込の対象外:

```text
build/**
.gradle/**
生成JAR / run directory / logs
全release notes
全resources / 全testの再帰読込
対象外package
```

## 固定座標

```text
Minecraft        1.21.1
NeoForge         21.1.247+
Java             21
AE2              19.2.17
Advanced AE      1.6.11-1.21.1
AE2 Omni Cells   1.1.6
ACO              optional 1.6.x
```

## Task router

| Route | Task | Read first | Source scope | Verification scope |
| --- | --- | --- | --- | --- |
| `C0` | 製品定義、version line、文書 | `../README.md`, `IMPLEMENTATION.md` | docs中心 | 文書差分、`build` |
| `Q1` | Quantum Computer slot role、core/storage/accelerator/multi-threader/entangler | READMEのBlocks/Structure Rules、`IMPLEMENTATION.md` | `block`, `blockentity`, `registry` | structure/Mixin test、実structure確認 |
| `B1` | BigInteger capacity、sum/multiplier、saturated facade、format | READMEのBigInteger節、`IMPLEMENTATION.md` | `integration`のcapacity/host/formatter classes | capacity math/snapshot tests |
| `A1` | Optional ACO BigInteger host/backend | READMEのACO説明、`IMPLEMENTATION.md` | `integration`のACO/BigCrafting classes、必要なMixin | ACO backend/metadata/host tests |
| `M1` | Advanced AE/AE2 Mixin、runtime method compatibility | `IMPLEMENTATION.md`, `TESTING.md` | `mixin`と直接targetだけ | Mixin contract tests、実load |
| `U1` | Client display、tooltip、screen integration | READMEの表示仕様 | `client`, display formatter/marker | formatter tests、client確認 |
| `K1` | Config、limits、experimental values | READMEのConfiguration、`IMPLEMENTATION.md` | `config`と値参照元 | config/limit test、起動確認 |
| `R1` | Blocks/items/recipes/models/lang/metadata | READMEのBlocks/Recipes | `registry`, `item`, 対象resources namespace | resource validation、game load |
| `V1` | Build、CI、publishing、release evidence | `TESTING.md`, `PUBLISHING.md`, `.github/workflows/build.yml` | `build.gradle`, `gradle.properties`, `src/test` | `clean test build` |

## Package map

| Package/path | Responsibility |
| --- | --- |
| `AdvancedQuantumEngineering.java` | NeoForge mod entrypoint、registration、integration bootstrap |
| `block` | Advanced AE crafting unit roleを再利用するAQE blocks |
| `blockentity` | modified/experimental/BigInteger part metadataとruntime state |
| `integration` | exact capacity snapshot、BigInteger host、ACO backend、diagnostics、display bridge |
| `mixin` | Advanced AE/AE2 Quantum Computer internalsへのversion固定hook |
| `registry` | blocks/items/block entities/creative tab registration |
| `client` | client-only表示・model・tooltip integration |
| `config` | common config、capacity/co-processor limits |
| `item` | BlockItem/tooltipなどのitem behavior |
| `src/main/resources` | NeoForge metadata、Mixin descriptor、assets、recipes/tags |
| `src/test` | capacity、host/backend、formatter、Mixin contract tests |

## 主要entrypointとhot files

| Purpose | Path |
| --- | --- |
| Mod entrypoint | `src/main/java/com/syaru/advancedquantumengineering/AdvancedQuantumEngineering.java` |
| Exact capacity snapshot | `src/main/java/com/syaru/advancedquantumengineering/integration/BigIntegerCapacitySnapshot.java` |
| Capacity math | `src/main/java/com/syaru/advancedquantumengineering/integration/BigIntegerCapacityMath.java` |
| Big crafting integration | `src/main/java/com/syaru/advancedquantumengineering/integration/BigCraftingIntegration.java` |
| ACO backend | `src/main/java/com/syaru/advancedquantumengineering/integration/AcoBigCraftingBackend.java` |
| Advanced AE bridge | `src/main/java/com/syaru/advancedquantumengineering/integration/AdvancedAEIntegration.java` |
| Diagnostics | `src/main/java/com/syaru/advancedquantumengineering/integration/AQEDiagnostics.java` |
| BigInteger core BE | `src/main/java/com/syaru/advancedquantumengineering/blockentity/BigIntegerQuantumCoreBlockEntity.java` |
| Mod metadata | `src/main/resources/META-INF/neoforge.mods.toml` |

Mixin file名とtargetは、作業開始時に`src/main/java/com/syaru/advancedquantumengineering/mixin`とMixin descriptorから対象だけ確認する。

## Test map

| Concern | Primary tests |
| --- | --- |
| BigInteger arithmetic | `integration/BigIntegerCapacityMathTest.java` |
| Complete structure capacity snapshot | `integration/BigIntegerCapacitySnapshotTest.java` |
| Optional ACO backend | `integration/AcoBigCraftingBackendTest.java` |
| Local host behavior | `integration/LocalBigCraftingHostTest.java` |
| ACO optional metadata | `integration/OptionalAcoMetadataTest.java` |
| Storage text/display | `integration/CraftingStorageFormatterTest.java` |
| Advanced AE target compatibility | `src/test/.../mixin`の対象contract test |

## 文書の読み分け

| Need | Document |
| --- | --- |
| ユーザー向けcontract、versions、blocks、rules | `../README.md` |
| runtime/BigInteger/Mixin設計 | `IMPLEMENTATION.md` |
| test手順と受入 | `TESTING.md` |
| 調査背景 | `RESEARCH.md` |
| 公開手順 | `PUBLISHING.md` |
| version固有履歴 | 該当するrelease notesだけ |

## 最小検証コマンド

```text
./gradlew test --no-daemon
./gradlew clean test build --no-daemon
```

実NeoForge process、Quantum Computer形成、capacity表示、crafting、save/restartを実行していない場合は、その未実施を結果に明記する。

## 省トークン用prompt

```text
AGENTS.mdとdocs/CODEBASE_MAP.mdの<Route ID>だけを基準に作業する。
Task: <作業内容>
最初はroute記載の文書、package、直近test以外を読まない。
別scopeへ広げる場合はcompile dependencyまたはtest failureを根拠として示す。
```
