package com.dspread.blusalt.activities;

import android.content.Intent;
import android.os.Bundle;

import com.dspread.blusalt.blusaltmpos.util.AppPreferenceHelper;
import com.dspread.blusalt.blusaltmpos.util.Constants;
import com.dspread.blusalt.databinding.ActivityConfirmTransactionBinding;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;


public class ConfirmTransaction extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityConfirmTransactionBinding binding;
    private AppPreferenceHelper appPreferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityConfirmTransactionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        appPreferenceHelper = new AppPreferenceHelper(this);

        binding.amountText.setText(appPreferenceHelper.getSharedPreferenceString(Constants.AMOUNT));
//        setSupportActionBar(binding.toolbar);

//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_confirm_transaction);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.initiateButton.setOnClickListener( v -> {
            Intent intent = new Intent(ConfirmTransaction.this, PaymentMethodActivity.class);
            startActivity(intent);
        });



    }

//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_confirm_transaction);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
}