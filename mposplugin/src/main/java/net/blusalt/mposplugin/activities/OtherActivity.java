package net.blusalt.mposplugin.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.blusalt.mposplugin.BaseApplication;

import net.blusalt.mposplugin.R;
import net.blusalt.mposplugin.USBClass;
import net.blusalt.mposplugin.blusaltmpos.pay.BlusaltTerminalInfo;
import net.blusalt.mposplugin.blusaltmpos.pay.CreditCard;
import net.blusalt.mposplugin.blusaltmpos.pay.TerminalInfo;
import net.blusalt.mposplugin.blusaltmpos.pay.TerminalResponse;
import net.blusalt.mposplugin.blusaltmpos.pos.AppLog;
import net.blusalt.mposplugin.blusaltmpos.pos.TlvDataList;
import net.blusalt.mposplugin.blusaltmpos.util.AppPreferenceHelper;
import net.blusalt.mposplugin.blusaltmpos.util.Constants;
import net.blusalt.mposplugin.blusaltmpos.util.KSNUtilities;
import net.blusalt.mposplugin.keyboard.KeyBoardNumInterface;
import net.blusalt.mposplugin.keyboard.KeyboardUtil;
import net.blusalt.mposplugin.keyboard.MyKeyboardView;
import net.blusalt.mposplugin.network.RetrofitClientInstance;
import net.blusalt.mposplugin.utils.DUKPK2009_CBC;
import net.blusalt.mposplugin.utils.FileUtils;
import net.blusalt.mposplugin.utils.QPOSUtil;
import net.blusalt.mposplugin.utils.TRACE;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;
import com.dspread.xpos.QPOSService.TransactionType;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import Decoder.BASE64Encoder;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class OtherActivity extends BaseActivity {

    private Button doTradeButton, serialBtn, audioBtn;
    private EditText statusEditText;
    private ListView appListView;
    private Dialog dialog;
    private String nfcLog = "";
    private Button btnUSB, continueBtn, insert_amount;

    private ImageView usbImage, validateImage;

    private TextView connectedText, validate_amount_text, about_to_text, validate_text, insert_text;

    private MaterialTextView mToolbar;

    private GifImageView spinKit;

    private Button btnDisconnect;
    private EditText mKeyIndex;
    private EditText mhipStatus;
    private QPOSService pos;
    private UpdateThread updateThread;

    private String pubModel;
    private String amount = "";
    private String cashbackAmount = "";
    private boolean isPinCanceled = false;
    private String blueTootchAddress = "";
    private boolean isUart = true;
    private LinearLayout lin;
    private int type;
    private UsbDevice usbDevice;
    private Context mContext;
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private static final int REQUEST_CODE_AUDIO_PERMISSIONS = 2;
    private boolean autoDoTrade = false;
    private LinearLayout mafireLi, mafireUL;
    private Button operateCardBtn, pollBtn, pollULbtn, veriftBtn, veriftULBtn, readBtn, writeBtn, finishBtn, finishULBtn, getULBtn, readULBtn, fastReadUL, writeULBtn, transferBtn;
    private Spinner mafireSpinner;
    private EditText blockAdd, status, status11, block_address11;
    private Spinner cmdSp;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    private boolean isUpdateFw = false;
    private boolean isVisiblePosID;

    private CreditCard creditCard;

    private String cPin = "";

    private String serialNo;
    private AppPreferenceHelper appPreferenceHelper;

    private TransactionType transactionType;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //When the window is visible to the user, keep the device normally open and keep the brightness unchanged
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!isUart) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        appPreferenceHelper = new AppPreferenceHelper(this);

        mContext = this;
        initView();
        initIntent();
        initListener();
    }

    @Override
    public void onToolbarLinstener() {
        finish();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_other;
    }

    private void initIntent() {
        Intent intent = getIntent();
        type = intent.getIntExtra("connect_type", 2);
        switch (type) {
            case 1:
                setTitle(getString(R.string.title_audio));
                requestPermission();
                posType = POS_TYPE.AUDIO;
                open(QPOSService.CommunicationMode.AUDIO);
                pos.openAudio();
                audioBtn.setVisibility(View.VISIBLE);
                serialBtn.setVisibility(View.GONE);
                serialBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        posType = POS_TYPE.AUDIO;
                        open(QPOSService.CommunicationMode.AUDIO);
                        pos.openAudio();
                    }
                });
                break;
            case 2:
                setTitle(getString(R.string.serial_port));
                posType = POS_TYPE.UART;
                open(QPOSService.CommunicationMode.UART);
//                        blueTootchAddress = "/dev/ttyMT0";//tongfang is s1，tianbo is s3
                blueTootchAddress = "/dev/ttyS1";//tongfang is s1，tianbo is s3
//                        blueTootchAddress = "/dev/ttyHSL1";//tongfang is s1，tianbo is s3
                pos.setDeviceAddress(blueTootchAddress);
                pos.openUart();
                serialBtn.setVisibility(View.VISIBLE);
                audioBtn.setVisibility(View.GONE);

                posType = POS_TYPE.UART;
                open(QPOSService.CommunicationMode.UART);
//                        blueTootchAddress = "/dev/ttyMT0";//tongfang is s1，tianbo is s3
                blueTootchAddress = "/dev/ttyS1";//tongfang is s1，tianbo is s3
//                        blueTootchAddress = "/dev/ttyHSL1";//tongfang is s1，tianbo is s3
                pos.setDeviceAddress(blueTootchAddress);
                pos.openUart();

                Log.e("TAG", "It's Serial Connection");

