package com.nfcscanner.app.ui

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.nfcscanner.app.nfc.NFCManager
import com.nfcscanner.app.ui.theme.NFCScannerTheme
import com.nfcscanner.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity principale dell'app NFC Scanner
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var nfcManager: NFCManager
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Inizializza NFC Manager
        nfcManager.initialize(this)
        
        // Verifica supporto NFC
        checkNFCSupport()
        
        setContent {
            NFCScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NFCScannerApp(
                        nfcManager = nfcManager,
                        viewModel = viewModel
                    )
                }
            }
        }
        
        // Gestisci intent NFC se l'app è stata aperta da un tag
        handleNFCIntent(intent)
    }
    
    override fun onResume() {
        super.onResume()
        
        // Abilita reader mode se NFC è supportato e abilitato
        if (nfcManager.isNFCSupported() && nfcManager.isNFCEnabled()) {
            if (viewModel.isScanningEnabled.value == true) {
                nfcManager.enableReaderMode(this)
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        
        // Disabilita reader mode
        nfcManager.disableReaderMode()
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleNFCIntent(intent)
    }
    
    /**
     * Verifica supporto NFC del dispositivo
     */
    private fun checkNFCSupport() {
        when {
            !nfcManager.isNFCSupported() -> {
                Toast.makeText(
                    this,
                    "NFC not supported on this device",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.setNFCSupported(false)
            }
            
            !nfcManager.isNFCEnabled() -> {
                showEnableNFCDialog()
                viewModel.setNFCEnabled(false)
            }
            
            else -> {
                viewModel.setNFCSupported(true)
                viewModel.setNFCEnabled(true)
            }
        }
    }
    
    /**
     * Mostra dialog per abilitare NFC
     */
    private fun showEnableNFCDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("NFC Disabled")
            .setMessage("NFC is required for this app to work. Would you like to enable it?")
            .setPositiveButton("Enable") { _, _ ->
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * Gestisce intent NFC quando l'app viene aperta da un tag
     */
    private fun handleNFCIntent(intent: Intent?) {
        if (intent?.action == NfcAdapter.ACTION_TAG_DISCOVERED ||
            intent?.action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
            intent?.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                lifecycleScope.launch {
                    // Processa il tag scoperto
                    // Il NFCManager gestirà automaticamente il parsing
                }
            }
        }
    }
    
    /**
     * Avvia scansione NFC
     */
    fun startNFCScanning() {
        when {
            !nfcManager.isNFCSupported() -> {
                Toast.makeText(this, "NFC not supported", Toast.LENGTH_SHORT).show()
            }
            
            !nfcManager.isNFCEnabled() -> {
                showEnableNFCDialog()
            }
            
            else -> {
                nfcManager.enableReaderMode(this)
                viewModel.setScanningEnabled(true)
                Toast.makeText(this, "NFC scanning started", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * Ferma scansione NFC
     */
    fun stopNFCScanning() {
        nfcManager.disableReaderMode()
        viewModel.setScanningEnabled(false)
        Toast.makeText(this, "NFC scanning stopped", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Composable principale dell'app
 */
@Composable
fun NFCScannerApp(
    nfcManager: NFCManager,
    viewModel: MainViewModel
) {
    // Implementazione UI con Jetpack Compose
    // Questo sarà collegato alla UI React Native esistente
    
    val scannerState by nfcManager.scannerState.observeAsState()
    val lastScannedTag by nfcManager.lastScannedTag.observeAsState()
    
    // La UI effettiva sarà gestita dal frontend React Native
    // Questo è solo il bridge per le funzionalità native
}