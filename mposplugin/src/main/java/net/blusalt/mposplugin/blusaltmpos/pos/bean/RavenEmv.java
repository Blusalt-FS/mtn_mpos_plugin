package net.blusalt.mposplugin.blusaltmpos.pos.bean;

import androidx.annotation.Keep;


import net.blusalt.mposplugin.blusaltmpos.util.TransactionResponse;

import java.io.Serializable;

@Keep
public class RavenEmv implements Serializable {
    public TransactionResponse dataModel;

    public  RavenEmv(){

    }
}
