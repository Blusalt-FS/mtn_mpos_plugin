package com.dspread.blusalt.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.dspread.blusalt.R
import com.dspread.blusalt.blusaltmpos.pay.TerminalResponse
import com.dspread.blusalt.databinding.ActivityTransactionStatusBinding
import com.google.gson.Gson

var activityTransactionStatusBinding: ActivityTransactionStatusBinding? = null

class TransactionStatus : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_status)

        activityTransactionStatusBinding = ActivityTransactionStatusBinding.inflate(layoutInflater)
        val view: View = activityTransactionStatusBinding!!.root
        setContentView(view)

        var intent : Intent = intent
        val result : String? = intent.getStringExtra("result")
        Log.e("TAG response", result.toString())

        val response: TerminalResponse = Gson().fromJson(
            result,
            TerminalResponse::class.java
        )

        if (result != null) {
            Log.e("TAG response", Gson().toJson(response))
            Log.e("TAG amount", response.data.receiptInfo.transactionAmount)
            activityTransactionStatusBinding!!.amountText.setText("₦ " + response.data.receiptInfo.transactionAmount + ".00")
            if (response.data.posResponseCode.equals("00")) {
                activityTransactionStatusBinding!!.statusText.setText("Transaction Approved")
            } else {
                activityTransactionStatusBinding!!.statusText.setText("Transaction Declined")
            }
            activityTransactionStatusBinding!!.terminalIdTxtValue.setText(response.data.receiptInfo.merchantTID)
            activityTransactionStatusBinding!!.cardholderNameTxtValue.setText(response.data.receiptInfo.customerCardName)
            activityTransactionStatusBinding!!.rrnTxtValue.setText(response.data.receiptInfo.rrn)
            activityTransactionStatusBinding!!.cardNumberTxtValue.setText(response.data.receiptInfo.customerCardPan)
        }

        activityTransactionStatusBinding!!.backButton.setOnClickListener {
            intent = Intent(this, AmountEntryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }

    }
}