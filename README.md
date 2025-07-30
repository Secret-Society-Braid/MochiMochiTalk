# MochiMochiTalk

個人開発DiscordBot「MochiMochiTalk」およびむつコード様で提供させていただいている「聖ちゃんの聖歌隊」Botのソースコードです。
既存のテキスト読み上げ機能に加え、アイドルマスターシリーズのプロデューサーさんに向けた機能を追加しています。

> [!NOTE]
> このBotはDiscordコミュニティ「むつコード」のために開発されており、主にそのコミュニティ内で使用されることを目的としています。
> ビルドされたBotは、むつコードのサーバーにデプロイされており、他のサーバーでの使用は想定されていません。
> ただし、ソースコードはアプリケーションの透明性を確保するために公開し、適切なライセンスの下でOSS化しています。

## 概要

MochiMochiTalkは、Discordで動作するBotで、主に以下の機能を提供しています。

- **ボイスチャンネルでのテキスト読み上げ**: ボイスチャンネルに参加し、紐づけられたテキストチャンネルのメッセージを音声で読み上げます。
- **楽曲検索**: アイドルマスターシリーズの楽曲を検索し、歌詞や関連情報を提供します。（Powered by 楽曲DB
  ふじわらはじめ）

## 依存ライブラリ

- [JDA](https://github.com/DV8FromTheWorld/JDA) (Java Discord API wrapper)
- [SLF4J](https://www.slf4j.org/) (ログ出力用Facade System)
    - [logback](https://logback.qos.ch/) (ログ出力)
- [jackson-core, databind, annotation](https://github.com/FasterXML/jackson-core) (JSON読み書き用)
- [HajimeAPI4J - Java wrapper for HajimeAPI](https://github.com/Secret-Society-Braid/HajimeAPI4J) (
  楽曲DB ふじわらはじめのAPIラッパー)
- [VOICEVOX - 無料で使える中品質なテキスト読み上げ・歌声合成ソフトウェア](https://voicevox.hiroshiba.jp/) (
  音声合成用)
