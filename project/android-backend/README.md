# NFC Scanner - Android Backend

Questo è il backend Android Studio completo per l'app NFC Scanner, progettato per funzionare con l'interfaccia React Native esistente.

## 🏗️ Struttura del Progetto

```
android-backend/
├── AndroidManifest.xml          # Configurazione app e permessi NFC
├── build.gradle                 # Dipendenze e configurazione build
├── NFCScannerApplication.kt     # Application class con Hilt
├── data/
│   ├── models/                  # Modelli dati (NFCTag, NFCRecord, etc.)
│   ├── database/               # Room database e DAO
│   └── repository/             # Repository pattern per data access
├── nfc/
│   ├── NFCManager.kt           # Manager principale per operazioni NFC
│   └── NFCEmulationService.kt  # Servizio HCE per emulazione
├── ui/
│   └── MainActivity.kt         # Activity principale
├── viewmodel/
│   └── MainViewModel.kt        # ViewModel con logica business
├── di/
│   └── DatabaseModule.kt       # Dependency injection con Hilt
└── res/
    ├── xml/                    # Configurazioni NFC e file provider
    └── values/                 # Stringhe e risorse
```

## 🚀 Funzionalità Implementate

### **📱 Core NFC**
- **Scansione automatica** con `NfcAdapter.enableReaderMode()`
- **Parsing completo** di tag NDEF, tecnologie, e metadati
- **Emulazione HCE** con `HostApduService`
- **Scrittura tag** con supporto NDEF e NdefFormatable

### **💾 Persistenza Dati**
- **Room Database** con TypeConverters per oggetti complessi
- **Repository Pattern** per accesso dati centralizzato
- **LiveData/Flow** per osservazione reattiva dei dati
- **Export/Import** JSON per backup e ripristino

### **🏛️ Architettura**
- **MVVM** con ViewModel e LiveData
- **Hilt** per dependency injection
- **Coroutines** per operazioni asincrone
- **StateFlow** per gestione stato UI

### **🔧 Configurazioni**
- **Permessi NFC** completi nel manifest
- **Intent filters** per gestione tag discovery
- **HCE service** configurato con AID personalizzati
- **ProGuard rules** per ottimizzazione release

## 📋 Come Integrare in Android Studio

### **1. Crea Nuovo Progetto**
```bash
# In Android Studio:
File > New > New Project
Empty Activity > Kotlin
minSdk 26, targetSdk 34
```

### **2. Copia File Backend**
```bash
# Copia tutti i file da android-backend/ nella struttura:
app/src/main/java/com/nfcscanner/app/
app/src/main/res/
app/src/main/AndroidManifest.xml
app/build.gradle
```

### **3. Configura Dipendenze**
Il `build.gradle` include tutte le dipendenze necessarie:
- Room Database
- Hilt Dependency Injection
- Coroutines
- Material Design
- Navigation Component

### **4. Sincronizza Progetto**
```bash
# In Android Studio:
File > Sync Project with Gradle Files
```

## 🔌 Integrazione con Frontend React Native

Il backend è progettato per funzionare con l'interfaccia React Native esistente attraverso:

### **Bridge Pattern**
```kotlin
// MainActivity.kt gestisce il bridge tra native e RN
class MainActivity : ComponentActivity() {
    @Inject lateinit var nfcManager: NFCManager
    
    // Metodi esposti al frontend
    fun startNFCScanning()
    fun stopNFCScanning()
    fun writeTag(data: NFCWriteData)
    fun emulateTag(tagId: String)
}
```

### **Shared Data Models**
I modelli dati sono identici tra backend e frontend:
```kotlin
data class NFCTag(
    val id: String,
    val timestamp: Date,
    val records: List<NFCRecord>,
    val technologies: List<String>,
    // ... altri campi
)
```

## 🛡️ Sicurezza e Privacy

### **Permessi Minimi**
```xml
<uses-permission android:name="android.permission.NFC" />
<uses-permission android:name="android.permission.VIBRATE" />
```

### **Dati Locali**
- Tutti i dati salvati localmente con Room
- Nessuna trasmissione di dati esterni
- Export/import completamente offline

### **HCE Sicuro**
- AID personalizzati per evitare conflitti
- Validazione comandi APDU
- Gestione errori robusta

## 📱 Funzionalità NFC Avanzate

### **Scansione Multi-Tecnologia**
```kotlin
// Supporta tutte le tecnologie NFC
val flags = NfcAdapter.FLAG_READER_NFC_A or
           NfcAdapter.FLAG_READER_NFC_B or
           NfcAdapter.FLAG_READER_NFC_F or
           NfcAdapter.FLAG_READER_NFC_V or
           NfcAdapter.FLAG_READER_ISO_DEP or
           NfcAdapter.FLAG_READER_NDEF
```

### **Parsing Intelligente**
```kotlin
// Analizza automaticamente:
- Record NDEF (testo, URI, MIME)
- Tecnologie supportate
- Metadati del tag (dimensione, scrivibilità)
- UID e identificatori
```

### **Emulazione Completa**
```kotlin
// HCE Service per emulazione
class NFCEmulationService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray
    // Gestisce comandi SELECT, READ DATA, READ ID
}
```

## 🔧 Configurazione Avanzata

### **Ottimizzazioni Performance**
- Reader mode con presenza check ottimizzato
- Database queries indicizzate
- Coroutines per operazioni I/O
- StateFlow per UI reattiva

### **Gestione Errori**
- Try-catch completo per operazioni NFC
- Fallback per dispositivi non supportati
- Logging dettagliato per debugging
- User feedback chiaro

### **Backup e Ripristino**
- Export JSON strutturato
- Import con validazione dati
- Merge intelligente senza duplicati
- Statistiche di utilizzo

## 🚀 Deployment

### **Build Release**
```bash
# Configura signing in build.gradle
# Abilita ProGuard per ottimizzazione
./gradlew assembleRelease
```

### **Testing**
```bash
# Unit tests per logica business
./gradlew test

# Instrumented tests per NFC
./gradlew connectedAndroidTest
```

Questo backend fornisce tutte le funzionalità native Android necessarie per un'app NFC completa, mantenendo la compatibilità con l'interfaccia React Native esistente!