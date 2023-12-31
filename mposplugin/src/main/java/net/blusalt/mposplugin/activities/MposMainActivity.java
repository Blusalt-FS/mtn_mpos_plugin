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
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
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
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.blusalt.mposplugin.Pos;
import net.blusalt.mposplugin.blusaltmpos.pay.BlusaltTerminalInfo;
import net.blusalt.mposplugin.blusaltmpos.pay.CreditCard;
import net.blusalt.mposplugin.blusaltmpos.pay.TerminalInfo;
import net.blusalt.mposplugin.blusaltmpos.pay.TerminalResponse;
import net.blusalt.mposplugin.blusaltmpos.pos.AppLog;
import net.blusalt.mposplugin.blusaltmpos.pos.TlvDataList;
import net.blusalt.mposplugin.blusaltmpos.util.AppPreferenceHelper;
import net.blusalt.mposplugin.blusaltmpos.util.Constants;
import net.blusalt.mposplugin.blusaltmpos.util.KSNUtilities;
import net.blusalt.mposplugin.MemoryManager;
import net.blusalt.mposplugin.BaseApplication;
import net.blusalt.mposplugin.R;
import net.blusalt.mposplugin.USBClass;
import net.blusalt.mposplugin.blusaltmpos.util.SharedPreferencesUtils;
import net.blusalt.mposplugin.network.BaseData;
import net.blusalt.mposplugin.network.RetrofitClientInstance;
import net.blusalt.mposplugin.network.RetrofitClientInstanceParam;
import net.blusalt.mposplugin.network.RetrofitClientInstanceProcessor;
import net.blusalt.mposplugin.processor.LocalData;
import net.blusalt.mposplugin.processor.processor_blusalt.BlusaltTerminalInfoProcessor;
import net.blusalt.mposplugin.processor.processor_blusalt.CardData;
import net.blusalt.mposplugin.processor.processor_blusalt.EmvData;
import net.blusalt.mposplugin.processor.processor_blusalt.KeyDownloadRequest;
import net.blusalt.mposplugin.processor.processor_blusalt.KeyDownloadResponse;
import net.blusalt.mposplugin.processor.processor_blusalt.TerminalInfoProcessor;
import net.blusalt.mposplugin.processor.processor_blusalt.TerminalInformation;
import net.blusalt.mposplugin.processor.processor_blusalt.param.ModelError;
import net.blusalt.mposplugin.processor.processor_blusalt.param.ParamDownloadResponse;
import net.blusalt.mposplugin.processor.util.AppExecutors;
import net.blusalt.mposplugin.processor.util.TerminalKeyParamDownloadListener;
import net.blusalt.mposplugin.processor.util.TimeUtil;
import net.blusalt.mposplugin.processor.util.TripleDES;
import net.blusalt.mposplugin.processor.util.ValueGenerator;
import net.blusalt.mposplugin.utils.DUKPK2009_CBC;
import net.blusalt.mposplugin.utils.FileUtils;
import net.blusalt.mposplugin.utils.ParseASN1Util;
import net.blusalt.mposplugin.utils.QPOSUtil;
import net.blusalt.mposplugin.utils.ShowGuideView;
import net.blusalt.mposplugin.utils.TRACE;
import net.blusalt.mposplugin.widget.BluetoothAdapter;

