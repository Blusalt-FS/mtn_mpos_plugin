package com.dspread.blusalt.blusaltmpos.pos.bean;

import androidx.annotation.Keep;


import com.dspread.blusalt.blusaltmpos.util.TransactionResponse;

import java.io.Serializable;

@Keep
public class RavenEmv implements Serializable {
    public TransactionResponse dataModel;

    public  RavenEmv(){

    }
}