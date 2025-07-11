package com.nfcscanner.app.ui

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.nfcscanner.app.nfc.NFCManager
import com.nfcscanner.app.ui.theme.NFCScannerTheme
import com.nfcscanner.app.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity principale che integra UI Compose con backend Kotlin nativo
 * Mantiene la stessa grafica dell'app React Native originale
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var nfcManager: NFCManager
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Inizializza NFC Manager
        nfcManager.initialize(this)
        
        // Verifica supporto NFC
        checkNFCSupport()
        
        setContent {
            NFCScannerTheme {
                // Setup system UI
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = MaterialTheme.colorScheme.background.luminance() > 0.5f
                
                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = androidx.compose.ui.graphics.Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }
                
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
                viewModel.setNFCSupported(false)
            }
            
            !nfcManager.isNFCEnabled() -> {
                viewModel.setNFCEnabled(false)
            }
            
            else -> {
                viewModel.setNFCSupported(true)
                viewModel.setNFCEnabled(true)
            }
        }
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
                    // Il NFCManager gestirà automaticamente il parsing
                }
            }
        }
    }
    
    /**
     * Avvia scansione NFC nativa
     */
    fun startNFCScanningNative() {
        when {
            !nfcManager.isNFCSupported() -> {
                // Mostra errore
            }
            
            !nfcManager.isNFCEnabled() -> {
                // Mostra dialog per abilitare NFC
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
            
            else -> {
                nfcManager.enableReaderMode(this)
                viewModel.setScanningEnabled(true)
            }
        }
    }
    
    /**
     * Ferma scansione NFC nativa
     */
    fun stopNFCScanningNative() {
        nfcManager.disableReaderMode()
        viewModel.setScanningEnabled(false)
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
    // L'app principale sarà implementata con Jetpack Compose
    // mantenendo la stessa grafica dell'app React Native
    NFCScannerNavigation(viewModel = viewModel)
}

// Estensione per calcolare la luminanza
private fun androidx.compose.ui.graphics.Color.luminance(): Float {
    return (0.299 * red + 0.587 * green + 0.114 * blue).toFloat()
}