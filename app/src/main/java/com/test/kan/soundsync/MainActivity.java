package com.test.kan.soundsync;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.usb.*;

import android.media.AudioManager;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.lang.*;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    private UsbManager manager;
    private UsbDevice device;
    private TextView text,textSound;
    private TextView[] mName;
    private Button btn;
    private Button[] soundButton;
    private boolean flag=false;
    private SoundSlot[] soundSlot;

    private int n=0,i,btnNum;
    private final int SLOT_MAX=10,SOUND_BUTTON_MAX=10;
    private String filename;
    private String path;
    private static int EXTERNAL_STORAGE_REQUEST_CODE=1;
    private Handler h_Down,h_Up,h_Default;
    private CheckBox[] checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundSlot = new SoundSlot[SLOT_MAX];
        btn = (Button)findViewById(R.id.button);
        manager = (UsbManager)getSystemService(USB_SERVICE);
        text=(TextView)findViewById(R.id.textView);
        textSound=(TextView)findViewById(R.id.textView2);
        checkBox=new CheckBox[SLOT_MAX];
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        soundButton=new Button[SOUND_BUTTON_MAX];
        mName=new TextView[SOUND_BUTTON_MAX];
        for(i=0;i<SLOT_MAX;i++) {
            final int index=i;
            soundSlot[i] = new SoundSlot(this);
            String str2 = "CheckBox" + (index+1);
            int checkId = getResources().getIdentifier(str2,"id",getPackageName());
            checkBox[index]=(CheckBox)findViewById(checkId);
        }

        for(i=0; i < SOUND_BUTTON_MAX; i++) {
            final int index=i;
            String str = "button" + (index+1);
            String str3 = "tag" + (index+1);
            int buttonId = getResources().getIdentifier(str, "id", getPackageName());
            int tagID = getResources().getIdentifier(str3,"id",getPackageName());
            soundButton[index] = (Button)findViewById(buttonId);
            mName[index] = (TextView)findViewById(tagID);
            soundButton[index].setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    btnNum = index;
                    fileSelect();
                    showName();
                    return true;
                }
            });
            soundButton[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    soundSlot[n].playSound(index);
                }
            });
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (device == null) {
                    return;
                }
                if (!manager.hasPermission(device)) {
                    manager.requestPermission(device, PendingIntent.getBroadcast(MainActivity.this, 0, new Intent("なにか"), 0));
                    return;
                }
                text.setText("SUCCESS");
                if(!flag) {
                    btn.setEnabled(false);
                    btn.setClickable(false);
                    connectDevice();
                    flag = true;
                }
            }
        });
        checkPermission();

        h_Up = new Handler() {
            public void handleMessage(Message msg){
                n = (n + 1) % SLOT_MAX;
                if(!checkBox[n].isChecked()) {
                    for (i = 0; i < SLOT_MAX; i++) {
                        n=(n+1)%SLOT_MAX;
                        if(checkBox[n].isChecked())break;
                    }
                }
                showName();
            }
        };
        h_Down = new Handler() {
            public void handleMessage(Message msg) {
                if(n == 0)
                    n = SLOT_MAX-1;
                else
                    n = (n - 1) % SLOT_MAX;

                if(!checkBox[n].isChecked()) {
                    for (i = 0; i < SLOT_MAX; i++) {
                        if (n == 0)
                            n = SLOT_MAX - 1;
                        else
                            n = (n - 1) % SLOT_MAX;

                        if (checkBox[n].isChecked()) break;
                    }
                }
                showName();
            }
        };
        h_Default = new Handler() {
            public void handleMessage(Message msg){
                n=0;
                showName();
            }
        };
    }

    public void onResume(){
        super.onResume();
        updateList();
    }

    public void fileSelect() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, EXTERNAL_STORAGE_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EXTERNAL_STORAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            //ファイル選択結果
            if (data.getData().getPath().contains("file://")) {
                filename = data.getData().getPath().replace("file://", "");
            } else {
                path = data.getData().getLastPathSegment();
                filename = android.os.Environment.getExternalStorageDirectory().toString() + "/" + path.substring(path.indexOf(':') + 1, path.length());
            }
            soundSlot[n].fileSet(btnNum,filename);
        }

    }

    // Permissionの確認
    @TargetApi(Build.VERSION_CODES.M)
    public void checkPermission() {
        // 既に許可している
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 許可していない場合、パーミッションの取得を行う
        // 以前拒否されている場合は、なぜ必要かを通知し、手動で許可してもらう
        if (!shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT).show();
        }
        // パーミッションの取得を依頼
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
    }

    // 結果の受け取り
    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // requestPermissionsの引数に指定した値が、requestCodeで返却される
        if (requestCode != EXTERNAL_STORAGE_REQUEST_CODE) {
            return;
        }
        // 自分がリクエストしたコードで戻ってきた場合
        // 使用が許可された
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // ローカルファイルの読み取り処理実行
            return;
        }
        // 拒否されたが永続的ではない場合
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "許可されないとアプリが実行できません", Toast.LENGTH_SHORT).show();
            // パーミッションの取得を依頼
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
            return;
        }
        // 永続的に拒否された場合
        Toast.makeText(this, "許可されないとアプリが実行できません\nアプリ設定＞権限をチェックしてください", Toast.LENGTH_SHORT).show();
    }

    public void onDestroy(){
        super.onDestroy();

        for(i=0;i<SLOT_MAX;i++){
            soundSlot[i].delete();
        }
    }


    private void updateList() {
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        if (deviceList == null || deviceList.isEmpty()) {
            text.setText("no device found");
        } else {
            for (String name : deviceList.keySet()) {

                if (deviceList.get(name).getVendorId() == 10755) {
                    device = deviceList.get(name);
                }
            }
            text.setText("STANDBY");
        }
    }

    private void connectDevice() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                byte[] buf = new byte[16];
                UsbDeviceConnection connection = manager.openDevice(device);
                if (!connection.claimInterface(device.getInterface(1), true)) {
                    connection.close();
                    return;
                }

                connection.controlTransfer(0x21, 34, 0, 0, null, 0, 0);
                connection.controlTransfer(0x21, 32, 0, 0, new byte[]{
                        (byte) 0x80, 0x25, 0x00, 0x00, 0x00, 0x00, 0x08
                }, 7, 0);


                UsbEndpoint epIN = null;
                UsbInterface usbIf = device.getInterface(1);
                for (int i = 0; i < usbIf.getEndpointCount(); i++) {
                    if (usbIf.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (usbIf.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
                            epIN = usbIf.getEndpoint(i);
                    }
                }

                while (true) {
                    if (connection.bulkTransfer(epIN, buf, 16, 0) > 0) {
                        sound(new String(buf));
                    }
                }
            }
        }).start();
    }

    private void sound(String s){
        //sound->a,b,c,d,e,f,g,h,i,j
        //SLOT_MAX up->k
        //SLOT_MAX down->l
        //SLOT_MAX 1->m
        switch (s.charAt(0)) {
            case 'k':
                Down();
                break;
            case 'l':
                Up();
                break;
            case 'm':
                Default();
                break;

            default:
                soundSlot[n].playSound((int)s.charAt(0) - (int)'a');
        }
    }

    public void onUp(View v){
        Up();
    }
    public void onDown(View v){
        Down();
    }
    public void onDefault(View v){
        Default();
    }

    public void Up(){
        h_Up.sendMessage(new Message());
    }
    public void Down(){
        h_Down.sendMessage(new Message());
    }
    public void Default(){
        h_Default.sendMessage(new Message());
    }

    public void onSet(View v){
        for(i=0;i<SLOT_MAX;i++){
            soundSlot[i].delete();
            soundSlot[i].generate();
            soundSlot[i].fileLoad();
        }
        n=0;
        showName();
    }

    public void onSave(View v){
        try {
            FileOutputStream fos = openFileOutput("SaveData.dat", MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(soundSlot);
            oos.close();
        }catch (Exception e){
            Log.d(TAG, e.toString());
        }

    }

    public void onLoad(View v){

        try {
            FileInputStream fis = openFileInput("SaveData.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            soundSlot = (SoundSlot[]) ois.readObject();
            ois.close();
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }

    }

    public void showName(){
        String name;

        textSound.setText("SLOT "+(n+1));
        for(int i=0;i<SOUND_BUTTON_MAX;i++){
            final int index = i;
            name = soundSlot[n].fileGet(i);
            if(name == null)
                name = "";
            else
                name = name.substring(name.lastIndexOf("/") + 1);

            mName[index].setText(name);
        }
    }

}
