package com.peerbits.nfccardread

import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.peerbits.creditCardNfcReader.CardNfcAsyncTask
import com.peerbits.creditCardNfcReader.utils.CardNfcUtils
import pl.droidsonroids.gif.GifImageView

class NFCCardReading : AppCompatActivity(), CardNfcAsyncTask.CardNfcInterface {

    private var cardNfcAsyncTask: CardNfcAsyncTask? = null
    private var nfcAdapter: NfcAdapter? = null
    private var turnNfcDialog: AlertDialog? = null
    private var progressDialog: ProgressDialog? = null
    private var cardNfcUtils: CardNfcUtils? = null
    
    private lateinit var ivBack: ImageView
    private lateinit var imgRead: GifImageView
    private lateinit var llText: LinearLayout
    private lateinit var imgRight: GifImageView
    
    private var card: String = ""
    private var cardType: String = ""
    private var expiredDate: String = ""
    private var isScanNow = false
    private var intentFromCreate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nfc_cardread)

        initViews()
        setupNfc()
    }

    private fun initViews() {
        imgRight = findViewById(R.id.imgRight)
        ivBack = findViewById(R.id.ivBack)
        imgRead = findViewById(R.id.imgRead)
        llText = findViewById(R.id.llText)
        
        ivBack.setOnClickListener { finish() }
    }

    private fun setupNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        cardNfcUtils = CardNfcUtils(this)
        createProgressDialog()
        intentFromCreate = true
        onNewIntent(intent)
    }

    private fun createProgressDialog() {
        progressDialog = ProgressDialog(this).apply {
            setTitle(getString(R.string.ad_progressBar_title))
            setMessage(getString(R.string.ad_progressBar_mess))
            isIndeterminate = true
            setCancelable(false)
        }
    }

    override fun onResume() {
        super.onResume()
        intentFromCreate = false
        
        if (nfcAdapter != null && !nfcAdapter!!.isEnabled) {
            showTurnOnNfcDialog()
        } else if (nfcAdapter != null) {
            if (!isScanNow) {
                // Ready to scan
            }
            cardNfcUtils?.enableDispatch()
        }
    }

    override fun onPause() {
        super.onPause()
        cardNfcUtils?.disableDispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (nfcAdapter != null && nfcAdapter!!.isEnabled) {
            cardNfcAsyncTask = CardNfcAsyncTask.Builder(this, intent, intentFromCreate).build()
        }
    }

    private fun showTurnOnNfcDialog() {
        if (turnNfcDialog == null) {
            turnNfcDialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.ad_nfcTurnOn_title))
                .setMessage(getString(R.string.ad_nfcTurnOn_message))
                .setPositiveButton(getString(R.string.ad_nfcTurnOn_pos)) { _, _ ->
                    startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
                }
                .setNegativeButton(getString(R.string.ad_nfcTurnOn_neg)) { _, _ ->
                    onBackPressed()
                }
                .create()
        }
        turnNfcDialog?.show()
    }

    // CardNfcInterface implementation
    override fun startNfcReadCard() {
        isScanNow = true
        progressDialog?.show()
    }

    override fun cardIsReadyToRead() {
        cardNfcAsyncTask?.let { task ->
            card = getPrettyCardNumber(task.cardNumber ?: "")
            expiredDate = task.cardExpireDate ?: ""
            cardType = task.cardType ?: ""
            
            val cardHolderFirstName = task.cardFirstName ?: ""
            val cardHolderLastName = task.cardLastName ?: ""
            val cardCvv = task.cardCvv
            
            Log.d("NFCCardReading", "Card: $card, Type: $cardType, Expire: $expiredDate")
            
            imgRight.visibility = View.VISIBLE
            llText.visibility = View.GONE
            
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this@NFCCardReading, NFCPayActivity::class.java).apply {
                    putExtra("card", card)
                    putExtra("cardType", cardType)
                    putExtra("expiredDate", expiredDate)
                }
                startActivity(intent)
                finish()
            }, 1200)
        }
    }

    override fun doNotMoveCardSoFast() {
        showSnackBar("Please do not move card so fast!")
    }

    override fun unknownEmvCard() {
        showSnackBar("Unknown EMV card")
    }

    override fun cardWithLockedNfc() {
        showSnackBar("NFC is locked on this card")
    }

    override fun finishNfcReadCard() {
        progressDialog?.dismiss()
        cardNfcAsyncTask = null
        isScanNow = false
    }

    private fun showSnackBar(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun getPrettyCardNumber(card: String): String {
        return if (card.length >= 16) {
            "${card.substring(0, 4)} - ${card.substring(4, 8)} - ${card.substring(8, 12)} - ${card.substring(12, 16)}"
        } else {
            card
        }
    }
}