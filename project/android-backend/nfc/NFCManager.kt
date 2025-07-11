package com.nfcscanner.app.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.*
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nfcscanner.app.data.models.*
import com.nfcscanner.app.data.repository.NFCRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager principale per operazioni NFC
 */
@Singleton
class NFCManager @Inject constructor(
    private val repository: NFCRepository
) {
    
    private var nfcAdapter: NfcAdapter? = null
    private var currentActivity: Activity? = null
    
    private val _scannerState = MutableLiveData<NFCScannerState>(NFCScannerState.IDLE)
    val scannerState: LiveData<NFCScannerState> = _scannerState
    
    private val _lastScannedTag = MutableLiveData<NFCTag?>()
    val lastScannedTag: LiveData<NFCTag?> = _lastScannedTag
    
    private val _nfcResult = MutableLiveData<NFCResult<NFCTag>>()
    val nfcResult: LiveData<NFCResult<NFCTag>> = _nfcResult
    
    companion object {
        private const val TAG = "NFCManager"
    }
    
    /**
     * Inizializza NFC Manager
     */
    fun initialize(context: Context) {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context)
    }
    
    /**
     * Verifica se NFC è supportato
     */
    fun isNFCSupported(): Boolean {
        return nfcAdapter != null
    }
    
    /**
     * Verifica se NFC è abilitato
     */
    fun isNFCEnabled(): Boolean {
        return nfcAdapter?.isEnabled == true
    }
    
    /**
     * Avvia la modalità reader per scansione
     */
    fun enableReaderMode(activity: Activity) {
        currentActivity = activity
        nfcAdapter?.let { adapter ->
            val flags = NfcAdapter.FLAG_READER_NFC_A or
                       NfcAdapter.FLAG_READER_NFC_B or
                       NfcAdapter.FLAG_READER_NFC_F or
                       NfcAdapter.FLAG_READER_NFC_V or
                       NfcAdapter.FLAG_READER_ISO_DEP or
                       NfcAdapter.FLAG_READER_NDEF
            
            val options = Bundle().apply {
                putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            }
            
            adapter.enableReaderMode(
                activity,
                { tag -> onTagDiscovered(tag) },
                flags,
                options
            )
            
            _scannerState.value = NFCScannerState.SCANNING
            Log.d(TAG, "Reader mode enabled")
        }
    }
    
    /**
     * Disabilita la modalità reader
     */
    fun disableReaderMode() {
        currentActivity?.let { activity ->
            nfcAdapter?.disableReaderMode(activity)
            _scannerState.value = NFCScannerState.IDLE
            Log.d(TAG, "Reader mode disabled")
        }
    }
    
    /**
     * Callback quando un tag viene scoperto
     */
    private fun onTagDiscovered(tag: Tag) {
        Log.d(TAG, "Tag discovered: ${tag.id.toHexString()}")
        _scannerState.value = NFCScannerState.TAG_DETECTED
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val nfcTag = parseTag(tag)
                _nfcResult.postValue(NFCResult.Success(nfcTag))
                _lastScannedTag.postValue(nfcTag)
                
                // Salva automaticamente se abilitato
                val settings = repository.getSettings()
                if (settings.autoSave) {
                    repository.insertTag(nfcTag)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing tag", e)
                _nfcResult.postValue(NFCResult.Error(e))
                _scannerState.postValue(NFCScannerState.ERROR)
            }
        }
    }
    
    /**
     * Analizza un tag NFC e estrae tutti i dati
     */
    private fun parseTag(tag: Tag): NFCTag {
        val tagId = tag.id.toHexString()
        val technologies = tag.techList.toList()
        val records = mutableListOf<NFCRecord>()
        
        // Leggi NDEF se disponibile
        if (technologies.contains(Ndef::class.java.name)) {
            val ndef = Ndef.get(tag)
            try {
                ndef?.connect()
                val ndefMessage = ndef?.ndefMessage
                ndefMessage?.records?.forEach { record ->
                    records.add(parseNdefRecord(record))
                }
                ndef?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error reading NDEF", e)
            }
        }
        
        // Leggi NdefFormatable se disponibile
        if (technologies.contains(NdefFormatable::class.java.name)) {
            // Tag formattabile ma vuoto
            records.add(NFCRecord(
                recordType = "empty",
                data = "Tag is NDEF formatable but empty"
            ))
        }
        
        // Determina tipo di tag
        val tagType = determineTagType(technologies)
        
        return NFCTag(
            id = tagId,
            uid = tagId,
            type = tagType,
            timestamp = Date(),
            technologies = technologies.map { it.substringAfterLast('.') },
            records = records,
            size = calculateTagSize(tag),
            isWritable = isTagWritable(tag),
            maxSize = getMaxTagSize(tag),
            isVirtual = false
        )
    }
    
    /**
     * Analizza un record NDEF
     */
    private fun parseNdefRecord(record: NdefRecord): NFCRecord {
        return when (record.tnf) {
            NdefRecord.TNF_WELL_KNOWN -> {
                when {
                    Arrays.equals(record.type, NdefRecord.RTD_TEXT) -> {
                        parseTextRecord(record)
                    }
                    Arrays.equals(record.type, NdefRecord.RTD_URI) -> {
                        parseUriRecord(record)
                    }
                    else -> {
                        NFCRecord(
                            recordType = "well_known",
                            data = String(record.payload),
                            mimeType = String(record.type)
                        )
                    }
                }
            }
            NdefRecord.TNF_MIME_MEDIA -> {
                NFCRecord(
                    recordType = "mime",
                    data = String(record.payload),
                    mimeType = String(record.type)
                )
            }
            NdefRecord.TNF_ABSOLUTE_URI -> {
                NFCRecord(
                    recordType = "uri",
                    data = String(record.payload)
                )
            }
            else -> {
                NFCRecord(
                    recordType = "unknown",
                    data = record.payload.toHexString()
                )
            }
        }
    }
    
    /**
     * Analizza record di testo
     */
    private fun parseTextRecord(record: NdefRecord): NFCRecord {
        val payload = record.payload
        if (payload.isEmpty()) {
            return NFCRecord(recordType = "text", data = "")
        }
        
        val languageCodeLength = payload[0].toInt() and 0x3F
        val languageCode = String(payload, 1, languageCodeLength)
        val text = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1)
        
        return NFCRecord(
            recordType = "text",
            data = text,
            language = languageCode,
            encoding = "UTF-8"
        )
    }
    
    /**
     * Analizza record URI
     */
    private fun parseUriRecord(record: NdefRecord): NFCRecord {
        val payload = record.payload
        if (payload.isEmpty()) {
            return NFCRecord(recordType = "uri", data = "")
        }
        
        val uriIdentifier = payload[0].toInt() and 0xFF
        val uriPrefix = getUriPrefix(uriIdentifier)
        val uriField = String(payload, 1, payload.size - 1)
        
        return NFCRecord(
            recordType = "uri",
            data = uriPrefix + uriField
        )
    }
    
    /**
     * Ottieni prefisso URI
     */
    private fun getUriPrefix(identifier: Int): String {
        return when (identifier) {
            0x01 -> "http://www."
            0x02 -> "https://www."
            0x03 -> "http://"
            0x04 -> "https://"
            0x05 -> "tel:"
            0x06 -> "mailto:"
            else -> ""
        }
    }
    
    /**
     * Determina il tipo di tag
     */
    private fun determineTagType(technologies: List<String>): String {
        return when {
            technologies.contains("android.nfc.tech.IsoDep") -> "ISO 14443-4"
            technologies.contains("android.nfc.tech.MifareClassic") -> "MIFARE Classic"
            technologies.contains("android.nfc.tech.MifareUltralight") -> "MIFARE Ultralight"
            technologies.contains("android.nfc.tech.NfcA") -> "NFC-A"
            technologies.contains("android.nfc.tech.NfcB") -> "NFC-B"
            technologies.contains("android.nfc.tech.NfcF") -> "NFC-F"
            technologies.contains("android.nfc.tech.NfcV") -> "NFC-V"
            else -> "Unknown"
        }
    }
    
    /**
     * Calcola dimensione tag
     */
    private fun calculateTagSize(tag: Tag): Int? {
        return try {
            if (tag.techList.contains(Ndef::class.java.name)) {
                val ndef = Ndef.get(tag)
                ndef?.connect()
                val size = ndef?.ndefMessage?.toByteArray()?.size
                ndef?.close()
                size
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Verifica se il tag è scrivibile
     */
    private fun isTagWritable(tag: Tag): Boolean {
        return try {
            if (tag.techList.contains(Ndef::class.java.name)) {
                val ndef = Ndef.get(tag)
                ndef?.connect()
                val writable = ndef?.isWritable == true
                ndef?.close()
                writable
            } else {
                tag.techList.contains(NdefFormatable::class.java.name)
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Ottieni dimensione massima tag
     */
    private fun getMaxTagSize(tag: Tag): Int? {
        return try {
            if (tag.techList.contains(Ndef::class.java.name)) {
                val ndef = Ndef.get(tag)
                ndef?.connect()
                val maxSize = ndef?.maxSize
                ndef?.close()
                maxSize
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Scrive dati su un tag NFC
     */
    suspend fun writeTag(tag: Tag, writeData: NFCWriteData): Boolean {
        return try {
            val records = writeData.records.map { record ->
                when (record.recordType) {
                    "text" -> createTextRecord(record.data, record.language ?: "en")
                    "uri" -> createUriRecord(record.data)
                    else -> createTextRecord(record.data, "en")
                }
            }.toTypedArray()
            
            val message = NdefMessage(records)
            
            if (tag.techList.contains(Ndef::class.java.name)) {
                val ndef = Ndef.get(tag)
                ndef.connect()
                ndef.writeNdefMessage(message)
                ndef.close()
                true
            } else if (tag.techList.contains(NdefFormatable::class.java.name)) {
                val format = NdefFormatable.get(tag)
                format.connect()
                format.format(message)
                format.close()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error writing tag", e)
            false
        }
    }
    
    /**
     * Crea record di testo NDEF
     */
    private fun createTextRecord(text: String, language: String): NdefRecord {
        val languageBytes = language.toByteArray()
        val textBytes = text.toByteArray()
        val payload = ByteArray(1 + languageBytes.size + textBytes.size)
        
        payload[0] = languageBytes.size.toByte()
        System.arraycopy(languageBytes, 0, payload, 1, languageBytes.size)
        System.arraycopy(textBytes, 0, payload, 1 + languageBytes.size, textBytes.size)
        
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }
    
    /**
     * Crea record URI NDEF
     */
    private fun createUriRecord(uri: String): NdefRecord {
        val uriBytes = uri.toByteArray()
        val payload = ByteArray(1 + uriBytes.size)
        
        payload[0] = 0x00 // No URI prefix
        System.arraycopy(uriBytes, 0, payload, 1, uriBytes.size)
        
        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_URI, ByteArray(0), payload)
    }
}

/**
 * Estensione per convertire ByteArray in stringa esadecimale
 */
fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}