//                serialBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // TODO Auto-generated method stub
//                        if (ClickUtil.isFastClick()) { //
//                            posType = POS_TYPE.UART;
//                            open(QPOSService.CommunicationMode.UART);
////                        blueTootchAddress = "/dev/ttyMT0";//tongfang is s1，tianbo is s3
//                            blueTootchAddress = "/dev/ttyS1";//tongfang is s1，tianbo is s3
////                        blueTootchAddress = "/dev/ttyHSL1";//tongfang is s1，tianbo is s3
//                            pos.setDeviceAddress(blueTootchAddress);
//                            pos.openUart();
//                        }
//                    }
//                });
                break;
        }
    }

    private void initView() {

        mToolbar = (MaterialTextView) findViewById(R.id.mtoolbar);
        doTradeButton = (Button) findViewById(R.id.doTradeButton);//start to do trade
        serialBtn = (Button) findViewById(R.id.serialPort);
        audioBtn = (Button) findViewById(R.id.audioButton);
        statusEditText = (EditText) findViewById(R.id.statusEditText);

        validateImage = (ImageView) findViewById(R.id.validateImage);
        usbImage = (ImageView) findViewById(R.id.usbImage);
        connectedText = (TextView) findViewById(R.id.connected_text);
        validate_text = (TextView) findViewById(R.id.validate_text);
        insert_text = (TextView) findViewById(R.id.insert_text);
        about_to_text = (TextView) findViewById(R.id.about_to_text);
        validate_amount_text = (TextView) findViewById(R.id.validate_amount_text);

        continueBtn = (Button) findViewById(R.id.continueBtn);
        insert_amount = (Button) findViewById(R.id.insert_amount);

        btnUSB = (Button) findViewById(R.id.btnUSB);//Scan  USB device
        btnDisconnect = (Button) findViewById(R.id.disconnect);//disconnect
        mKeyIndex = ((EditText) findViewById(R.id.keyindex));
        mhipStatus = (findViewById(R.id.chipStatus));
        lin = findViewById(R.id.lin);
        pollBtn = (Button) findViewById(R.id.search_card);
        pollULbtn = (Button) findViewById(R.id.poll_ulcard);
        veriftBtn = (Button) findViewById(R.id.verify_card);
        veriftULBtn = (Button) findViewById(R.id.verify_ulcard);
        readBtn = (Button) findViewById(R.id.read_card);
        writeBtn = (Button) findViewById(R.id.write_card);
        finishBtn = (Button) findViewById(R.id.finish_card);
        finishULBtn = (Button) findViewById(R.id.finish_ulcard);
        getULBtn = (Button) findViewById(R.id.get_ul);
        readULBtn = (Button) findViewById(R.id.read_ulcard);
        fastReadUL = (Button) findViewById(R.id.fast_read_ul);
        writeULBtn = (Button) findViewById(R.id.write_ul);
        transferBtn = (Button) findViewById(R.id.transfer_card);
        mafireSpinner = (Spinner) findViewById(R.id.verift_spinner);
        blockAdd = (EditText) findViewById(R.id.block_address);
        String[] keyClass = new String[]{"Key A", "Key B"};
        ArrayAdapter<String> spinneradapter = new ArrayAdapter<String>(OtherActivity.this, android.R.layout.simple_spinner_item, keyClass);
        mafireSpinner.setAdapter(spinneradapter);
        cmdSp = (Spinner) findViewById(R.id.cmd_spinner);
        String[] cmdList = new String[]{"add", "reduce", "restore"};
        ArrayAdapter<String> cmdAdapter = new ArrayAdapter<String>(OtherActivity.this, android.R.layout.simple_spinner_item, cmdList);
        cmdSp.setAdapter(cmdAdapter);
        status = (EditText) findViewById(R.id.status);
        status11 = (EditText) findViewById(R.id.status11);
        block_address11 = (EditText) findViewById(R.id.block_address11);
        operateCardBtn = (Button) findViewById(R.id.operate_card);
        mafireLi = (LinearLayout) findViewById(R.id.mifareid);
        mafireUL = (LinearLayout) findViewById(R.id.ul_ll);
    }

    private void initListener() {
        MyOnClickListener myOnClickListener = new MyOnClickListener();
        //The following is the click event of the button
        continueBtn.setOnClickListener(myOnClickListener);
        doTradeButton.setOnClickListener(myOnClickListener);//start
        btnDisconnect.setOnClickListener(myOnClickListener);
        btnUSB.setOnClickListener(myOnClickListener);
        pollBtn.setOnClickListener(myOnClickListener);
        pollULbtn.setOnClickListener(myOnClickListener);
        finishBtn.setOnClickListener(myOnClickListener);
        finishULBtn.setOnClickListener(myOnClickListener);
        readBtn.setOnClickListener(myOnClickListener);
        writeBtn.setOnClickListener(myOnClickListener);
        veriftBtn.setOnClickListener(myOnClickListener);
        veriftULBtn.setOnClickListener(myOnClickListener);
        operateCardBtn.setOnClickListener(myOnClickListener);
        getULBtn.setOnClickListener(myOnClickListener);
        readULBtn.setOnClickListener(myOnClickListener);
        fastReadUL.setOnClickListener(myOnClickListener);
        writeULBtn.setOnClickListener(myOnClickListener);
        transferBtn.setOnClickListener(myOnClickListener);
    }

    private POS_TYPE posType = POS_TYPE.BLUETOOTH;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private enum POS_TYPE {
        BLUETOOTH, AUDIO, UART, USB, OTG, BLUETOOTH_BLE
    }

    /**
     * open and get the class object,  start listening
     *
     * @param mode
     */
    private void open(QPOSService.CommunicationMode mode) {
        TRACE.d("open");
        //pos=null;
        MyPosListener listener = new MyPosListener();
        //implement singleton mode
        pos = QPOSService.getInstance(OtherActivity.this, mode);
        if (pos == null) {
            Log.e("TAG", "CommunicationMode unknow");
            return;
        }
        if (mode == QPOSService.CommunicationMode.USB_OTG_CDC_ACM) {
            pos.setUsbSerialDriver(QPOSService.UsbOTGDriver.CDCACM);
        }
        if (posType == POS_TYPE.UART) {
            pos.setD20Trade(true);
        } else {
            pos.setD20Trade(false);
        }
        pos.setConext(this);
        //init handler
        Handler handler = new Handler(Looper.myLooper());
        pos.initListener(handler, listener);
        String sdkVersion = pos.getSdkVersion();
        Log.e("TAG POS sdkVersion ", sdkVersion);
//        Toast.makeText(this, "sdkVersion--" + sdkVersion, Toast.LENGTH_SHORT).show();
    }

    /**
     * close device
     */
    private void close() {
        TRACE.d("close");
        if (pos == null) {
            return;
        } else if (posType == POS_TYPE.AUDIO) {
            pos.closeAudio();
        } else if (posType == POS_TYPE.BLUETOOTH) {
            pos.disconnectBT();

        } else if (posType == POS_TYPE.BLUETOOTH_BLE) {
            pos.disconnectBLE();
        } else if (posType == POS_TYPE.UART) {
            pos.closeUart();
        } else if (posType == POS_TYPE.USB) {
            pos.closeUsb();
        } else if (posType == POS_TYPE.OTG) {
            pos.closeUsb();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // MenuItem audioitem = menu.findItem(R.id.audio_test);
        if (pos != null) {
            if (pos.getAudioControl()) {
                audioitem.setTitle(R.string.audio_open);
            } else {
                audioitem.setTitle(R.string.audio_close);
            }
        } else {
            audioitem.setTitle(R.string.audio_unknow);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    MenuItem audioitem = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        audioitem = menu.findItem(R.id.audio_test);
        if (pos != null) {
            if (pos.getAudioControl()) {
                audioitem.setTitle(R.string.audio_open);
            } else {
                audioitem.setTitle(R.string.audio_close);
            }
        } else {
            audioitem.setTitle(R.string.audio_unknow);
        }
        return true;
    }

    class UpdateThread extends Thread {
        private boolean concelFlag = false;

        @Override
        public void run() {

            while (!concelFlag) {
                int i = 0;
                while (!concelFlag && i < 100) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    i++;
                }
                if (concelFlag) {
                    break;
                }
                if (pos == null) {
                    return;
                }
                final int progress = pos.getUpdateProgress();
                if (progress < 100) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("TAG", progress + "%");
                        }
                    });
                    continue;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("TAG", "Update Finished 100%");
                    }
                });

                break;
            }
        }

        public void concelSelf() {
            concelFlag = true;
        }
    }

    /**
     * Click event of the menu bar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (pos == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.device_not_connect), Toast.LENGTH_LONG).show();
            return true;
        } else if (item.getItemId() == R.id.reset_qpos) {
            boolean a = pos.resetPosStatus();
            if (a) {
                Toast.makeText(getApplicationContext(), "POS reset", Toast.LENGTH_LONG).show();
                Log.e("TAG", "pos reset");
            }
        } else if (item.getItemId() == R.id.get_ksn) {
            pos.getKsn();
        } else if (item.getItemId() == R.id.getEncryptData) {
            //get encrypt data
            pos.getEncryptData("70563".getBytes(), "1", "0", 10);
        } else if (item.getItemId() == R.id.addKsn) {
            pos.addKsn("00");
        } else if (item.getItemId() == R.id.doTradeLogOperation) {
            pos.doTradeLogOperation(QPOSService.DoTransactionType.GetAll, 0);
        } else if (item.getItemId() == R.id.get_update_key) {//get the key value
            pos.getUpdateCheckValue();

        } else if (item.getItemId() == R.id.get_device_public_key) {//get the key value

            pos.getDevicePublicKey(5);
        } else if (item.getItemId() == R.id.set_sleepmode_time) {//set pos sleep mode time
//            0~Integer.MAX_VALUE

            pos.setSleepModeTime(20);//the time is in 10s and 10000s
        } else if (item.getItemId() == R.id.set_shutdowm_time) {
            pos.setShutDownTime(15 * 60);
        }
        //update ipek
        else if (item.getItemId() == R.id.updateIPEK) {
            int keyIndex = getKeyIndex();
            String ipekGrop = "0" + keyIndex;
            pos.doUpdateIPEKOperation(
                    ipekGrop, "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944",
                    "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944",
                    "09118012400705E00000", "C22766F7379DD38AA5E1DA8C6AFA75AC", "B2DE27F60A443944");
        } else if (item.getItemId() == R.id.getSleepTime) {
            pos.getShutDownTime();
        } else if (item.getItemId() == R.id.getQuickEmvStatus) {
            pos.getQuickEMVStatus(QPOSService.EMVDataOperation.getEmv, "9F061000000000000000000000000000000000");
        } else if (item.getItemId() == R.id.setQuickEmvStatus) {
            pos.setQuickEmvStatus(true);
        } else if (item.getItemId() == R.id.audio_test) {
            if (pos.getAudioControl()) {
                pos.setAudioControl(false);
                item.setTitle(getString(R.string.audio_close));
            } else {
                pos.setAudioControl(true);
                item.setTitle(getString(R.string.audio_open));
            }
        } else if (item.getItemId() == R.id.about) {

            Log.e("TAG", "SDK Version：" + pos.getSdkVersion());
        } else if (item.getItemId() == R.id.setBuzzer) {
            pos.doSetBuzzerOperation(3);//set buzzer
        } else if (item.getItemId() == R.id.menu_get_deivce_info) {
            Log.e("TAG", String.valueOf(R.string.getting_info));
            pos.getQposInfo();
        } else if (item.getItemId() == R.id.menu_get_deivce_key_checkvalue) {
            Log.e("TAG", "get_deivce_key_checkvalue..............");
            int keyIdex = getKeyIndex();
            pos.getKeyCheckValue(keyIdex, QPOSService.CHECKVALUE_KEYTYPE.DUKPT_MKSK_ALLTYPE);

        } else if (item.getItemId() == R.id.menu_get_pos_id) {
            pos.getQposId();
            Log.e("TAG", String.valueOf(R.string.getting_pos_id));
        } else if (item.getItemId() == R.id.setMasterkey) {
            //key:0123456789ABCDEFFEDCBA9876543210
            //result；0123456789ABCDEFFEDCBA9876543210
            int keyIndex = getKeyIndex();
            pos.setMasterKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", keyIndex);

        } else if (item.getItemId() == R.id.menu_get_pin) {
            Log.e("TAG", String.valueOf(R.string.input_pin));
            pos.getPin(1, 0, 6, "please input pin", "622262XXXXXXXXX4406", "", 20);
        } else if (item.getItemId() == R.id.isCardExist) {
            pos.isCardExist(30);
        } else if (item.getItemId() == R.id.resetSessionKey) {
            int keyIndex = getKeyIndex();
            pos.updateWorkKey(
                    "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",//PIN KEY
                    "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885",  //TRACK KEY
                    "1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", //MAC KEY
                    keyIndex, 5);
        } else if (item.getItemId() == R.id.updateFirmWare) {
//            isUpdateFw = true;
//            pos.getUpdateCheckValue();
            updateFirmware();
        } else if (item.getItemId() == R.id.cusDisplay) {
            deviceShowDisplay("test info");
        } else if (item.getItemId() == R.id.closeDisplay) {
            pos.lcdShowCloseDisplay();
        } else if (item.getItemId() == R.id.menu_operate_mafire) {
            Log.e("TAG", "operate mafire card");
            showSingleChoiceDialog();
        } else if (item.getItemId() == R.id.updateEMVByXml) {
            Log.e("TAG", "updating...");
            pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("QPOS cute,CR100,D20,D30.xml", OtherActivity.this)));
        }
        return true;
    }

    private int yourChoice = 0;

    private void showSingleChoiceDialog() {
        final String[] items = {"Mifare classic 1", "Mifare UL"};
//	    yourChoice = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(OtherActivity.this);
        singleChoiceDialog.setTitle("please select one");
        // The second parameter is default
        singleChoiceDialog.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        yourChoice = which;
                    }
                });
        singleChoiceDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (yourChoice == 0) {
                            mafireLi.setVisibility(View.VISIBLE);//display m1 mafire card
                            mafireUL.setVisibility(View.GONE);//display ul mafire card
                        } else if (yourChoice == 1) {
                            mafireLi.setVisibility(View.GONE);
                            mafireUL.setVisibility(View.VISIBLE);
                        }
                    }
                });
        singleChoiceDialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        TRACE.d("onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        TRACE.d("onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TRACE.d("onDestroy");
        if (updateThread != null) {
            updateThread.concelSelf();
        }
        if (pos != null) {
            close();
            pos = null;
        }
    }


    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private KeyboardUtil keyboardUtil;
    private List<String> keyBoardList = new ArrayList<>();

    /**
     * @author qianmengChen
     * @ClassName: MyPosListener
     * @Function: TODO ADD FUNCTION
     * @date: 2016-11-10 下午6:35:06
     */
    class MyPosListener extends CQPOSService {

        @Override
        public void onQposRequestPinResult(List<String> dataList, int offlineTime) {
            super.onQposRequestPinResult(dataList, offlineTime);
            keyBoardList = dataList;
            MyKeyboardView.setKeyBoardListener(new KeyBoardNumInterface() {
                @Override
                public void getNumberValue(String value) {
//                    Log.e("TAG", "Pls click "+dataList.get(0));
                    pos.pinMapSync(value, 20);
                }
            });
            keyboardUtil = new KeyboardUtil(OtherActivity.this, lin, dataList);
            keyboardUtil.initKeyboard(MyKeyboardView.KEYBOARDTYPE_Only_Num_Pwd, statusEditText);//Random keyboard
        }

        @Override
        public void onReturnGetKeyBoardInputResult(String result) {
            super.onReturnGetKeyBoardInputResult(result);
            mhipStatus.setText(result);
        }

        @Override
        public void onReturnGetPinInputResult(int num) {
            super.onReturnGetPinInputResult(num);
            String s = "";
            if (num == -1) {
                if (keyboardUtil != null) {
                    keyboardUtil.hide();
                }
            } else {
                for (int i = 0; i < num; i++) {
                    s += "*";
                }
                Log.e("TAG", "result is ：" + s);
            }
        }

        @Override
        public void onRequestWaitingUser() {//wait for card
            TRACE.d("onRequestWaitingUser()");
            dismissDialog();

            Log.e("TAG", getString(R.string.waiting_for_card));
        }

        /**
         * return the result of the transaction
         */
        @Override
        public void onDoTradeResult(QPOSService.DoTradeResult result, Hashtable<String, String> decodeData) {
            TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
            dismissDialog();

            if (result == QPOSService.DoTradeResult.NONE) {
                Log.e("TAG", getString(R.string.no_card_detected));
            } else if (result == QPOSService.DoTradeResult.TRY_ANOTHER_INTERFACE) {
                Log.e("TAG", getString(R.string.try_another_interface));
            } else if (result == QPOSService.DoTradeResult.ICC) {
                Log.e("TAG", getString(R.string.icc_card_inserted));
                TRACE.d("EMV ICC Start");
                pos.doEmvApp(QPOSService.EmvOption.START);
            } else if (result == QPOSService.DoTradeResult.NOT_ICC) {
                Log.e("TAG", getString(R.string.card_inserted));
            } else if (result == QPOSService.DoTradeResult.BAD_SWIPE) {
                Log.e("TAG", getString(R.string.bad_swipe));
            } else if (result == QPOSService.DoTradeResult.MCR) {//Magnetic card
                String content = getString(R.string.card_swiped);
                String formatID = decodeData.get("formatID");
                if (formatID.equals("31") || formatID.equals("40") || formatID.equals("37") || formatID.equals("17") || formatID.equals("11") || formatID.equals("10")) {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String trackblock = decodeData.get("trackblock");
                    String psamId = decodeData.get("psamId");
                    String posId = decodeData.get("posId");
                    String pinblock = decodeData.get("pinblock");
                    String macblock = decodeData.get("macblock");
                    String activateCode = decodeData.get("activateCode");
                    String trackRandomNumber = decodeData.get("trackRandomNumber");

                    content += getString(R.string.format_id) + " " + formatID + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                    content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";

                    content += getString(R.string.service_code) + " " + serviceCode + "\n";
                    content += "trackblock: " + trackblock + "\n";
                    content += "psamId: " + psamId + "\n";
                    content += "posId: " + posId + "\n";
                    content += getString(R.string.pinBlock) + " " + pinblock + "\n";
                    content += "macblock: " + macblock + "\n";
                    content += "activateCode: " + activateCode + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                } else if (formatID.equals("FF")) {
                    String type = decodeData.get("type");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    content += "cardType:" + " " + type + "\n";
                    content += "track_1:" + " " + encTrack1 + "\n";
                    content += "track_2:" + " " + encTrack2 + "\n";
                    content += "track_3:" + " " + encTrack3 + "\n";
                } else {
                    String orderID = decodeData.get("orderId");
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
//					String ksn = decodeData.get("ksn");
                    String serviceCode = decodeData.get("serviceCode");
                    String track1Length = decodeData.get("track1Length");
                    String track2Length = decodeData.get("track2Length");
                    String track3Length = decodeData.get("track3Length");
                    String encTracks = decodeData.get("encTracks");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    String partialTrack = decodeData.get("partialTrack");
                    String pinKsn = decodeData.get("pinKsn");
                    String trackksn = decodeData.get("trackksn");
                    String pinBlock = decodeData.get("pinBlock");
                    String encPAN = decodeData.get("encPAN");
                    String trackRandomNumber = decodeData.get("trackRandomNumber");
                    String pinRandomNumber = decodeData.get("pinRandomNumber");
                    if (orderID != null && !"".equals(orderID)) {
                        content += "orderID:" + orderID;
                    }
                    content += getString(R.string.format_id) + " " + formatID + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                    content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
//					content += getString(R.string.ksn) + " " + ksn + "\n";
                    content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
                    content += getString(R.string.trackksn) + " " + trackksn + "\n";
                    content += getString(R.string.service_code) + " " + serviceCode + "\n";
                    content += getString(R.string.track_1_length) + " " + track1Length + "\n";
                    content += getString(R.string.track_2_length) + " " + track2Length + "\n";
                    content += getString(R.string.track_3_length) + " " + track3Length + "\n";
                    content += getString(R.string.encrypted_tracks) + " " + encTracks + "\n";
                    content += getString(R.string.encrypted_track_1) + " " + encTrack1 + "\n";
                    content += getString(R.string.encrypted_track_2) + " " + encTrack2 + "\n";
                    content += getString(R.string.encrypted_track_3) + " " + encTrack3 + "\n";
                    content += getString(R.string.partial_track) + " " + partialTrack + "\n";
                    content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
                    content += "encPAN: " + encPAN + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    content += "pinRandomNumber:" + " " + pinRandomNumber + "\n";
                    String realPan = null;
                    if (!TextUtils.isEmpty(trackksn) && !TextUtils.isEmpty(encTrack2)) {
                        String clearPan = DUKPK2009_CBC.getData(trackksn, encTrack2, DUKPK2009_CBC.Enum_key.DATA, DUKPK2009_CBC.Enum_mode.CBC);
                        content += "encTrack2:" + " " + clearPan + "\n";
                        realPan = clearPan.substring(0, maskedPAN.length());
                        content += "realPan:" + " " + realPan + "\n";
                    }
                    if (!TextUtils.isEmpty(pinKsn) && !TextUtils.isEmpty(pinBlock) && !TextUtils.isEmpty(realPan)) {
                        String date = DUKPK2009_CBC.getData(pinKsn, pinBlock, DUKPK2009_CBC.Enum_key.PIN, DUKPK2009_CBC.Enum_mode.CBC);
                        String parsCarN = "0000" + realPan.substring(realPan.length() - 13, realPan.length() - 1);
                        String s = DUKPK2009_CBC.xor(parsCarN, date);
                        content += "PIN:" + " " + s + "\n";
                    }
                }

                Log.e("TAG", content);
//                autoDoTrade(0);

            } else if ((result == QPOSService.DoTradeResult.NFC_ONLINE) || (result == QPOSService.DoTradeResult.NFC_OFFLINE)) {
                nfcLog = decodeData.get("nfcLog");
                String content = getString(R.string.tap_card);
                String formatID = decodeData.get("formatID");
                if (formatID.equals("31") || formatID.equals("40")
                        || formatID.equals("37") || formatID.equals("17")
                        || formatID.equals("11") || formatID.equals("10")) {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String trackblock = decodeData.get("trackblock");
                    String psamId = decodeData.get("psamId");
                    String posId = decodeData.get("posId");
                    String pinblock = decodeData.get("pinblock");
                    String macblock = decodeData.get("macblock");
                    String activateCode = decodeData.get("activateCode");
                    String trackRandomNumber = decodeData
                            .get("trackRandomNumber");

                    content += getString(R.string.format_id) + " " + formatID
                            + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN
                            + "\n";
                    content += getString(R.string.expiry_date) + " "
                            + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " "
                            + cardHolderName + "\n";

                    content += getString(R.string.service_code) + " "
                            + serviceCode + "\n";
                    content += "trackblock: " + trackblock + "\n";
                    content += "psamId: " + psamId + "\n";
                    content += "posId: " + posId + "\n";
                    content += getString(R.string.pinBlock) + " " + pinblock
                            + "\n";
                    content += "macblock: " + macblock + "\n";
                    content += "activateCode: " + activateCode + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                } else {

                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
//					String ksn = decodeData.get("ksn");
                    String serviceCode = decodeData.get("serviceCode");
                    String track1Length = decodeData.get("track1Length");
                    String track2Length = decodeData.get("track2Length");
                    String track3Length = decodeData.get("track3Length");
                    String encTracks = decodeData.get("encTracks");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    String partialTrack = decodeData.get("partialTrack");
                    String pinKsn = decodeData.get("pinKsn");
                    String trackksn = decodeData.get("trackksn");
                    String pinBlock = decodeData.get("pinBlock");
                    String encPAN = decodeData.get("encPAN");
                    String trackRandomNumber = decodeData
                            .get("trackRandomNumber");
                    String pinRandomNumber = decodeData.get("pinRandomNumber");

                    content += getString(R.string.format_id) + " " + formatID
                            + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN
                            + "\n";
                    content += getString(R.string.expiry_date) + " "
                            + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " "
                            + cardHolderName + "\n";
//					content += getString(R.string.ksn) + " " + ksn + "\n";
                    content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
                    content += getString(R.string.trackksn) + " " + trackksn
                            + "\n";
                    content += getString(R.string.service_code) + " "
                            + serviceCode + "\n";
                    content += getString(R.string.track_1_length) + " "
                            + track1Length + "\n";
                    content += getString(R.string.track_2_length) + " "
                            + track2Length + "\n";
                    content += getString(R.string.track_3_length) + " "
                            + track3Length + "\n";
                    content += getString(R.string.encrypted_tracks) + " "
                            + encTracks + "\n";
                    content += getString(R.string.encrypted_track_1) + " "
                            + encTrack1 + "\n";
                    content += getString(R.string.encrypted_track_2) + " "
                            + encTrack2 + "\n";
                    content += getString(R.string.encrypted_track_3) + " "
                            + encTrack3 + "\n";
                    content += getString(R.string.partial_track) + " "
                            + partialTrack + "\n";
                    content += getString(R.string.pinBlock) + " " + pinBlock
                            + "\n";
                    content += "encPAN: " + encPAN + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    content += "pinRandomNumber:" + " " + pinRandomNumber
                            + "\n";
                }
                Log.e("TAG", content);
                sendMsg(8003);
            } else if ((result == QPOSService.DoTradeResult.NFC_DECLINED)) {
                Log.e("TAG", getString(R.string.transaction_declined));
            } else if (result == QPOSService.DoTradeResult.NO_RESPONSE) {
                Log.e("TAG", getString(R.string.card_no_response));
            }

        }

        @Override
        public void onQposInfoResult(Hashtable<String, String> posInfoData) {
            TRACE.d("onQposInfoResult" + posInfoData.toString());
            String isSupportedTrack1 = posInfoData.get("isSupportedTrack1") == null ? "" : posInfoData.get("isSupportedTrack1");
            String isSupportedTrack2 = posInfoData.get("isSupportedTrack2") == null ? "" : posInfoData.get("isSupportedTrack2");
            String isSupportedTrack3 = posInfoData.get("isSupportedTrack3") == null ? "" : posInfoData.get("isSupportedTrack3");
            String bootloaderVersion = posInfoData.get("bootloaderVersion") == null ? "" : posInfoData.get("bootloaderVersion");
            String firmwareVersion = posInfoData.get("firmwareVersion") == null ? "" : posInfoData.get("firmwareVersion");
            String isUsbConnected = posInfoData.get("isUsbConnected") == null ? "" : posInfoData.get("isUsbConnected");
            String isCharging = posInfoData.get("isCharging") == null ? "" : posInfoData.get("isCharging");
            String batteryLevel = posInfoData.get("batteryLevel") == null ? "" : posInfoData.get("batteryLevel");
            String batteryPercentage = posInfoData.get("batteryPercentage") == null ? ""
                    : posInfoData.get("batteryPercentage");
            String hardwareVersion = posInfoData.get("hardwareVersion") == null ? "" : posInfoData.get("hardwareVersion");
            String SUB = posInfoData.get("SUB") == null ? "" : posInfoData.get("SUB");
            String pciFirmwareVersion = posInfoData.get("PCI_firmwareVersion") == null ? ""
                    : posInfoData.get("PCI_firmwareVersion");
            String pciHardwareVersion = posInfoData.get("PCI_hardwareVersion") == null ? ""
                    : posInfoData.get("PCI_hardwareVersion");
            String compileTime = posInfoData.get("compileTime") == null ? ""
                    : posInfoData.get("compileTime");
            String content = "";
            content += getString(R.string.bootloader_version) + bootloaderVersion + "\n";
            content += getString(R.string.firmware_version) + firmwareVersion + "\n";
            content += getString(R.string.usb) + isUsbConnected + "\n";
            content += getString(R.string.charge) + isCharging + "\n";
//			if (batteryPercentage==null || "".equals(batteryPercentage)) {
            content += getString(R.string.battery_level) + batteryLevel + "\n";
//			}else {
            content += getString(R.string.battery_percentage) + batteryPercentage + "\n";
//			}
            content += getString(R.string.hardware_version) + hardwareVersion + "\n";
            content += "SUB : " + SUB + "\n";
            content += getString(R.string.track_1_supported) + isSupportedTrack1 + "\n";
            content += getString(R.string.track_2_supported) + isSupportedTrack2 + "\n";
            content += getString(R.string.track_3_supported) + isSupportedTrack3 + "\n";
            content += "PCI FirmwareVresion:" + pciFirmwareVersion + "\n";
            content += "PCI HardwareVersion:" + pciHardwareVersion + "\n";
            content += "compileTime:" + compileTime + "\n";
            Log.e("TAG", content);
        }

        /**
         * Request transaction
         *
         * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestTransactionResult(com.dspread.xpos.QPOSService.TransactionResult)
         */
        @Override
        public void onRequestTransactionResult(QPOSService.TransactionResult transactionResult) {
            TRACE.d("onRequestTransactionResult()" + transactionResult.toString());
            if (transactionResult == QPOSService.TransactionResult.CARD_REMOVED) {
                clearDisplay();
            }

            dismissDialog();

            dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.alert_dialog);
            dialog.setTitle(R.string.transaction_result);
            TextView messageTextView = (TextView) dialog.findViewById(R.id.messageTextView);

            if (transactionResult == QPOSService.TransactionResult.APPROVED) {
                TRACE.d("TransactionResult.APPROVED");
                String message = getString(R.string.transaction_approved) + "\n" + getString(R.string.amount) + ": $" + amount + "\n";
                if (!cashbackAmount.equals("")) {
                    message += getString(R.string.cashback_amount) + ": INR" + cashbackAmount;
                }
                messageTextView.setText(message);


            } else if (transactionResult == QPOSService.TransactionResult.TERMINATED) {
                clearDisplay();
                messageTextView.setText(getString(R.string.transaction_terminated));
            } else if (transactionResult == QPOSService.TransactionResult.DECLINED) {
                messageTextView.setText(getString(R.string.transaction_declined));

            } else if (transactionResult == QPOSService.TransactionResult.CANCEL) {
                clearDisplay();
                messageTextView.setText(getString(R.string.transaction_cancel));

            } else if (transactionResult == QPOSService.TransactionResult.CAPK_FAIL) {
                messageTextView.setText(getString(R.string.transaction_capk_fail));
            } else if (transactionResult == QPOSService.TransactionResult.NOT_ICC) {
                messageTextView.setText(getString(R.string.transaction_not_icc));
            } else if (transactionResult == QPOSService.TransactionResult.SELECT_APP_FAIL) {
                messageTextView.setText(getString(R.string.transaction_app_fail));
            } else if (transactionResult == QPOSService.TransactionResult.DEVICE_ERROR) {
                messageTextView.setText(getString(R.string.transaction_device_error));
            } else if (transactionResult == QPOSService.TransactionResult.TRADE_LOG_FULL) {
                Log.e("TAG", "pls clear the trace log and then to begin do trade");
                messageTextView.setText("the trade log has fulled!pls clear the trade log!");
            } else if (transactionResult == QPOSService.TransactionResult.CARD_NOT_SUPPORTED) {
                messageTextView.setText(getString(R.string.card_not_supported));
            } else if (transactionResult == QPOSService.TransactionResult.MISSING_MANDATORY_DATA) {
                messageTextView.setText(getString(R.string.missing_mandatory_data));
            } else if (transactionResult == QPOSService.TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS) {
                messageTextView.setText(getString(R.string.card_blocked_or_no_evm_apps));
            } else if (transactionResult == QPOSService.TransactionResult.INVALID_ICC_DATA) {
                messageTextView.setText(getString(R.string.invalid_icc_data));
            } else if (transactionResult == QPOSService.TransactionResult.FALLBACK) {
                messageTextView.setText("trans fallback");
            } else if (transactionResult == QPOSService.TransactionResult.NFC_TERMINATED) {
                clearDisplay();
                messageTextView.setText("NFC Terminated");
            } else if (transactionResult == QPOSService.TransactionResult.CARD_REMOVED) {
                clearDisplay();
                messageTextView.setText("CARD REMOVED");
            } else if (transactionResult == QPOSService.TransactionResult.TRANS_TOKEN_INVALID) {
                clearDisplay();
                messageTextView.setText("TOKEN INVALID");
            }


            dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismissDialog();
                }
            });
            Toast.makeText(getApplicationContext(), messageTextView.getText(), Toast.LENGTH_SHORT).show();
