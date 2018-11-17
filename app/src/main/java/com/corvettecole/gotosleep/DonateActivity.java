package com.corvettecole.gotosleep;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import org.w3c.dom.Text;

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

    bp = new BillingProcessor(this, getResources().getString(R.string.license_key), this);
    bp.initialize();
    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

    donate1.setOnClickListener(view -> {
        bp.purchase(this, "donate_1");
    });

    donate3.setOnClickListener(view -> {
        bp.purchase(this, "donate_3");
    });

    donate5.setOnClickListener(view -> {
        bp.purchase(this, "donate_5");
    });

    ethereum.setOnClickListener(view -> {
        ClipData clip = ClipData.newPlainText("Ethereum Address", "0x8eFF5600A23708EFa475Be2C18892c9c0C43373B");
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied ethereum address to clipboard", Toast.LENGTH_LONG).show();
    });

    paypal.setOnClickListener(view -> {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/CGerdemann"));
        startActivity(browserIntent);
    });

    googlePay.setOnClickListener(view -> {
        ClipData clip = ClipData.newPlainText("Google Pay Address", "corvettecole@gmail.com");
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Copied Google Pay to clipboard", Toast.LENGTH_LONG).show();
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
