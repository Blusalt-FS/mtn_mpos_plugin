package net.blusalt.mposplugin.blusaltmpos.pay.printing;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class MerchantDetails  implements Serializable {
    public String name;
    public String address;
}
