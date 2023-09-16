package com.blusalt.blusaltmpos.pay.printing;

import androidx.annotation.Keep;
import com.blusalt.blusaltmpos.pay.DesirailizeGeneric;
import com.blusalt.blusaltmpos.pay.TerminalResponse;
import java.io.Serializable;

@Keep
public class BluSaltPrinter implements Serializable {
   public DesirailizeGeneric transDetail; // Transaction Detail
   public MerchantDetails merchantDetails;
   public  PrinterType printerType;
   public  BankTransfer bankTransfer; // Bank transfer & Ussd
   public TerminalResponse posResponse; // Pos transaction

   public ResAccTransactionData resAccTransactionData; // Reserve Account
   public  CashRecord cashRecord; // Card Record
   public  String transactionDate; // Card Record
   public  String transactionSource; // Card Record
   public  String supportPhoneNumber; // Card Record
   public  boolean isMerchantCopy; // Card Record

   public  BluSaltPrinter(){}

}
