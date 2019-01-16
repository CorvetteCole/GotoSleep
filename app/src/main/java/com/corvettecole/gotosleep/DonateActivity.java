/**
 *         Go to Sleep is an open source app to manage a healthy sleep schedule
 *         Copyright (C) 2019 Cole Gerdemann
 *
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.corvettecole.gotosleep;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.corvettecole.gotosleep.R;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import androidx.appcompat.app.AppCompatActivity;

public class DonateActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private Button donate1;
    private Button donate3;
    private Button donate5;

    private TextView ethereum;
    private TextView paypal;
    private TextView googlePay;

    private BillingProcessor bp;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
    super.onCreate(savedInstanceBundle);
    setContentView(R.layout.activity_donate);

    donate1 = findViewById(R.id.donate1);
    donate3 = findViewById(R.id.donate3);
    donate5 = findViewById(R.id.donate5);
    ethereum = findViewById(R.id.ethereumTextView);
    paypal = findViewById(R.id.payPalTextView);
    googlePay = findViewById(R.id.googlePayTextView);

    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

    //COMPILE INSTRUCTIONS: comment out the following block of code
    //Start
    bp = new BillingProcessor(this, getResources().getString(R.string.license_key), this);
    bp.initialize();
    donate1.setOnClickListener(view -> {
        bp.purchase(this, "donate_1");
    });

    donate3.setOnClickListener(view -> {
        bp.purchase(this, "donate_3");
    });

    donate5.setOnClickListener(view -> {
        bp.purchase(this, "donate_5");
    });
    //End

    ethereum.setOnClickListener(view -> {
        ClipData clip = ClipData.newPlainText(getString(R.string.supportEthereum), "0x8eFF5600A23708EFa475Be2C18892c9c0C43373B");
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, getString(R.string.supportEthereumCopied), Toast.LENGTH_LONG).show();
    });

    paypal.setOnClickListener(view -> {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/CGerdemann"));
        startActivity(browserIntent);
    });

    googlePay.setOnClickListener(view -> {
        ClipData clip = ClipData.newPlainText(getString(R.string.supportGooglePay), "corvettecole@gmail.com");
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, getString(R.string.supportGooglePayCopied), Toast.LENGTH_LONG).show();
    });



    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }
}