//            dialog.show();
            amount = "";
            cashbackAmount = "";
        }

        @Override
        public void onRequestBatchData(String tlv) {
            TRACE.d(getString(R.string.end_transaction));
            String content = getString(R.string.batch_data);
            TRACE.d("onRequestBatchData(String tlv):" + tlv);
            content += tlv;
            Log.e("TAG", content);
//            autoDoTrade(0);
        }

        @Override
        public void onRequestTransactionLog(String tlv) {
            TRACE.d("onRequestTransactionLog(String tlv):" + tlv);
            dismissDialog();
            String content = getString(R.string.transaction_log);
            content += tlv;
            Log.e("TAG", content);
        }

        @Override
        public void onQposIdResult(Hashtable<String, String> posIdTable) {
            TRACE.w("onQposIdResult():" + posIdTable.toString());
            String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
            serialNo = posId;

            String csn = posIdTable.get("csn") == null ? "" : posIdTable.get("csn");
            String psamId = posIdTable.get("psamId") == null ? "" : posIdTable
                    .get("psamId");
            String NFCId = posIdTable.get("nfcID") == null ? "" : posIdTable
                    .get("nfcID");
            String content = "";
            content += getString(R.string.posId) + posId + "\n";
            content += "csn: " + csn + "\n";
            content += "conn: " + pos.getBluetoothState() + "\n";
            content += "psamId: " + psamId + "\n";
            content += "NFCId: " + NFCId + "\n";
            if (!isVisiblePosID) {
                Log.e("TAG", content);
            } else {
                isVisiblePosID = false;
                BaseApplication.setmPosID(posId);
            }

//            if (pos == null) {
//                pos = QPOSService.getInstance(QPOSService.CommunicationMode.BLUETOOTH);
//                pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("NIGERIA-QPOS cute,CR100,D20,D30.xml", OtherActivity.this)));
//                Log.e("TAG open", "updating...");
//            } else {
//                pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("NIGERIA-QPOS cute,CR100,D20,D30.xml", OtherActivity.this)));
//                Log.e("TAG open", "updating...");
//            }
        }

        @Override
        public void onRequestSelectEmvApp(ArrayList<String> appList) {
            TRACE.d("onRequestSelectEmvApp():" + appList.toString());
            TRACE.d(getString(R.string.select_app_start));
            dismissDialog();
            dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.emv_app_dialog);
            dialog.setTitle(R.string.please_select_app);
            String[] appNameList = new String[appList.size()];
            for (int i = 0; i < appNameList.length; ++i) {
                appNameList[i] = appList.get(i);
            }
            appListView = (ListView) dialog.findViewById(R.id.appList);
            appListView.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, appNameList));
            appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    pos.selectEmvApp(position);
                    TRACE.d(getString(R.string.select_app_end) + position);
                    dismissDialog();
                }

            });
            dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pos.cancelSelectEmvApp();
                    dismissDialog();
                }
            });
            dialog.show();
        }

        @Override
        public void onRequestSetAmount() {
            TRACE.d("enter amount -- start");
            TRACE.d("onRequestSetAmount()");
            dismissDialog();
            dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.amount_dialog);
            dialog.setTitle(getString(R.string.set_amount));
            String[] transactionTypes = new String[]{"GOODS", "SERVICES", "CASH", "CASHBACK", "INQUIRY",
                    "TRANSFER", "ADMIN", "CASHDEPOSIT",
                    "PAYMENT", "PBOCLOG||ECQ_INQUIRE_LOG", "SALE",
                    "PREAUTH", "ECQ_DESIGNATED_LOAD", "ECQ_UNDESIGNATED_LOAD",
                    "ECQ_CASH_LOAD", "ECQ_CASH_LOAD_VOID", "CHANGE_PIN", "REFOUND", "SALES_NEW"};
            ((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item,
                    transactionTypes));

            transactionType = TransactionType.GOODS;

            Log.e("TAG amount Int", appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT_INT));
            Log.e("TAG amount", appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT));

            OtherActivity.this.amount = appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT_INT);
            insert_amount.setText(appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT));

            OtherActivity.this.cashbackAmount = cashbackAmount;
            pos.setAmount(amount + "00", cashbackAmount, "566", TransactionType.GOODS);

