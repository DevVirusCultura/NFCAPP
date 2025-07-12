package com.peerbits.nfccardread

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bhargavms.dotloader.DotLoader

class NFCRead : AppCompatActivity() {

    private lateinit var tvNFCMessage: TextView
    private lateinit var dotloader: DotLoader
    private lateinit var ivBack: ImageView
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nfc_read)
        
        initViews()
        setupNfc()
    }

    private fun initViews() {
        tvNFCMessage = findViewById(R.id.tvNFCMessage)
        dotloader = findViewById(R.id.text_dot_loader)
        ivBack = findViewById(R.id.ivBack)
        
        ivBack.setOnClickListener { finish() }
    }

    private fun setupNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        
        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val ndefDetected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val techDetected = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        val nfcIntentFilter = arrayOf(techDetected, tagDetected, ndefDetected)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, 
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 
            PendingIntent.FLAG_MUTABLE
        )
        
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        tag?.let { readFromNFC(it, intent) }
    }

    private fun readFromNFC(tag: Tag, intent: Intent) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                val ndefMessage = ndef.ndefMessage

                if (ndefMessage != null) {
                    val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                    
                    if (messages != null) {
                        val ndefMessages = Array(messages.size) { i -> messages[i] as NdefMessage }
                        val record = ndefMessages[0].records[0]
                        val payload = record.payload
                        val text = String(payload)
                        
                        tvNFCMessage.text = text
                        dotloader.visibility = View.GONE
                        Log.d("NFCRead", "NFC Text: $text")
                        ndef.close()
                    }
                } else {
                    Toast.makeText(this, "Unable to read from NFC, please try again", Toast.LENGTH_LONG).show()
                }
            } else {
                val format = NdefFormatable.get(tag)
                if (format != null) {
                    try {
                        format.connect()
                        val ndefMessage = ndef?.ndefMessage
                        
                        if (ndefMessage != null) {
                            val message = String(ndefMessage.records[0].payload)
                            Log.d("NFCRead", "NFC Message: $message")
                            tvNFCMessage.text = message
                            ndef?.close()
                        } else {
                            Toast.makeText(this, "Unable to read from NFC, please try again", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("NFCRead", "Error reading NFC", e)
                    }
                } else {
                    Toast.makeText(this, "NFC is not readable", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("NFCRead", "Error reading NFC", e)
            Toast.makeText(this, "Error reading NFC: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}