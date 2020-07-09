package com.test.kan.soundsync;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.Serializable;

import static android.content.ContentValues.TAG;

public class SoundSlot implements Serializable {
    private final int SOUND_BUTTON_MAX = 10;
    private String[] filename;
    private String now_bgm;
    private int[] soundID;
    private int bgm_state = 0;

    private transient SoundPool soundPool;
    private transient AudioAttributes attr;
    private transient MediaPlayer mediaPlayer;

    private transient Handler handler;
    private transient Context context;

    public SoundSlot(Context context){
        this.context = context;
        filename = new String[SOUND_BUTTON_MAX];
        soundID = new int[SOUND_BUTTON_MAX];

        generate();

        create_handler();
    }

    public void create_handler(){
        handler = new Handler() {
            public void handleMessage(Message msg) {
                if(bgm_state == 0){
                    audioPlay();
                    bgm_state = 1;
                }else{
                    audioStop();
                    bgm_state = 0;
                }
            }
        };
    }

    public void delete(){
        if(bgm_state == 1)
            audioStop();
        for(int i=0;i<SOUND_BUTTON_MAX;i++){
            if(soundID[i] > 0)
                try {
                    soundPool.unload(soundID[i]);
                }catch (Exception e){
                    Log.d(TAG,"Error unload");
                }
        }
        try {
            soundPool.release();
        }catch (Exception e){
            Log.d(TAG,"Error release");
        }
    }
    public void generate(){
        attr = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attr)
                .setMaxStreams(2)
                .build();

        create_handler();
    }
    public void fileSet(int index,String name){
        filename[index] = name;
    }
    public void fileLoad(){
        for(int i=0;i<SOUND_BUTTON_MAX;i++){
            if(filename[i]==null) continue;
            if (filename[i].contains("_bgm")) {
                soundID[i] = -1;
            } else {
                soundID[i] = soundPool.load(filename[i], 0);
            }
        }
    }
    public String fileGet(int n){
        return filename[n];
    }

    private boolean audioSetup(){
        boolean fileCheck = false;

        // インタンスを生成
        mediaPlayer = new MediaPlayer();

        // assetsから mp3 ファイルを読み込み
        try
        {
            // MediaPlayerに読み込んだ音楽ファイルを指定
            mediaPlayer.setDataSource(now_bgm);
            // 音量調整を端末のボタンに任せる
            //setVolumeControlStream(AudioManager.STREAM_MUSIC);

            mediaPlayer.prepare();
            fileCheck = true;
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return fileCheck;
    }

    private void audioPlay() {

        if (mediaPlayer == null) {
            // audio ファイルを読出し
            if (audioSetup()){
                //Toast.makeText(context, "Rread audio file", Toast.LENGTH_SHORT).show();
            }else{
                //Toast.makeText(context, "Error: read audio file", Toast.LENGTH_SHORT).show();
                return;
            }
        }else{
            // 繰り返し再生する場合
            mediaPlayer.stop();
            mediaPlayer.reset();
            // リソースの解放
            mediaPlayer.release();
        }

        // 再生する
        mediaPlayer.start();

        // 終了を検知するリスナー
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                audioStop();
                bgm_state = 0;
            }
        });
    }

    private void audioStop() {
        // 再生終了
        mediaPlayer.stop();
        // リセット
        mediaPlayer.reset();
        // リソースの解放
        mediaPlayer.release();

        mediaPlayer = null;
    }

    public void playSound(int num){
        if(soundPool == null) return;
        if(soundID[num]>0){
            soundPool.play(soundID[num],1.0f,1.0f,0,0,1.0f);
        }else if(soundID[num]<0){
            now_bgm=filename[num];
            handler.sendMessage(new Message());
        }
    }


}
