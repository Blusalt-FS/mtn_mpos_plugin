package com.dspread.blusalt.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.davidmiguel.numberkeyboard.NumberKeyboardListener
import com.dspread.blusalt.R
import com.dspread.blusalt.blusaltmpos.util.AppPreferenceHelper
import com.dspread.blusalt.blusaltmpos.util.Constants
import com.dspread.blusalt.databinding.ActivityAmountEntryBinding


var activityAmountEntryBinding: ActivityAmountEntryBinding? = null
var appPreferenceHelper : AppPreferenceHelper? = null

class AmountEntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amount_entry)
        appPreferenceHelper = AppPreferenceHelper(this)

        activityAmountEntryBinding = ActivityAmountEntryBinding.inflate(layoutInflater)
        val view: View = activityAmountEntryBinding!!.root
        setContentView(view)

        var builder: java.lang.StringBuilder = StringBuilder().append("")

        activityAmountEntryBinding!!.amountText.apply {
            setCurrency("₦")
            setDecimals(true)
            setSpacing(true)
            setDelimiter(false)
            setSeparator(".")
        }

        activityAmountEntryBinding!!.amountKeypad.setListener(
            object : NumberKeyboardListener {
                override fun onNumberClicked(number: Int) {
                    builder.append(number)
                    activityAmountEntryBinding!!.amountText.setText(builder.toString())
                }

                override fun onLeftAuxButtonClicked() {
                }

                override fun onRightAuxButtonClicked() {
                    val co = builder.toString().length
                    if (co > 0) {
                        builder.deleteCharAt(co - 1)
                        activityAmountEntryBinding!!.amountText.setText(builder.toString())
                    }
                }
            }
        )


//        setSupportActionBar(binding.toolbar);

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_confirm_transaction);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        activityAmountEntryBinding!!.continueButton.setOnClickListener { v ->
            Log.e("TAG amount", activityAmountEntryBinding!!.amountText.text.toString())
            Log.e("TAG amount", activityAmountEntryBinding!!.amountText.cleanDoubleValue.toString())
            Log.e("TAG amount", activityAmountEntryBinding!!.amountText.cleanIntValue.toString())

            appPreferenceHelper!!.setSharedPreferenceString(Constants.AMOUNT, activityAmountEntryBinding!!.amountText.text.toString())
            appPreferenceHelper!!.setSharedPreferenceString(Constants.AMOUNT_INT, activityAmountEntryBinding!!.amountText.cleanIntValue.toString())

            val intent = Intent(this@AmountEntryActivity, ConfirmTransaction::class.java)
            startActivity(intent)
        }

    }
}