# Advanced Quantum Engineering 2.2.1

## English

This release expands the default BigInteger Quantum Core capacity and fixes
production JAR generation.

- Raises the default raw BigInteger Quantum Core capacity from `10^64 - 1 B`
  to `10^1024 - 1 B`.
- Keeps the configurable raw-core range at 20 to 16,372 decimal digits.
- Keeps the exact complete-structure ceiling at 16,384 decimal digits,
  including the existing headroom for summed storage and Data Entangler
  multiplication.
- Preserves explicitly configured values in existing installations.
- Connects ForgeGradle reobfuscation to the normal production build so the
  distributed JAR uses runtime Minecraft names.

ACO remains optional. Install the same AQE JAR on the dedicated server and
every client.

## 日本語

BigInteger量子コアの既定容量拡張と、配布JAR生成の修正を含む
リリースです。

- BigInteger量子コアの既定生容量を`10^64 - 1 B`から
  `10^1024 - 1 B`へ拡張。
- 生コアの設定可能範囲20〜16,372桁は維持。
- ストレージ合算とData Entangler倍率用の余裕を含む、完成構造の
  正確な上限16,384桁は維持。
- 既存環境で明示的に設定済みの値は変更しない。
- 通常のGradleビルドへForgeGradleのreobfuscationを接続し、
  配布JAR内のMinecraft参照を実行時名へ変換。

ACOは引き続き任意連携です。専用サーバーと全クライアントへ
同じAQE JARを導入してください。
