package com.peerbits.nfccardread

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class NfcHome : AppCompatActivity(), View.OnClickListener {

    private lateinit var rlRead: RelativeLayout
    private lateinit var rlWrite: RelativeLayout
    private lateinit var rlCreditCard: RelativeLayout
    private lateinit var ivHomeicon: ImageView
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nfc_home)
        
        initViews()
        checkNfcSupport()
        
        rlRead.setOnClickListener(this)
        rlWrite.setOnClickListener(this)
        rlCreditCard.setOnClickListener(this)
    }

    private fun initViews() {
        rlRead = findViewById(R.id.rlReadNFCTAG)
        rlWrite = findViewById(R.id.rlWriteWithNFC)
        rlCreditCard = findViewById(R.id.rlCreditCard)
        ivHomeicon = findViewById(R.id.ivHomeicon)
        
        val animation = AnimationUtils.loadAnimation(this, R.anim.swinging)
        ivHomeicon.startAnimation(animation)
    }

    private fun checkNfcSupport() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not supported on this device", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "Please enable NFC in settings", Toast.LENGTH_LONG).show()
        }
    }

    override fun onClick(view: View) {
        if (nfcAdapter == null || !nfcAdapter!!.isEnabled) {
            Toast.makeText(this, "NFC is not enabled", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = when (view.id) {
            R.id.rlReadNFCTAG -> Intent(this, NFCRead::class.java)
            R.id.rlWriteWithNFC -> Intent(this, NFCWrite::class.java)
            R.id.rlCreditCard -> Intent(this, NFCCardReading::class.java)
            else -> return
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        val animation = AnimationUtils.loadAnimation(this, R.anim.swinging)
        ivHomeicon.startAnimation(animation)
    }

    override fun onPause() {
        super.onPause()
        ivHomeicon.clearAnimation()
    }
}