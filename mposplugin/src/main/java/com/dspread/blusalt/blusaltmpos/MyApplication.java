package com.dspread.blusalt.blusaltmpos;

import android.app.Application;

import androidx.annotation.Keep;

@Keep
public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private static MyApplication INSTANCE;

    public static MyApplication getINSTANCE(){
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        // AppLog.debug(true);
    }


}

