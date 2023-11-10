package net.blusalt.mposplugin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import net.blusalt.mposplugin.R
import net.blusalt.mposplugin.blusaltmpos.pay.TerminalResponse
import net.blusalt.mposplugin.blusaltmpos.util.Constants
import net.blusalt.mposplugin.databinding.ActivityTransactionStatusBinding
import com.google.gson.Gson

var activityTransactionStatusBinding: ActivityTransactionStatusBinding? = null

class TransactionStatus : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_status)

        activityTransactionStatusBinding = ActivityTransactionStatusBinding.inflate(layoutInflater)
        val view: View = activityTransactionStatusBinding!!.root
        setContentView(view)

        val intent : Intent = intent
        val result : String? = intent.getStringExtra("result")
        Log.e("TAG response", result.toString())

        val response: TerminalResponse = Gson().fromJson(
            result,
            TerminalResponse::class.java
        )

        if (result != null) {
            Log.e("TAG response", Gson().toJson(response))
//            Log.e("TAG amount", response.data.receiptInfo.transactionAmount)
            if (response.message != "card payment failed") {
                activityTransactionStatusBinding?.amountText?.setText("₦ " + response.data.receiptInfo.transactionAmount + ".00")
                if (response.data.posResponseCode.equals("00")) {
                    activityTransactionStatusBinding?.statusText?.setText("Transaction Approved")
                } else {
                    activityTransactionStatusBinding?.statusText?.setText("Transaction Declined")
                }
                activityTransactionStatusBinding?.terminalIdTxtValue?.setText(response.data.receiptInfo.merchantTID)
                activityTransactionStatusBinding?.cardholderNameTxtValue?.setText(response.data.receiptInfo.customerCardName)
                activityTransactionStatusBinding?.rrnTxtValue?.setText(response.data.receiptInfo.rrn)
                activityTransactionStatusBinding?.cardNumberTxtValue?.setText(response.data.receiptInfo.customerCardPan)
            }
        }

        activityTransactionStatusBinding!!.backButton.setOnClickListener {

            appPreferenceHelper?.setSharedPreferenceString(
                Constants.TRANSACTION_RESPONSE,
                response.data.posResponseCode ?: ""
            )

            val intent = Intent(this@TransactionStatus, AmountEntryActivity::class.java)
            intent.putExtra("responseCode", response.data.posResponseCode)
//            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT;

            startActivity(intent)
        }

        activityTransactionStatusBinding!!.shareReceiptButton.setOnClickListener {
            val intent = Intent(this@TransactionStatus, TransactionDetail::class.java)
            intent.putExtra("result", result)
            startActivity(intent)
        }

    }
}