# Advanced Quantum Engineering 2.0.1

## English

AQE 2.0.1 fixes optional integration with AE2 Crafting Optimizer 1.3.1.

- ACO remains optional and is not required to run AQE.
- Compatible ACO versions are accepted across `[1.3.0,1.4.0)`.
- API v3 and all reflected methods are still validated before activation.
- Missing or disabled ACO still uses AQE's local fallback.
- Opaque BigInteger state remains preserved when the backend is unavailable.

Validation: clean build and all automated tests passed. Forge/Arclight startup,
multiplayer, recovery, and long-running world qualification remain operator-run.

## 日本語

AQE 2.0.1では、AE2 Crafting Optimizer 1.3.1との任意連携を修正しました。

- ACOは引き続き任意導入で、AQEの必須前提MODではありません。
- `[1.3.0,1.4.0)`の互換ACOを受け入れます。
- 有効化前にAPI v3と全反射メソッドを検証します。
- ACOが未導入または無効な場合はAQEのローカルfallbackを使用します。
- backendが利用できない場合も不透明なBigInteger状態を保持します。

クリーンビルドと全自動テストを通過しています。Forge/Arclight起動、
マルチプレイ、復旧、長時間ワールド試験は運用者が行います。