//            dialog.findViewById(R.id.setButton).setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//
//                    String amount = ((EditText) (dialog.findViewById(R.id.amountEditText))).getText().toString();
//                    String cashbackAmount = ((EditText) (dialog.findViewById(R.id.cashbackAmountEditText))).getText().toString();
//                    String transactionTypeString = (String) ((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).getSelectedItem();
//
//                    TransactionType transactionType = null;
//                    if (transactionTypeString.equals("GOODS")) {
//                        transactionType = QPOSService.TransactionType.GOODS;
//                    } else if (transactionTypeString.equals("SERVICES")) {
//                        transactionType = QPOSService.TransactionType.SERVICES;
//                    } else if (transactionTypeString.equals("CASH")) {
//                        transactionType = QPOSService.TransactionType.CASH;
//                    } else if (transactionTypeString.equals("CASHBACK")) {
//                        transactionType = QPOSService.TransactionType.CASHBACK;
//                    } else if (transactionTypeString.equals("INQUIRY")) {
//                        transactionType = QPOSService.TransactionType.INQUIRY;
//                    } else if (transactionTypeString.equals("TRANSFER")) {
//                        transactionType = QPOSService.TransactionType.TRANSFER;
//                    } else if (transactionTypeString.equals("ADMIN")) {
//                        transactionType = QPOSService.TransactionType.ADMIN;
//                    } else if (transactionTypeString.equals("CASHDEPOSIT")) {
//                        transactionType = QPOSService.TransactionType.CASHDEPOSIT;
//                    } else if (transactionTypeString.equals("PAYMENT")) {
//                        transactionType = QPOSService.TransactionType.PAYMENT;
//                    } else if (transactionTypeString.equals("PBOCLOG||ECQ_INQUIRE_LOG")) {
//                        transactionType = QPOSService.TransactionType.PBOCLOG;
//                    } else if (transactionTypeString.equals("SALE")) {
//                        transactionType = QPOSService.TransactionType.SALE;
//                    } else if (transactionTypeString.equals("PREAUTH")) {
//                        transactionType = QPOSService.TransactionType.PREAUTH;
//                    } else if (transactionTypeString.equals("ECQ_DESIGNATED_LOAD")) {
//                        transactionType = QPOSService.TransactionType.ECQ_DESIGNATED_LOAD;
//                    } else if (transactionTypeString.equals("ECQ_UNDESIGNATED_LOAD")) {
//                        transactionType = QPOSService.TransactionType.ECQ_UNDESIGNATED_LOAD;
//                    } else if (transactionTypeString.equals("ECQ_CASH_LOAD")) {
//                        transactionType = QPOSService.TransactionType.ECQ_CASH_LOAD;
//                    } else if (transactionTypeString.equals("ECQ_CASH_LOAD_VOID")) {
//                        transactionType = QPOSService.TransactionType.ECQ_CASH_LOAD_VOID;
//                    } else if (transactionTypeString.equals("CHANGE_PIN")) {
//                        transactionType = QPOSService.TransactionType.UPDATE_PIN;
//                    } else if (transactionTypeString.equals("REFOUND")) {
//                        transactionType = QPOSService.TransactionType.REFUND;
//                    } else if (transactionTypeString.equals("SALES_NEW")) {
//                        transactionType = QPOSService.TransactionType.SALES_NEW;
//                    }
//                    OtherActivity.this.amount = amount;
//                    OtherActivity.this.cashbackAmount = cashbackAmount;
//                    pos.setAmount(amount, cashbackAmount, "156", transactionType);
//                    TRACE.d("enter amount  -- end");
//                    dismissDialog();
//                }
//            });
//
//            dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    pos.cancelSetAmount();
//                    dialog.dismiss();
//                }
//            });
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.show();
//            pos.setAmount("200", cashbackAmount, "156", QPOSService.TransactionType.GOODS);
        }

        /**
         * judge request server is connected or not
         *
         * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestIsServerConnected()
         */
        @Override
        public void onRequestIsServerConnected() {
            TRACE.d("onRequestIsServerConnected()");
            pos.isServerConnected(true);
        }

        @Override
        public void onRequestOnlineProcess(final String tlv) {
            TRACE.d("onRequestOnlineProcess" + tlv);
            dismissDialog();
            dialog = new Dialog(mContext);
            dialog.setContentView(R.layout.alert_dialog);
            dialog.setTitle(R.string.request_data_to_server);
            Hashtable<String, String> decodeData = pos.anlysEmvIccData(tlv);
            TRACE.d("anlysEmvIccData(tlv):" + decodeData.toString());

//            if (isPinCanceled) {
//                ((TextView) dialog.findViewById(R.id.messageTextView))
//                        .setText(R.string.replied_failed);
//            } else {
//                ((TextView) dialog.findViewById(R.id.messageTextView))
//                        .setText(R.string.replied_success);
//            }
//            try {
////                    analyData(tlv);// analy tlv ,get the tag you need
//            } catch (Exception e) {
//                e.printStackTrace();
//            }


            String track2 = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "57"));
            Log.e("Tag track2", track2.substring(5, track2.length() - 1));

            String cardNo = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "5A"));
            Log.e("Tag CardNo", cardNo.substring(5, cardNo.length() - 1));

            String expiryDate = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "5F24"));
            Log.e("Tag expiryDate", expiryDate.substring(5, expiryDate.length() - 1));

            String currencyCode = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "5F2A"));
            Log.e("Tag currencyCode", currencyCode.substring(5, currencyCode.length() - 1));

            String countryCode = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "9F1A"));
            Log.e("Tag countryCode", countryCode.substring(5, countryCode.length() - 1));

            creditCard.setCardNumber(cardNo);
            creditCard.setExpireDate(expiryDate);

            try {
                if (isPinCanceled) {
                    pos.sendOnlineProcessResult(null);
                } else {
//									String str = "5A0A6214672500000000056F5F24032307315F25031307085F2A0201565F34010182027C008407A00000033301018E0C000000000000000002031F009505088004E0009A031406179C01009F02060000000000019F03060000000000009F0702AB009F080200209F0902008C9F0D05D86004A8009F0E0500109800009F0F05D86804F8009F101307010103A02000010A010000000000CE0BCE899F1A0201569F1E0838333230314943439F21031826509F2608881E2E4151E527899F2701809F3303E0F8C89F34030203009F3501229F3602008E9F37042120A7189F4104000000015A0A6214672500000000056F5F24032307315F25031307085F2A0201565F34010182027C008407A00000033301018E0C000000000000000002031F00";
//									str = "9F26088930C9018CAEBCD69F2701809F101307010103A02802010A0100000000007EF350299F370415B4E5829F360202179505000004E0009A031504169C01009F02060000000010005F2A02015682027C009F1A0201569F03060000000000009F330360D8C89F34030203009F3501229F1E0838333230314943438408A0000003330101019F090200209F410400000001";
                    String str = "8A023030";//Currently the default value,
                    // should be assigned to the server to return data,
                    // the data format is TLV
//                                pos.sendOnlineProcessResult(str);//Script notification/55domain/ICCDATA
                    Log.e("Check", "Process Trans");
                    proceedToExChangeData("00", creditCard);
                    insert_text.setText("Transaction Processing...");
                    dismissDialog();
                }
//                    analyData(tlv);// analy tlv ,get the tag you need
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//            dialog.findViewById(R.id.confirmButton).setOnClickListener(
//                    new View.OnClickListener() {
//
//                        @Override
//                        public void onClick(View v) {
//                            if (isPinCanceled) {
//                                pos.sendOnlineProcessResult(null);
//                            } else {
////									String str = "5A0A6214672500000000056F5F24032307315F25031307085F2A0201565F34010182027C008407A00000033301018E0C000000000000000002031F009505088004E0009A031406179C01009F02060000000000019F03060000000000009F0702AB009F080200209F0902008C9F0D05D86004A8009F0E0500109800009F0F05D86804F8009F101307010103A02000010A010000000000CE0BCE899F1A0201569F1E0838333230314943439F21031826509F2608881E2E4151E527899F2701809F3303E0F8C89F34030203009F3501229F3602008E9F37042120A7189F4104000000015A0A6214672500000000056F5F24032307315F25031307085F2A0201565F34010182027C008407A00000033301018E0C000000000000000002031F00";
////									str = "9F26088930C9018CAEBCD69F2701809F101307010103A02802010A0100000000007EF350299F370415B4E5829F360202179505000004E0009A031504169C01009F02060000000010005F2A02015682027C009F1A0201569F03060000000000009F330360D8C89F34030203009F3501229F1E0838333230314943438408A0000003330101019F090200209F410400000001";
//                                String str = "8A023030";//Currently the default value,
//                                // should be assigned to the server to return data,
//                                // the data format is TLV
//                                pos.sendOnlineProcessResult(str);//Script notification/55domain/ICCDATA
//
//                            }
//                            dismissDialog();
//                        }
//                    });
//
//            dialog.show();
//        }

        @Override
        public void onRequestTime() {
            TRACE.d("onRequestTime");
            dismissDialog();
            String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
            pos.sendTime(terminalTime);
            Log.e("TAG", getString(R.string.request_terminal_time) + " " + terminalTime);
        }

        @Override
        public void onRequestDisplay(QPOSService.Display displayMsg) {
            TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());

            dismissDialog();

            String msg = "";
            if (displayMsg == QPOSService.Display.CLEAR_DISPLAY_MSG) {
                msg = "";
            } else if (displayMsg == QPOSService.Display.MSR_DATA_READY) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Audio");
                builder.setMessage("Success,Contine ready");
                builder.setPositiveButton("Confirm", null);
                builder.show();
            } else if (displayMsg == QPOSService.Display.PLEASE_WAIT) {
                msg = getString(R.string.wait);
            } else if (displayMsg == QPOSService.Display.REMOVE_CARD) {
                msg = getString(R.string.remove_card);
            } else if (displayMsg == QPOSService.Display.TRY_ANOTHER_INTERFACE) {
                msg = getString(R.string.try_another_interface);
            } else if (displayMsg == QPOSService.Display.PROCESSING) {
                msg = getString(R.string.processing);

            } else if (displayMsg == QPOSService.Display.INPUT_PIN_ING) {
                msg = "please input pin on pos";

            } else if (displayMsg == QPOSService.Display.INPUT_OFFLINE_PIN_ONLY || displayMsg == QPOSService.Display.INPUT_LAST_OFFLINE_PIN) {
                msg = "please input offline pin on pos";

            } else if (displayMsg == QPOSService.Display.MAG_TO_ICC_TRADE) {
                msg = "please insert chip card on pos";
            } else if (displayMsg == QPOSService.Display.CARD_REMOVED) {
                msg = "card removed";
            }
            Log.e("TAG", msg);
        }

        @Override
        public void onRequestFinalConfirm() {
            TRACE.d("onRequestFinalConfirm() ");
            TRACE.d("onRequestFinalConfirm+Confirm Amount-- S");
            dismissDialog();
            if (!isPinCanceled) {
                dialog = new Dialog(mContext);
                dialog.setContentView(R.layout.confirm_dialog);
                dialog.setTitle(getString(R.string.confirm_amount));

                String message = getString(R.string.amount) + ": $" + amount;
                if (!cashbackAmount.equals("")) {
                    message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmount;
                }

                ((TextView) dialog.findViewById(R.id.messageTextView)).setText(message);

                dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pos.finalConfirm(true);
                        dialog.dismiss();
                        TRACE.d("Confirm Amount-- End");
                    }
                });

                dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pos.finalConfirm(false);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            } else {
                pos.finalConfirm(false);
            }
        }

        @Override
        public void onRequestNoQposDetected() {
            TRACE.d("onRequestNoQposDetected()");
            dismissDialog();
            Log.e("TAG", getString(R.string.no_device_detected));
        }

        @Override
        public void onRequestQposConnected() {
            TRACE.d("onRequestQposConnected()");
            Toast.makeText(mContext, "Mpos Connected", Toast.LENGTH_LONG).show();
            dismissDialog();
            Log.e("TAG", getString(R.string.device_plugged));

            btnUSB.setVisibility(View.INVISIBLE);

            continueBtn.setVisibility(View.VISIBLE);
            usbImage.setVisibility(View.VISIBLE);
            connectedText.setVisibility(View.VISIBLE);

//            doTradeButton.setEnabled(true);
//            btnDisconnect.setEnabled(true);
            if (ActivityCompat.checkSelfPermission(OtherActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PERMISSION_GRANTED) {
                //申请权限
                ActivityCompat.requestPermissions(OtherActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            pos.getQposId();
            isVisiblePosID = true;
        }

        @Override
        public void onRequestQposDisconnected() {
            dismissDialog();
            TRACE.d("onRequestQposDisconnected()");

            Log.e("TAG", getString(R.string.device_unplugged));
            if (pos != null) {
                String occupyPackName = pos.getOccupyPackName();
                if (!TextUtils.isEmpty(occupyPackName)) {
                    Toast.makeText(mContext, "The port is occupied:" + occupyPackName, Toast.LENGTH_SHORT).show();
                }
            }

            btnDisconnect.setEnabled(false);
            doTradeButton.setEnabled(false);
        }

        @Override
        public void onError(QPOSService.Error errorState) {
            if (updateThread != null) {
                updateThread.concelSelf();
            }

            TRACE.d("onError" + errorState.toString());
            dismissDialog();

            if (errorState == QPOSService.Error.CMD_NOT_AVAILABLE) {
                Log.e("TAG", getString(R.string.command_not_available));

            } else if (errorState == QPOSService.Error.TIMEOUT) {
                Log.e("TAG", getString(R.string.device_no_response));
            } else if (errorState == QPOSService.Error.DEVICE_RESET) {
                Log.e("TAG", getString(R.string.device_reset));
            } else if (errorState == QPOSService.Error.UNKNOWN) {
                Log.e("TAG", getString(R.string.unknown_error));
            } else if (errorState == QPOSService.Error.DEVICE_BUSY) {
                Log.e("TAG", getString(R.string.device_busy));
            } else if (errorState == QPOSService.Error.INPUT_OUT_OF_RANGE) {
                Log.e("TAG", getString(R.string.out_of_range));
            } else if (errorState == QPOSService.Error.INPUT_INVALID_FORMAT) {
                Log.e("TAG", getString(R.string.invalid_format));
            } else if (errorState == QPOSService.Error.INPUT_ZERO_VALUES) {
                Log.e("TAG", getString(R.string.zero_values));
            } else if (errorState == QPOSService.Error.INPUT_INVALID) {
                Log.e("TAG", getString(R.string.input_invalid));
            } else if (errorState == QPOSService.Error.CASHBACK_NOT_SUPPORTED) {
                Log.e("TAG", getString(R.string.cashback_not_supported));
            } else if (errorState == QPOSService.Error.CRC_ERROR) {
                Log.e("TAG", getString(R.string.crc_error));
            } else if (errorState == QPOSService.Error.COMM_ERROR) {
                Log.e("TAG", getString(R.string.comm_error));
            } else if (errorState == QPOSService.Error.MAC_ERROR) {
                Log.e("TAG", getString(R.string.mac_error));
            } else if (errorState == QPOSService.Error.APP_SELECT_TIMEOUT) {
                Log.e("TAG", getString(R.string.app_select_timeout_error));
            } else if (errorState == QPOSService.Error.CMD_TIMEOUT) {
                Log.e("TAG", getString(R.string.cmd_timeout));
            } else if (errorState == QPOSService.Error.ICC_ONLINE_TIMEOUT) {
                if (pos == null) {
                    return;
                }
                pos.resetPosStatus();
                Log.e("TAG POS", getString(R.string.device_reset));
                Toast.makeText(getApplicationContext(), R.string.device_reset, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onReturnReversalData(String tlv) {
            String content = getString(R.string.reversal_data);
            content += tlv;
            TRACE.d("onReturnReversalData(): " + tlv);
            Log.e("TAG", content);
        }

        @Override
        public void onReturnGetPinResult(Hashtable<String, String> result) {
            TRACE.d("onReturnGetPinResult(Hashtable<String, String> result):" + result.toString());
            String pinBlock = result.get("pinBlock");
            String pinKsn = result.get("pinKsn");
            String content = "get pin result\n";
            content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
            content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
            Log.e("TAG", content);
            TRACE.i(content);
        }

        @Override
        public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
        }

        @Override
        public void onReturnPowerOffIccResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnPowerOffIccResult(boolean arg0):" + arg0);

        }

        @Override
        public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) :" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);

            if (arg0) {
                pos.sendApdu("123456");
            }
        }

        @Override
        public void onReturnSetSleepTimeResult(boolean isSuccess) {
            TRACE.d("onReturnSetSleepTimeResult(boolean isSuccess):" + isSuccess);

            String content = "";
            if (isSuccess) {
                content = "set the sleep time success.";
            } else {
                content = "set the sleep time failed.";
            }
            Log.e("TAG", content);
        }

        @Override
        public void onGetCardNoResult(String cardNo) {//get card number result
            TRACE.d("onGetCardNoResult(String cardNo):" + cardNo);

            Log.e("TAG", "cardNo: " + cardNo);
        }

        @Override
        public void onRequestCalculateMac(String calMac) {
            TRACE.d("onRequestCalculateMac(String calMac):" + calMac);

            if (calMac != null && !"".equals(calMac)) {
                calMac = QPOSUtil.byteArray2Hex(calMac.getBytes());
            }
            Log.e("TAG", "calMac: " + calMac);
            TRACE.d("calMac_result: calMac=> e: " + calMac);

        }

        @Override
        public void onRequestSignatureResult(byte[] arg0) {
            TRACE.d("onRequestSignatureResult(byte[] arg0):" + arg0.toString());

        }

        @Override
        public void onRequestUpdateWorkKeyResult(QPOSService.UpdateInformationResult result) {
            TRACE.d("onRequestUpdateWorkKeyResult(UpdateInformationResult result):" + result);

            if (result == QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
                Log.e("TAG", "update work key success");
            } else if (result == QPOSService.UpdateInformationResult.UPDATE_FAIL) {
                Log.e("TAG", "update work key fail");
            } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
                Log.e("TAG", "update work key packet vefiry error");
            } else if (result == QPOSService.UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
                Log.e("TAG", "update work key packet len error");
            }
        }

        @Override
        public void onReturnCustomConfigResult(boolean isSuccess, String result) {

            if (isSuccess){
                appPreferenceHelper.setSharedPreferenceBoolean(Constants.IS_KEY_INJECTED, true);
                appPreferenceHelper.setSharedPreferenceString(Constants.KEY_INJECTED_BLUETOOTH, blueTootchAddress);
                Log.e("Log TAG", "Key Injected: " + isSuccess + "\nblueTootchAddress: " + blueTootchAddress);
            }else {
                appPreferenceHelper.setSharedPreferenceBoolean(Constants.IS_KEY_INJECTED, false);
            }

            insert_text.setText("Insert Card into the mPOS");
            isPinCanceled = false;
            Log.e("TAG", String.valueOf(R.string.starting));
//                terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
            if (posType == POS_TYPE.UART) {//postype is UART
                pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
//                    pos.doTrade(terminalTime, 0, 30);
                pos.doTrade(20);
            } else {
                int keyIdex = getKeyIndex();
//                    pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
                pos.doTrade(keyIdex, 30);//start do trade
            }

            TRACE.d("onReturnCustomConfigResult(boolean isSuccess, String result):" + isSuccess + TRACE.NEW_LINE + result);
            Log.e("TAG", "result: " + isSuccess + "\ndata: " + result);
        }

        @Override
        public void onRequestSetPin() {
            TRACE.d("onRequestSetPin()");
            creditCard = new CreditCard();

            dismissDialog();
            dialog = new Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.pin_dialog);
            dialog.setTitle(getString(R.string.enter_pin));
            dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String pin = ((EditText) dialog.findViewById(R.id.pinEditText)).getText().toString();
                    if (pin.length() >= 4 && pin.length() <= 12) {
//                        if (pin.equals("000000")) {
//                            pos.sendEncryptPin("5516422217375116");
//
//                        } else {
//                            pos.sendPin(pin);
//                        }

//                        TRACE.d("myPinblock" + " " + pin);
                        cPin = pin;
                        creditCard.setPIN(pin);
                        pos.sendPin(pin);

                        dismissDialog();
                    }
                }
            });

