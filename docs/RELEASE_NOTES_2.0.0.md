## English

### BigInteger Quantum Computer capacity

- Added `advanced_quantum_engineering:big_integer_quantum_core` with a default exact capacity of `10^64 - 1 B`.
- Kept the original AQE core and its registry ID intact; its display name is now Long Quantum Core.
- Added checked BigInteger structure-capacity aggregation and exact shared reservation accounting across multiple Advanced AE Quantum Computer jobs.
- Added exact `10^digits - 1 B` display in the CPU list, CPU tooltip, and crafting confirmation screen.
- Added optional ACO 1.3.0 host API v3 integration for native BigInteger jobs. ACO is not a required dependency.
- Preserved opaque BigInteger job state when ACO is absent or disabled so capacity cannot be spent twice.
- Preserved Advanced AE's original multiblock shape, one-core rule, Multi-Threader behavior, Data Entangler behavior, network integration, and normal long-job path.

The BigInteger Quantum Core has no survival recipe and is intended for operator-controlled integration and stress testing. Install the same jar on the server and every client.

This release was clean-built and passed Forge client bootstrap and Arclight dedicated-server startup with ACO 1.3.0 on the documented dependency versions.

## 日本語

### BigInteger量子コンピュータ容量

- 既定の正確な容量が`10^64 - 1 B`となる`advanced_quantum_engineering:big_integer_quantum_core`を追加しました。
- 従来のAQEコアとRegistry IDは維持し、表示名をlong型量子コアへ変更しました。
- 構造全体のchecked BigInteger容量計算と、Advanced AE Quantum Computerの複数ジョブに対する正確な共有予約会計を追加しました。
- CPU一覧、CPUツールチップ、クラフト確認画面へ正確な`10^digits - 1 B`表示を追加しました。
- ネイティブBigIntegerジョブ向けにACO 1.3.0ホストAPI v3へ任意連携します。ACOは必須前提MODではありません。
- ACOが存在しない、または無効な場合も不透明なBigIntegerジョブ状態を保持し、容量の二重使用を防ぎます。
- Advanced AE本来のマルチブロック形状、一構造一コア制限、Multi-Threader、Data Entangler、ネットワーク連携、通常longジョブ経路を維持します。

BigInteger型量子コアにはサバイバルレシピがなく、管理者が制御する連携・負荷試験用です。サーバーと全クライアントへ同じJARを導入してください。

このリリースは記載した依存バージョンでクリーンビルドし、ACO 1.3.0と共にForgeクライアント起動、Arclight専用サーバー起動を確認しています。
