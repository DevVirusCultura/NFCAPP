package com.nfcscanner.app.ui

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.provider.Settings
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nfcscanner.app.R
import com.nfcscanner.app.databinding.ActivityMainBinding
import com.nfcscanner.app.nfc.NFCManager
import com.nfcscanner.app.viewmodel.MainViewModel
import com.nfcscanner.app.data.models.NFCTag
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity principale che integra React Native UI con backend Kotlin
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var nfcManager: NFCManager
    
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView
    private val gson = Gson()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inizializza NFC Manager
        nfcManager.initialize(this)
        
        // Configura WebView per React Native UI
        setupWebView()
        
        // Verifica supporto NFC
        checkNFCSupport()
        
        // Osserva cambiamenti dal ViewModel
        observeViewModel()
        
        // Gestisci intent NFC se l'app è stata aperta da un tag
        handleNFCIntent(intent)
    }
    
    private fun setupWebView() {
        webView = binding.webView
        
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
            }
            
            // Aggiungi JavaScript Interface per comunicazione con React Native
            addJavascriptInterface(NFCBridge(), "NFCBridge")
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    // Notifica React Native che il backend è pronto
                    executeJavaScript("window.nativeReady = true;")
                }
            }
            
            // Carica l'app React Native (puoi servire da assets o server locale)
            loadUrl("file:///android_asset/index.html")
        }
    }
    
    /**
     * JavaScript Interface per comunicazione bidirezionale
     */
    inner class NFCBridge {
        
        @JavascriptInterface
        fun startNFCScanning() {
            runOnUiThread {
                startNFCScanningNative()
            }
        }
        
        @JavascriptInterface
        fun stopNFCScanning() {
            runOnUiThread {
                stopNFCScanningNative()
            }
        }
        
        @JavascriptInterface
        fun getTagHistory(): String {
            return try {
                val tags = viewModel.allTags.value
                gson.toJson(tags)
            } catch (e: Exception) {
                "[]"
            }
        }
        
        @JavascriptInterface
        fun deleteTag(tagId: String) {
            runOnUiThread {
                viewModel.deleteTag(tagId)
            }
        }
        
        @JavascriptInterface
        fun emulateTag(tagId: String) {
            runOnUiThread {
                lifecycleScope.launch {
                    val tag = viewModel.getTagById(tagId)
                    tag?.let {
                        viewModel.startEmulation(it)
                    }
                }
            }
        }
        
        @JavascriptInterface
        fun exportData(): String {
            return viewModel.exportData() ?: ""
        }
        
        @JavascriptInterface
        fun importData(jsonData: String) {
            runOnUiThread {
                viewModel.importData(jsonData)
            }
        }
        
        @JavascriptInterface
        fun isNFCSupported(): Boolean {
            return nfcManager.isNFCSupported()
        }
        
        @JavascriptInterface
        fun isNFCEnabled(): Boolean {
            return nfcManager.isNFCEnabled()
        }
    }
    
    private fun observeViewModel() {
        // Osserva nuovi tag scansionati
        viewModel.selectedTag.observe(this) { tag ->
            tag?.let {
                // Notifica React Native del nuovo tag
                val tagJson = gson.toJson(it)
                executeJavaScript("window.onTagScanned && window.onTagScanned($tagJson);")
            }
        }
        
        // Osserva errori
        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                executeJavaScript("window.onError && window.onError('$it');")
                viewModel.clearError()
            }
        }
        
        // Osserva stato scanner
        viewModel.scannerState.observe(this) { state ->
            val stateJson = gson.toJson(mapOf("state" to state.name))
            executeJavaScript("window.onScannerStateChanged && window.onScannerStateChanged($stateJson);")
        }
        
        // Osserva aggiornamenti cronologia
        viewModel.allTags.observe(this) { tags ->
            val tagsJson = gson.toJson(tags)
            executeJavaScript("window.onHistoryUpdated && window.onHistoryUpdated($tagsJson);")
        }
    }
    
    private fun executeJavaScript(script: String) {
        runOnUiThread {
            webView.evaluateJavascript(script, null)
        }
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
                executeJavaScript("window.onNFCStatusChanged && window.onNFCStatusChanged({supported: false, enabled: false});")
            }
            
            !nfcManager.isNFCEnabled() -> {
                showEnableNFCDialog()
                viewModel.setNFCEnabled(false)
                executeJavaScript("window.onNFCStatusChanged && window.onNFCStatusChanged({supported: true, enabled: false});")
            }
            
            else -> {
                viewModel.setNFCSupported(true)
                viewModel.setNFCEnabled(true)
                executeJavaScript("window.onNFCStatusChanged && window.onNFCStatusChanged({supported: true, enabled: true});")
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
                    // Il NFCManager gestirà automaticamente il parsing
                    // e notificherà il ViewModel
                }
            }
        }
    }
    
    /**
     * Avvia scansione NFC nativa
     */
    private fun startNFCScanningNative() {
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
     * Ferma scansione NFC nativa
     */
    private fun stopNFCScanningNative() {
        nfcManager.disableReaderMode()
        viewModel.setScanningEnabled(false)
        Toast.makeText(this, "NFC scanning stopped", Toast.LENGTH_SHORT).show()
    }
    
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}