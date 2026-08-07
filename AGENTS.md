# Advanced Quantum Engineering agent entrypoint

このファイルはCodex・LLM・自動レビューが、リポジトリ全体を毎回読み込まずに作業範囲を決めるための入口です。

## 最小読込手順

1. 最初に本書と [`docs/CODEBASE_MAP.md`](docs/CODEBASE_MAP.md) だけを読む。
2. MapのTask routeを1つ選び、そのrouteに記載された文書・package・直近testだけを開く。
3. 大きなJavaファイルは対象symbolを検索し、必要なmethod周辺だけを読む。
4. compile error、test failure、実依存関係が示した場合だけ隣接packageへ範囲を広げる。
5. `build/**`、`.gradle/**`、生成JAR、全release notes、全resources、全testの再帰読込を開始条件にしない。

## 固定契約

```text
Minecraft                 1.21.1
Loader                    NeoForge 21.1.247+
Runtime Java              21
Applied Energistics 2     19.2.17
Advanced AE               1.6.11-1.21.1
AE2 Omni Cells            1.1.6
AE2 Crafting Optimizer    optional 1.6.x
Forge 1.20.1 line         AQE 2.2.x
```

Advanced AEのQuantum Computer multiblock所有権を維持します。AQE blockは既存slot roleの置換であり、別のcrafting CPU systemや追加structure slotを作りません。一構造一core、wall/frame/internal/network/size ruleはAdvanced AEがauthorityです。

BigInteger計算はexact physical/reserved/available totalsを保持し、既存`long` APIへ渡す値だけを明示的にsaturateします。暗黙の`longValue()`、overflow wrap、保存値の切り捨てを入れません。Co-processorはAE2/Advanced AEの`int`契約を超えません。

ACO integrationはoptionalです。ACO不在でもAQE単体がloadし、Quantum Computer block behaviorとBigInteger capacity accountingが成立する必要があります。

## 安全規則

- Advanced AEのstructure validityをAQE独自判定で置き換えない。
- normal/modified/experimental/BigInteger coreのone-core ruleを崩さない。
- capacity sumとData Entangler multiplierはchecked BigInteger arithmeticを使う。
- Mixin target、method descriptor、optional integration versionが不一致なら推測で継続しない。
- build/test成功だけで実Quantum Computer、save/restart、ACO連携を検証済みと書かない。

## 編集規則

- source変更では同じpackageの`src/test`と [`docs/TESTING.md`](docs/TESTING.md) を先に確認する。
- capacity/ownership変更はREADMEと [`docs/IMPLEMENTATION.md`](docs/IMPLEMENTATION.md) を同じ変更で更新する。
- entrypoint、主要package、Mixin、重要testの位置が変わる場合は `docs/CODEBASE_MAP.md` を更新する。
- `BigIntegerCapacitySnapshot`、ACO backend、diagnostics、Mixin類は対象method周辺だけを読む。

## 検証順

```text
対象test class
-> ./gradlew test --no-daemon
-> ./gradlew clean test build --no-daemon
-> 必要な場合だけNeoForge実環境でstructure / capacity / save / restart確認
```

unit testやCIだけの結果をruntime verifiedとして扱いません。