import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;
import com.dspread.xpos.QPOSService.CommunicationMode;
import com.dspread.xpos.QPOSService.Display;
import com.dspread.xpos.QPOSService.DoTradeResult;
import com.dspread.xpos.QPOSService.DoTransactionType;
import com.dspread.xpos.QPOSService.EMVDataOperation;
import com.dspread.xpos.QPOSService.EmvOption;
import com.dspread.xpos.QPOSService.Error;
import com.dspread.xpos.QPOSService.LcdModeAlign;
import com.dspread.xpos.QPOSService.TransactionResult;
import com.dspread.xpos.QPOSService.TransactionType;
import com.dspread.xpos.QPOSService.UpdateInformationResult;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MposMainActivity extends BaseActivity {
    private QPOSService pos;
    private UpdateThread updateThread;
    private UsbDevice usbDevice;
    private Spinner cmdSp;
    private RecyclerView m_ListView;
    private EditText statusEditText, blockAdd, status, status11, block_address11;
    private EditText mhipStatus;
    private BluetoothAdapter m_Adapter = null;
    private ImageView imvAnimScan, bluetoothImage, validateImage;
    private AnimationDrawable animScan;
    private ListView appListView;
    private List<BluetoothDevice> lstDevScanned;
    //private List<TagApp> appList;
    private LinearLayout mafireLi, mafireUL, lin_remote_key_load;
    private Dialog dialog;
    private Button btn_exchange_cert, btn_verify_keys, btn_remote_key_loading;
    private String verifySignatureCommand, pedvVerifySignatureCommand;
    private String KB;
    private boolean isInitKey;
    private boolean isUpdateFw = false;

    private TextView connectedText, validate_amount_text, about_to_text, validate_text, insert_text;
    private MaterialTextView mToolbar;

    private Toolbar hometoolbar;
    private Spinner mafireSpinner;
    private Button doTradeButton;
    private Button operateCardBtn;
    private Button pollBtn, pollULbtn, veriftBtn, veriftULBtn, readBtn, writeBtn, finishBtn, finishULBtn, getULBtn, readULBtn, fastReadUL, writeULBtn, transferBtn;
    private Button btnUSB;
    private Button btnQuickEMV;
    private Button btnBT;
    private Button continueBtn, insert_amount;
    private Button btnDisconnect;
    private Button updateFwBtn;
    private EditText mKeyIndex;
    private String nfcLog = "";
    private String pubModel = "";
    private String amount = "";
    private String cashbackAmount = "";
    private String blueTootchAddress = "";
    private String blueTitle;
    private String title;
    private boolean isPinCanceled = false;
    private boolean isNormalBlu = false;//to judge if is normal bluetooth
    private int type;
    private ShowGuideView showGuideView;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1001;
    private String deviceSignCert;

    private String serialNo;
    private String cPin = "";

    private String iccDATA = "";
    private CreditCard creditCard;

    private POS_TYPE posType = POS_TYPE.BLUETOOTH;
    private boolean isVisiblePosID;

    private GifImageView spinKit;
    private AppPreferenceHelper appPreferenceHelper;

    private static TerminalKeyParamDownloadListener mlistener;
    public static LocalData localData;

    static String merchantId;
    static String mClearTPK;
    static String terminalId;
    static String tsk;
    static String merchantLoc;
    static String merchantCategoryCode;
    static KeyDownloadResponse mKeyDownloadResponse;
//    @Override
//    public void onGuideListener(Button button) {
//        switch (button.getId()) {
//            case R.id.doTradeButton:
//                showGuideView.show(btnDisconnect, MposMainActivity.this, getString(R.string.msg_disconnect));
//                break;
//            case R.id.disconnect:
//                showGuideView.show(btnUSB, MposMainActivity.this, getString(R.string.msg_conn_usb));
//                break;
//            case R.id.btnBT:
//                showGuideView.show(doTradeButton, MposMainActivity.this, getString(R.string.msg_do_trade));
//                break;
//        }
//    }

    private enum POS_TYPE {
        BLUETOOTH, AUDIO, UART, USB, OTG, BLUETOOTH_BLE
    }

    private void onBTPosSelected(Map<String, ?> itemdata) {
        if (isNormalBlu) {
            pos.stopScanQPos2Mode();
        } else {
            pos.stopScanQposBLE();
        }
        start_time = new Date().getTime();
        Map<String, ?> dev = (Map<String, ?>) itemdata;
        blueTootchAddress = (String) dev.get("ADDRESS");
        blueTitle = (String) dev.get("TITLE");
        blueTitle = blueTitle.split("\\(")[0];

        Log.e("TAG", blueTitle);
        sendMsg(1001);
    }

    private void refreshAdapter() {
        if (m_Adapter != null) {
            m_Adapter.clearData();
        }
        ArrayList<Map<String, ?>> data = new ArrayList<>();
        m_Adapter.setListData(data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_pos_main);
        //When the window is visible to the user, keep the device normally open and keep the brightness unchanged
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        appPreferenceHelper = new AppPreferenceHelper(this);

        initView();
        initIntent();
        initListener();

        TRACE.d("type==" + type);
        pos = null;//reset the pos
        if (pos == null) {
            if (type == 3) {
                open(CommunicationMode.BLUETOOTH);
                posType = POS_TYPE.BLUETOOTH;
            } else if (type == 4) {
                open(CommunicationMode.BLUETOOTH_BLE);
                posType = POS_TYPE.BLUETOOTH_BLE;
            }
        }
        pos.clearBluetoothBuffer();
        if (isNormalBlu) {
            TRACE.d("begin scan====");
            pos.scanQPos2Mode(MposMainActivity.this, 20);
        } else {
            pos.startScanQposBLE(6);
        }
        animScan.start();
        imvAnimScan.setVisibility(View.VISIBLE);
        refreshAdapter();
        if (m_Adapter != null) {
            TRACE.d("+++++=" + m_Adapter);
            m_Adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onToolbarLinstener() {
        finish();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pos_main;
    }

    private void initIntent() {
        Intent intent = getIntent();
        type = intent.getIntExtra("connect_type", 0);
        switch (type) {
            case 3://normal bluetooth
                btnBT.setVisibility(View.VISIBLE);
                this.isNormalBlu = true;
                title = getString(R.string.title_blu);
                break;
            case 4://Ble
                btnBT.setVisibility(View.VISIBLE);
                isNormalBlu = false;
                title = getString(R.string.title_ble);
                break;
        }
        setTitle(title);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initView() {
//        showGuideView = new ShowGuideView();
        mToolbar = (MaterialTextView) findViewById(R.id.mtoolbar);
        hometoolbar = (Toolbar) findViewById(R.id.hometoolbar);
        validateImage = (ImageView) findViewById(R.id.validateImage);
        bluetoothImage = (ImageView) findViewById(R.id.bluetoothImage);
        imvAnimScan = (ImageView) findViewById(R.id.img_anim_scanbt);
        animScan = (AnimationDrawable) getApplicationContext().getResources().getDrawable(R.drawable.progressanmi);

        mafireLi = (LinearLayout) findViewById(R.id.mifareid);
        mafireUL = (LinearLayout) findViewById(R.id.ul_ll);
        status = (EditText) findViewById(R.id.status);
        status11 = (EditText) findViewById(R.id.status11);

        connectedText = (TextView) findViewById(R.id.connected_text);
        validate_text = (TextView) findViewById(R.id.validate_text);
        insert_text = (TextView) findViewById(R.id.insert_text);
        about_to_text = (TextView) findViewById(R.id.about_to_text);
        validate_amount_text = (TextView) findViewById(R.id.validate_amount_text);

        lin_remote_key_load = findViewById(R.id.lin_remote_key_load);
        btn_exchange_cert = findViewById(R.id.btn_init_keys);
        btn_verify_keys = findViewById(R.id.btn_verify_keys);
        btn_remote_key_loading = findViewById(R.id.btn_remote_key_loading);

        insert_amount = (Button) findViewById(R.id.insert_amount);
        operateCardBtn = (Button) findViewById(R.id.operate_card);
        updateFwBtn = (Button) findViewById(R.id.updateFW);
        cmdSp = (Spinner) findViewById(R.id.cmd_spinner);

        imvAnimScan.setBackgroundDrawable(animScan);

        String[] cmdList = new String[]{"add", "reduce", "restore"};
        ArrayAdapter<String> cmdAdapter = new ArrayAdapter<String>(MposMainActivity.this, android.R.layout.simple_spinner_item, cmdList);
        cmdSp.setAdapter(cmdAdapter);

        mafireSpinner = (Spinner) findViewById(R.id.verift_spinner);
        blockAdd = (EditText) findViewById(R.id.block_address);
        block_address11 = (EditText) findViewById(R.id.block_address11);

        String[] keyClass = new String[]{"Key A", "Key B"};
        ArrayAdapter<String> spinneradapter = new ArrayAdapter<String>(MposMainActivity.this, android.R.layout.simple_spinner_item, keyClass);
        mafireSpinner.setAdapter(spinneradapter);


        doTradeButton = (Button) findViewById(R.id.doTradeButton);//start to do trade
        statusEditText = (EditText) findViewById(R.id.statusEditText);
        mhipStatus = (findViewById(R.id.chipStatus));

        continueBtn = (Button) findViewById(R.id.continueBtn);
        btnBT = (Button) findViewById(R.id.btnBT);//start to scan bluetooth device
        btnUSB = (Button) findViewById(R.id.btnUSB);//scan USB device
        btnDisconnect = (Button) findViewById(R.id.disconnect);//disconnect
        btnQuickEMV = (Button) findViewById(R.id.btnQuickEMV);
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

        NestedScrollView parentScrollView = (NestedScrollView) findViewById(R.id.parentScrollview);
        parentScrollView.smoothScrollTo(0, 0);

        m_ListView = (RecyclerView) findViewById(R.id.lv_indicator_BTPOS);
        mKeyIndex = ((EditText) findViewById(R.id.keyindex));
//        btnBT.post(new Runnable() {
//            @Override
//            public void run() {
//                showGuideView.show(btnBT, MposMainActivity.this, getString(R.string.msg_select_device));
//            }
//        });
//        showGuideView.setListener(this);
    }

    private void initListener() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        m_ListView.setLayoutManager(manager);
        m_ListView.setLayoutManager(new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        if (m_Adapter == null) {
            m_Adapter = new BluetoothAdapter(MposMainActivity.this, null);
        }
        m_ListView.setAdapter(m_Adapter);
        m_Adapter.setOnBluetoothItemClickListener(new BluetoothAdapter.OnBluetoothItemClickListener() {
            @Override
            public void onItemClick(int position, Map<String, ?> itemdata) {
                onBTPosSelected(itemdata);
                m_ListView.setVisibility(View.GONE);
                animScan.stop();
                imvAnimScan.setVisibility(View.GONE);
            }
        });


        MyOnClickListener myOnClickListener = new MyOnClickListener();
        //btn click
        hometoolbar.setOnClickListener(myOnClickListener);
        continueBtn.setOnClickListener(myOnClickListener);
        doTradeButton.setOnClickListener(myOnClickListener);
        btnBT.setOnClickListener(myOnClickListener);
        btnDisconnect.setOnClickListener(myOnClickListener);
        btnUSB.setOnClickListener(myOnClickListener);
        updateFwBtn.setOnClickListener(myOnClickListener);
        btnQuickEMV.setOnClickListener(myOnClickListener);
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

    public static String getDigitalEnvelopStr(String encryptData, String encryptDataWith3des, String keyType, String clearData, String signData, String IV) {
        int encryptDataLen = (encryptData.length() / 2);
        int encryptDataWith3desLen = (encryptDataWith3des.length() / 2);
        int clearDataLen = (clearData.length() / 2);
        int signDataLen = (signData.length() / 2);
        int ivLen = IV.length() / 2;
        int len = 2 + 1 + 2 + 2 + encryptDataLen + 2 + encryptDataWith3desLen + 1 + ivLen + 1 + 2 + clearDataLen + 2 + signDataLen;
        String len2 = QPOSUtil.byteArray2Hex(QPOSUtil.intToBytes(len));
        String result = len2 + "010000" + QPOSUtil.intToHex2(encryptDataLen) + encryptData + QPOSUtil.intToHex2(encryptDataWith3desLen) + encryptDataWith3des
                + "0" + Integer.toString(ivLen, 16) + IV
                + keyType + QPOSUtil.intToHex2(clearDataLen) + clearData + QPOSUtil.intToHex2(signDataLen) + signData;
        System.out.println("sys = " + result);
        return result;
    }

    /**
     * open and init bluetooth
     *
     * @param mode
     */
    private void open(CommunicationMode mode) {
        Log.e("TAG Open", "open");
        //pos=null;
//        MyPosListener listener = new MyPosListener();
        MyQposClass listener = new MyQposClass();
        pos = QPOSService.getInstance(mode);
        if (pos == null) {
            Log.e("TAG POS", "CommunicationMode unknow");
            return;
        }
        if (mode == CommunicationMode.USB_OTG_CDC_ACM) {
            pos.setUsbSerialDriver(QPOSService.UsbOTGDriver.CDCACM);
        }
        pos.setConext(MposMainActivity.this);
        pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("NIGERIA-QPOS cute,CR100,D20,D30.xml", MposMainActivity.this)));

        //init handler
        Handler handler = new Handler(Looper.myLooper());
        pos.initListener(handler, listener);
        String sdkVersion = pos.getSdkVersion();
        Log.e("TAG POS sdkVersion ", sdkVersion);

//        Toast.makeText(MposMainActivity.this, "sdkVersion--" + sdkVersion, Toast.LENGTH_SHORT).show();
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
            try {
                pos.disconnectBT();
            } catch (Exception e) {
            }
//			pos.disConnectBtPos();
        } else if (posType == POS_TYPE.BLUETOOTH_BLE) {
            try {
                pos.disconnectBLE();
            } catch (Exception e) {

            }

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
                audioitem.setTitle("Audio Control : Open");
            } else {
                audioitem.setTitle("Audio Control : Close");
            }
        } else {
            audioitem.setTitle("Audio Control : Check");
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
                            Log.e("TAG POS", progress + "%");
                        }
                    });
                    continue;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("TAG POS", "Update Finished 100%");
                    }
                });

                break;
            }
        }

        public void concelSelf() {
            concelFlag = true;
        }
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        TRACE.d("onOptionsItemSelected");
        if (pos == null) {
            Toast.makeText(getApplicationContext(), "Device Disconnect", Toast.LENGTH_LONG).show();
            return true;
        } else if (item.getItemId() == R.id.reset_qpos) {
            boolean a = pos.resetPosStatus();
            if (a) {
                Log.e("TAG POS", "pos reset");
            }
        } else if (item.getItemId() == R.id.doTradeLogOperation) {
            pos.doTradeLogOperation(DoTransactionType.GetAll, 0);
        } else if (item.getItemId() == R.id.get_update_key) {//get the key value
            pos.getUpdateCheckValue();
        } else if (item.getItemId() == R.id.get_device_public_key) {//get the key value
            pos.getDevicePublicKey(5);
        } else if (item.getItemId() == R.id.set_sleepmode_time) {//set pos sleep mode time
//            0~Integer.MAX_VALUE
            pos.setSleepModeTime(20);//the time is in 10s and 10000s
        } else if (item.getItemId() == R.id.set_shutdowm_time) {
            pos.setShutDownTime(15 * 60);
        } else if (item.getItemId() == R.id.menu_exchange_cert) {//inject key remotely
            Log.e("TAG POS", "Exchange Certificates the device with server...");
            pos.getDeviceSigningCertificate();
        } else if (item.getItemId() == R.id.menu_init_key_loading) {//inject key remotely
            Log.e("TAG POS", "is go to init the keys loading...");
            if (deviceSignCert != null) {
                String deviceNonce = ParseASN1Util.generateNonce();
                verifySignatureCommand = getString(R.string.pedk_command, "10", deviceNonce, "1");
                String rkmsNonce = "04916CCC6289600A55118FC37AF0999E";
                String requestSignatureData = "000A" + deviceNonce + rkmsNonce + "01";
                // use device key to sign the data, and get the sign data in callback onReturnAnalyseDigEnvelop
                pos.analyseDigEnvelop(QPOSService.AnalyseDigEnvelopMode.SIGNATURE_ENV, requestSignatureData, 20);
            }
        } else if (item.getItemId() == R.id.menu_confirm_key_type) {//inject key remotely
            String deviceNonce = ParseASN1Util.generateNonce();
            String keyNameASN1 = "301802010131133011130a4473707265616442444b0201001300";
            pedvVerifySignatureCommand = getString(R.string.pedv_command, keyNameASN1, deviceNonce, "1");
            String rkmsNonce = "04916CCC6289600A55118FC37AF0999E";
            String requestSignatureData = keyNameASN1 + deviceNonce + rkmsNonce + "01";
            //the api calback is onReturnAnalyseDi
            // gEnvelop
            pos.analyseDigEnvelop(QPOSService.AnalyseDigEnvelopMode.SIGNATURE_ENV, requestSignatureData, 20);

        } else if (item.getItemId() == R.id.init_device) {
            try {
                String publicKeyStr = QPOSUtil.readRSANStream(getAssets().open("FX-Dspread-signed.pem"));
                BASE64Decoder base64Decoder = new BASE64Decoder();
                byte[] buffer = base64Decoder.decodeBuffer(publicKeyStr);
                String deviceCert = QPOSUtil.byteArray2Hex(buffer);
                String scertChain = QPOSUtil.byteArray2Hex(base64Decoder.decodeBuffer(QPOSUtil.readRSANStream(getAssets().open("FX-Dspread-CA-Tree.pem"))));
                //the api callback is onReturnStoreCertificatesResult
                isInitKey = true;
                pos.loadCertificates(deviceCert, scertChain);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("TAG POS", "Init key...");
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
        } else if (item.getItemId() == R.id.updateEMVAPP) {
            Log.e("TAG POS", "updating emvapp...");
            String appTLV = "9F0610000000000000000000000000000000005F2A0205665F3601009F01060000000000009F090200969F150200009F161e3030303030303030303030303030303030303030303030303030303030309F1A0205669F1B04000000009F1C0800000000000000009F1E0800000000000000009F33032009C89F3501229F3901009F3C0205669F3D01009F4005F000F0A0019F4E0f0000000000000000000000000000009F6604300040809F7B06000000001388DF010101DF11050000000000DF12050000000000DF13050000000000DF1400DF150400000000DF160100DF170100DF1906000000001388DF2006999999999999DF2106000000020000DF7208F4F0F0FAAFFE8000DF730101DF74010FDF750101DF7600DF7806000000020000DF7903E0F8C8DF7A05F000F0A001DF7B00";
            pos.updateEmvAPPByTlv(EMVDataOperation.Add, appTLV);

        } else if (item.getItemId() == R.id.updateEMVCAPK) {
            Log.e("TAG POS", "updating emvcapk...");
            String capkTLV = "9F0605A0000003339F22010BDF0281f8CF9FDF46B356378E9AF311B0F981B21A1F22F250FB11F55C958709E3C7241918293483289EAE688A094C02C344E2999F315A72841F489E24B1BA0056CFAB3B479D0E826452375DCDBB67E97EC2AA66F4601D774FEAEF775ACCC621BFEB65FB0053FC5F392AA5E1D4C41A4DE9FFDFDF1327C4BB874F1F63A599EE3902FE95E729FD78D4234DC7E6CF1ABABAA3F6DB29B7F05D1D901D2E76A606A8CBFFFFECBD918FA2D278BDB43B0434F5D45134BE1C2781D157D501FF43E5F1C470967CD57CE53B64D82974C8275937C5D8502A1252A8A5D6088A259B694F98648D9AF2CB0EFD9D943C69F896D49FA39702162ACB5AF29B90BADE005BC157DF0314BD331F9996A490B33C13441066A09AD3FEB5F66CDF0403000003DF050420311222DF060101DF070101";
            pos.updateEmvCAPKByTlv(EMVDataOperation.Add, capkTLV);

        } else if (item.getItemId() == R.id.setBuzzer) {
            pos.doSetBuzzerOperation(3);//set buzzer
        } else if (item.getItemId() == R.id.menu_get_deivce_info) {
            Log.e("TAG POS", String.valueOf(R.string.getting_info));
            pos.getQposInfo();
        } else if (item.getItemId() == R.id.menu_get_deivce_key_checkvalue) {
            Log.e("TAG POS", "get_deivce_key_checkvalue..............");
            int keyIdex = getKeyIndex();
            pos.getKeyCheckValue(keyIdex, QPOSService.CHECKVALUE_KEYTYPE.DUKPT_MKSK_ALLTYPE);
        } else if (item.getItemId() == R.id.menu_get_pos_id) {
            pos.getQposId();
            Log.e("TAG POS", String.valueOf(R.string.getting_pos_id));
        } else if (item.getItemId() == R.id.setMasterkey) {
            //key:0123456789ABCDEFFEDCBA9876543210
            //result；0123456789ABCDEFFEDCBA9876543210
            int keyIndex = getKeyIndex();
            pos.setMasterKey("1A4D672DCA6CB3351FD1B02B237AF9AE", "08D7B4FB629D0885", keyIndex);
        } else if (item.getItemId() == R.id.menu_get_pin) {
            Log.e("TAG POS", String.valueOf(R.string.input_pin));
            pos.getPin(1, 0, 6, "please input pin", "622262XXXXXXXXX4406", "", 20);
        } else if (item.getItemId() == R.id.isCardExist) {
            pos.isCardExist(30);
        } else if (item.getItemId() == R.id.menu_operate_mafire) {
            Log.e("TAG POS", "operate mafire card");
            showSingleChoiceDialog();
        } else if (item.getItemId() == R.id.menu_operate_update) {
            if (updateFwBtn.getVisibility() == View.VISIBLE || btnQuickEMV.getVisibility() == View.VISIBLE) {
                updateFwBtn.setVisibility(View.GONE);
                btnQuickEMV.setVisibility(View.GONE);
            } else {
                updateFwBtn.setVisibility(View.VISIBLE);
                btnQuickEMV.setVisibility(View.VISIBLE);
            }
        } else if (item.getItemId() == R.id.resetSessionKey) {
            //key：0123456789ABCDEFFEDCBA9876543210
            //result：0123456789ABCDEFFEDCBA9876543210
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
        } else if (item.getItemId() == R.id.updateEMVByXml) {
            Log.e("TAG POS", "updating...");
//            pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("QPOS cute,CR100,D20,D30.xml", MposMainActivity.this)));
            pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("NIGERIA-QPOS cute,CR100,D20,D30.xml", MposMainActivity.this)));
        }
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        TRACE.d("onPause");
        if (type == 3 || type == 4) {
            if (pos != null) {
                if (isNormalBlu) {
                    //stop to scan bluetooth
                    pos.stopScanQPos2Mode();
                } else {
                    //stop to scan ble
                    pos.stopScanQposBLE();
                }
            }
        }
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

    private int yourChoice = 0;

    private void showSingleChoiceDialog() {
        final String[] items = {"Mifare classic 1", "Mifare UL"};
//	    yourChoice = -1;
        AlertDialog.Builder singleChoiceDialog =
                new AlertDialog.Builder(MposMainActivity.this);
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

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    /**
     * @author qianmengChen
     * @ClassName: MyPosListener
     * @date: 2016-11-10 6:35:06pm
     */
    class MyQposClass extends CQPOSService {

        @Override
        public void onRequestWaitingUser() {//wait user to insert/swipe/tap card
            TRACE.d("onRequestWaitingUser()");
            dismissDialog();
            Log.e("TAG POS", getString(R.string.waiting_for_card));
        }

        @Override
        public void onDoTradeResult(DoTradeResult result, Hashtable<String, String> decodeData) {

            TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
            dismissDialog();
            String cardNo = "";
            if (result == DoTradeResult.NONE) {
                Log.e("TAG POS", getString(R.string.no_card_detected));
            } else if (result == QPOSService.DoTradeResult.TRY_ANOTHER_INTERFACE) {
                Log.e("TAG POS", getString(R.string.try_another_interface));
            } else if (result == DoTradeResult.ICC) {
                Log.e("TAG POS", getString(R.string.icc_card_inserted));
                TRACE.d("EMV ICC Start");
//                String maskedPAN = decodeData.get("maskedPAN");
//                String expiryDate = decodeData.get("expiryDate");
//                String cardHolderName = decodeData.get("cardholderName");
//                Log.e("Check", maskedPAN +" "+ expiryDate +" "+ cardHolderName);
                pos.doEmvApp(EmvOption.START);
            } else if (result == DoTradeResult.NOT_ICC) {
                Log.e("TAG POS", getString(R.string.card_inserted));
            } else if (result == DoTradeResult.BAD_SWIPE) {
                Log.e("TAG POS", getString(R.string.bad_swipe));
            } else if (result == DoTradeResult.CARD_NOT_SUPPORT) {
                Log.e("TAG POS", "GPO NOT SUPPORT");
            } else if (result == DoTradeResult.PLS_SEE_PHONE) {
                Log.e("TAG POS", "PLS SEE PHONE");
            } else if (result == DoTradeResult.MCR) {//Magnetic card
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
                    cardNo = maskedPAN;
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
                    cardNo = maskedPAN;
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
                Log.e("TAG POS", content);
            } else if ((result == DoTradeResult.NFC_ONLINE) || (result == DoTradeResult.NFC_OFFLINE)) {
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
                    cardNo = maskedPAN;
                } else {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
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
                    cardNo = maskedPAN;
                }
                Log.e("TAG POS", content);
                sendMsg(8003);
            } else if ((result == DoTradeResult.NFC_DECLINED)) {
                Log.e("TAG POS", getString(R.string.transaction_declined));
            } else if (result == DoTradeResult.NO_RESPONSE) {
                Log.e("TAG POS", getString(R.string.card_no_response));
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
            Log.e("TAG POS", content);
        }

        /**
         * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestTransactionResult(com.dspread.xpos.QPOSService.TransactionResult)
         */
        @Override
        public void onRequestTransactionResult(TransactionResult transactionResult) {
            TRACE.d("onRequestTransactionResult()" + transactionResult.toString());
            if (transactionResult == TransactionResult.CARD_REMOVED) {
                clearDisplay();
            }
            dismissDialog();
            dialog = new Dialog(MposMainActivity.this);
            dialog.setContentView(R.layout.alert_dialog);
            dialog.setTitle(R.string.transaction_result);
            TextView messageTextView = (TextView) dialog.findViewById(R.id.messageTextView);
            if (transactionResult == TransactionResult.APPROVED) {
                TRACE.d("TransactionResult.APPROVED");
                String message = getString(R.string.transaction_approved) + "\n" + getString(R.string.amount) + ": $" + amount + "\n";
                if (!cashbackAmount.equals("")) {
                    message += getString(R.string.cashback_amount) + ": INR" + cashbackAmount;
                }
                messageTextView.setText(message);
//                    deviceShowDisplay("APPROVED");
            } else if (transactionResult == TransactionResult.TERMINATED) {
                clearDisplay();
                messageTextView.setText(getString(R.string.transaction_terminated));
            } else if (transactionResult == TransactionResult.DECLINED) {
                messageTextView.setText(getString(R.string.transaction_declined));
//                    deviceShowDisplay("DECLINED");
            } else if (transactionResult == TransactionResult.CANCEL) {
                clearDisplay();
                messageTextView.setText(getString(R.string.transaction_cancel));
            } else if (transactionResult == TransactionResult.CAPK_FAIL) {
                messageTextView.setText(getString(R.string.transaction_capk_fail));
            } else if (transactionResult == TransactionResult.NOT_ICC) {
                messageTextView.setText(getString(R.string.transaction_not_icc));
            } else if (transactionResult == TransactionResult.SELECT_APP_FAIL) {
                messageTextView.setText(getString(R.string.transaction_app_fail));
            } else if (transactionResult == TransactionResult.DEVICE_ERROR) {
                messageTextView.setText(getString(R.string.transaction_device_error));
            } else if (transactionResult == TransactionResult.TRADE_LOG_FULL) {
                Log.e("TAG POS", "pls clear the trace log and then to begin do trade");
                messageTextView.setText("the trade log has fulled!pls clear the trade log!");
            } else if (transactionResult == TransactionResult.CARD_NOT_SUPPORTED) {
                messageTextView.setText(getString(R.string.card_not_supported));
            } else if (transactionResult == TransactionResult.MISSING_MANDATORY_DATA) {
                messageTextView.setText(getString(R.string.missing_mandatory_data));
            } else if (transactionResult == TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS) {
                messageTextView.setText(getString(R.string.card_blocked_or_no_evm_apps));
            } else if (transactionResult == TransactionResult.INVALID_ICC_DATA) {
                messageTextView.setText(getString(R.string.invalid_icc_data));
            } else if (transactionResult == TransactionResult.FALLBACK) {
                messageTextView.setText("Carding not successful, Please retry");
            } else if (transactionResult == TransactionResult.NFC_TERMINATED) {
                clearDisplay();
                messageTextView.setText("NFC Terminated");
            } else if (transactionResult == TransactionResult.CARD_REMOVED) {
                clearDisplay();
                messageTextView.setText("CARD REMOVED");
            } else if (transactionResult == TransactionResult.CONTACTLESS_TRANSACTION_NOT_ALLOW) {
                clearDisplay();
                messageTextView.setText("TRANS NOT ALLOW");
            } else if (transactionResult == TransactionResult.CARD_BLOCKED) {
                clearDisplay();
                messageTextView.setText("CARD BLOCKED");
            } else if (transactionResult == TransactionResult.TRANS_TOKEN_INVALID) {
                clearDisplay();
                messageTextView.setText("TOKEN INVALID");
            }
            dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismissDialog();
                }
            });
            //            dialog.show();
            Toast.makeText(getApplicationContext(), messageTextView.getText(), Toast.LENGTH_SHORT).show();
            amount = "";
            cashbackAmount = "";
        }

        @Override
        public void onRequestBatchData(String tlv) {
            TRACE.d("ICC trade finished");
            String content = getString(R.string.batch_data);
            TRACE.d("onRequestBatchData(String tlv):" + tlv);
            content += tlv;
            Log.e("TAG POS", content);
        }

        @Override
        public void onRequestTransactionLog(String tlv) {
            TRACE.d("onRequestTransactionLog(String tlv):" + tlv);
            dismissDialog();
            String content = getString(R.string.transaction_log);
            content += tlv;
            Log.e("TAG POS", content);
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
                Log.e("TAG POS", content);
            } else {
                isVisiblePosID = false;
                BaseApplication.setmPosID(posId);
            }

            Log.e("Check Param Download", String.valueOf(SharedPreferencesUtils.getInstance().getBooleanValue(Constants.INTENT_TERMINAL_CONFIG, false)));
            Log.e("Check Key Download", String.valueOf(SharedPreferencesUtils.getInstance().getBooleanValue(Constants.INTENT_KEY_CONFIG, false)));

