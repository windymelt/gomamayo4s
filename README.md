# gomamayo4s

このツールはゴママヨを検出するためのCLIツールです。アルファ版であり、APIは常に変更されます。

Scala CLIで動作します。

https://scala-cli.virtuslab.org

## Prerequisites

事前にkabosu用の辞書ファイルをダウンロードする必要があります。`fetch-dict.sh`を実行すると辞書がダウンロードされますが、126MiBのファイルがダウンロードされるため通信帯域に注意してください。

```scala
% ./fetch-dict.sh
+ curl -o sudachi.zip -sSL http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict/sudachi-dictionary-20240109-full.zip
+ unzip sudachi.zip
Archive:  sudachi.zip
  inflating: sudachi-dictionary-20240109/system_full.dic
  inflating: sudachi-dictionary-20240109/LEGAL
  inflating: sudachi-dictionary-20240109/LICENSE-2.0.txt
+ ln -s sudachi-dictionary-20240109/system_full.dic system_core.dic
```

## Usage

`scala-cli ./gomamayo.scala.sc -- 文字列`を実行するとゴママヨ判定が行われます。ゴママヨと判定された箇所はハイライトされます。

```shell
% scala-cli ./gomamayo.scala.sc -- このプログラムを実行する必要要件は辞書ファイルです
このプログラムを実行する必要(ヒツヨウ)要件(ヨウケン)は辞書ファイルです
```

## Inside gomamayo4s

![diagram of PDA](./gomamayo.drawio.png)
