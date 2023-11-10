package net.blusalt.blusaltmpos.pos.bean;

import androidx.annotation.Keep;


import net.blusalt.blusaltmpos.util.TransactionResponse;

import java.io.Serializable;

@Keep
public class RavenEmv implements Serializable {
    public TransactionResponse dataModel;

    public  RavenEmv(){

    }
}
