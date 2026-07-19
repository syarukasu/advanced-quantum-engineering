# Advanced Quantum Engineering 2.0.2

## English

AQE configuration now lives in the global Minecraft config directory:

`config/advanced_quantum_engineering.toml`

The file is organized into three compact sections for standard Quantum
Computer tuning, endgame cores, and safety/diagnostics. On the first server
start, AQE imports values from the old per-world
`serverconfig/advanced_quantum_engineering-server.toml` and renames that file
with a `.migrated` suffix.

This release does not rename items or blocks and does not change the default
capacity or co-processor values.

## 日本語

AQEの設定ファイルをワールドごとの`serverconfig`から、Minecraft共通の
`config/advanced_quantum_engineering.toml`へ移動しました。

設定内容は「通常量子コンピュータ」「終盤コア」「安全・診断」の3区分へ
整理しています。初回サーバー起動時に旧設定値を自動移行し、旧ファイルは
`.migrated`付きで残します。

アイテム・ブロックのID、および既定の容量・コプロセッサ値は変更していません。
