# Advanced Quantum Engineering 2.2.0

## English

This release completes AQE's optional integration with AE2 Crafting Optimizer
1.5.x while keeping ACO fully optional.

- Extends the compatible optional ACO range to `[1.3.0,1.6.0)`.
- Adds server-authoritative counts for active standard jobs and exact
  BigInteger parent jobs to the bounded Quantum Computer display snapshot.
- Prevents ACO-managed checked-long child windows from being counted as
  separate player jobs.
- Shares one exact physical-capacity ledger between standard Advanced AE jobs
  and ACO BigInteger parent jobs when the compatible API v3 backend is present.
- Preserves AQE's local saturated-long fallback and opaque ACO state when ACO
  is absent or disabled.
- Keeps the existing Quantum Computer structure, Multi-Threader, Data
  Entangler, recipes, and registry IDs unchanged.

ACO remains an optional dependency. AQE performs no direct production linkage
to ACO classes and activates the adapter only after validating the compatible
API v3 contract.

## 日本語

ACOを必須化せず、AE2 Crafting Optimizer 1.5.xとの任意連携を完成させる
リリースです。

- 任意ACOの対応範囲を`[1.3.0,1.6.0)`へ拡張。
- Quantum Computerの上限付き表示Snapshotへ、サーバーを正とする通常Job数と
  BigInteger親Job数を追加。
- ACOが管理する検査済みlong子Windowを、別のプレイヤーJobとして二重表示
  しないよう修正。
- 互換API v3 Backendが存在する場合、Advanced AE通常JobとACO BigInteger
  親Jobで一つの正確な物理容量Ledgerを共有。
- ACOが存在しない、または無効な場合も、AQE本来の飽和long Fallbackと
  不透明なACO状態の保存を維持。
- Quantum Computer構造、Multi-Threader、Data Entangler、レシピ、
  Registry IDは変更なし。

ACOは引き続き任意依存です。AQEの本番コードはACOクラスへ直接依存せず、
互換API v3契約を検証できた場合だけAdapterを有効化します。
