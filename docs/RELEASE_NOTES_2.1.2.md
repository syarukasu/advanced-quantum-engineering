# Advanced Quantum Engineering 2.1.2

## English

This patch makes very large crafting CPU capacities easier to read.

- Long-range capacities now promote through `B / k / M / G / T / P / E`.
- AE2 and Advanced AE crafting CPU lists now use the same display rules.
- BigInteger capacities above `Long.MAX_VALUE` use bounded scientific notation
  based on the actual server-synchronized value.
- Capacity accounting, reservations, recipes, structures, and crafting
  behavior are unchanged.

## 日本語

巨大なクラフトCPU容量を判別しやすくする表示改善です。

- `long`範囲の容量を`B / k / M / G / T / P / E`へ自動的に繰り上げます。
- AE2とAdvanced AEのクラフトCPU一覧で表示規則を統一します。
- `Long.MAX_VALUE`を超えるBigInteger容量は、サーバーから同期された
  実際の値を基にした固定長の指数表記へ切り替えます。
- 容量会計、予約、レシピ、構造、クラフト動作は変更していません。
