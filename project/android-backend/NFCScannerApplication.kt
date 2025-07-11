package com.nfcscanner.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class per NFC Scanner
 * Configurazione Hilt per dependency injection
 */
@HiltAndroidApp
class NFCScannerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inizializzazione app
        initializeApp()
    }
    
    private fun initializeApp() {
        // Configurazioni iniziali dell'app
        // Log, crash reporting, analytics, etc.
    }
}