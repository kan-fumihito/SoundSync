# SoundSync
+ マジック用品[-サウンドスーツ-](https://soundsuit.com/?pid=89316951)のAndroid版アプリ(非公認)
+ 知り合いの依頼により製作 - [GooglePlay](https://play.google.com/store/apps/details?id=com.test.kan.soundsync&hl=ja)

## 使い方
<img src="https://github.com/kan-fumihito/SoundSync/blob/master/screen.png" width=400px>

+ オーディオファイルの設定(A～J,SET SLOTボタン)
  - SE:mp3以外の拡張子のオーディオファイル
  - BGM:mp3拡張子のオーディオファイル
  - A~Jのボタンを長押しして各ボタンにオーディオファイルを割り当て
  - SET SLOTボタンを押して、オーディオファイルをロード(ロード完了後、ボタンの上にファイル名が表示)
  - A~Jのボタンをタップすることで、設定されているオーディオが再生


+ オーディオ割り当ての保存(SAVEボタンとLOADボタン)
アプリを終了すると、ボタンに割り当てたオーディオファイルが全てリセットされます。SAVEボタンとLOADボタンを使うことで、1パターンだけオーディオ割り当てを保存することが出来ます。
  - SAVEボタンで、各ボタンに割り当てられているオーディオファイルを保存(全スロット)
  - LOADボタンで、保存されている割り当てを設定(全スロット)
  - SET SLOTボタンでロードする
  
  ※SAVEボタンをタップすると、前回保存されていたデータは一発で上書きされてしまうため注意
  
  
+ Arduinoとの接続(STARTボタン)

  1.こちらの[サイト](https://ehbtj.com/info/serial-monitor-android-apps/)を参考にして「USB機器を検出」をオンにする
  
  2.アプリ画面上のSTARTボタンを押して、Arduinoと接続開始
  
  3.Arduino側のボタン操作でオーディオを再生

 
+ Arduinoのピンとボタンの対応
  - デジタル2～10: A～I
  - デジタル11: UP
  - デジタル12: DOWN
  - デジタル13: SLOT1
  - アナログ0: J
  
  ※使わないボタンは必ずグランドに接続してください
 
 
+ SLOT操作(1～10チェックボックス,UP,DOWN,SLOT1ボタン)
  - 1～10チェックボックス:チェックが入った番号のスロットのみ有効。チェックが入っていない番号は、UP,DOWNで飛ばされる
  - UP:スロットの番号を上げる
  - DOWN:スロットの番号を下げる
  - SLOT1:1にチェックが入っていても入っていなくても、スロット番号を1にする
  
  ※UP,DOWNボタンの操作は、ループする(UP:8->9->10->1->2... DOWN:2->1->10->9->8... )



## サウンドスーツデバイスについて
本家サウンドスーツのデバイスとは互換性はありません。[Arduino Uno](http://akizukidenshi.com/catalog/g/gM-07385/)に[soundsync.ino](https://github.com/kan-fumihito/SoundSync/blob/master/arduino/soundsync.ino)のプログラムを書き込んだ上で、ボタン配線を行ってください。
ArduinoのGPIOは、デジタル2～13とアナログ0のピンを使用してください。
