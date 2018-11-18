package com.corvettecole.gotosleep

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails

import org.w3c.dom.Text

import androidx.appcompat.app.AppCompatActivity

class DonateActivity : AppCompatActivity(), BillingProcessor.IBillingHandler {

    private var donate1: Button? = null
    private var donate3: Button? = null
    private var donate5: Button? = null

    private var ethereum: TextView? = null
    private var paypal: TextView? = null
    private var googlePay: TextView? = null

    private var bp: BillingProcessor? = null

    public override fun onCreate(savedInstanceBundle: Bundle?) {
        super.onCreate(savedInstanceBundle)
        setContentView(R.layout.activity_donate)

        donate1 = findViewById(R.id.donate1)
        donate3 = findViewById(R.id.donate3)
        donate5 = findViewById(R.id.donate5)
        ethereum = findViewById(R.id.ethereumTextView)
        paypal = findViewById(R.id.payPalTextView)
        googlePay = findViewById(R.id.googlePayTextView)

        bp = BillingProcessor(this, resources.getString(R.string.license_key), this)
        bp!!.initialize()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        donate1!!.setOnClickListener { view -> bp!!.purchase(this, "donate_1") }

        donate3!!.setOnClickListener { view -> bp!!.purchase(this, "donate_3") }

        donate5!!.setOnClickListener { view -> bp!!.purchase(this, "donate_5") }

        ethereum!!.setOnClickListener { view ->
            val clip = ClipData.newPlainText("Ethereum Address", "0x8eFF5600A23708EFa475Be2C18892c9c0C43373B")
            clipboard.primaryClip = clip
            Toast.makeText(this, "Copied ethereum address to clipboard", Toast.LENGTH_LONG).show()
        }

        paypal!!.setOnClickListener { view ->
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://paypal.me/CGerdemann"))
            startActivity(browserIntent)
        }

        googlePay!!.setOnClickListener { view ->
            val clip = ClipData.newPlainText("Google Pay Address", "corvettecole@gmail.com")
            clipboard.primaryClip = clip
            Toast.makeText(this, "Copied Google Pay to clipboard", Toast.LENGTH_LONG).show()
        }


    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {

    }

    override fun onPurchaseHistoryRestored() {

    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {

    }

    override fun onBillingInitialized() {

    }
}
