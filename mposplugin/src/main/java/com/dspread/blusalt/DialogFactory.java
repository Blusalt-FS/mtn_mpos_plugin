package com.dspread.blusalt;

import android.content.Context;

import com.dspread.blusalt.activities.mp5801.LoadingDialog;

public class DialogFactory {

    private static LoadingDialog loadingDialog;

    private DialogFactory() {

    }

    public static void showLoadingDialog(Context context) {
        if (null == loadingDialog) {
            loadingDialog = new LoadingDialog(context);
        }
        loadingDialog.show();
    }

    public static void dismissLoadingDialog() {
        if (null != loadingDialog) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}
