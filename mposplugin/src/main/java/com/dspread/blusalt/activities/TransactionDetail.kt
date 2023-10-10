package com.dspread.blusalt.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.dspread.blusalt.R
import com.dspread.blusalt.blusaltmpos.pay.TerminalResponse
import com.dspread.blusalt.databinding.ActivityTransactionDetailBinding
import com.google.gson.Gson
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

var activityTransactionDetailBinding: ActivityTransactionDetailBinding? = null

class TransactionDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_transaction_detail)

        activityTransactionDetailBinding = ActivityTransactionDetailBinding.inflate(layoutInflater)
        val view: View = activityTransactionDetailBinding!!.root
        setContentView(view)

        var intent: Intent = intent
        val result: String? = intent.getStringExtra("result")
        Log.e("TAG response", result.toString())

        val response: TerminalResponse = Gson().fromJson(
            result,
            TerminalResponse::class.java
        )


        activityTransactionDetailBinding!!.toolbar.setOnClickListener {
            finish()
        }


        if (result != null) {
            Log.e("TAG response", Gson().toJson(response))
            Log.e("TAG amount", response.data.receiptInfo.transactionAmount)

            activityTransactionDetailBinding!!.receiptValue.setText(response.data.receiptInfo.reference)
            activityTransactionDetailBinding!!.terminalValue.setText(response.data.receiptInfo.merchantTID)
            activityTransactionDetailBinding!!.dateValue.setText(response.data.receiptInfo.transactionDate + " " + response.data.receiptInfo.transactionTime)
            activityTransactionDetailBinding!!.cardValue.setText(response.data.cardScheme)
            activityTransactionDetailBinding!!.cardExValue.setText(response.data.receiptInfo.customerCardExpiry)
            activityTransactionDetailBinding!!.clientValue.setText(response.data.receiptInfo.customerCardName)
            activityTransactionDetailBinding!!.panValue.setText(response.data.receiptInfo.customerCardPan)
            activityTransactionDetailBinding!!.aidValue.setText(response.data.receiptInfo.transactionAID)
            if (response.data.posResponseCode.equals("00")) {
                activityTransactionDetailBinding!!.messageValue.setText("Transaction Approved")
            } else {
                activityTransactionDetailBinding!!.messageValue.setText("Transaction Declined")
            }
            activityTransactionDetailBinding!!.stanValue.setText(response.data.receiptInfo.transactionSTAN)
            activityTransactionDetailBinding!!.rrnValue.setText(response.data.receiptInfo.rrn)

            activityTransactionDetailBinding!!.amountText.setText("₦ " + response.data.receiptInfo.transactionAmount + ".00")

            timer.start()
        }

    }

    val timer = object : CountDownTimer(1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            shareData()
        }
    }

    private fun shareData() {
        val bitmap = getBitmapFromView(activityTransactionDetailBinding!!.receiptParentView)

        if (bitmap != null) {

            //Save the image inside the APPLICTION folder
            val mediaStorageDir = File(
                applicationContext.externalCacheDir.toString() + "Image.png"
            )
            try {
                val outputStream = FileOutputStream(mediaStorageDir.toString())
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val imageUri: Uri = FileProvider.getUriForFile(
                applicationContext, applicationContext.packageName + ".provider",
                mediaStorageDir
            )
            val waIntent = Intent(Intent.ACTION_SEND)
            waIntent.type = "image/*"
            waIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            startActivity(Intent.createChooser(waIntent, "Share with"))
        }

    }

    fun getBitmapFromView(view: View): Bitmap? {

        Log.e("View: ", view.rootView.toString())
        Log.e("Width: ", view.rootView.width.toString())
        Log.e("Width: ", view.width.toString())
        Log.e("Height: ", view.rootView.height.toString())
        Log.e("Height: ", view.height.toString())

//        try {
        //Define a bitmap with the same size as the view
        val returnedBitmap: Bitmap =
            Bitmap.createBitmap(view.rootView.width, view.rootView.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        return returnedBitmap

    }


}