# Advanced Quantum Engineering 2.1.1

## English

BigInteger Quantum CPU screens now display the Quantum Computer's live,
server-authoritative capacity Ledger. The CPU list reports capacity currently
reserved by active jobs, while tooltips show physical total, in-use, and
available capacity after storage aggregation and Data Entangler multiplication.

The old fixed `10^N - 1 B` label only represented the raw core Config. It did
not describe the formed structure or its concurrent jobs, so it has been
replaced.

Values through 19 decimal digits are displayed exactly. Larger values include
their leading grouped digits and complete decimal digit count. Only this bounded
summary is synchronized; a 16,384-digit BigInteger is never copied wholesale
into every CPU-name packet.

Automated tests cover exact long-range values, a multiplied 64-digit core,
the 16,384-digit boundary, bounded encoding, round trips, and malformed input.
Forge client and dedicated-server runtime qualification remains required before
release deployment.

## 日本語

BigInteger量子CPUの画面表示を、量子コンピュータがサーバー側で実際に
管理している容量Ledgerへ変更しました。CPU一覧には実行中ジョブが予約中の
容量を表示し、ツールチップにはストレージ加算とData Entangler倍率を反映した
総容量、使用中容量、空き容量を表示します。

従来の固定表示`10^N - 1 B`は単体コアのConfigだけを表し、形成済み構造や
複数の同時実行ジョブを表していなかったため廃止しました。

19桁以内の値は正確に表示します。それを越える値は先頭の3桁区切り値と
全体の10進桁数を表示します。同期するのは固定長の要約だけであり、
16,384桁のBigInteger全文をCPU名パケットへ複製しません。

long範囲、倍率適用後の64桁コア、16,384桁境界、固定長エンコード、往復、
不正入力を自動試験します。リリース導入前にはForgeクライアントと
専用サーバーでの実動確認が必要です。
