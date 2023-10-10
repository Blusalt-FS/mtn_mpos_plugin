package com.dspread.blusalt.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.dspread.blusalt.R
import com.dspread.blusalt.databinding.ActivityPaymentMethodBinding

var activityPaymentMethodBinding: ActivityPaymentMethodBinding? = null

class PaymentMethodActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setContentView(R.layout.activity_payment_method)
        //        setTitle(getString(R.string.title_welcome));
        activityPaymentMethodBinding = ActivityPaymentMethodBinding.inflate(layoutInflater)
        val view: View = activityPaymentMethodBinding!!.root
        setContentView(view)

        activityPaymentMethodBinding!!.connectBleText.setOnClickListener {
            intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        activityPaymentMethodBinding!!.toolbar.setOnClickListener {
            finish()
        }

        activityPaymentMethodBinding!!.connectBleButton.setOnClickListener {
            intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onToolbarLinstener() {
        TODO("Not yet implemented")
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_payment_method
    }
}