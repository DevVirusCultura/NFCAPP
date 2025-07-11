# NFC Scanner - Progetto Android Studio Ibrido

Questo è un progetto Android Studio completo che integra un **backend nativo Kotlin** con l'**interfaccia React Native esistente** tramite WebView e JavaScript Bridge.

## 🏗️ Architettura Ibrida

```
android-hybrid/
├── app/
│   ├── src/main/
│   │   ├── java/com/nfcscanner/app/
│   │   │   ├── ui/MainActivity.kt           # Activity principale con WebView
│   │   │   ├── viewmodel/MainViewModel.kt   # ViewModel per gestione stato
│   │   │   ├── data/                        # Database Room e Repository
│   │   │   ├── nfc/                         # Manager NFC e HCE Service
│   │   │   └── di/                          # Dependency Injection
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml     # Layout con WebView
│   │   │   ├── values/                      # Stringhe e colori
│   │   │   └── xml/                         # Configurazioni NFC
│   │   ├── assets/
│   │   │   └── index.html                   # UI React Native convertita
│   │   └── AndroidManifest.xml              # Permessi e configurazioni
│   ├── build.gradle                         # Dipendenze del modulo
│   └── proguard-rules.pro                   # Regole ProGuard
├── build.gradle                             # Configurazione progetto
├── settings.gradle                          # Impostazioni Gradle
└── gradle.properties                        # Proprietà Gradle
```

## 🚀 Funzionalità Implementate

### **📱 Backend Nativo Kotlin**
- **NFC Manager completo** per scansione e emulazione
- **Room Database** per persistenza locale
- **MVVM Architecture** con ViewModel e LiveData
- **Hilt Dependency Injection** per gestione dipendenze
- **HCE Service** per emulazione tag NFC
- **Repository Pattern** per accesso dati

### **🎨 Frontend Ibrido**
- **WebView** che carica l'interfaccia React Native convertita
- **JavaScript Bridge** per comunicazione bidirezionale
- **UI identica** all'app React Native originale
- **Responsive design** ottimizzato per Android

### **🔗 Comunicazione Bridge**
```kotlin
// Dal JavaScript al Kotlin
window.NFCBridge.startNFCScanning()
window.NFCBridge.getTagHistory()
window.NFCBridge.emulateTag(tagId)

// Dal Kotlin al JavaScript
executeJavaScript("window.onTagScanned($tagJson)")
executeJavaScript("window.onHistoryUpdated($tagsJson)")
```

## 📱 Come Importare in Android Studio

### **1. Crea Nuovo Progetto**
```bash
# In Android Studio:
File > New > New Project
Empty Activity > Kotlin
minSdk 26, targetSdk 34
Package: com.nfcscanner.app
```

### **2. Sostituisci File Progetto**
```bash
# Copia tutti i file da android-hybrid/ nella root del progetto:
- Sostituisci app/build.gradle
- Sostituisci app/src/main/AndroidManifest.xml
- Copia tutti i file Kotlin in app/src/main/java/
- Copia layout XML in app/src/main/res/
- Copia index.html in app/src/main/assets/
- Sostituisci build.gradle, settings.gradle, gradle.properties
```

### **3. Sync & Build**
```bash
# In Android Studio:
File > Sync Project with Gradle Files
Build > Clean Project
Build > Rebuild Project
```

### **4. Run**
```bash
# Connetti dispositivo Android con NFC
Run > Run 'app'
```

## 🔧 Configurazioni Incluse

### **Permessi NFC Completi**
```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="true" />
<uses-feature android:name="android.hardware.nfc.hce" android:required="false" />
```

### **WebView Configurato**
```kotlin
webView.settings.apply {
    javaScriptEnabled = true
    domStorageEnabled = true
    allowFileAccess = true
}
webView.addJavascriptInterface(NFCBridge(), "NFCBridge")
```

### **JavaScript Bridge**
```kotlin
@JavascriptInterface
fun startNFCScanning() {
    runOnUiThread { startNFCScanningNative() }
}

@JavascriptInterface
fun getTagHistory(): String {
    return gson.toJson(viewModel.allTags.value)
}
```

## 🎯 Vantaggi dell'Approccio Ibrido

### **✅ Pro**
- **UI identica** all'app React Native originale
- **Performance native** per operazioni NFC
- **Sviluppo rapido** senza riscrivere l'interfaccia
- **Manutenibilità** separata tra UI e logica business
- **Compatibilità completa** con Android Studio

### **⚠️ Considerazioni**
- **WebView overhead** minimo per rendering UI
- **Bridge communication** per sincronizzazione dati
- **Debug** richiede strumenti per WebView e native

## 🛠️ Funzionalità NFC Native

### **Scansione Avanzata**
```kotlin
nfcAdapter.enableReaderMode(activity, { tag ->
    val nfcTag = parseTag(tag)
    viewModel.handleTagScanned(nfcTag)
}, flags, options)
```

### **Emulazione HCE**
```kotlin
class NFCEmulationService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        // Gestisce comandi APDU per emulazione
    }
}
```

### **Database Room**
```kotlin
@Entity(tableName = "nfc_tags")
data class NFCTag(
    @PrimaryKey val id: String,
    val timestamp: Date,
    val records: List<NFCRecord>,
    val technologies: List<String>
)
```

## 🔄 Flusso di Comunicazione

1. **UI JavaScript** chiama `window.NFCBridge.startNFCScanning()`
2. **Bridge Kotlin** riceve chiamata e avvia `NFCManager.enableReaderMode()`
3. **NFC Manager** rileva tag e notifica `ViewModel`
4. **ViewModel** salva tag nel database e aggiorna `LiveData`
5. **MainActivity** osserva cambiamenti e chiama `executeJavaScript()`
6. **UI JavaScript** riceve callback e aggiorna interfaccia

## 🚀 Risultato Finale

Ottieni un'app Android Studio **completamente funzionante** con:
- ✅ **Backend nativo Kotlin** per performance ottimali
- ✅ **UI React Native** identica all'originale
- ✅ **Funzionalità NFC complete** (scansione, emulazione, HCE)
- ✅ **Database locale** con Room
- ✅ **Architettura MVVM** professionale
- ✅ **Pronta per produzione** e Play Store

Il progetto è **immediatamente eseguibile** in Android Studio e mantiene la bellissima interfaccia che hai già sviluppato! 🎉