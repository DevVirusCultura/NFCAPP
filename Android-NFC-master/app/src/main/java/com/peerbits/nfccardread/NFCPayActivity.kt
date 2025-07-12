package com.peerbits.nfccardread

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.peerbits.creditCardNfcReader.CardNfcAsyncTask

class NFCPayActivity : AppCompatActivity() {

    private lateinit var tvCardNumber: TextView
    private lateinit var tvEXPDate: TextView
    private lateinit var cardLogoIcon: ImageView
    private lateinit var ivBack: ImageView
    
    private var card: String = ""
    private var cardType: String = ""
    private var expiredDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nfc_pay)
        
        initViews()
        extractIntentData()
        setupClickListeners()
    }

    private fun initViews() {
        tvCardNumber = findViewById(R.id.tvCardNumber)
        tvEXPDate = findViewById(R.id.tvEXPDate)
        ivBack = findViewById(R.id.ivBack)
        cardLogoIcon = findViewById(R.id.ivCardIcon)
    }

    private fun extractIntentData() {
        intent.extras?.let { extras ->
            card = extras.getString("card", "")
            cardType = extras.getString("cardType", "")
            expiredDate = extras.getString("expiredDate", "")

            tvCardNumber.text = card
            tvEXPDate.text = expiredDate

            parseCardType(cardType)
        }
    }

    private fun setupClickListeners() {
        ivBack.setOnClickListener { finish() }
    }

    private fun parseCardType(cardType: String) {
        Log.d("NFCPayActivity", "Card Type: $cardType")
        
        when (cardType) {
            CardNfcAsyncTask.CARD_UNKNOWN -> {
                Toast.makeText(this, getString(R.string.snack_unknown_bank_card), Toast.LENGTH_LONG).show()
            }
            CardNfcAsyncTask.CARD_VISA, CardNfcAsyncTask.CARD_NAB_VISA -> {
                cardLogoIcon.setImageResource(R.mipmap.visa_logo)
            }
            CardNfcAsyncTask.CARD_MASTER_CARD -> {
                cardLogoIcon.setImageResource(R.mipmap.master_logo)
            }
            else -> {
                // Default or other card types
                cardLogoIcon.setImageResource(R.mipmap.visa_logo)
            }
        }
    }
}