//            dialog.findViewById(R.id.bypassButton).setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
////					pos.bypassPin();
//                    pos.sendPin("");
//
//                    dismissDialog();
//                }
//            });

//            dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    isPinCanceled = true;
//                    pos.cancelPin();
//                    dismissDialog();
//                }
//            });

            dialog.show();

        }


        @Override
        public void onReturnSetMasterKeyResult(boolean isSuccess) {
            TRACE.d("onReturnSetMasterKeyResult(boolean isSuccess) : " + isSuccess);


            Log.e("TAG", "result: " + isSuccess);
        }

        @Override
        public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult) {
            TRACE.d("onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult):" + batchAPDUResult.toString());

            StringBuilder sb = new StringBuilder();
            sb.append("APDU Responses: \n");
            for (HashMap.Entry<Integer, String> entry : batchAPDUResult.entrySet()) {
                sb.append("[" + entry.getKey() + "]: " + entry.getValue() + "\n");
            }
            Log.e("TAG", "\n" + sb.toString());
        }

        @Override
        public void onBluetoothBondFailed() {
            TRACE.d("onBluetoothBondFailed()");
            Log.e("TAG", "bond failed");
        }

        @Override
        public void onBluetoothBondTimeout() {
            TRACE.d("onBluetoothBondTimeout()");
            Log.e("TAG", "bond timeout");
        }

        @Override
        public void onBluetoothBonded() {
            TRACE.d("onBluetoothBonded()");
            Log.e("TAG", "bond success");

        }

        @Override
        public void onBluetoothBonding() {
            TRACE.d("onBluetoothBonding()");
            Log.e("TAG", "bonding .....");

        }

        @Override
        public void onReturniccCashBack(Hashtable<String, String> result) {
            TRACE.d("onReturniccCashBack(Hashtable<String, String> result):" + result.toString());
            String s = "serviceCode: " + result.get("serviceCode");
            s += "\n";
            s += "trackblock: " + result.get("trackblock");

            Log.e("TAG", s);

        }


        @Override
        public void onLcdShowCustomDisplay(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onLcdShowCustomDisplay(boolean arg0):" + arg0);
        }

        @Override
        public void onUpdatePosFirmwareResult(QPOSService.UpdateInformationResult arg0) {
            TRACE.d("onUpdatePosFirmwareResult(UpdateInformationResult arg0):" + arg0.toString());
//            isUpdateFw = false;
            if (arg0 != QPOSService.UpdateInformationResult.UPDATE_SUCCESS) {
                updateThread.concelSelf();
            } else {
                mhipStatus.setText("");
            }
            Log.e("TAG", "onUpdatePosFirmwareResult" + arg0.toString());
        }

        @Override
        public void onReturnDownloadRsaPublicKey(HashMap<String, String> map) {
            TRACE.d("onReturnDownloadRsaPublicKey(HashMap<String, String> map):" + map.toString());
            if (map == null) {
                TRACE.d("MposMainActivity++++++++++++++map == null");
                return;
            }
            String randomKeyLen = map.get("randomKeyLen");
            String randomKey = map.get("randomKey");
            String randomKeyCheckValueLen = map.get("randomKeyCheckValueLen");
            String randomKeyCheckValue = map.get("randomKeyCheckValue");
            TRACE.d("randomKey" + randomKey + "    \n    randomKeyCheckValue" + randomKeyCheckValue);
            Log.e("TAG", "randomKeyLen:" + randomKeyLen + "\nrandomKey:" + randomKey + "\nrandomKeyCheckValueLen:" + randomKeyCheckValueLen + "\nrandomKeyCheckValue:"
                    + randomKeyCheckValue);
        }

        @Override
        public void onGetPosComm(int mod, String amount, String posid) {
            TRACE.d("onGetPosComm(int mod, String amount, String posid):" + mod + TRACE.NEW_LINE + amount + TRACE.NEW_LINE + posid);


        }


        @Override
        public void onPinKey_TDES_Result(String arg0) {
            TRACE.d("onPinKey_TDES_Result(String arg0):" + arg0);
            Log.e("TAG", "result:" + arg0);

        }

        @Override
        public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
            // TODO Auto-generated method stub
            TRACE.d("onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());

        }

        @Override
        public void onEmvICCExceptionData(String arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onEmvICCExceptionData(String arg0):" + arg0);

        }

        @Override
        public void onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1) {
            // TODO Auto-generated method stub
            TRACE.d("onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());

        }

        @Override
        public void onGetInputAmountResult(boolean arg0, String arg1) {
            // TODO Auto-generated method stub
            TRACE.d("onGetInputAmountResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());

        }

        @Override
        public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
            Log.e("TAG", "onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);

        }

        @Override
        public void onReturnPowerOffNFCResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
            Log.e("TAG",  "onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
        }

        @Override
        public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
            Log.e("TAG", "onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
        }


        @Override
        public void onCbcMacResult(String result) {
            TRACE.d("onCbcMacResult(String result):" + result);

            if (result == null || "".equals(result)) {
                Log.e("TAG", "cbc_mac:false");
            } else {
                Log.e("TAG", "cbc_mac: " + result);
            }
        }

        @Override
        public void onReadBusinessCardResult(boolean arg0, String arg1) {
            // TODO Auto-generated method stub
            TRACE.d(" onReadBusinessCardResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);

        }

        @Override
        public void onWriteBusinessCardResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d(" onWriteBusinessCardResult(boolean arg0):" + arg0);

        }

        @Override
        public void onConfirmAmountResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onConfirmAmountResult(boolean arg0):" + arg0);

        }

        @Override
        public void onQposIsCardExist(boolean cardIsExist) {
            TRACE.d("onQposIsCardExist(boolean cardIsExist):" + cardIsExist);

            if (cardIsExist) {
                Log.e("TAG", "cardIsExist:" + cardIsExist);
            } else {
                Log.e("TAG", "cardIsExist:" + cardIsExist);

            }
        }

        @Override
        public void onSearchMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onSearchMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String statuString = arg0.get("status");
                String cardTypeString = arg0.get("cardType");
                String cardUidLen = arg0.get("cardUidLen");
                String cardUid = arg0.get("cardUid");
                String cardAtsLen = arg0.get("cardAtsLen");
                String cardAts = arg0.get("cardAts");
                String ATQA = arg0.get("ATQA");
                String SAK = arg0.get("SAK");
                Log.e("TAG", "statuString:" + statuString + "\n" + "cardTypeString:" + cardTypeString + "\ncardUidLen:" + cardUidLen
                        + "\ncardUid:" + cardUid + "\ncardAtsLen:" + cardAtsLen + "\ncardAts:" + cardAts
                        + "\nATQA:" + ATQA + "\nSAK:" + SAK);
            } else {
                Log.e("TAG", "poll card failed");
            }
        }

        @Override
        public void onBatchReadMifareCardResult(String msg, Hashtable<String, List<String>> cardData) {
            if (cardData != null) {
                TRACE.d("onBatchReadMifareCardResult(boolean arg0):" + msg + cardData.toString());
            }
        }

        @Override
        public void onBatchWriteMifareCardResult(String msg, Hashtable<String, List<String>> cardData) {
            if (cardData != null) {
                TRACE.d("onBatchWriteMifareCardResult(boolean arg0):" + msg + cardData.toString());
            }
        }

        @Override
        public void onSetBuzzerResult(boolean arg0) {
            TRACE.d("onSetBuzzerResult(boolean arg0):" + arg0);


        }

        @Override
        public void onSetBuzzerTimeResult(boolean b) {
            TRACE.d("onSetBuzzerTimeResult(boolean b):" + b);

        }

        @Override
        public void onSetBuzzerStatusResult(boolean b) {
            TRACE.d("onSetBuzzerStatusResult(boolean b):" + b);

        }

        @Override
        public void onGetBuzzerStatusResult(String s) {
            TRACE.d("onGetBuzzerStatusResult(String s):" + s);

        }

        @Override
        public void onSetManagementKey(boolean arg0) {
            TRACE.d("onSetManagementKey(boolean arg0):" + arg0);

        }

        @Override
        public void onReturnUpdateIPEKResult(boolean arg0) {
            TRACE.d("onReturnUpdateIPEKResult(boolean arg0):" + arg0);

        }

        @Override
        public void onReturnUpdateEMVRIDResult(boolean arg0) {
            TRACE.d("onReturnUpdateEMVRIDResult(boolean arg0):" + arg0);

            if (arg0) {
                Log.e("TAG", "operation RID EMV success");

            } else {
                Log.e("TAG", "operation RID EMV fail");

            }
        }

        @Override
        public void onReturnUpdateEMVResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnUpdateEMVResult(boolean arg0):" + arg0);
            if (arg0) {
                Log.e("TAG", "operation EMV app success");
            } else {
                Log.e("TAG", "operation emv app fail~");
            }
        }

        @Override
        public void onBluetoothBoardStateResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onBluetoothBoardStateResult(boolean arg0):" + arg0);


        }

        @Override
        public void onDeviceFound(BluetoothDevice arg0) {
            // TODO Auto-generated method stub

        }


        @Override
        public void onSetSleepModeTime(boolean arg0) {
            TRACE.d("onSetSleepModeTime(boolean arg0):" + arg0);

            if (arg0) {
                Log.e("TAG", "set the Sleep timee Success");
            } else {
                Log.e("TAG", "set the Sleep timee unSuccess");
            }
        }

        @Override
        public void onReturnGetEMVListResult(String arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnGetEMVListResult(String arg0):" + arg0);

            if (arg0 != null && arg0.length() > 0) {
                Log.e("TAG", "The emv list is : " + arg0);
            }
        }

        @Override
        public void onWaitingforData(String arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onWaitingforData(String arg0):" + arg0);

        }

        @Override
        public void onRequestDeviceScanFinished() {
            // TODO Auto-generated method stub
            TRACE.d("onRequestDeviceScanFinished()");

        }

        @Override
        public void onRequestUpdateKey(String arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onRequestUpdateKey(String arg0):" + arg0);
            mhipStatus.setText("update checkvalue : " + arg0);
//            if(isUpdateFw){
//                updateFirmware();
//            }

        }

        @Override
        public void onReturnGetQuickEmvResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onReturnGetQuickEmvResult(boolean arg0):" + arg0);

            if (arg0) {
                Log.e("TAG", getString(R.string.emv_configured));
//				isQuickEmv=true;
                pos.setQuickEmv(true);
            } else {
                Log.e("TAG", getString(R.string.emv_not_configured));
            }
        }

        @Override
        public void onQposDoGetTradeLogNum(String arg0) {
            TRACE.d("onQposDoGetTradeLogNum(String arg0):" + arg0);

            int a = Integer.parseInt(arg0, 16);
            if (a >= 188) {
                Log.e("TAG", "the trade num has become max value!!");
                return;
            }
            Log.e("TAG", "get log num:" + a);
        }

        @Override
        public void onQposDoTradeLog(boolean arg0) {
            TRACE.d("onQposDoTradeLog(boolean arg0) :" + arg0);

            // TODO Auto-generated method stub
            if (arg0) {
                Log.e("TAG", "clear log success!");
            } else {
                Log.e("TAG", "clear log fail!");
            }
        }

        @Override
        public void onAddKey(boolean arg0) {
            TRACE.d("onAddKey(boolean arg0) :" + arg0);

            if (arg0) {
                Log.e("TAG", "ksn add 1 success");
            } else {
                Log.e("TAG", "ksn add 1 failed");
            }
        }

        @Override
        public void onEncryptData(Hashtable<String, String> resultTable) {
            if (resultTable != null) {
                TRACE.d("onEncryptData(String arg0) :" + resultTable);
            }
        }

        @Override
        public void onQposKsnResult(Hashtable<String, String> arg0) {
            TRACE.d("onQposKsnResult(Hashtable<String, String> arg0):" + arg0.toString());

            // TODO Auto-generated method stub
            String pinKsn = arg0.get("pinKsn");
            String trackKsn = arg0.get("trackKsn");
            String emvKsn = arg0.get("emvKsn");
            TRACE.d("get the ksn result is :" + "pinKsn" + pinKsn + "\ntrackKsn" + trackKsn + "\nemvKsn" + emvKsn);

        }

        @Override
        public void onQposDoGetTradeLog(String arg0, String arg1) {
            TRACE.d("onQposDoGetTradeLog(String arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);

            // TODO Auto-generated method stub
            arg1 = QPOSUtil.convertHexToString(arg1);
            Log.e("TAG", "orderId:" + arg1 + "\ntrade log:" + arg0);
        }

        @Override
        public void onRequestDevice() {
            List<UsbDevice> deviceList = getPermissionDeviceList();
            UsbManager mManager = (UsbManager) mContext.getSystemService(USB_SERVICE);
            for (int i = 0; i < deviceList.size(); i++) {
                UsbDevice usbDevice = deviceList.get(i);
                if (usbDevice.getVendorId() == 2965 || usbDevice.getVendorId() == 0x03EB) {

                    if (mManager.hasPermission(usbDevice)) {
                        pos.setPermissionDevice(usbDevice);
                    } else {
                        devicePermissionRequest(mManager, usbDevice);

                    }
                }
            }


        }

        @Override
        public void onGetKeyCheckValue(List<String> checkValue) {
            if (checkValue != null) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("{");
                for (int i = 0; i < checkValue.size(); i++) {
                    buffer.append(checkValue.get(i)).append(",");
                }
                buffer.append("}");

                Log.e("TAG", buffer.toString());
            }


        }

        @Override
        public void onGetDevicePubKey(String clearKeys) {
            TRACE.d("onGetDevicePubKey(clearKeys):" + clearKeys);

            Log.e("TAG", clearKeys);
            String lenStr = clearKeys.substring(0, 4);
            int sum = 0;
            for (int i = 0; i < 4; i++) {
                int bit = Integer.parseInt(lenStr.substring(i, i + 1));
                sum += bit * Math.pow(16, (3 - i));
            }
            pubModel = clearKeys.substring(4, 4 + sum * 2);


        }


        @Override
        public void onTradeCancelled() {
            TRACE.d("onTradeCancelled");
            dismissDialog();

        }

        @Override
        public void onReturnSetAESResult(boolean isSuccess, String result) {

        }

        @Override
        public void onReturnAESTransmissonKeyResult(boolean isSuccess, String result) {

        }

        @Override
        public void onReturnSignature(boolean b, String signaturedData) {
            if (b) {
                BASE64Encoder base64Encoder = new BASE64Encoder();
                String encode = base64Encoder.encode(signaturedData.getBytes());
                Log.e("TAG", "signature data (Base64 encoding):" + encode);
            }
        }

        @Override
        public void onReturnConverEncryptedBlockFormat(String result) {
            Log.e("TAG", result);
        }

        @Override
        public void onQposIsCardExistInOnlineProcess(boolean haveCard) {

        }


        @Override
        public void onFinishMifareCardResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onFinishMifareCardResult(boolean arg0):" + arg0);

            if (arg0) {
                Log.e("TAG", "finish success");
            } else {
                Log.e("TAG", "finish fail");
            }
        }

        @Override
        public void onVerifyMifareCardResult(boolean arg0) {
            TRACE.d("onVerifyMifareCardResult(boolean arg0):" + arg0);

            // TODO Auto-generated method stub
//			String msg = pos.getMifareStatusMsg();
            if (arg0) {
                Log.e("TAG",  "onVerifyMifareCardResult success");
            } else {
                Log.e("TAG", "onVerifyMifareCardResult fail");
            }
        }

        @Override
        public void onReadMifareCardResult(Hashtable<String, String> arg0) {
            // TODO Auto-generated method stub
//			String msg = pos.getMifareStatusMsg();
            if (arg0 != null) {
                TRACE.d("onReadMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());

                String addr = arg0.get("addr");
                String cardDataLen = arg0.get("cardDataLen");
                String cardData = arg0.get("cardData");
                Log.e("TAG", "addr:" + addr + "\ncardDataLen:" + cardDataLen + "\ncardData:" + cardData);
            } else {
                Log.e("TAG", "onReadWriteMifareCardResult fail");
            }
        }

        @Override
        public void onWriteMifareCardResult(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onWriteMifareCardResult(boolean arg0):" + arg0);

            if (arg0) {
                Log.e("TAG", "write data success!");
            } else {
                Log.e("TAG", "write data fail!");
            }
        }

        @Override
        public void onOperateMifareCardResult(Hashtable<String, String> arg0) {
            // TODO Auto-generated method stub

            if (arg0 != null) {
                TRACE.d("onOperateMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());

                String cmd = arg0.get("Cmd");
                String blockAddr = arg0.get("blockAddr");
                Log.e("TAG", "Cmd:" + cmd + "\nBlock Addr:" + blockAddr);
            } else {
                Log.e("TAG", "operate failed");
            }
        }

        @Override
        public void getMifareCardVersion(Hashtable<String, String> arg0) {

            // TODO Auto-generated method stub
            if (arg0 != null) {
                TRACE.d("getMifareCardVersion(Hashtable<String, String> arg0):" + arg0.toString());

                String verLen = arg0.get("versionLen");
                String ver = arg0.get("cardVersion");
                Log.e("TAG", "versionLen:" + verLen + "\nverison:" + ver);
            } else {
                Log.e("TAG", "get mafire UL version failed");
            }
        }

        @Override
        public void getMifareFastReadData(Hashtable<String, String> arg0) {
            // TODO Auto-generated method stub

            if (arg0 != null) {
                TRACE.d("getMifareFastReadData(Hashtable<String, String> arg0):" + arg0.toString());
                String startAddr = arg0.get("startAddr");
                String endAddr = arg0.get("endAddr");
                String dataLen = arg0.get("dataLen");
                String cardData = arg0.get("cardData");
                Log.e("TAG", "startAddr:" + startAddr + "\nendAddr:" + endAddr + "\ndataLen:" + dataLen
                        + "\ncardData:" + cardData);
            } else {
                Log.e("TAG", "read fast UL failed");
            }
        }

        @Override
        public void getMifareReadData(Hashtable<String, String> arg0) {

            if (arg0 != null) {
                TRACE.d("getMifareReadData(Hashtable<String, String> arg0):" + arg0.toString());

                String blockAddr = arg0.get("blockAddr");
                String dataLen = arg0.get("dataLen");
                String cardData = arg0.get("cardData");
                Log.e("TAG", "blockAddr:" + blockAddr + "\ndataLen:" + dataLen + "\ncardData:" + cardData);
            } else {
                Log.e("TAG", "read mafire UL failed");
            }
        }

        @Override
        public void writeMifareULData(String arg0) {

            if (arg0 != null) {
                TRACE.d("writeMifareULData(String arg0):" + arg0.toString());

                Log.e("TAG", "addr:" + arg0);
            } else {
                Log.e("TAG", "write UL failed");
            }
        }

        @Override
        public void verifyMifareULData(Hashtable<String, String> arg0) {

            if (arg0 != null) {
                TRACE.d("verifyMifareULData(Hashtable<String, String> arg0):" + arg0.toString());

                String dataLen = arg0.get("dataLen");
                String pack = arg0.get("pack");
                Log.e("TAG", "dataLen:" + dataLen + "\npack:" + pack);
            } else {
                Log.e("TAG", "verify UL failed");
            }
        }

        @Override
        public void onGetSleepModeTime(String arg0) {
            // TODO Auto-generated method stub

            if (arg0 != null) {
                TRACE.d("onGetSleepModeTime(String arg0):" + arg0.toString());

                int time = Integer.parseInt(arg0, 16);
                Log.e("TAG", "time is ： " + time + " seconds");
            } else {
                Log.e("TAG", "get the time is failed");
            }
        }

        @Override
        public void onGetShutDownTime(String arg0) {

            if (arg0 != null) {
                TRACE.d("onGetShutDownTime(String arg0):" + arg0.toString());

                Log.e("TAG", "shut down time is : " + Integer.parseInt(arg0, 16) + "s");
            } else {
                Log.e("TAG", "get the shut down time is fail!");
            }
        }

        @Override
        public void onQposDoSetRsaPublicKey(boolean arg0) {
            // TODO Auto-generated method stub
            TRACE.d("onQposDoSetRsaPublicKey(boolean arg0):" + arg0);

            if (arg0) {
                Log.e("TAG", "set rsa is successed!");

            } else {
                Log.e("TAG", "set rsa is failed!");
            }
        }

        @Override
        public void onQposGenerateSessionKeysResult(Hashtable<String, String> arg0) {

            if (arg0 != null) {
                TRACE.d("onQposGenerateSessionKeysResult(Hashtable<String, String> arg0):" + arg0.toString());
                String rsaFileName = arg0.get("rsaReginString");
                String enPinKeyData = arg0.get("enPinKey");
                String enKcvPinKeyData = arg0.get("enPinKcvKey");
                String enCardKeyData = arg0.get("enDataCardKey");
                String enKcvCardKeyData = arg0.get("enKcvDataCardKey");
                Log.e("TAG", "rsaFileName:" + rsaFileName + "\nenPinKeyData:" + enPinKeyData + "\nenKcvPinKeyData:" +
                        enKcvPinKeyData + "\nenCardKeyData:" + enCardKeyData + "\nenKcvCardKeyData:" + enKcvCardKeyData);
            } else {
                Log.e("TAG", "get key failed,pls try again!");
            }
        }

        @Override
        public void transferMifareData(String arg0) {
            TRACE.d("transferMifareData(String arg0):" + arg0.toString());

            // TODO Auto-generated method stub
            if (arg0 != null) {
                Log.e("TAG", "response data:" + arg0);
            } else {
                Log.e("TAG", "transfer data failed!");
            }
        }

        @Override
        public void onReturnRSAResult(String arg0) {
            TRACE.d("onReturnRSAResult(String arg0):" + arg0.toString());

            if (arg0 != null) {
                Log.e("TAG", "rsa data:\n" + arg0);
            } else {
                Log.e("TAG", "get the rsa failed");
            }
        }

        @Override
        public void onRequestNoQposDetectedUnbond() {
            // TODO Auto-generated method stub
            TRACE.d("onRequestNoQposDetectedUnbond()");


        }


    }

    private String ksn;
    private String terminalId;

    private void proceedToExChangeData(String responseCode, CreditCard creditCard) {
        if (creditCard.getPIN() == null) {
            Log.e("FailedTransaction ", "FailedTransaction");
//            FailedTransaction();
            return;
        }
        TerminalInfo response = showTerminalEmvTransResult(cPin, responseCode, getDeviceMac());
        KSNUtilities ksnUtilitites = new KSNUtilities();
        //  String workingKey = ksnUtilitites.getWorkingKey("3F2216D8297BCE9C", "000002DDDDE00002");


        String workingKey = ksnUtilitites.getWorkingKey("3F2216D8297BCE9C", getInitialKSN());
        String pinBlock = ksnUtilitites.DesEncryptDukpt(workingKey, response.pan, cPin);


        ksn = ksnUtilitites.getLatestKsn();
        response.ksn = ksn;
        response.pinBlock = pinBlock;
        response.terminalId = "2076NA61";
//        response.terminalId = terminalId;
        // response.cardOwner = creditCard.getHolderName();
        stopEmvProcess(response);
    }

    private String getInitialKSN() {
        SharedPreferences sharedPref = getSharedPreferences("KSNCOUNTER", MODE_PRIVATE);
        int ksn = sharedPref.getInt("KSN", 00001);
        if (ksn > 9999) {
            ksn = 00000;
        }
        int latestKSN = ksn + 1;
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("KSN", latestKSN);
        editor.apply();
        return "0000000002DDDDE" + String.format("%05d", latestKSN);
    }

    private void stopEmvProcess(TerminalInfo response) {
        AppLog.d("Processing", "Transaction" + "Please wait");
        Log.e("Processing", "Processing Transaction....");
        String mTerminal = new Gson().toJson(response);
        BlusaltTerminalInfo blusaltTerminalInfo = new Gson().fromJson(mTerminal, BlusaltTerminalInfo.class);
        blusaltTerminalInfo.deviceOs = "Android";
        blusaltTerminalInfo.serialNumber = getDeviceMac();
        blusaltTerminalInfo.device = "Horizon " + getDeviceModel();
        blusaltTerminalInfo.currency = "NGN";
        blusaltTerminalInfo.currencyCode = "566";
        response.currencyCode = "566";
        response.currency = "NGN";
        response.deviceOs = "Android";
        response.serialNumber = getDeviceMac();
        response.device = "Mpos " + getDeviceModel();
        String rtt = new Gson().toJson(blusaltTerminalInfo);
//        init("test_57566e7a223f98cf6aebfd093c8f295dd77f74a6690cd24672352c7477ebae336cf759516d2a2f500440686eb96d92121663836633811sk", getApplicationContext());

        ProcessTransaction(blusaltTerminalInfo);
//        onCompleteTransaction(response);
    }

//    public static void init(String secretKey) {
//        if (!TextUtils.isEmpty(secretKey)) {
//            try {
//                MemoryManager.getInstance().putUserSecretKey(secretKey);
//            } catch (Exception e) {
//                AppLog.e("prepareForPrinter", e.getMessage());
//            }
//        } else {
//            AppLog.e("init", "Secret Key is Empty");
//        }
//    }

    private void ProcessTransaction(BlusaltTerminalInfo blusaltTerminalInfo) {
        RetrofitClientInstance.getInstance().getDataService().postTransactionToMiddleWare(blusaltTerminalInfo).enqueue(new Callback<TerminalResponse>() {
            @Override
            public void onResponse(@NonNull Call<TerminalResponse> call, @NonNull Response<TerminalResponse> response) {
                Log.e("ProcessTransaction", "ProcessTransaction" + response);
                TerminalResponse terminalResponse = new TerminalResponse("card payment failed", "01", "Unable to process transaction");
                if (response.isSuccessful()) {
                    if (response.body().message.contains("Access denied! invalid apiKey passed")) {
                        terminalResponse.responseCode = "01";
                        terminalResponse.responseDescription = "card payment failed";

                        Log.e("ProcessTransaction", "ProcessTransaction err" + new Gson().toJson(terminalResponse));
                        apiResponseCall(terminalResponse);
                    } else {
                        terminalResponse = response.body();
                        terminalResponse.responseCode = "00";
                        terminalResponse.responseDescription = "card payment successful";

                        Log.e("ProcessTransaction", "ProcessTransaction isSuccessful" + new Gson().toJson(terminalResponse));
                        apiResponseCall(terminalResponse);
                    }
                } else {
                    try {
                        Gson gson = new Gson();
                        Type type = new TypeToken<TerminalResponse>() {
                        }.getType();
                        terminalResponse = gson.fromJson(response.errorBody().charStream(), type);
                        terminalResponse.responseCode = "01";
                        terminalResponse.responseDescription = "card payment failed";

                        Log.e("ProcessTransaction", "ProcessTransaction failed" + new Gson().toJson(terminalResponse));
                        apiResponseCall(terminalResponse);
                    } catch (Exception e) {
                        Log.e("ProcessTransaction", "ProcessTransaction failed" + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<TerminalResponse> call, @NonNull Throwable t) {
                TerminalResponse terminalResponse = new TerminalResponse();
                Log.e("ProcessTransaction", "ProcessTransaction onFailure" + t.getMessage());
                terminalResponse.status = false;
                terminalResponse.message = t.getMessage();
                terminalResponse.responseCode = "02";
                terminalResponse.responseDescription = "Unable to connect to the server";
                apiResponseCall(terminalResponse);
            }
        });
    }

    public void apiResponseCall(TerminalResponse terminalResponse) {
        try {

            dismissDialog();
            dialog = new Dialog(OtherActivity.this);
            dialog.setContentView(R.layout.alert_dialog);
            dialog.setTitle(R.string.transaction_result);
            TextView messageTextView = (TextView) dialog.findViewById(R.id.messageTextView);

            TRACE.d("onRequestTransactionResult()" + terminalResponse.message.toString());
            TRACE.d("onRequestTransactionResult()" + terminalResponse.responseCode.toString());

            if (Objects.equals(terminalResponse.responseCode, "00")) {
                TRACE.d("TransactionResult.APPROVED");
                String message = getString(R.string.transaction_approved) + "\n" + getString(R.string.amount) + ": N" + amount + "\n";
                if (!cashbackAmount.equals("")) {
                    message += getString(R.string.cashback_amount) + ": INR" + cashbackAmount;
                }
                messageTextView.setText(message);
                Log.e("TAG POS", "APPROVED");

//                    deviceShowDisplay("APPROVED");
            } else {
                messageTextView.setText(getString(R.string.transaction_declined));
                Log.e("TAG POS", "DECLINED");

//                    deviceShowDisplay("DECLINED");
            }

            dialog.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismissDialog();
                }
            });

//            dialog.show();
            amount = "";
            cashbackAmount = "";

//            Intent intent = new Intent(MposMainActivity.this, TransactionStatus.class);
//            intent.putExtra("result", terminalResponse);
//            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(OtherActivity.this, TransactionStatus.class);
        String fullPay = new Gson().toJson(terminalResponse);
        intent.putExtra("result", fullPay);
        startActivity(intent);

        close();
//        intent.putExtra(getString(com.blusalt.blusaltmpos.R.string.data), fullPay);
//        // prepareForPrinters(PosActivity.this,terminalResponse);
//        finishTransaction(intent);
    }

    public String getDeviceMac() {
        try {
            String devInfo = serialNo;
            return devInfo;
        } catch (Exception e) {
            Log.e("getDeviceMac", e.getLocalizedMessage());
        }
        return "";
    }

    public static String getDeviceModel() {
        try {
            String devInfo = "CR100";
            return devInfo;
        } catch (Exception e) {
            Log.e("getDeviceMac", e.getLocalizedMessage());
        }
        return "";
    }

    public TerminalInfo showTerminalEmvTransResult(String _PinBlock, String _responseCode, String deviceMacAddress) {
        TerminalInfo terminalInfo = getDefaultTerminalInfo();
        terminalInfo.fromAccount = "Default";// getAccountTypeString(accountType);
        terminalInfo.responseCode = _responseCode;
        terminalInfo.responseDescription = "Data collected successfully";
        terminalInfo.TerminalName = "horizonpay";
        TlvDataList tlvDataList = null;
        String tlv = null;
        try {
//            tlv = DeviceHelper.getEmvHandler().getTlvByTags(EmvUtil.tags);
//            tlvDataList = TlvDataList.fromBinary(tlv);
//            if (tlvDataList.getTLV(EmvTags.EMV_TAG_IC_CHNAME) != null) {
//                String name = EmvUtil.readCardHolder();
//                terminalInfo.cardOwner =  ConvertUtils.formatHexString(name);
//            } else {
//                terminalInfo.cardOwner = "CUSTOMER / INSTANT";
//            }

            String track2 = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "57"));
            Log.e("Tag track2", track2.substring(9, track2.length() - 1));

            String cardNo = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "5A"));
            Log.e("Tag CardNo", cardNo.substring(9, cardNo.length() - 1));

            String expiryDate = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "5F24"));
            Log.e("Tag expiryDate", expiryDate.substring(11, expiryDate.length() - 1));

            String currencyCode = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "5F2A"));
            Log.e("Tag currencyCode", currencyCode.substring(11, currencyCode.length() - 1));

            String countryCode = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "9F1A"));
            Log.e("Tag CardNo", countryCode.substring(11, countryCode.length() - 1));

            String AmountAuthorized = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "9F02"));
            Log.e("Tag AmountAuthorized", AmountAuthorized.substring(11, AmountAuthorized.length() - 1));

            String UnpredictableNumber = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "9F37"));
            Log.e("Tag UnpredictableNumber", UnpredictableNumber.substring(11, UnpredictableNumber.length() - 1));

            String iad = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "9F10"));
            Log.e("Tag iad", iad.substring(11, iad.length() - 1));

            String atc = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "9F36"));
            Log.e("Tag atc", atc.substring(11, atc.length() - 1));

            String cardSequenceNumber = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "5F34"));
            Log.e("Tag cardSequenceNumber", cardSequenceNumber.substring(11, cardSequenceNumber.length() - 1));

            String TerminalCapabilities = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "9F33"));
            Log.e("TerminalCapabilities", TerminalCapabilities.substring(11, TerminalCapabilities.length() - 1));

            String Cryptogram = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "9F26"));
            Log.e("Tag Cryptogram", Cryptogram.substring(11, Cryptogram.length() - 1));

            String TransactionDate = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "9A"));
            Log.e("Tag TransactionDate", TransactionDate.substring(9, TransactionDate.length() - 1));

            String TerminalVerificationResult = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "95"));
            Log.e("TerminalVerificationResult", TerminalVerificationResult.substring(9, TerminalVerificationResult.length() - 1));

            String ApplicationInterchangeProfile = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "82"));
            Log.e("ApplicationInterchangeProfile", ApplicationInterchangeProfile.substring(9, ApplicationInterchangeProfile.length() - 1));

            String CvmResults = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "9F34"));
            Log.e("Tag CvmResults", CvmResults.substring(11, CvmResults.length() - 1));

            String DedicatedFileName = String.valueOf(pos.getICCTag(QPOSService.EncryptType.PLAINTEXT, 0, 1, "84"));
            Log.e("Tag DedicatedFileName", DedicatedFileName.substring(9, DedicatedFileName.length() - 1));

            terminalInfo.cardOwner = "CUSTOMER / INSTANT";
            Log.d(TAG, "ICC Data: " + "\n" + tlv);
            terminalInfo.DedicatedFileName = DedicatedFileName.substring(9, DedicatedFileName.length() - 1);
            terminalInfo.CvmResults = CvmResults.substring(11, CvmResults.length() - 1);
            terminalInfo.ApplicationInterchangeProfile = ApplicationInterchangeProfile.substring(9, ApplicationInterchangeProfile.length() - 1);
            terminalInfo.TerminalVerificationResult = TerminalVerificationResult.substring(9, TerminalVerificationResult.length() - 1);
            terminalInfo.TransactionDate = TransactionDate.substring(9, TransactionDate.length() - 1);
            terminalInfo.CryptogramInformationData = "80";
            terminalInfo.Cryptogram = Cryptogram.substring(11, Cryptogram.length() - 1);
            terminalInfo.TerminalCapabilities = TerminalCapabilities.substring(11, TerminalCapabilities.length() - 1);
            terminalInfo.cardSequenceNumber = cardSequenceNumber.substring(11, cardSequenceNumber.length() - 1);
            terminalInfo.atc = atc.substring(11, atc.length() - 1);
            terminalInfo.iad = iad.substring(11, iad.length() - 1);
            terminalInfo.track2 = track2.substring(9, track2.length() - 1);
            String strTrack2 = terminalInfo.track2.split("F")[0];
            String pan = strTrack2.split("D")[0];
            String expiry = strTrack2.split("D")[1].substring(0, 4);
            Log.e("My track2", strTrack2 + " " + pan + " " + expiry);

            terminalInfo.pan = pan;
            terminalInfo.expiryYear = expiry.substring(0, 2);
            terminalInfo.expiryMonth = expiry.substring(2);
            terminalInfo.AmountAuthorized = AmountAuthorized.substring(11, AmountAuthorized.length() - 1);
            terminalInfo.UnpredictableNumber = UnpredictableNumber.substring(11, UnpredictableNumber.length() - 1);
