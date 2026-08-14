# Advanced Quantum Engineering 2.2.6

## English

- Registers AQE's optional BigInteger CPU host through the public external
  consumer boundary introduced by ACO 1.5.19.
- Keeps AQE in control of CPU execution, capacity reservations, progress,
  cancellation, persistence, and presentation.
- ACO remains optional. Older ACO 1.5.x versions still load, while the exact
  external-consumer handoff activates when ACO 1.5.19 or newer is installed.

## 日本語

- ACO 1.5.19で追加された公開外部コンシューマ境界を通して、AQEの任意BigInteger CPUホストを
  登録するようにしました。
- CPU実行、容量予約、進捗、キャンセル、永続化、表示は引き続きAQEが管理します。
- ACOは任意依存のままです。旧ACO 1.5.xでも起動でき、ACO 1.5.19以降では正確な
  外部コンシューマ引き渡しが有効になります。
