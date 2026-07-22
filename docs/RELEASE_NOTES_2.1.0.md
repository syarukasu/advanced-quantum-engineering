# Advanced Quantum Engineering 2.1.0

## English

AQE and ACO now share an exact BigInteger capacity ceiling of
`10^16384 - 1`. Values are checked against the exact maximum in addition to
their bit length, so a 16,385-digit value cannot cross NBT, local-ledger, or
capacity-arithmetic boundaries without an explicit failure.

The configurable raw BigInteger Quantum Core is limited to 16,372 decimal
digits. The remaining 12 digits are reserved for Advanced AE structure
aggregation and Data Entangler multiplication before the complete structure
reaches the shared 16,384-digit ceiling.

Optional ACO API v3 compatibility now covers `[1.3.0,1.5.0)`, including ACO
1.4.x. ACO remains optional and AQE contains no direct ACO class dependency.

The clean build completed with 10 passing automated tests. Forge client,
dedicated-server, Quantum Computer formation, restart, and live ACO integration
still need operator qualification.

## 日本語

AQEとACOのBigInteger容量上限を、厳密な`10^16384 - 1`へ統一しました。
bit長に加えて最大値そのものを比較するため、同じ54,427bitに収まる
16,385桁の値も、NBT、ローカルLedger、容量演算の境界で明示的に拒否します。

設定可能な単体BigInteger量子コアは最大16,372桁です。残り12桁は
Advanced AE構造内の容量加算とData Entangler倍率用に予約し、構造全体の
上限を16,384桁に維持します。

任意のACO API v3互換範囲を`[1.3.0,1.5.0)`へ広げ、ACO 1.4.xへ対応しました。
ACOは引き続き任意導入であり、AQEからACOクラスへの直接依存はありません。

クリーンビルドと10件の自動試験は成功しています。Forgeクライアント、
専用サーバー、Quantum Computer形成、再起動、ACO実機連携は運用環境での
確認が必要です。
