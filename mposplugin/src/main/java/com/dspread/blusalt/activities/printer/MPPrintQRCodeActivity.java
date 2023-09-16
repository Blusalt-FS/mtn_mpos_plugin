package com.dspread.blusalt.activities.printer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.action.printerservice.barcode.Barcode2D;
import com.dspread.blusalt.R;
import com.dspread.blusalt.utils.QRCodeUtil;
import com.dspread.blusalt.utils.TRACE;
import com.dspread.blusalt.view.BitmapPrintLine;
import com.dspread.blusalt.view.PrintLine;
import com.dspread.blusalt.view.PrinterLayout;
import com.dspread.blusalt.view.TextPrintLine;

public class MPPrintQRCodeActivity extends CommonActivity {
    private EditText etQRCode, etSize;
    private Spinner spErrLevel;
    private Spinner spAlignment;
    private String qrcontent = "";
    private int size = 72;
    private Spinner mSpinnerSpGreyLevel, mSpinnerSpAlignment;
    private int bitMapPosition;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    private int position = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.mp_qrcode_print));
        etQRCode = findViewById(R.id.et_qrcode_content);
        etSize = findViewById(R.id.et_qrcode_size);
        mSpinnerSpGreyLevel = findViewById(R.id.sp_grey_level);
        findViewById(R.id.align_area).setVisibility(View.VISIBLE);
        mSpinnerSpAlignment = findViewById(R.id.sp_alignment);
        mSpinnerSpAlignment.setSelection(1);
        mSpinnerSpAlignment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getAdapter().getItem(position);
                if (item.equals("Left")) {
                    setPosition(0);
                } else if (item.equals("Right")) {
                    setPosition(2);
                } else if (item.equals("Center")) {
                    setPosition(1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpinnerSpGreyLevel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getAdapter().getItem(position);
                try {
                    mPrinter.setPrintDensity(Integer.parseInt(item));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    @Override
    public void onToolbarLinstener() {
        finish();

    }

    @Override
    int getLayoutId() {
        return R.layout.activity_mpprint_qrcode;
    }

    @Override
    int printTest() throws RemoteException {
        PrinterLayout printerLayout = new PrinterLayout(MPPrintQRCodeActivity.this);
        int size = Integer.parseInt(getText(etSize));
        Bitmap qrcodeBM = QRCodeUtil.getQrcodeBM(getText(etQRCode), size);
        switch (getPosition()) {
            case 0:
                bitMapPosition = PrintLine.LEFT;
                break;
            case 1:
                bitMapPosition = PrintLine.CENTER;
                break;
            case 2:
                bitMapPosition = PrintLine.RIGHT;
                break;
        }
        BitmapPrintLine bitmapPrintLine2 = new BitmapPrintLine(qrcodeBM, bitMapPosition);
        printerLayout.addBitmap(bitmapPrintLine2);
        TextPrintLine textPrintLine = new TextPrintLine();
        textPrintLine.setContent("\n");
        textPrintLine.setPosition(PrintLine.CENTER);
        printerLayout.addText(textPrintLine);
        Bitmap bitmap = printerLayout.viewToBitmap();
        // mPrinter.printBitmap(this, bitmap);
        mPrinter.printQRCode(this, Barcode2D.ErrorLevel.L.name(), size, getText(etQRCode), bitMapPosition);
        return 0;
    }

    @Override
    void onPrintFinished(boolean isSuccess, String status, int type) {
        TRACE.d("onPrintFinished:" + isSuccess + "---" + "status:" + status);
        if (status != null) {

        }

    }

    @Override
    void onPrintError(boolean isSuccess, String status, int type) {

    }

    private String getText(EditText etText) {
        if (etText.getText() != null) {
            return etText.getText().toString().trim();
        } else {
            return "";
        }
    }
}
