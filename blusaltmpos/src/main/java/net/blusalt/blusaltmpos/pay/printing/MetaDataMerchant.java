package net.blusalt.blusaltmpos.pay.printing;

import java.io.Serializable;

public class MetaDataMerchant  implements Serializable {
    public  String email;
    public  String name;
    public  String logo;
    public String agentRef;
    public BankSource source_account;



}