//            clearData(getApplicationContext());
            if (!SharedPreferencesUtils.getInstance().getBooleanValue(Constants.INTENT_TERMINAL_CONFIG, false)) {
                Log.e("Check Config Download", "Download now");
                Toast.makeText(MposMainActivity.this, "Configuring Mpos Device", Toast.LENGTH_LONG).show();
                downloadConfigurations(serialNo, Pos.mContext, mlistener);
            } else if (!SharedPreferencesUtils.getInstance().getBooleanValue(Constants.INTENT_KEY_CONFIG, false)) {
                Toast.makeText(MposMainActivity.this, "Configuring Mpos Device", Toast.LENGTH_LONG).show();
                Log.e("Check Key Download", "Download now");
                downloadConfigurations(serialNo, Pos.mContext, mlistener);
            } else {
                Log.e("Check Config Download", "Do nothing");
            }
        }

        @Override
        public void onRequestSelectEmvApp(ArrayList<String> appList) {
            TRACE.d("onRequestSelectEmvApp():" + appList.toString());
            TRACE.d("Please select App -- S，emv card config");
            dismissDialog();
            dialog = new Dialog(MposMainActivity.this);
            dialog.setContentView(R.layout.emv_app_dialog);
            dialog.setTitle(R.string.please_select_app);
            String[] appNameList = new String[appList.size()];
            for (int i = 0; i < appNameList.length; ++i) {

                appNameList[i] = appList.get(i);
            }
            appListView = (ListView) dialog.findViewById(R.id.appList);
            appListView.setAdapter(new ArrayAdapter<String>(MposMainActivity.this, android.R.layout.simple_list_item_1, appNameList));
            appListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    pos.selectEmvApp(position);
                    TRACE.d("select emv app position = " + position);
                    dismissDialog();
                }

            });
            dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

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
            TRACE.d("input amount -- S");
            TRACE.d("onRequestSetAmount()");
            dismissDialog();
            dialog = new Dialog(MposMainActivity.this);
            dialog.setContentView(R.layout.amount_dialog);
            dialog.setTitle(getString(R.string.set_amount));
            String[] transactionTypes = new String[]{"GOODS", "SERVICES", "CASH", "CASHBACK", "INQUIRY",
                    "TRANSFER", "ADMIN", "CASHDEPOSIT",
                    "PAYMENT", "PBOCLOG||ECQ_INQUIRE_LOG", "SALE",
                    "PREAUTH", "ECQ_DESIGNATED_LOAD", "ECQ_UNDESIGNATED_LOAD",
                    "ECQ_CASH_LOAD", "ECQ_CASH_LOAD_VOID", "CHANGE_PIN", "REFOUND", "SALES_NEW"};
            ((Spinner) dialog.findViewById(R.id.transactionTypeSpinner)).setAdapter(new ArrayAdapter<String>(MposMainActivity.this, android.R.layout.simple_spinner_item,
                    transactionTypes));

            transactionType = TransactionType.GOODS;

            Log.e("TAG amount Int", appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT_INT));
            Log.e("TAG amount", appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT));

            MposMainActivity.this.amount = appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT_INT);
            insert_amount.setText(appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT));

            MposMainActivity.this.cashbackAmount = cashbackAmount;
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
//                    if (transactionTypeString.equals("GOODS")) {
//                        transactionType = TransactionType.GOODS;
//                    } else if (transactionTypeString.equals("SERVICES")) {
//                        transactionType = TransactionType.SERVICES;
//                    } else if (transactionTypeString.equals("CASH")) {
//                        transactionType = TransactionType.CASH;
//                    } else if (transactionTypeString.equals("CASHBACK")) {
//                        transactionType = TransactionType.CASHBACK;
//                    } else if (transactionTypeString.equals("INQUIRY")) {
//                        transactionType = TransactionType.INQUIRY;
//                    } else if (transactionTypeString.equals("TRANSFER")) {
//                        transactionType = TransactionType.TRANSFER;
//                    } else if (transactionTypeString.equals("ADMIN")) {
//                        transactionType = TransactionType.ADMIN;
//                    } else if (transactionTypeString.equals("CASHDEPOSIT")) {
//                        transactionType = TransactionType.CASHDEPOSIT;
//                    } else if (transactionTypeString.equals("PAYMENT")) {
//                        transactionType = TransactionType.PAYMENT;
//                    } else if (transactionTypeString.equals("PBOCLOG||ECQ_INQUIRE_LOG")) {
//                        transactionType = TransactionType.PBOCLOG;
//                    } else if (transactionTypeString.equals("SALE")) {
//                        transactionType = TransactionType.SALE;
//                    } else if (transactionTypeString.equals("PREAUTH")) {
//                        transactionType = TransactionType.PREAUTH;
//                    } else if (transactionTypeString.equals("ECQ_DESIGNATED_LOAD")) {
//                        transactionType = TransactionType.ECQ_DESIGNATED_LOAD;
//                    } else if (transactionTypeString.equals("ECQ_UNDESIGNATED_LOAD")) {
//                        transactionType = TransactionType.ECQ_UNDESIGNATED_LOAD;
//                    } else if (transactionTypeString.equals("ECQ_CASH_LOAD")) {
//                        transactionType = TransactionType.ECQ_CASH_LOAD;
//                    } else if (transactionTypeString.equals("ECQ_CASH_LOAD_VOID")) {
//                        transactionType = TransactionType.ECQ_CASH_LOAD_VOID;
//                    } else if (transactionTypeString.equals("CHANGE_PIN")) {
//                        transactionType = TransactionType.UPDATE_PIN;
//                    } else if (transactionTypeString.equals("REFOUND")) {
//                        transactionType = TransactionType.REFUND;
//                    } else if (transactionTypeString.equals("SALES_NEW")) {
//                        transactionType = TransactionType.SALES_NEW;
//                    }
//                    MposMainActivity.this.amount = amount;
//                    MposMainActivity.this.cashbackAmount = cashbackAmount;
//                    pos.setAmount(amount + "00", cashbackAmount, "566", transactionType);
//                    dismissDialog();
//                }
//
//            });
//            dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    pos.cancelSetAmount();
//                    dialog.dismiss();
//                }
//
//            });
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.show();
        }

        /**
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
            dialog = new Dialog(MposMainActivity.this);
            dialog.setContentView(R.layout.alert_dialog);
            dialog.setTitle(R.string.request_data_to_server);
            Hashtable<String, String> decodeData = pos.anlysEmvIccData(tlv);
            TRACE.d("anlysEmvIccData(tlv):" + decodeData.toString());
            TRACE.d("anlysEmvIccData(tlv):" + decodeData.get("pinBlock"));
            TRACE.d("iccData(tlv):" + decodeData.get("iccdata"));

            if (!decodeData.get("pinBlock").isEmpty()) {
                creditCard.setPINBLOCK(decodeData.get("iccdata"));
            } else {
                creditCard.setPINBLOCK("");
            }

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

            CreditCard.EmvData emvData = new CreditCard.EmvData("", track2, decodeData.get("iccdata"));
            creditCard.setEmvData(emvData);
//            if (isPinCanceled) {
//                ((TextView) dialog.findViewById(R.id.messageTextView))
//                        .setText(R.string.replied_failed);
//            } else {
//                ((TextView) dialog.findViewById(R.id.messageTextView))
//                        .setText(R.string.replied_success);
//            }
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
                    dismissDialog();
                }
//                    analyData(tlv);// analy tlv ,get the tag you need
            } catch (Exception e) {
                e.printStackTrace();
            }
//            dialog.findViewById(R.id.confirmButton).setOnClickListener(
//                    new OnClickListener() {
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
////                                pos.sendOnlineProcessResult(str);//Script notification/55domain/ICCDATA
//                                Log.e("Check", "Process Trans");
//                                proceedToExChangeData("00", creditCard);
//                                dismissDialog();
//                            }
//                            dismissDialog();
//                        }
//                    });
//            dialog.show();
        }

        @Override
        public void onRequestTime() {
            TRACE.d("onRequestTime");
            dismissDialog();
            String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
            pos.sendTime(terminalTime);
            Log.e("TAG POS", getString(R.string.request_terminal_time) + " " + terminalTime);
        }

        @Override
        public void onRequestDisplay(Display displayMsg) {
            TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
            dismissDialog();
            String msg = "";
            if (displayMsg == Display.CLEAR_DISPLAY_MSG) {
                msg = "";
            } else if (displayMsg == Display.MSR_DATA_READY) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MposMainActivity.this);
                builder.setTitle("Audio");
                builder.setMessage("Success,Contine ready");
                builder.setPositiveButton("Confirm", null);
                builder.show();
            } else if (displayMsg == Display.PLEASE_WAIT) {
                msg = getString(R.string.wait);
            } else if (displayMsg == Display.REMOVE_CARD) {
                msg = getString(R.string.remove_card);
            } else if (displayMsg == Display.TRY_ANOTHER_INTERFACE) {
                msg = getString(R.string.try_another_interface);
            } else if (displayMsg == Display.PROCESSING) {
                msg = getString(R.string.processing);

            } else if (displayMsg == Display.INPUT_PIN_ING) {
                msg = "please input pin on pos";

            } else if (displayMsg == Display.INPUT_OFFLINE_PIN_ONLY || displayMsg == Display.INPUT_LAST_OFFLINE_PIN) {
                msg = "please input offline pin on pos";

            } else if (displayMsg == Display.MAG_TO_ICC_TRADE) {
                msg = "please insert chip card on pos";
            } else if (displayMsg == Display.CARD_REMOVED) {
                msg = "card removed";
            }
            Log.e("TAG POS", msg);
        }

        @Override
        public void onRequestFinalConfirm() {
            TRACE.d("onRequestFinalConfirm() ");
            TRACE.d("onRequestFinalConfirm - S");
            dismissDialog();
            if (!isPinCanceled) {
                dialog = new Dialog(MposMainActivity.this);
                dialog.setContentView(R.layout.confirm_dialog);
                dialog.setTitle(getString(R.string.confirm_amount));

                String message = getString(R.string.amount) + ": $" + amount;
                if (!cashbackAmount.equals("")) {
                    message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmount;
                }
                ((TextView) dialog.findViewById(R.id.messageTextView)).setText(message);
                dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pos.finalConfirm(true);
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
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
            Log.e("TAG POS", getString(R.string.no_device_detected));
        }

        @Override
        public void onRequestQposConnected() {
            TRACE.d("onRequestQposConnected()");

            Toast.makeText(MposMainActivity.this, "Mpos Connected", Toast.LENGTH_LONG).show();
            dismissDialog();
            long use_time = new Date().getTime() - start_time;
            // Log.e("TAG POS", getString(R.string.device_plugged));
            Log.e("TAG POS", getString(R.string.device_plugged) + "--" + getResources().getString(R.string.used) + QPOSUtil.formatLongToTimeStr(use_time, MposMainActivity.this));
            btnBT.setVisibility(View.INVISIBLE);

            continueBtn.setVisibility(View.VISIBLE);
            bluetoothImage.setVisibility(View.VISIBLE);
            connectedText.setVisibility(View.VISIBLE);

//            btnDisconnect.setEnabled(false);
//            btnQuickEMV.setEnabled(false);
            if (posType == POS_TYPE.BLUETOOTH || posType == POS_TYPE.BLUETOOTH_BLE) {
                setTitle(title + "(" + blueTitle.substring(0, 6) + "..." + blueTitle.substring(blueTitle.length() - 3) + ")");
            } else {
                setTitle("Device connect");
            }
            if (ActivityCompat.checkSelfPermission(MposMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PERMISSION_GRANTED) {
                //申请权限
                ActivityCompat.requestPermissions(MposMainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
            isVisiblePosID = true;
            pos.getQposId();

        }

        @Override
        public void onRequestQposDisconnected() {
            dismissDialog();
            setTitle(title);
            TRACE.d("onRequestQposDisconnected()");
            Log.e("TAG POS", getString(R.string.device_unplugged));
//            btnDisconnect.setEnabled(false);
//            doTradeButton.setEnabled(false);
//            lin_remote_key_load.setVisibility(View.GONE);
        }

        @Override
        public void onError(Error errorState) {
            if (updateThread != null) {
                updateThread.concelSelf();
            }
            TRACE.d("onError" + errorState.toString());
            dismissDialog();
            if (errorState == Error.CMD_NOT_AVAILABLE) {
                Log.e("TAG POS", getString(R.string.command_not_available));
            } else if (errorState == Error.TIMEOUT) {
                Log.e("TAG POS", getString(R.string.device_no_response));
            } else if (errorState == Error.DEVICE_RESET) {
                Log.e("TAG POS", getString(R.string.device_reset));
            } else if (errorState == Error.UNKNOWN) {
                Log.e("TAG POS", getString(R.string.unknown_error));
            } else if (errorState == Error.DEVICE_BUSY) {
                Log.e("TAG POS", getString(R.string.device_busy));
            } else if (errorState == Error.INPUT_OUT_OF_RANGE) {
                Log.e("TAG POS", getString(R.string.out_of_range));
            } else if (errorState == Error.INPUT_INVALID_FORMAT) {
                Log.e("TAG POS", getString(R.string.invalid_format));
            } else if (errorState == Error.INPUT_ZERO_VALUES) {
                Log.e("TAG POS", getString(R.string.zero_values));
            } else if (errorState == Error.INPUT_INVALID) {
                Log.e("TAG POS", getString(R.string.input_invalid));
            } else if (errorState == Error.CASHBACK_NOT_SUPPORTED) {
                Log.e("TAG POS", getString(R.string.cashback_not_supported));
            } else if (errorState == Error.CRC_ERROR) {
                Log.e("TAG POS", getString(R.string.crc_error));
            } else if (errorState == Error.COMM_ERROR) {
                Log.e("TAG POS", getString(R.string.comm_error));
            } else if (errorState == Error.MAC_ERROR) {
                Log.e("TAG POS", getString(R.string.mac_error));
            } else if (errorState == Error.APP_SELECT_TIMEOUT) {
                Log.e("TAG POS", getString(R.string.app_select_timeout_error));
            } else if (errorState == Error.CMD_TIMEOUT) {
                Log.e("TAG POS", getString(R.string.cmd_timeout));
            } else if (errorState == Error.ICC_ONLINE_TIMEOUT) {
                if (pos == null) {
                    return;
                }
                pos.resetPosStatus();
                Toast.makeText(getApplicationContext(), R.string.device_reset, Toast.LENGTH_LONG).show();
                Log.e("TAG POS", getString(R.string.device_reset));
            }
        }

        @Override
        public void onReturnReversalData(String tlv) {
            String content = getString(R.string.reversal_data);
            content += tlv;
            TRACE.d("onReturnReversalData(): " + tlv);
            Log.e("TAG POS", content);
        }

        @Override
        public void onReturnupdateKeyByTR_31Result(boolean result, String keyType) {
            super.onReturnupdateKeyByTR_31Result(result, keyType);
            if (result) {
                Log.e("TAG POS", "send TR31 key success! The keyType is " + keyType);
            } else {
                Log.e("TAG POS", "send TR31 key fail");
            }
        }

        @Override
        public void onReturnServerCertResult(String serverSignCert, String serverEncryptCert) {
            super.onReturnServerCertResult(serverSignCert, serverEncryptCert);
        }

        @Override
        public void onReturnGetPinResult(Hashtable<String, String> result) {
            TRACE.d("onReturnGetPinResult(Hashtable<String, String> result):" + result.toString());
            String pinBlock = result.get("pinBlock");
            String pinKsn = result.get("pinKsn");
            String content = "get pin result\n";
            content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
            content += getString(R.string.pinBlock) + " " + pinBlock + "\n";

            TRACE.d("Check pinblock" + getString(R.string.pinBlock) + " " + pinBlock + "\n");
            Log.e("TAG POS", content);
            TRACE.i(content);
        }

        @Override
        public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
            TRACE.d("onReturnApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
        }

        @Override
        public void onReturnPowerOffIccResult(boolean arg0) {
            TRACE.d("onReturnPowerOffIccResult(boolean arg0):" + arg0);
        }

        @Override
        public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
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
            Log.e("TAG POS", content);
        }

        @Override
        public void onGetCardNoResult(String cardNo) {
            TRACE.d("onGetCardNoResult(String cardNo):" + cardNo);
            Log.e("TAG POS", "cardNo: " + cardNo);
        }

        @Override
        public void onRequestCalculateMac(String calMac) {
            TRACE.d("onRequestCalculateMac(String calMac):" + calMac);
            if (calMac != null && !"".equals(calMac)) {
                calMac = QPOSUtil.byteArray2Hex(calMac.getBytes());
            }
            Log.e("TAG POS", "calMac: " + calMac);
            TRACE.d("calMac_result: calMac=> e: " + calMac);
        }

        @Override
        public void onRequestSignatureResult(byte[] arg0) {
            TRACE.d("onRequestSignatureResult(byte[] arg0):" + arg0.toString());
        }

        @Override
        public void onRequestUpdateWorkKeyResult(UpdateInformationResult result) {
            TRACE.d("onRequestUpdateWorkKeyResult(UpdateInformationResult result):" + result);
            if (result == UpdateInformationResult.UPDATE_SUCCESS) {
                Log.e("TAG POS", "update work key success");
            } else if (result == UpdateInformationResult.UPDATE_FAIL) {
                Log.e("TAG POS", "update work key fail");
            } else if (result == UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
                Log.e("TAG POS", "update work key packet vefiry error");
            } else if (result == UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
                Log.e("TAG POS", "update work key packet len error");
            }
        }

        @Override
        public void onReturnCustomConfigResult(boolean isSuccess, String result) {

            if (isSuccess) {
                appPreferenceHelper.setSharedPreferenceBoolean(Constants.IS_KEY_INJECTED, true);
                appPreferenceHelper.setSharedPreferenceString(Constants.KEY_INJECTED_BLUETOOTH, blueTootchAddress);
                Log.e("Log TAG", "Key Injected: " + isSuccess + "\nblueTootchAddress: " + blueTootchAddress);
            } else {
                appPreferenceHelper.setSharedPreferenceBoolean(Constants.IS_KEY_INJECTED, false);
            }

            insert_text.setText("Insert Card into the mPOS");
            isPinCanceled = false;
            Log.e("TAG POS", String.valueOf(R.string.starting));
            terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
            if (posType == POS_TYPE.UART) {
                pos.doTrade(terminalTime, 0, 30);
            } else {
                int keyIdex = getKeyIndex();
                pos.doTrade(keyIdex, 30);//start do trade
            }

            Log.e("onReturnCustomConfigResult(boolean isSuccess, String result):", isSuccess + TRACE.NEW_LINE + result);
            Log.e("TAG POS", "result: " + isSuccess + "\ndata: " + result);
        }

        @Override
        public void onRequestSetPin() {
            TRACE.i("onRequestSetPin()");
            creditCard = new CreditCard();

            dismissDialog();
            dialog = new Dialog(MposMainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
            dialog.setContentView(R.layout.pin_dialog);
            dialog.setTitle(getString(R.string.enter_pin));

            dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

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
//                        cPin = pin;
                        creditCard.setPIN(pin);
                        pos.sendPin(pin);
                        insert_text.setText("Transaction Processing...");
                    } else {
                        Toast.makeText(MposMainActivity.this, "The length just can input 4 - 12 digits", Toast.LENGTH_LONG).show();
                    }
                }
            });
//            dialog.findViewById(R.id.bypassButton).setOnClickListener(new OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
////					pos.bypassPin();
//                    pos.sendPin("");
//
//                    dismissDialog();
//                }
//            });

//            dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
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
            Log.e("TAG POS", "result: " + isSuccess);
        }

        @Override
        public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult) {
            TRACE.d("onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> batchAPDUResult):" + batchAPDUResult.toString());
            StringBuilder sb = new StringBuilder();
            sb.append("APDU Responses: \n");
            for (HashMap.Entry<Integer, String> entry : batchAPDUResult.entrySet()) {
                sb.append("[" + entry.getKey() + "]: " + entry.getValue() + "\n");
            }
            Log.e("TAG POS", "\n" + sb.toString());
        }

        @Override
        public void onBluetoothBondFailed() {
            TRACE.d("onBluetoothBondFailed()");
            Log.e("TAG POS", "bond failed");
        }

        @Override
        public void onBluetoothBondTimeout() {
            TRACE.d("onBluetoothBondTimeout()");
            Log.e("TAG POS", "bond timeout");
        }

        @Override
        public void onBluetoothBonded() {
            TRACE.d("onBluetoothBonded()");
            Log.e("TAG POS", "bond success");
        }

        @Override
        public void onBluetoothBonding() {
            TRACE.d("onBluetoothBonding()");
            Log.e("TAG POS", "bonding .....");
        }

        @Override
        public void onReturniccCashBack(Hashtable<String, String> result) {
            TRACE.d("onReturniccCashBack(Hashtable<String, String> result):" + result.toString());
            String s = "serviceCode: " + result.get("serviceCode");
            s += "\n";
            s += "trackblock: " + result.get("trackblock");
            Log.e("TAG POS", s);
        }

        @Override
        public void onLcdShowCustomDisplay(boolean arg0) {
            TRACE.d("onLcdShowCustomDisplay(boolean arg0):" + arg0);
        }

        @Override
        public void onUpdatePosFirmwareResult(UpdateInformationResult arg0) {
            TRACE.d("onUpdatePosFirmwareResult(UpdateInformationResult arg0):" + arg0.toString());
//            isUpdateFw = false;
            if (arg0 != UpdateInformationResult.UPDATE_SUCCESS) {
                updateThread.concelSelf();
            } else {
                mhipStatus.setText("");
            }
            Log.e("TAG POS", "onUpdatePosFirmwareResult" + arg0.toString());
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
            Log.e("TAG POS", "randomKeyLen:" + randomKeyLen + "\nrandomKey:" + randomKey + "\nrandomKeyCheckValueLen:" + randomKeyCheckValueLen + "\nrandomKeyCheckValue:"
                    + randomKeyCheckValue);
        }

        @Override
        public void onGetPosComm(int mod, String amount, String posid) {
            TRACE.d("onGetPosComm(int mod, String amount, String posid):" + mod + TRACE.NEW_LINE + amount + TRACE.NEW_LINE + posid);
        }

        @Override
        public void onPinKey_TDES_Result(String arg0) {
            TRACE.d("onPinKey_TDES_Result(String arg0):" + arg0);
            Log.e("TAG POS", "result:" + arg0);
        }

        @Override
        public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
            TRACE.d("onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
        }

        @Override
        public void onEmvICCExceptionData(String arg0) {
            TRACE.d("onEmvICCExceptionData(String arg0):" + arg0);
        }

        @Override
        public void onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1) {
            TRACE.d("onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
        }

        @Override
        public void onGetInputAmountResult(boolean arg0, String arg1) {
            TRACE.d("onGetInputAmountResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1.toString());
        }

        @Override
        public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
            TRACE.d("onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
            Log.e("TAG POS", "onReturnNFCApduResult(boolean arg0, String arg1, int arg2):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2);
        }

        @Override
        public void onReturnPowerOffNFCResult(boolean arg0) {
            TRACE.d(" onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
            Log.e("TAG POS", " onReturnPowerOffNFCResult(boolean arg0) :" + arg0);
        }

        @Override
        public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
            TRACE.d("onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
            Log.e("TAG POS", "onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3):" + arg0 + TRACE.NEW_LINE + arg1 + TRACE.NEW_LINE + arg2 + TRACE.NEW_LINE + arg3);
        }

        @Override
        public void onCbcMacResult(String result) {
            TRACE.d("onCbcMacResult(String result):" + result);
            if (result == null || "".equals(result)) {
                Log.e("TAG POS", "cbc_mac:false");
            } else {
                Log.e("TAG POS", "cbc_mac: " + result);
            }
        }

        @Override
        public void onReadBusinessCardResult(boolean arg0, String arg1) {
            TRACE.d(" onReadBusinessCardResult(boolean arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);
        }

        @Override
        public void onWriteBusinessCardResult(boolean arg0) {
            TRACE.d(" onWriteBusinessCardResult(boolean arg0):" + arg0);
        }

        @Override
        public void onConfirmAmountResult(boolean arg0) {
            TRACE.d("onConfirmAmountResult(boolean arg0):" + arg0);
        }

        @Override
        public void onQposIsCardExist(boolean cardIsExist) {
            TRACE.d("onQposIsCardExist(boolean cardIsExist):" + cardIsExist);
            if (cardIsExist) {
                Log.e("TAG POS", "cardIsExist:" + cardIsExist);
            } else {
                Log.e("TAG POS", "cardIsExist:" + cardIsExist);
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
                Log.e("TAG POS", "statuString:" + statuString + "\n" + "cardTypeString:" + cardTypeString + "\ncardUidLen:" + cardUidLen
                        + "\ncardUid:" + cardUid + "\ncardAtsLen:" + cardAtsLen + "\ncardAts:" + cardAts
                        + "\nATQA:" + ATQA + "\nSAK:" + SAK);
            } else {
                Log.e("TAG POS", "poll card failed");
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
            if (arg0) {
                Log.e("TAG POS", "Set buzzer success");
            } else {
                Log.e("TAG POS", "Set buzzer failed");
            }
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
            if (arg0) {
                Log.e("TAG POS", "Set master key success");
            } else {
                Log.e("TAG POS", "Set master key failed");
            }
        }

        @Override
        public void onReturnUpdateIPEKResult(boolean arg0) {
            TRACE.d("onReturnUpdateIPEKResult(boolean arg0):" + arg0);
            if (arg0) {
                Log.e("TAG POS", "update IPEK success");
            } else {
                Log.e("TAG POS", "update IPEK fail");
            }
        }

        @Override
        public void onReturnUpdateEMVRIDResult(boolean arg0) {
            TRACE.d("onReturnUpdateEMVRIDResult(boolean arg0):" + arg0);
        }

        @Override
        public void onReturnUpdateEMVResult(boolean arg0) {
            TRACE.d("onReturnUpdateEMVResult(boolean arg0):" + arg0);
        }

        @Override
        public void onBluetoothBoardStateResult(boolean arg0) {
            TRACE.d("onBluetoothBoardStateResult(boolean arg0):" + arg0);
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onDeviceFound(BluetoothDevice arg0) {
            if (arg0 != null && arg0.getName() != null) {
                TRACE.d("onDeviceFound(BluetoothDevice arg0):" + arg0.getName() + ":" + arg0.toString());
                m_ListView.setVisibility(View.VISIBLE);
                animScan.start();
                imvAnimScan.setVisibility(View.VISIBLE);
                if (m_Adapter != null) {
                    Map<String, Object> itm = new HashMap<String, Object>();
                    itm.put("ICON", arg0.getBondState() == BluetoothDevice.BOND_BONDED ? Integer
                            .valueOf(R.drawable.bluetooth_blue) : Integer
                            .valueOf(R.drawable.bluetooth_blue_unbond));
                    itm.put("TITLE", arg0.getName() + "(" + arg0.getAddress() + ")");
                    itm.put("ADDRESS", arg0.getAddress());
                    m_Adapter.setData(itm);
                }
                String address = arg0.getAddress();
                String name = arg0.getName();
                name += address + "\n";
                Log.e("TAG POS", name);
                TRACE.d("found new device" + name);
            } else {
                Log.e("TAG POS", "Don't found new device");
                TRACE.d("Don't found new device");
            }
        }

        @Override
        public void onSetSleepModeTime(boolean arg0) {
            TRACE.d("onSetSleepModeTime(boolean arg0):" + arg0);
            if (arg0) {
                Log.e("TAG POS", "set the Sleep timee Success");
            } else {
                Log.e("TAG POS", "set the Sleep timee unSuccess");
            }
        }

        @Override
        public void onReturnGetEMVListResult(String arg0) {
            TRACE.d("onReturnGetEMVListResult(String arg0):" + arg0);
            if (arg0 != null && arg0.length() > 0) {
                Log.e("TAG POS", "The emv list is : " + arg0);
            }
        }

        @Override
        public void onWaitingforData(String arg0) {
            TRACE.d("onWaitingforData(String arg0):" + arg0);
        }

        @Override
        public void onRequestDeviceScanFinished() {
            TRACE.d("onRequestDeviceScanFinished()");
//            Toast.makeText(MposMainActivity.this, R.string.scan_over, Toast.LENGTH_LONG).show();
            animScan.stop();
            imvAnimScan.setVisibility(View.GONE);
        }

        @Override
        public void onRequestUpdateKey(String arg0) {
            TRACE.d("onRequestUpdateKey(String arg0):" + arg0);
            mhipStatus.setText("update checkvalue : " + arg0);
//            if(isUpdateFw){
//                updateFirmware();
//            }
        }

        @Override
        public void onReturnGetQuickEmvResult(boolean arg0) {
            TRACE.d("onReturnGetQuickEmvResult(boolean arg0):" + arg0);
            if (arg0) {
                Log.e("TAG POS", "emv configed");
                pos.setQuickEmv(true);
            } else {
                Log.e("TAG POS", "emv don't configed");
            }
        }

        @Override
        public void onQposDoGetTradeLogNum(String arg0) {
            TRACE.d("onQposDoGetTradeLogNum(String arg0):" + arg0);
            int a = Integer.parseInt(arg0, 16);
            if (a >= 188) {
                Log.e("TAG POS", "the trade num has become max value!!");
                return;
            }
            Log.e("TAG POS", "get log num:" + a);
        }

        @Override
        public void onQposDoTradeLog(boolean arg0) {
            TRACE.d("onQposDoTradeLog(boolean arg0) :" + arg0);
            if (arg0) {
                Log.e("TAG POS", "clear log success!");
            } else {
                Log.e("TAG POS", "clear log fail!");
            }
        }

        @Override
        public void onAddKey(boolean arg0) {
            TRACE.d("onAddKey(boolean arg0) :" + arg0);
            if (arg0) {
                Log.e("TAG POS", "ksn add 1 success");
            } else {
                Log.e("TAG POS", "ksn add 1 failed");
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
            String pinKsn = arg0.get("pinKsn");
            String trackKsn = arg0.get("trackKsn");
            String emvKsn = arg0.get("emvKsn");
            TRACE.d("get the ksn result is :" + "pinKsn" + pinKsn + "\ntrackKsn" + trackKsn + "\nemvKsn" + emvKsn);
        }

        @Override
        public void onQposDoGetTradeLog(String arg0, String arg1) {
            TRACE.d("onQposDoGetTradeLog(String arg0, String arg1):" + arg0 + TRACE.NEW_LINE + arg1);
            arg1 = QPOSUtil.convertHexToString(arg1);
            Log.e("TAG POS", "orderId:" + arg1 + "\ntrade log:" + arg0);
        }

        @Override
        public void onRequestDevice() {
            List<UsbDevice> deviceList = getPermissionDeviceList();
            UsbManager mManager = (UsbManager) MposMainActivity.this.getSystemService(USB_SERVICE);
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
                Log.e("TAG POS", buffer.toString());
            }
        }

        @Override
        public void onGetDevicePubKey(String clearKeys) {
            TRACE.d("onGetDevicePubKey(clearKeys):" + clearKeys);
            Log.e("TAG POS", clearKeys);
            String lenStr = clearKeys.substring(0, 4);
            int sum = 0;
            for (int i = 0; i < 4; i++) {
                int bit = Integer.parseInt(lenStr.substring(i, i + 1));
                sum += bit * Math.pow(16, (3 - i));
            }
            pubModel = clearKeys.substring(4, 4 + sum * 2);
        }

//        @Override
//        public void onSetPosBlePinCode(boolean b) {
//            TRACE.d("onSetPosBlePinCode(b):" + b);
//            if (b) {
//                Log.e("TAG POS", "onSetPosBlePinCode success");
//            } else {
//                Log.e("TAG POS", "onSetPosBlePinCode fail");
//            }
//        }

        @Override
        public void onTradeCancelled() {
            TRACE.d("onTradeCancelled");
            dismissDialog();
        }

        @Override
        public void onReturnSignature(boolean b, String signaturedData) {
            if (b) {
                BASE64Encoder base64Encoder = new BASE64Encoder();
                String encode = base64Encoder.encode(signaturedData.getBytes());
                Log.e("TAG POS", "signature data (Base64 encoding):" + encode);
            }
        }

        @Override
        public void onReturnConverEncryptedBlockFormat(String result) {
            Log.e("TAG POS", result);
        }

        @Override
        public void onFinishMifareCardResult(boolean arg0) {
            TRACE.d("onFinishMifareCardResult(boolean arg0):" + arg0);
            if (arg0) {
                Log.e("TAG POS", "finish success");
            } else {
                Log.e("TAG POS", "finish fail");
            }
        }

        @Override
        public void onVerifyMifareCardResult(boolean arg0) {
            TRACE.d("onVerifyMifareCardResult(boolean arg0):" + arg0);
            if (arg0) {
                Log.e("TAG POS", " onVerifyMifareCardResult success");
            } else {
                Log.e("TAG POS", "onVerifyMifareCardResult fail");
            }
        }

        @Override
        public void onReadMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onReadMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String addr = arg0.get("addr");
                String cardDataLen = arg0.get("cardDataLen");
                String cardData = arg0.get("cardData");
                Log.e("TAG POS", "addr:" + addr + "\ncardDataLen:" + cardDataLen + "\ncardData:" + cardData);
            } else {
                Log.e("TAG POS", "onReadWriteMifareCardResult fail");
            }
        }

        @Override
        public void onWriteMifareCardResult(boolean arg0) {
            TRACE.d("onWriteMifareCardResult(boolean arg0):" + arg0);
            if (arg0) {
                Log.e("TAG POS", "write data success!");
            } else {
                Log.e("TAG POS", "write data fail!");
            }
        }

        @Override
        public void onOperateMifareCardResult(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("onOperateMifareCardResult(Hashtable<String, String> arg0):" + arg0.toString());
                String cmd = arg0.get("Cmd");
                String blockAddr = arg0.get("blockAddr");
                Log.e("TAG POS", "Cmd:" + cmd + "\nBlock Addr:" + blockAddr);
            } else {
                Log.e("TAG POS", "operate failed");
            }
        }

        @Override
        public void getMifareCardVersion(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("getMifareCardVersion(Hashtable<String, String> arg0):" + arg0.toString());

                String verLen = arg0.get("versionLen");
                String ver = arg0.get("cardVersion");
                Log.e("TAG POS", "versionLen:" + verLen + "\nverison:" + ver);
            } else {
                Log.e("TAG POS", "get mafire UL version failed");
            }
        }

        @Override
        public void getMifareFastReadData(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("getMifareFastReadData(Hashtable<String, String> arg0):" + arg0.toString());
                String startAddr = arg0.get("startAddr");
                String endAddr = arg0.get("endAddr");
                String dataLen = arg0.get("dataLen");
                String cardData = arg0.get("cardData");
                Log.e("TAG POS", "startAddr:" + startAddr + "\nendAddr:" + endAddr + "\ndataLen:" + dataLen
                        + "\ncardData:" + cardData);
            } else {
                Log.e("TAG POS", "read fast UL failed");
            }
        }

        @Override
        public void getMifareReadData(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("getMifareReadData(Hashtable<String, String> arg0):" + arg0.toString());
                String blockAddr = arg0.get("blockAddr");
                String dataLen = arg0.get("dataLen");
                String cardData = arg0.get("cardData");
                Log.e("TAG POS", "blockAddr:" + blockAddr + "\ndataLen:" + dataLen + "\ncardData:" + cardData);
            } else {
                Log.e("TAG POS", "read mafire UL failed");
            }
        }

        @Override
        public void writeMifareULData(String arg0) {
            if (arg0 != null) {
                TRACE.d("writeMifareULData(String arg0):" + arg0);
                Log.e("TAG POS", "addr:" + arg0);
            } else {
                Log.e("TAG POS", "write UL failed");
            }
        }

        @Override
        public void verifyMifareULData(Hashtable<String, String> arg0) {
            if (arg0 != null) {
                TRACE.d("verifyMifareULData(Hashtable<String, String> arg0):" + arg0.toString());
                String dataLen = arg0.get("dataLen");
                String pack = arg0.get("pack");
                Log.e("TAG POS", "dataLen:" + dataLen + "\npack:" + pack);
            } else {
                Log.e("TAG POS", "verify UL failed");
            }
        }

        @Override
        public void onGetSleepModeTime(String arg0) {
            if (arg0 != null) {
                TRACE.d("onGetSleepModeTime(String arg0):" + arg0.toString());

                int time = Integer.parseInt(arg0, 16);
                Log.e("TAG POS", "time is ： " + time + " seconds");
            } else {
                Log.e("TAG POS", "get the time is failed");
            }
        }

        @Override
        public void onGetShutDownTime(String arg0) {
            if (arg0 != null) {
                TRACE.d("onGetShutDownTime(String arg0):" + arg0.toString());
                Log.e("TAG POS", "shut down time is : " + Integer.parseInt(arg0, 16) + "s");
            } else {
                Log.e("TAG POS", "get the shut down time is fail!");
            }
        }

        @Override
        public void onQposDoSetRsaPublicKey(boolean arg0) {
            TRACE.d("onQposDoSetRsaPublicKey(boolean arg0):" + arg0);
            if (arg0) {
                Log.e("TAG POS", "set rsa is successed!");
            } else {
                Log.e("TAG POS", "set rsa is failed!");
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
                Log.e("TAG POS", "rsaFileName:" + rsaFileName + "\nenPinKeyData:" + enPinKeyData + "\nenKcvPinKeyData:" +
                        enKcvPinKeyData + "\nenCardKeyData:" + enCardKeyData + "\nenKcvCardKeyData:" + enKcvCardKeyData);
            } else {
                Log.e("TAG POS", "get key failed,pls try again!");
            }
        }

        @Override
        public void transferMifareData(String arg0) {
            TRACE.d("transferMifareData(String arg0):" + arg0.toString());

            // TODO Auto-generated method stub
            if (arg0 != null) {
                Log.e("TAG POS", "response data:" + arg0);
            } else {
                Log.e("TAG POS", "transfer data failed!");
            }
        }

        @Override
        public void onReturnRSAResult(String arg0) {
            TRACE.d("onReturnRSAResult(String arg0):" + arg0.toString());

            if (arg0 != null) {
                Log.e("TAG POS", "rsa data:\n" + arg0);
            } else {
                Log.e("TAG POS", "get the rsa failed");
            }
        }

        @Override
        public void onRequestNoQposDetectedUnbond() {
            // TODO Auto-generated method stub
            TRACE.d("onRequestNoQposDetectedUnbond()");
        }

        @Override
        public void onReturnDeviceCSRResult(String re) {
            TRACE.d("onReturnDeviceCSRResult:" + re);
            Log.e("TAG POS", "onReturnDeviceCSRResult:" + re);
        }

        @Override
        public void onReturnStoreCertificatesResult(boolean re) {
            TRACE.d("onReturnStoreCertificatesResult:" + re);
            if (isInitKey) {
                Log.e("TAG POS", "Init key result is :" + re);
                isInitKey = false;
            } else {
                Log.e("TAG POS", "Exchange Certificates result is :" + re);
            }

        }

        @Override
        public void onReturnDeviceSigningCertResult(String certificates, String certificatesTree) {
            TRACE.d("onReturnDeviceSigningCertResult:" + certificates + "\n" + certificatesTree);
            deviceSignCert = certificates;
            String command = getString(R.string.pedi_command, certificates, "1", "oeap");
            command = ParseASN1Util.addTagToCommand(command, "CD", certificates);
            TRACE.i("request the RKMS command is " + command);
            String pediRespose = "[AOPEDI;ANY;CC308203B33082029BA00302010202074EB0D60000987E300D06092A864886F70D01010B0500308190310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A5669727475437279707431173015060355040C0C0E44737072656164204B44482043413117301506035504410C0E44737072656164204B44482043413117301506035504030C0E44737072656164204B4448204341301E170D3231303330363030303030305A170D3330303330373030303030305A3081A2310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A56697274754372797074311D301B060355040C0C14447370726561645F417573524B4D533130312D56311D301B06035504410C14447370726561645F417573524B4D533130312D56311D301B06035504030C14447370726561645F417573524B4D533130312D5630820122300D06092A864886F70D01010105000382010F003082010A0282010100D7FD40DD513EE82491FABA3EB734C3FE69C79973797007A2183EC9C468F73D8E1CB669DDA6DC32CA125F9FAEAC0C0556893C9196FB123B06BC9B880EEF367CD17000C7E0ECF7313DD2D396F29C8D977A65946258BE5A4133462F0675161407EED3D263BC20E9271B9070DCC1A6376F89E7E9E2B304BC756E3E3B61B869A2E39F11067D00B5BA3817673A730F42DC4C037FC214207C70A1E3E43F7D7494E71EBDD5BB0E9AFAE32E422DB90B85E230DF406FB12470AD0360FD7BDFDD1A29BCE91655A835129858A0E9EB04845A80F1E9F8EAA20C67C6B8A61113D6FFDD7DF5719778A03A30F69B0DD9033D5E975F723CC18792CC6988250A7DBD20901450651A810203010001300D06092A864886F70D01010B050003820101008F002AE3AFB49C2E7D99CC7B933617D180CB4E8EA13CBCBE7469FC4E5124033F06E4C3B0DAB3C6CA4625E3CD53E7B86C247CDF100E266059366F8FEEC746507E1B0D0924029805AAB89FCE1482946B8B0C1F546DD56B399AB48891B731148C878EF4D02AE641717A3D381C7B62011B76A6FFBF20846217EB68149C96B4B134F980060A542DBE2F32BF7AD308F26A279B41C65E32D4E260AE68B3010685CE36869EFF09D211CE64401F417A72F29F49A2EE713ACC37C29AECBFEBE571EF11D883815F54FA3E52A917CC3D6B008A3E3C52164FF5591D869026D248873F15DE531104F329C279FC5B6BC28ABC833F8C31BEF47783A5D5B9C534A57530D9AE463DC3;CD308203B33082029BA00302010202074EB0D60000987C300D06092A864886F70D01010B0500308190310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A5669727475437279707431173015060355040C0C0E44737072656164204B44482043413117301506035504410C0E44737072656164204B44482043413117301506035504030C0E44737072656164204B4448204341301E170D3231303330373030303030305A170D3330303330383030303030305A3081A2310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A56697274754372797074311D301B060355040C0C14447370726561645F417573524B4D533130312D45311D301B06035504410C14447370726561645F417573524B4D533130312D45311D301B06035504030C14447370726561645F417573524B4D533130312D4530820122300D06092A864886F70D01010105000382010F003082010A0282010100A62A4935B57BA478F41B6C8B3F79E84DB61E516FEC8D5BE3E86FD296C6906625E0316A77F59D6D5075811BA7BB0801366BA7E370B758E3E1DCE005008C13D368536C2216FAF8AF70EBC6B5D1D231AFD19D6270DDBEA6535B46135D1DE11F374978A655FAA8C2A0DDC933CF82E9DC69DABF8676D0E81762D9B01799C83A8DF3EE70584AA4543EBBDAB02A0EFCA6A276588893DD28BD096400E315ECF5FE91EC210EEC2BE8763FEFB57D1448CC7D0FCDC3BDCE4B7BAAD546E0E5E99281B4F1AB052E1B0361977406B6B57B32353E9F338BED29E55E2D1F65C4322B5850D45146D5A66BFE8323C0D3E78E55A8945B622E15295B9176454A868399990B31D7B104CF0203010001300D06092A864886F70D01010B05000382010100296101AC1ED80EF9DD845D03F2D1F822B4AEFD50E0A1F47FA98105155327FDA6CE52BCD650BE1EB6DCD7F3CDF73325E85EE979EF0364970ADF6ED3A247B2E3E2D83D877BEBD66B20F3983E8DF8932F82F30C3FAF980ADF72E9FEE30EBAFC42B19FB1EAEC74BAE16E2D4EF245D18B58FB560A64C9B515EA065ECA7AE81D6ED0B97A24636E1E70EE3F2F3A3364C17C6B36BE82588BBED79F23914D4E4E7E1E3FC2A5438FAB0535D37D6FA52009ACD37B6F413700BBF440B6B94E4F12C7F465B8AAC2A03776AAB9AFBAE42FE19664DC0B4E3D8A90EB185529CABE39335AEC58295E1E073A765733410FD769345E9B99C0AA0CBE3FA815661857DCF7EA3BD35EFB4C;RD04916CCC6289600A55118FC37AF0999E;]";
            String cc = ParseASN1Util.parseToken(pediRespose, "CC");
            String cd = ParseASN1Util.parseToken(pediRespose, "CD");
            BASE64Decoder base64Decoder = new BASE64Decoder();
            try {
                String caChain = QPOSUtil.byteArray2Hex(base64Decoder.decodeBuffer(QPOSUtil.readRSANStream(getAssets().open("FX-Dspread-CA-Tree.pem"))));
                //the api callback is onReturnStoreCertificatesResult
                pos.loadCertificates(cc, cd, caChain);
//                Log.e("TAG POS", "is load the server cert to device...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onReturnAnalyseDigEnvelop(String result) {
            super.onReturnAnalyseDigEnvelop(result);
            verifySignatureCommand = ParseASN1Util.addTagToCommand(verifySignatureCommand, "KA", result);
            if (pedvVerifySignatureCommand != null) {
                pedvVerifySignatureCommand = ParseASN1Util.addTagToCommand(pedvVerifySignatureCommand, "KA", result);
                TRACE.i("send key encryption command to RMKS is " + pedvVerifySignatureCommand);
            }
            TRACE.i("send key encryption command to RMKS is " + verifySignatureCommand);
            String response = "[AOPEDK;ANY;KN0;KB30819D0201013181973081941370413031313242315458303045303130304B5331384646464630303030303030303031453030303030414332313038323435443442443733373445443932464142363838373438314544363034344137453635433239463132393739383931384441394434353631443235324143414641020102040AFFFF0000000001E00000020100130A4473707265616442444B04021D58;KA0D0B2F2F3178D4045C1363274890494664B23D32BABEA47E5DB42F15C06816107FD293BAFF7371119F0B11B685A29D40DE78D397F9629A56112629452A9525F5261F8BDCA168328C49ACCFF0133C90E91AFCCA1E18178EBBA5E0BFA054B09514BA87EE05F2E4837D2C74E00BFD3B14EB708598517F357F79AA34C89DFEA9F59B6D3CECABA6C211809400DE9D0B0CA09384FDD834B8BFD416C4B09D32B3F5E45001F18E5C3116A0FFD8E0C6ACE567FCCE1AC909FD038FB58F16BB32163866CD9DCB4B131A394757638111B2CF3DC968D58CBAA95279BEFF697C0D92C6A42248B53A3E56E595AD128EDB50710BDBFFCB113A7DC4ECBCE8668482CBFD22CD7B2E42;RDB077A03C07C94F161842AA0C4831E0EF;CE1;AP308205A906092A864886F70D010702A082059A30820596020149310D300B06096086480165030402013082032506092A864886F70D010703A082031604820312020100318201FB308201F70201003081A830819C310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A56697274754372797074311B3019060355040C0C12447370726561642044657669636573204341311B301906035504410C12447370726561642044657669636573204341311B301906035504030C1244737072656164204465766963657320434102074EB0D600009880304306092A864886F70D0101073036300B0609608648016503040201301806092A864886F70D010108300B0609608648016503040201300D06092A864886F70D01010904000482010092583A07F6280625EE4CA043E3245F2CD6CCA8BAE6E198F4046A5DDE055723D2591A84DDCA4D7F7BB1B179881FD9EC4E33ED22333A9008DAEB3C3B1D7143D1953F2363BEA4C0D2592667C3468F228F856A95A6DCA1FA9CA0AB05D25DC612E7E2BF2AE3012D22C78BB7224C8C8E02146929937C3DF9FA3589B2A486C132477ACFA50BE09528FCBFDA43079AF54C050843BE4BDE701D246D8D8A4C947F12AFD97A66010459BBAE4ED627F687CC3E6DC30B5B35FE3564D9FB07F501B57A73A70AB9C3398E14391B16A5FE45C374984219F0B3A3265A82D3F5A48CEEF3998DCEA59F1CC5821B51605C66C8FD2687778C84B51CCE51C1FBFA876F978E0A9546C425FF3082010C06092A864886F70D010701301406082A864886F70D03070408C8FA8F2094E103118081E85816DF38AEC7C0E569C011DB7212278A767C8934770C7E994E9508E256B693973FBB4B47A78A9F6B1AB2D326CC2A76A53E3731B8A8128B1DE4BEDCCA51E0E740C1A474C21C8CF4A4726F4FBE0DC5CE41C4DB7A2CDBB2EF7B2C0F61B50E34A1A327A5069EB23524DB0D8119C4C407B90277B806288ECAC2826AF8AF6D092B29E90C03554986F38345B6BB247BC1498C2185661BDE318ADECAF199E798D70A058305F686ECC3A267D28EED6052483401EB5B5B84F897CAEA7968B8EEAB23F465CE3F1E7F7F7E402D1AA681D76D34CF9EC0B6BBBE9A513B8C42E5EA5319E218AC996F87767966DBD8F8318202573082025302014930819C308190310B3009060355040613025553310B300906035504080C0254583114301206035504070C0B53414E20414E544F4E494F31133011060355040A0C0A5669727475437279707431173015060355040C0C0E44737072656164204B44482043413117301506035504410C0E44737072656164204B44482043413117301506035504030C0E44737072656164204B444820434102074EB0D60000987E300B0609608648016503040201A0818E301806092A864886F70D010903310B06092A864886F70D0107033020060A2A864886F70D01091903311204104CDCEDD916AAACEEAE548A1C5B0A0EAA301F06092A864886F70D0107013112041041303031364B30544E30304530303030302F06092A864886F70D01090431220420A0E06A133DA8D4A5EC5A2E51E468B470B19E13834019A0C2563BA39308660A1F300D06092A864886F70D0101010500048201003BA0F51DC5B3400E5CD29429663008713C3B61DE0C053590296421635218AEB228A1802C971B18CCF0A137D66FE07B08A0B2A592F11557CC401C353C859E1B82C4BAE146F8AC2955BD1326A3482B173E5589B321FBA0517DCA071F120D0940DC7B8CD33C861E1403CCBD7C3203F1609D261D38B415A0BF234CC9370D18B1004D89BE4C7C4631C7A5D3A1010F0371E25F70B8000D5B94C946571D0F6A730DEF57950AED18839B38B0FF6497D03E960194CF3F113C57575F62E8299FCDE855A1BD36ECE5CAF3DC9F942387A76A329715EC09FDBED3C4FACA06160D538EC00D0166D46152D61F6C665F749E91A0E70E532CE726525B946ACD81510FF47146F00994;]";
            String KA = ParseASN1Util.parseToken(response, "KA");

            KB = ParseASN1Util.parseToken(response, "KB");
            String signatureData = "a57e821386de1038b1a12dc22fa59ce317625680c523bd66bf2b9f840aebe52d020e07105d4107eeb05edd560d0345cd73ce2b68dbf19c61f9d56fbd1ddf9222c47956595b773c88eb7ec4577fb17053d42acf64f3e5c38ff325cdac7b689df029299087b69211e61bdfc22e329eb287456f83ef6c25e84fe1324e36ee85ba7e3accb79eb8ab7b270916a28a42a867e0e050c6950100c90daddb1f421444d16accb6005a312c3273c2f1b28f0c77456ae875081ae594d26139efd267c8dafa15e1b6cf961f3acdb92b26777127f474d24d57611b29f01dec062c02d720c4e759e1757f85ee39e74e05e23aa0aed53d62d05a902a6539a3e986e6dd237888ff92";
            boolean verifyResult = pos.authenticServerResponse(QPOSUtil.HexStringToByteArray(KA), signatureData);
            verifyResult = true;
            if (verifyResult) {
                if (response.contains("AP")) {
                    String AP = ParseASN1Util.parseToken(response, "AP");
                    ParseASN1Util.parseASN1new(AP.replace("A081", "3081"));
                    String nonce = ParseASN1Util.getNonce();
                    String header = ParseASN1Util.getHeader();
                    String digist = ParseASN1Util.getDigest();
                    String encryptData = ParseASN1Util.getEncryptData();
                    ParseASN1Util.parseASN1new(encryptData.substring(6));
                    String signData = ParseASN1Util.getSignData();
                    String encryptDataWith3des = ParseASN1Util.getEncryptDataWith3Des();
                    String IV = ParseASN1Util.getIVStr();
                    String clearData = "A0818e301806092a864886f70d010903310b06092a864886f70d0107033020060a2a864886f70d01091903311204104cdcedd916aaaceeae548a1c5b0a0eaa301f06092a864886f70d0107013112041041303031364b30544e30304530303030302f06092a864886f70d01090431220420a0e06a133da8d4a5ec5a2e51e468b470b19e13834019a0c2563ba39308660a1f";
                    String envelop = getDigitalEnvelopStr(encryptData, encryptDataWith3des, "01", clearData, signData, IV);
                    //the api callback is onRequestUpdateWorkKeyResult
                    pos.loadSessionKeyByTR_34(envelop);
                } else {
                    Log.e("TAG POS", "signature verification successful.");
                    ParseASN1Util.parseASN1new(KB);
                    String data = ParseASN1Util.getTr31Data();
                    //the api callback is onReturnupdateKeyByTR_31Result
                    pos.updateKeyByTR_31(data, 30);
                }
            } else {
                Log.e("TAG POS", "signature verification failed.");
            }
        }
    }

    private String ksn;

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


        String newPinBlock = null;
        try {
            newPinBlock = new TripleDES(mClearTPK, 4).encrypt(response.pan, creditCard.getPIN());
            if (!creditCard.getPINBLOCK().isEmpty()) {
                cPin = newPinBlock;
                Log.e("TAG", "Pinblock Gotten" + newPinBlock);
            } else {
                cPin = "";
            }
            Log.e("TAG", "Pinblock new Gotten" + newPinBlock);
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException(e);
        }

        ksn = ksnUtilitites.getLatestKsn();
        response.ksn = ksn;
        iccDATA = creditCard.getEmvData().getIccData();

//        response.pinBlock = pinBlock;
//        response.terminalId = "2076NA61";
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

    String time = new TimeUtil().getTimehhmmss(new Date(System.currentTimeMillis()));
    String date = new TimeUtil().getDateMMdd(new Date(System.currentTimeMillis()));

    private void stopEmvProcess(TerminalInfo response) {
        AppLog.d("Processing", "Transaction" + "Please wait");
        Log.e("Processing", "Processing Transaction....");
        String mTerminal = new Gson().toJson(response);
        BlusaltTerminalInfo blusaltTerminalInfo = new Gson().fromJson(mTerminal, BlusaltTerminalInfo.class);
        blusaltTerminalInfo.deviceOs = "Android";
        blusaltTerminalInfo.serialNumber = getDeviceMac();
        blusaltTerminalInfo.device = "MPOS " + getDeviceModel();
        blusaltTerminalInfo.currency = "NGN";
        blusaltTerminalInfo.currencyCode = "566";

        LocalData localData = new LocalData(getApplicationContext());
        String getTsk = localData.getTsk();
        String getMerchantId = localData.getMerchantId();
        String getMerchantLoc = localData.getMerchantLoc();
        String getMerchantCategoryCode = localData.getMerchantCategoryCode();
        String getTerminalId = localData.getTerminalId();

        Log.e("Processing", "KeyDownloadResponse: " + getTsk);
        Log.e("Processing", "KeyDownloadResponse: " + getTerminalId);

        response.terminalId = getTerminalId;
        response.sessionKey = getTsk;
        TerminalInformation terminalInformation = new TerminalInformation();
        terminalInformation.merchantID = getMerchantId;
        terminalInformation.merchantNameAndLocation = getMerchantLoc;
        terminalInformation.merchantCategoryCode = getMerchantCategoryCode;
        terminalInformation.posConditionCode = "00";
        terminalInformation.posEntryMode = "051";
        terminalInformation.terminalId = response.terminalId;
        response.terminalInformation = terminalInformation;

        response.processingCode = "000000";
        response.de62 = "MA";
        response.de63 = "05660566";

        String serviceCode = new ValueGenerator().getServiceCode(response.track2, response.pan);
        CardData cardData = new CardData();
        cardData.cardHolderName = response.cardOwner;
        cardData.cardSequenceNumber = response.cardSequenceNumber;
        cardData.expiryDate = response.expiryYear + response.expiryMonth;
        cardData.pan = response.pan;
        cardData.serviceCode = response.cardSequenceNumber;
        cardData.track2Data = response.track2;
        response.cardData = cardData;

        EmvData emvData = new EmvData();
        emvData.pinData = cPin;
        emvData.iccData = response.iccdata;
        response.emvData = emvData;

        response.currencyCode = "566";
        response.currency = "NGN";
        response.deviceOs = "Android";
        response.serialNumber = getDeviceMac();
        response.device = "Mpos " + getDeviceModel();
        String rtt = new Gson().toJson(blusaltTerminalInfo);
//        init("test_57566e7a223f98cf6aebfd093c8f295dd77f74a6690cd24672352c7477ebae336cf759516d2a2f500440686eb96d92121663836633811sk", getApplicationContext());
//        ProcessTransaction(blusaltTerminalInfo);
//        onCompleteTransaction(response);

        TerminalInfoProcessor terminalInfoProcessor = new TerminalInfoProcessor();
        terminalInfoProcessor.AmountAuthorized = response.AmountAuthorized;
        terminalInfoProcessor.cardOwner = response.cardOwner;
        terminalInfoProcessor.cardSequenceNumber = response.cardSequenceNumber;
        terminalInfoProcessor.expiryDate = response.expiryYear + response.expiryMonth;
        terminalInfoProcessor.pan = response.pan;
        terminalInfoProcessor.serviceCode = response.cardSequenceNumber;
        terminalInfoProcessor.track2 = response.track2;
        terminalInfoProcessor.currencyCode = response.currencyCode;
        terminalInfoProcessor.currency = response.currency;
        terminalInfoProcessor.de62 = response.de62;
        terminalInfoProcessor.de63 = response.de63;
        terminalInfoProcessor.iccData = iccDATA;
        terminalInfoProcessor.pinData = cPin;
        terminalInfoProcessor.AmountOther = response.AmountOther;
        terminalInfoProcessor.processingCode = response.processingCode;
        terminalInfoProcessor.rrn = new ValueGenerator().retrievalReferenceNumber();
        terminalInfoProcessor.sessionKey = response.sessionKey;
        terminalInfoProcessor.stan = new ValueGenerator().systemTraceAuditNumber();
        terminalInfoProcessor.merchantCategoryCode = getMerchantCategoryCode;
        terminalInfoProcessor.terminalMerchantID = getMerchantId;
        terminalInfoProcessor.merchantNameAndLocation = getMerchantLoc;
        terminalInfoProcessor.posConditionCode = "00";
        terminalInfoProcessor.posEntryMode = "051";
        terminalInfoProcessor.terminalId = response.terminalId;
        terminalInfoProcessor.TransactionDate = date;
        terminalInfoProcessor.transactionDateTime = date + time;
        terminalInfoProcessor.transactionTime = time;
        terminalInfoProcessor.responseCode = "00";
        terminalInfoProcessor.responseDescription = "Data collected successfully";
        Log.e("PROCESSOR DATA ", new Gson().toJson(terminalInfoProcessor));

        BlusaltTerminalInfoProcessor blusaltTerminalInfoProcessor = new BlusaltTerminalInfoProcessor();
        blusaltTerminalInfoProcessor.AmountAuthorized = response.AmountAuthorized;
        blusaltTerminalInfoProcessor.cardOwner = response.cardOwner;
        blusaltTerminalInfoProcessor.cardSequenceNumber = response.cardSequenceNumber;
        blusaltTerminalInfoProcessor.expiryDate = response.expiryYear + response.expiryMonth;
        blusaltTerminalInfoProcessor.pan = response.pan;
        blusaltTerminalInfoProcessor.serviceCode = response.cardSequenceNumber;
        blusaltTerminalInfoProcessor.track2 = response.track2;
        blusaltTerminalInfoProcessor.currencyCode = response.currencyCode;
        blusaltTerminalInfoProcessor.currency = response.currency;
        blusaltTerminalInfoProcessor.de62 = response.de62;
        blusaltTerminalInfoProcessor.de63 = response.de63;
        blusaltTerminalInfoProcessor.iccData = iccDATA;
        blusaltTerminalInfoProcessor.pinData = cPin;
        blusaltTerminalInfoProcessor.AmountOther = response.AmountOther;
        blusaltTerminalInfoProcessor.processingCode = response.processingCode;
        blusaltTerminalInfoProcessor.rrn = new ValueGenerator().retrievalReferenceNumber();
        blusaltTerminalInfoProcessor.sessionKey = response.sessionKey;
        blusaltTerminalInfoProcessor.stan = new ValueGenerator().systemTraceAuditNumber();
        blusaltTerminalInfoProcessor.merchantCategoryCode = getMerchantCategoryCode;
        blusaltTerminalInfoProcessor.terminalMerchantID = getMerchantId;
        blusaltTerminalInfoProcessor.merchantNameAndLocation = getMerchantLoc;
        blusaltTerminalInfoProcessor.posConditionCode = "00";
        blusaltTerminalInfoProcessor.posEntryMode = "051";
        blusaltTerminalInfoProcessor.terminalId = response.terminalId;
        blusaltTerminalInfoProcessor.TransactionDate = date;
        blusaltTerminalInfoProcessor.transactionDateTime = date + time;
        blusaltTerminalInfoProcessor.transactionTime = time;
        Log.e("PROCESSORTransactionData", new Gson().toJson(blusaltTerminalInfoProcessor));

        ProcessProcessorTransaction(blusaltTerminalInfoProcessor);
//        onCompleteTransaction(terminalInfoProcessor);
    }

    public static void init(String secretKey, Context context, TerminalKeyParamDownloadListener listener) {
        if (!TextUtils.isEmpty(secretKey)) {
            try {
                new Pos().init(context.getApplicationContext());
                mlistener = listener;

                MemoryManager.getInstance().putUserSecretKey(secretKey);
            } catch (Exception e) {
                AppLog.e("prepareForPrinter", e.getMessage());
            }
        } else {
            AppLog.e("init", "Secret Key is Empty");
        }
    }


    TerminalKeyParamDownloadListener listener = new TerminalKeyParamDownloadListener() {
        @Override
        public void onSuccess(String message) {
            Log.e("TAG: ", "Result: " + message);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed(String error) {
            Log.e("TAG: ","Result: " + error);
            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
        }
    };

    public void clearData(Context context) {
        new Pos().init(context.getApplicationContext());
        SharedPreferencesUtils.getInstance().clear();
    }

    public void downloadConfigurations(String serial, Context context, TerminalKeyParamDownloadListener listener) {
        try {
            AppExecutors.getInstance().diskIO().execute(() -> {
                mlistener = listener;
                localData = new LocalData(context);
                Log.e("Check Terminal Serial", serial);
                downloadTerminalParam(serial);

            });

        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void ProcessProcessorTransaction(BlusaltTerminalInfoProcessor blusaltTerminalInfoProcessor) {
        RetrofitClientInstance.getInstance().getDataService().postTransactionToProcessor(blusaltTerminalInfoProcessor).enqueue(new Callback<TerminalResponse>() {
            @Override
            public void onResponse(@NonNull Call<TerminalResponse> call, @NonNull Response<TerminalResponse> response) {
                Log.e("ProcessTransaction", "ProcessTransaction" + response);
                TerminalResponse terminalResponse = new TerminalResponse("card payment failed", "01", "Unable to process transaction");
                if (response.isSuccessful()) {
                    Log.e("ProcessTransaction", "ProcessTransaction response.body()" + response.body());

                    if (response.body().message.contains("Access denied! invalid apiKey passed")) {
                        terminalResponse.responseCode = "01";
                        terminalResponse.responseDescription = "Access denied! invalid apiKey passed";

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
                terminalResponse.message = t.getMessage();
                terminalResponse.responseCode = "02";
                terminalResponse.responseDescription = "Unable to connect to the server";
                apiResponseCall(terminalResponse);
            }
        });
    }


    public void downloadTerminalParam(String serialNumber) {
        Log.e("ProcessKeyDownload", "ProcessKeyDownload");
        RetrofitClientInstanceParam.getInstance().getDataService().downloadTerminalParam(serialNumber).enqueue(new Callback<BaseData<ParamDownloadResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseData<ParamDownloadResponse>> call, @NonNull Response<BaseData<ParamDownloadResponse>> response) {
                Log.e("ProcessKeyDownload", "response: " + response);
                ParamDownloadResponse paramDownloadResponse = new ParamDownloadResponse();

                if (response.isSuccessful()) {

                    try {
                        paramDownloadResponse = Objects.requireNonNull(response.body()).getData();

                        Log.e("ProcessKeyDownload", new Gson().toJson(response.body().getData()));
                        Log.e("ProcessKeyDownload", "isSuccessful" + response.code() + response.message());

                        KeyDownloadRequest keyDownloadRequest = new KeyDownloadRequest();
                        keyDownloadRequest.terminalId = paramDownloadResponse.terminalId.toString();
                        ProcessKeyDownload(keyDownloadRequest);
                        SharedPreferencesUtils.getInstance().setValue(Constants.INTENT_TERMINAL_CONFIG, true);

                        listener.onSuccess("Terminal Parameter Downloaded");
//                            localData.setMerchantId(keyDownloadResponse.downloadParameter.merchantId);
//                            localData.setTsk(keyDownloadResponse.sessionKey);
//                            localData.setMerchantLoc(keyDownloadResponse.downloadParameter.merchantNameAndLocation);
//                            localData.setMerchantCategoryCode(keyDownloadResponse.downloadParameter.merchantCategoryCode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    Log.e("TAG", "TerminalParameterFailed " + response.code());
                    BufferedReader reader = null;
                    StringBuilder sb = new StringBuilder();
                    reader = new BufferedReader(new InputStreamReader(response.errorBody().byteStream()));
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        String finallyError = sb.toString();
                        Log.e("TAG", "TerminalParameterFailed " + finallyError);

                        ModelError modelError = new Gson().fromJson(finallyError, ModelError.class);
                        Log.e("TAG", "TerminalParameterFailed: " + new Gson().toJson(modelError));
                        Log.e("TAG", "TerminalParameterFailed: " + modelError.getMessage());

                        SharedPreferencesUtils.getInstance().setValue(Constants.INTENT_TERMINAL_CONFIG, false);

                        listener.onFailed("Terminal Parameter Failed: " + modelError.getMessage());

//                        Gson gson = new Gson();
//
//                        ModelError modelError = new ModelError();
//                        Type type = new TypeToken<ModelError>() {
//                        }.getType();
//
//                        modelError = gson.fromJson(finallyError, type);
//                        Log.e("ProcessKeyDownload", "ProcessKeyDownload failed" + modelError.getMessage());
//
                    } catch (Exception e) {
                        Log.e("ProcessKeyDownload", "error" + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseData<ParamDownloadResponse>> call, @NonNull Throwable t) {
                SharedPreferencesUtils.getInstance().setValue(Constants.INTENT_TERMINAL_CONFIG, false);
                listener.onFailed("Terminal Parameter Failed: " + t.getMessage());

                KeyDownloadResponse keyDownloadResponse = new KeyDownloadResponse();
                Log.e("ProcessKeyDownload", "onFailure" + t.getMessage());
                Log.e("ProcessKeyDownload", "onFailure" + keyDownloadResponse);
            }
        });
    }


    public void ProcessKeyDownload(KeyDownloadRequest keyDownloadRequest) {
        Log.e("ProcessKeyDownload", "ProcessKeyDownload");
        RetrofitClientInstanceProcessor.getInstance().getDataService().downloadKeyExchangeFromProcessor(keyDownloadRequest).enqueue(new Callback<BaseData<KeyDownloadResponse>>() {
            @Override
            public void onResponse(@NonNull Call<BaseData<KeyDownloadResponse>> call, @NonNull Response<BaseData<KeyDownloadResponse>> response) {
                Log.e("ProcessKeyDownload", "response: " + response);
                KeyDownloadResponse keyDownloadResponse = new KeyDownloadResponse();

                if (response.isSuccessful()) {

                    if (response.body().getMessage().contains("Access denied! invalid apiKey passed")) {

                    } else {

                        try {

                            keyDownloadResponse = Objects.requireNonNull(response.body()).getData();

                            Log.e("ProcessKeyDownload", new Gson().toJson(response.body().getData()));
                            Log.e("ProcessKeyDownload", "isSuccessful" + new Gson().toJson(keyDownloadResponse));

                            String clearTMK = TripleDES.threeDesDecrypt(keyDownloadResponse.masterKey, "11111111111111111111111111111111");
                            Log.e("Tmk", clearTMK.toString());
                            Log.e("ClearTmk", keyDownloadResponse.masterKey);
//                        int retTMK = securityKeyManager.saveTMK("75EEF0E4ECD345089A9E22CA41EFC735", "5451BC0B64F146435BBF320ED579C4AE");

                            String clearTPK = TripleDES.threeDesDecrypt(keyDownloadResponse.pinKey, keyDownloadResponse.masterKey);
                            Log.e("clearTPK", clearTPK.toString());
                            mClearTPK = clearTPK.toString();
//                        int retTPK = securityKeyManager.saveTPK("3773E02C70A7B6C20BCDC7F1FDCECB57");

                            String clearTSK = TripleDES.threeDesDecrypt(keyDownloadResponse.sessionKey, clearTMK);
                            Log.e("clearTSK", clearTSK.toString());

                            merchantId = keyDownloadResponse.downloadParameter.merchantId;
                            tsk = keyDownloadResponse.sessionKey;
                            merchantLoc = keyDownloadResponse.downloadParameter.merchantNameAndLocation;
                            merchantCategoryCode = keyDownloadResponse.downloadParameter.merchantCategoryCode;
                            mKeyDownloadResponse = keyDownloadResponse;
                            terminalId = keyDownloadResponse.terminalId;
                            listener.onSuccess("Terminal Configuration Successful");


                            if (merchantId != null) {
                                Log.e("KeyDownlaod", merchantId);
                                Log.e("KeyDownlaod", tsk);
                                Log.e("KeyDownlaod", merchantLoc);
                                Log.e("KeyDownlaod", merchantCategoryCode);
                                Log.e("KeyDownlaod", terminalId);

                                localData.setMerchantId(merchantId);
                                localData.setTsk(tsk);
                                localData.setMerchantLoc(merchantLoc);
                                localData.setMerchantCategoryCode(merchantCategoryCode);
                                localData.setTerminalId(terminalId);
                            }

                            SharedPreferencesUtils.getInstance().setValue(Constants.INTENT_KEY_CONFIG, true);
                            Log.e("Okayy", "E reach " + merchantId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    try {
                        Gson gson = new Gson();
                        Type type = new TypeToken<KeyDownloadResponse>() {
                        }.getType();

                        keyDownloadResponse = gson.fromJson(response.errorBody().charStream(), type);
                        Log.e("ProcessTransaction", "ProcessTransaction failed" + new Gson().toJson(keyDownloadResponse));
                        SharedPreferencesUtils.getInstance().setValue(Constants.INTENT_KEY_CONFIG, false);
                        listener.onFailed("Terminal Configuration Failed");

                    } catch (Exception e) {
                        Log.e("ProcessKeyDownload", "error" + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<BaseData<KeyDownloadResponse>> call, @NonNull Throwable t) {
                SharedPreferencesUtils.getInstance().setValue(Constants.INTENT_KEY_CONFIG, false);
                listener.onFailed("Terminal Configuration Failed");

                KeyDownloadResponse keyDownloadResponse = new KeyDownloadResponse();
                Log.e("ProcessKeyDownload", "onFailure" + t.getMessage());
                Log.e("ProcessKeyDownload", "onFailure" + keyDownloadResponse);
            }
        });
    }

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
            dialog = new Dialog(MposMainActivity.this);
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

            dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {

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

        Intent intent = new Intent(MposMainActivity.this, TransactionStatus.class);
        String fullPay = new Gson().toJson(terminalResponse);
        intent.putExtra("result", fullPay);
        startActivity(intent);

        close();
//        intent.putExtra(getString(com.blusalt.blusaltmpos.R.string.data), fullPay);
//        // prepareForPrinters(PosActivity.this,terminalResponse);
//        finishTransaction(intent);
    }

    private void finishTransaction(Intent intent) {
        setResult(RESULT_OK, intent);
        finish();
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
//        terminalInfo.responseCode = _responseCode;
//        terminalInfo.responseDescription = "Data collected successfully";
        terminalInfo.TerminalName = "mpos";
        TlvDataList tlvDataList = null;
        String tlv = creditCard.getEmvData().getIccData();
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
            Log.e("Tag countryCode", countryCode.substring(11, countryCode.length() - 1));

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
            terminalInfo.iccData = tlv;
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
            terminalInfo.track2 = strTrack2;
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


    private void deviceShowDisplay(String diplay) {
        Log.e("execut start:", "deviceShowDisplay");
        String customDisplayString = "";
        try {
            byte[] paras = diplay.getBytes("GBK");
            customDisplayString = QPOSUtil.byteArray2Hex(paras);
            pos.lcdShowCustomDisplay(LcdModeAlign.LCD_MODE_ALIGNCENTER, customDisplayString, 60);
        } catch (Exception e) {
            e.printStackTrace();
            TRACE.d("gbk error");
            Log.e("execut error:", "deviceShowDisplay");
        }
        Log.e("execut end:", "deviceShowDisplay");
    }

    private String transformDevice(UsbDevice usbDevice) {
        String deviceName = new String();
        UsbManager mManager = (UsbManager) MposMainActivity.this.getSystemService(USB_SERVICE);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(MposMainActivity.this, 0, new Intent(
                "com.android.example.USB_PERMISSION"), 0);
        mManager.requestPermission(usbDevice, mPermissionIntent);
        UsbDeviceConnection connection = mManager.openDevice(usbDevice);
        byte rawBuf[] = new byte[255];
        int len = connection.controlTransfer(0x80, 0x06, 0x0302,
                0x0409, rawBuf, 0x00FF, 60);
        rawBuf = Arrays.copyOfRange(rawBuf, 2, len);
        deviceName = new String(rawBuf);
        return deviceName;
    }

    private void devicePermissionRequest(UsbManager mManager, UsbDevice usbDevice) {
        PendingIntent mPermissionIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            mPermissionIntent = PendingIntent.getBroadcast(MposMainActivity.this, 0, new Intent(
                    "com.android.example.USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE);
        } else {
            mPermissionIntent = PendingIntent.getBroadcast(MposMainActivity.this, 0, new Intent(
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
                    MposMainActivity.this.unregisterReceiver(mUsbReceiver);
                }
            }
        }
    };

    private List getPermissionDeviceList() {
        UsbManager mManager = (UsbManager) MposMainActivity.this.getSystemService(USB_SERVICE);
        List deviceList = new ArrayList<UsbDevice>();
        // check for existing devices
        for (UsbDevice device : mManager.getDeviceList().values()) {
            deviceList.add(device);
        }
        return deviceList;
    }

    private void clearDisplay() {
        Log.e("TAG POS", "");
    }

    private String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
    private TransactionType transactionType;

    class MyOnClickListener implements OnClickListener {
        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            Log.e("TAG POS", "");
            if (selectBTFlag) {
                Log.e("TAG POS", String.valueOf(R.string.wait));
                return;
            } else if (v == doTradeButton) {
                if (pos == null) {
                    Log.e("TAG POS", String.valueOf(R.string.scan_bt_pos_error));
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
                        Log.e("TAG POS", String.valueOf(R.string.starting));
                        terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
                        if (posType == POS_TYPE.UART) {
                            pos.doTrade(terminalTime, 0, 30);
                        } else {
                            int keyIdex = getKeyIndex();
                            pos.doTrade(keyIdex, 30);//start do trade
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Please connect to the Internet", Toast.LENGTH_SHORT).show();
                    }


                } else {

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
                        pos = QPOSService.getInstance(CommunicationMode.BLUETOOTH);
                        pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("NIGERIA-QPOS cute,CR100,D20,D30.xml", MposMainActivity.this)));
                        Log.e("TAG open", "updating...");
                    } else {
                        pos.updateEMVConfigByXml(new String(FileUtils.readAssetsLine("NIGERIA-QPOS cute,CR100,D20,D30.xml", MposMainActivity.this)));
                        Log.e("TAG open", "updating...");
                    }
                }


            } else if (v == btnUSB) {
                USBClass usb = new USBClass();
                ArrayList<String> deviceList = usb.GetUSBDevices(getBaseContext());
                if (deviceList == null) {
                    Toast.makeText(MposMainActivity.this, "No Permission", Toast.LENGTH_SHORT).show();
                    return;
                }
                final CharSequence[] items = deviceList.toArray(new CharSequence[deviceList.size()]);
                AlertDialog.Builder builder = new AlertDialog.Builder(MposMainActivity.this);
                builder.setTitle("Select a Reader");
                builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        String selectedDevice = (String) items[item];

                        dialog.dismiss();

                        usbDevice = USBClass.getMdevices().get(selectedDevice);
                        open(CommunicationMode.USB_OTG_CDC_ACM);
                        posType = POS_TYPE.OTG;
                        pos.openUsb(usbDevice);
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else if (v == btnBT) {
                TRACE.d("type==" + type);
                pos = null;//reset the pos
                if (pos == null) {
                    if (type == 3) {
                        open(CommunicationMode.BLUETOOTH);
                        posType = POS_TYPE.BLUETOOTH;
                    } else if (type == 4) {
                        open(CommunicationMode.BLUETOOTH_BLE);
                        posType = POS_TYPE.BLUETOOTH_BLE;
                    }
                }
                pos.clearBluetoothBuffer();
                if (isNormalBlu) {
                    TRACE.d("begin scan====");
                    pos.scanQPos2Mode(MposMainActivity.this, 20);
                } else {
                    pos.startScanQposBLE(6);
                }
                animScan.start();
                imvAnimScan.setVisibility(View.VISIBLE);
                refreshAdapter();
                if (m_Adapter != null) {
                    TRACE.d("+++++=" + m_Adapter);
                    m_Adapter.notifyDataSetChanged();
                }
            } else if (v == btnDisconnect) {
                close();
            } else if (v == continueBtn) {
                validateImage.setVisibility(View.VISIBLE);
                validate_text.setVisibility(View.VISIBLE);
                about_to_text.setVisibility(View.VISIBLE);
                validate_amount_text.setVisibility(View.VISIBLE);
                validate_amount_text.setText(appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT));
                doTradeButton.setVisibility(View.VISIBLE);
                doTradeButton.setEnabled(true);


                continueBtn.setVisibility(View.INVISIBLE);
                bluetoothImage.setVisibility(View.INVISIBLE);
                connectedText.setVisibility(View.INVISIBLE);

            } else if (v == btnQuickEMV) {
                Log.e("TAG POS", "updating emv config, please wait...");
                updateEmvConfig();
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
            } else if (v == updateFwBtn) {//update firmware
//                isUpdateFw = true;
//                pos.getUpdateCheckValue();
                updateFirmware();

            } else if (v == hometoolbar) {//update firmware
//                isUpdateFw = true;
//                pos.getUpdateCheckValue();
                finish();
            }
        }
    }

    private int getKeyIndex() {
        String s = mKeyIndex.getText().toString();
        if (TextUtils.isEmpty(s)) {
            return 0;
        }
        int i = 0;
        try {
            i = Integer.parseInt(s);
            if (i > 9 || i < 0) {
                i = 0;
            }
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
    private long start_time = 0L;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
//                    btnBT.setEnabled(false);
//                    btnQuickEMV.setEnabled(false);
//                    doTradeButton.setEnabled(false);
                    selectBTFlag = true;
                    Log.e("TAG POS", String.valueOf(R.string.connecting_bt_pos));
                    sendMsg(1002);
                    break;
                case 1002:
                    if (isNormalBlu) {
                        pos.connectBluetoothDevice(true, 25, blueTootchAddress);
                    } else {
                        pos.connectBLE(blueTootchAddress);
                    }
//                    btnBT.setEnabled(true);
                    selectBTFlag = false;
                    break;
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
                    Log.e("TAG POS", content);
                    break;
                case 1703:
//                    int keyIndex = getKeyIndex();
//                    String digEnvelopStr = null;
//                    Poskeys posKeys = null;
//                    try {
//                        if (resetIpekFlag) {
//                            posKeys = new DukptKeys();
//                        }
//                        if (resetMasterKeyFlag) {
//                            posKeys = new TMKKey();
//                        }
//                        posKeys.setRSA_public_key(pubModel); //Model of device public key
//                        digEnvelopStr = Envelope.getDigitalEnvelopStrByKey(getAssets().open("priva.pem"),
//                                posKeys, Poskeys.RSA_KEY_LEN.RSA_KEY_1024, keyIndex);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    pos.updateWorkKey(digEnvelopStr);
//                    break;
                default:
                    break;
            }
        }
    };

    public void updateEmvConfig() {
//      update emv config by bin files
        String emvAppCfg = QPOSUtil.byteArray2Hex(FileUtils.readAssetsLine("emv_app.bin", MposMainActivity.this));
        String emvCapkCfg = QPOSUtil.byteArray2Hex(FileUtils.readAssetsLine("emv_capk.bin", MposMainActivity.this));
        TRACE.d("emvAppCfg: " + emvAppCfg);
        TRACE.d("emvCapkCfg: " + emvCapkCfg);
        pos.updateEmvConfig(emvAppCfg, emvCapkCfg);
    }

    public void updateFirmware() {
        if (ActivityCompat.checkSelfPermission(MposMainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //request permission
            ActivityCompat.requestPermissions(MposMainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
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
                            Toast.makeText(MposMainActivity.this, "Upgrade package path:" +
                                    Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "dspread" + File.separator + fileName, Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                }
            }
            if (data == null || data.length == 0) {
                data = FileUtils.readAssetsLine("upgrader.asc", MposMainActivity.this);
            }
            int a = pos.updatePosFirmware(data, blueTootchAddress);
            if (a == -1) {
//                isUpdateFw = false;
                Toast.makeText(MposMainActivity.this, "please keep the device charging", Toast.LENGTH_LONG).show();
                return;
            }
            updateThread = new UpdateThread();
            updateThread.start();
        }
    }

    /*---------------------------------------------*/

    private static final String FILENAME = "dsp_axdd";

}