//            if(tlvDataList.getTLV(EmvTags.EMV_TAG_IC_APNAME) != null){
//                //terminalInfo.CardType = tlvDataList.getTLV(EmvTags.EMV_TAG_IC_APNAME).getGBKValue();
//            }
            String reult = new Gson().toJson(terminalInfo);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return terminalInfo;
    }

    private static TerminalInfo getDefaultTerminalInfo() {
        TerminalInfo terminalInfo = new TerminalInfo();
        terminalInfo.batteryInformation = "100";
        terminalInfo.languageInfo = "EN";
        terminalInfo.posConditionCode = "00";
        terminalInfo.printerStatus = "1";
        //  terminalInfo.minorAmount = "000000000001";
        terminalInfo.TransactionType = "00";
        terminalInfo.posEntryMode = "051";
        terminalInfo.posDataCode = "510101511344101";
        terminalInfo.posGeoCode = "00234000000000566";
        terminalInfo.pinType = "Dukpt";
        terminalInfo.stan = getNextStan();
        terminalInfo.AmountOther = "000000000000";
        terminalInfo.TransactionCurrencyCode = "0566";
        terminalInfo.TerminalCountryCode = "566";
        terminalInfo.TerminalType = "22";
        return terminalInfo;
    }

    public static String getNextStan() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return df.format(new Date()).substring(8);
    }

    private void sendMsgDelay(int what) {
        Message msg = new Message();
        msg.what = what;
        mHandler.sendMessageDelayed(msg, 500);
    }

    private void deviceShowDisplay(String diplay) {

        Log.e("execut start:", "deviceShowDisplay");
        String customDisplayString = "";
        try {
            byte[] paras = diplay.getBytes("GBK");
            customDisplayString = QPOSUtil.byteArray2Hex(paras);
            pos.lcdShowCustomDisplay(QPOSService.LcdModeAlign.LCD_MODE_ALIGNCENTER, customDisplayString, 60);
        } catch (Exception e) {
            e.printStackTrace();
            TRACE.d("gbk error");
            Log.e("execut error:", "deviceShowDisplay");
        }
        Log.e("execut end:", "deviceShowDisplay");

    }

    private void requestPermission() {

        if (ContextCompat.checkSelfPermission(OtherActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("permisson: ", "should open Audio");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO},
                    REQUEST_CODE_AUDIO_PERMISSIONS);

        } else {
            Log.e("permisson: ", "has Audio permission");

        }
    }

    private void devicePermissionRequest(UsbManager mManager, UsbDevice usbDevice) {
        PendingIntent mPermissionIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            mPermissionIntent = PendingIntent.getBroadcast(OtherActivity.this, 0, new Intent(
                    "com.android.example.USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE);
        } else {
            mPermissionIntent = PendingIntent.getBroadcast(OtherActivity.this, 0, new Intent(
                    "com.android.example.USB_PERMISSION"), 0);
        }
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        mManager.requestPermission(usbDevice, mPermissionIntent);
    }

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // call method to set up device communication
                            TRACE.i("usb" + "permission granted for device "
                                    + device);
                            pos.setPermissionDevice(device);
                        }
                    } else {
                        TRACE.i("usb" + "permission denied for device " + device);

                    }
                    mContext.unregisterReceiver(mUsbReceiver);
                }
            }
        }
    };

    private List getPermissionDeviceList() {
        UsbManager mManager = (UsbManager) this.getSystemService(USB_SERVICE);
        List deviceList = new ArrayList<UsbDevice>();
        // check for existing devices
        for (UsbDevice device : mManager.getDeviceList().values()) {

            deviceList.add(device);
        }
        return deviceList;
    }

    private void clearDisplay() {
        Log.e("TAG", " ");
    }

    class MyOnClickListener implements View.OnClickListener {

        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            Log.e("TAG", "");
            if (selectBTFlag) {
                Log.e("TAG", String.valueOf(R.string.wait));
                return;
            } else if (v == doTradeButton) {//do trade button
                mhipStatus.setTextColor(getResources().getColor(R.color.eb_col_34));
                mhipStatus.setText("");
                if (pos == null) {
                    Log.e("TAG", String.valueOf(R.string.scan_bt_pos_error));
                    return;
                }

                if (appPreferenceHelper.getSharedPreferenceBoolean(Constants.IS_KEY_INJECTED)
                        && appPreferenceHelper.getSharedPreferenceString(Constants.KEY_INJECTED_BLUETOOTH).equals(blueTootchAddress)) {

                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {

                        spinKit = findViewById(R.id.gifImageView);
                        spinKit.setImageResource(R.drawable.card);
                        spinKit.setVisibility(View.VISIBLE);

                        mToolbar.setText("Payment");
                        insert_amount.setText(appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT));

                        validate_text.setVisibility(View.VISIBLE);
                        insert_text.setVisibility(View.VISIBLE);
                        insert_amount.setVisibility(View.VISIBLE);

                        validateImage.setVisibility(View.GONE);
                        about_to_text.setVisibility(View.GONE);
                        validate_amount_text.setVisibility(View.GONE);
                        doTradeButton.setVisibility(View.GONE);

                        insert_text.setText("Insert Card into the mPOS");
                        isPinCanceled = false;
                        Log.e("TAG", String.valueOf(R.string.starting));
//                terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
                        if (posType == POS_TYPE.UART) {//postype is UART
                            pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
//                    pos.doTrade(terminalTime, 0, 30);
                            pos.doTrade(20);
                        } else {
                            int keyIdex = getKeyIndex();
//                    pos.setCardTradeMode(QPOSService.CardTradeMode.SWIPE_TAP_INSERT_CARD_NOTUP);
                            pos.doTrade(keyIdex, 30);//start do trade
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Please connect to the Internet", Toast.LENGTH_SHORT).show();
                    }

                }else {

                    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnected()) {

                        spinKit = findViewById(R.id.gifImageView);
                        spinKit.setImageResource(R.drawable.card);
                        spinKit.setVisibility(View.VISIBLE);

                        mToolbar.setText("Payment");
                        insert_amount.setText(appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT));

                        validate_text.setVisibility(View.VISIBLE);
                        insert_text.setVisibility(View.VISIBLE);
                        insert_amount.setVisibility(View.VISIBLE);

                        validateImage.setVisibility(View.GONE);
                        about_to_text.setVisibility(View.GONE);
                        validate_amount_text.setVisibility(View.GONE);
                        doTradeButton.setVisibility(View.GONE);

                    } else {
                        Toast.makeText(getApplicationContext(), "Please connect to the Internet", Toast.LENGTH_SHORT).show();
                    }

                    if (pos == null) {
                        pos = QPOSService.getInstance(QPOSService.CommunicationMode.BLUETOOTH);
                        pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("NIGERIA-QPOS cute,CR100,D20,D30.xml", OtherActivity.this)));
                        Log.e("TAG open", "updating...");
                    } else {
                        pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("NIGERIA-QPOS cute,CR100,D20,D30.xml", OtherActivity.this)));
                        Log.e("TAG open", "updating...");
                    }
                }


            } else if (v == btnUSB) {
                USBClass usb = new USBClass();
                ArrayList<String> deviceList = usb.GetUSBDevices(getBaseContext());
                if (deviceList == null) {
                    Toast.makeText(mContext, "No Permission", Toast.LENGTH_SHORT).show();
                    return;
                }
                final CharSequence[] items = deviceList.toArray(new CharSequence[deviceList.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Select a Reader");

                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        String selectedDevice = (String) items[item];
                        dialog.dismiss();
                        usbDevice = USBClass.getMdevices().get(selectedDevice);
                        open(QPOSService.CommunicationMode.USB_OTG_CDC_ACM);
                        posType = POS_TYPE.OTG;
                        pos.openUsb(usbDevice);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else if (v == continueBtn) {

                validateImage.setVisibility(View.VISIBLE);
                validate_text.setVisibility(View.VISIBLE);
                about_to_text.setVisibility(View.VISIBLE);
                validate_amount_text.setVisibility(View.VISIBLE);
                validate_amount_text.setText(appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT));
                doTradeButton.setVisibility(View.VISIBLE);
                doTradeButton.setEnabled(true);


                continueBtn.setVisibility(View.INVISIBLE);
                usbImage.setVisibility(View.INVISIBLE);
                connectedText.setVisibility(View.INVISIBLE);
            } else if (v == pollBtn) {
                pos.pollOnMifareCard(20);
//                pos.doMifareCard("01", 20);
            } else if (v == pollULbtn) {
                pos.pollOnMifareCard(20);
//                pos.doMifareCard("01", 20);
            } else if (v == finishBtn) {
                pos.finishMifareCard(20);
//                pos.doMifareCard("0E", 20);
            } else if (v == finishULBtn) {
                pos.finishMifareCard(20);
//                pos.doMifareCard("0E", 20);
            } else if (v == veriftBtn) {
                String keyValue = status.getText().toString();
                String blockaddr = blockAdd.getText().toString();
                String keyclass = (String) mafireSpinner.getSelectedItem();
                pos.setBlockaddr(blockaddr);
                pos.setKeyValue(keyValue);
//                pos.doMifareCard("02" + keyclass, 20);
                pos.authenticateMifareCard(QPOSService.MifareCardType.CLASSIC, keyclass, blockaddr, keyValue, 20);
            } else if (v == veriftULBtn) {
                String keyValue = status11.getText().toString();
                pos.setKeyValue(keyValue);
//                pos.doMifareCard("0D", 20);
                pos.authenticateMifareCard(QPOSService.MifareCardType.UlTRALIGHT, "", "", keyValue, 20);
            } else if (v == readBtn) {
                String blockaddr = blockAdd.getText().toString();
                pos.setBlockaddr(blockaddr);
//                pos.doMifareCard("03", 20);
                pos.readMifareCard(QPOSService.MifareCardType.CLASSIC, blockaddr, 20);
            } else if (v == writeBtn) {
                String blockaddr = blockAdd.getText().toString();
                String cardData = status.getText().toString();
//				SpannableString s = new SpannableString("please input card data");
//		        status.setHint(s);
                pos.setBlockaddr(blockaddr);
                pos.setKeyValue(cardData);
//                pos.doMifareCard("04", 20);
                pos.writeMifareCard(QPOSService.MifareCardType.CLASSIC, blockaddr, cardData, 20);
            } else if (v == operateCardBtn) {
                String blockaddr = blockAdd.getText().toString();
                String cardData = status.getText().toString();
                String cmd = (String) cmdSp.getSelectedItem();
                pos.setBlockaddr(blockaddr);
                pos.setKeyValue(cardData);
                if (cmd.equals("add")) {
                    pos.operateMifareCardData(QPOSService.MifareCardOperationType.ADD, blockaddr, cardData, 20);
                }
//                pos.doMifareCard("05" + cmd, 20);
            } else if (v == getULBtn) {
//                pos.doMifareCard("06", 20);
                pos.getMifareCardInfo(20);
            } else if (v == readULBtn) {
                String blockaddr = block_address11.getText().toString();
                pos.setBlockaddr(blockaddr);
//                pos.doMifareCard("07", 20);
                pos.readMifareCard(QPOSService.MifareCardType.UlTRALIGHT, blockaddr, 20);
            } else if (v == fastReadUL) {
                String endAddr = block_address11.getText().toString();
                String startAddr = status11.getText().toString();
                pos.setKeyValue(startAddr);
                pos.setBlockaddr(endAddr);
//                pos.doMifareCard("08", 20);
                pos.fastReadMifareCardData(startAddr, endAddr, 20);
            } else if (v == writeULBtn) {
                String addr = block_address11.getText().toString();
                String data = status11.getText().toString();
                pos.setKeyValue(data);
                pos.setBlockaddr(addr);
//                pos.doMifareCard("0B", 20);
                pos.writeMifareCard(QPOSService.MifareCardType.UlTRALIGHT, addr, data, 20);
            } else if (v == transferBtn) {
//                String data = status.getText().toString();
//                String len = blockAdd.getText().toString();
//                pos.setMafireLen(Integer.valueOf(len, 16));
//                pos.setKeyValue(data);
//                pos.transferMifareData(data,20);
            }
        }
    }

    private int getKeyIndex() {
        String s = mKeyIndex.getText().toString();
        if (TextUtils.isEmpty(s))
            return 0;
        int i = 0;
        try {
            i = Integer.parseInt(s);
            if (i > 9 || i < 0)
                i = 0;
        } catch (Exception e) {
            i = 0;
            return i;
        }
        return i;
    }

    private void sendMsg(int what) {
        Message msg = new Message();
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    private boolean selectBTFlag = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case 8003:
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String content = "";
                    if (nfcLog == null) {

                        Hashtable<String, String> h = pos.getNFCBatchData();
                        String tlv = h.get("tlv");
                        TRACE.i("nfc batchdata1: " + tlv);
                        content = statusEditText.getText().toString() + "\nNFCbatchData: " + h.get("tlv");
                    } else {
                        content = statusEditText.getText().toString() + "\nNFCbatchData: " + nfcLog;
                    }
                    Log.e("TAG", content);
//                    autoDoTrade(0);
                    break;

                default:
                    break;
            }
        }
    };

    private void updateFirmware() {
        if (ActivityCompat.checkSelfPermission(OtherActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(OtherActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
//            LogFileConfig.getInstance().setWriteFlag(true);
            byte[] data = null;
            List<String> allFiles = null;
//                    allFiles = FileUtils.getAllFiles(FileUtils.POS_Storage_Dir);
            if (allFiles != null) {
                for (String fileName : allFiles) {
                    if (!TextUtils.isEmpty(fileName)) {
                        if (fileName.toUpperCase().endsWith(".asc".toUpperCase())) {
                            data = FileUtils.readLine(fileName);
                            Toast.makeText(OtherActivity.this, "Upgrade package path:" +
                                    Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dspread" + File.separator + fileName, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            }
            if (data == null || data.length == 0) {
                data = FileUtils.readAssetsLine("upgrader.asc", OtherActivity.this);
            }
            int a = pos.updatePosFirmware(data, blueTootchAddress);
            //D20 doesn't need to keep charging
            if (a == -1) {
//                isUpdateFw = false;
                Toast.makeText(OtherActivity.this, "please keep the device charging", Toast.LENGTH_LONG).show();
                return;
            }

            updateThread = new UpdateThread();
            updateThread.start();

        }
    }

    /*---------------------------------------------*/
    private static final String FILENAME = "dsp_axdd";

}

