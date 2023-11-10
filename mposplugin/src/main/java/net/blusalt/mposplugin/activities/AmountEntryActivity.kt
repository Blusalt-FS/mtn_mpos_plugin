package net.blusalt.mposplugin.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.davidmiguel.numberkeyboard.NumberKeyboardListener
import net.blusalt.mposplugin.MemoryManager
import net.blusalt.mposplugin.R
import net.blusalt.mposplugin.blusaltmpos.pos.AppLog
import net.blusalt.mposplugin.blusaltmpos.util.AppPreferenceHelper
import net.blusalt.mposplugin.blusaltmpos.util.Constants
import net.blusalt.mposplugin.databinding.ActivityAmountEntryBinding


var activityAmountEntryBinding: ActivityAmountEntryBinding? = null
var appPreferenceHelper: AppPreferenceHelper? = null
var memoryManager: MemoryManager = MemoryManager.getInstance()
class AmountEntryActivity : AppCompatActivity() {

    var result: String? = null
    var api_key: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_amount_entry)
        appPreferenceHelper = AppPreferenceHelper(this)
        memoryManager.init(applicationContext)

        activityAmountEntryBinding = ActivityAmountEntryBinding.inflate(layoutInflater)
        val view: View = activityAmountEntryBinding!!.root
        setContentView(view)

        val intent = intent
        try {
            if (intent != null) {
                api_key = intent.getStringExtra("APIKEY")
                if (!api_key.isNullOrEmpty()) {
                    appPreferenceHelper!!.setSharedPreferenceString(
                        Constants.APIKEY,
                        api_key
                    )
                    init(api_key, applicationContext)
//                    Log.e("API KEY", api_key.toString())
                }else {
                    Toast.makeText(applicationContext, "No API Key", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        activityAmountEntryBinding!!.toolbar.setOnClickListener {
            finish()
        }

        val builder: java.lang.StringBuilder = StringBuilder().append("")

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

            appPreferenceHelper!!.setSharedPreferenceString(
                Constants.AMOUNT,
                activityAmountEntryBinding!!.amountText.text.toString()
            )
            appPreferenceHelper!!.setSharedPreferenceString(
                Constants.AMOUNT_INT,
                activityAmountEntryBinding!!.amountText.cleanIntValue.toString()
            )

            val int = Intent(this@AmountEntryActivity, ConfirmTransaction::class.java)
            startActivity(int)
        }

    }

    fun init(secretKey: String?, context: Context?) {
        if (!TextUtils.isEmpty(secretKey)) {
            try {
                MemoryManager.getInstance().putUserSecretKey(secretKey)
            } catch (e: java.lang.Exception) {
                AppLog.e("prepareForPrinter", e.message)
            }
        } else {
            AppLog.e("init", "Secret Key is Empty")
        }
    }

    override fun onStart() {
        super.onStart()
//        result = "Z1(92)"

        result = appPreferenceHelper!!.getSharedPreferenceString(Constants.TRANSACTION_RESPONSE)
        Log.e("Trans Result", "result " + result)
        if (!result.isNullOrEmpty()) {
            val mIntent: Intent = intent
            Log.e("Trans StatusCode", result.toString())
            mIntent.putExtra("responseCode", result.toString())
            setResult(Activity.RESULT_OK, mIntent)
            appPreferenceHelper!!.setSharedPreferenceString(
                Constants.TRANSACTION_RESPONSE,
                ""
            )
            finish()
        }
    }

}