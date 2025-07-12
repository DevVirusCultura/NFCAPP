package com.peerbits.nfccardread

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NFCWrite : AppCompatActivity() {

    private lateinit var evTagMessage: EditText
    private lateinit var ivBack: ImageView
    private var nfcAdapter: NfcAdapter? = null
    private val isWrite = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nfc_write)
        
        initViews()
        setupNfc()
    }

    private fun initViews() {
        ivBack = findViewById(R.id.ivBack)
        evTagMessage = findViewById(R.id.evTagMessage)
        
        ivBack.setOnClickListener { finish() }
    }

    private fun setupNfc() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun writeTag(tag: Tag, message: NdefMessage): Boolean {
        val size = message.toByteArray().size
        
        return try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    return false
                }
                if (ndef.maxSize < size) {
                    return false
                }
                ndef.writeNdefMessage(message)
                true
            } else {
                val format = NdefFormatable.get(tag)
                if (format != null) {
                    try {
                        format.connect()
                        format.format(message)
                        true
                    } catch (e: Exception) {
                        false
                    }
                } else {
                    false
                }
            }
        } catch (e: Exception) {
            false
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

        tag?.let {
            if (isWrite) {
                val messageToWrite = evTagMessage.text.toString()

                if (messageToWrite.isNotEmpty() && !TextUtils.equals(messageToWrite, "null")) {
                    val record = NdefRecord.createMime("text/plain", messageToWrite.toByteArray())
                    val message = NdefMessage(arrayOf(record))

                    if (writeTag(it, message)) {
                        Toast.makeText(this, getString(R.string.message_write_success), Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, getString(R.string.message_write_error), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    evTagMessage.error = "Please enter the text to write"
                }
            }
        }
    }
}