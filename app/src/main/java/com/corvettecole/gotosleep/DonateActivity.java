package com.corvettecole.gotosleep;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DonateActivity extends AppCompatActivity{

    private Button donate1;
    private Button donate3;
    private Button donate5;

    private TextView ethereum;
    private TextView paypal;
    private TextView googlePay;


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
}
