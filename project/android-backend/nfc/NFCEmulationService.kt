package com.nfcscanner.app.nfc

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import com.nfcscanner.app.data.models.NFCTag
import com.nfcscanner.app.data.repository.NFCRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Servizio per emulazione NFC usando Host Card Emulation (HCE)
 */
@AndroidEntryPoint
class NFCEmulationService : HostApduService() {
    
    @Inject
    lateinit var repository: NFCRepository
    
    private var emulatedTag: NFCTag? = null
    private var isEmulating = false
    
    companion object {
        private const val TAG = "NFCEmulationService"
        
        // AID (Application Identifier) per l'app
        private const val APP_AID = "F0394148148100"
        
        // Comandi APDU
        private val SELECT_APDU = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
            0x07.toByte(), 0xF0.toByte(), 0x39.toByte(), 0x41.toByte(),
            0x48.toByte(), 0x14.toByte(), 0x81.toByte(), 0x00.toByte(),
            0x00.toByte()
        )
        
        // Risposte APDU
        private val SELECT_OK_SW = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val UNKNOWN_CMD_SW = byteArrayOf(0x00.toByte(), 0x00.toByte())
        private val SELECT_FAILED_SW = byteArrayOf(0x6F.toByte(), 0x00.toByte())
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NFCEmulationService created")
    }
    
    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "NFCEmulationService started")
        
        // Recupera il tag da emulare dall'intent
        intent?.getStringExtra("tag_id")?.let { tagId ->
            CoroutineScope(Dispatchers.IO).launch {
                emulatedTag = repository.getTagById(tagId)
                isEmulating = emulatedTag != null
                Log.d(TAG, "Emulating tag: $tagId, success: $isEmulating")
            }
        }
        
        return START_STICKY
    }
    
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.d(TAG, "Received APDU: ${commandApdu?.toHexString()}")
        
        if (commandApdu == null) {
            return UNKNOWN_CMD_SW
        }
        
        return when {
            // Comando SELECT per selezionare l'applicazione
            commandApdu.contentEquals(SELECT_APDU) -> {
                Log.d(TAG, "SELECT command received")
                if (isEmulating && emulatedTag != null) {
                    SELECT_OK_SW
                } else {
                    SELECT_FAILED_SW
                }
            }
            
            // Comando per leggere dati del tag
            isReadDataCommand(commandApdu) -> {
                Log.d(TAG, "READ DATA command received")
                handleReadDataCommand()
            }
            
            // Comando per leggere ID del tag
            isReadIdCommand(commandApdu) -> {
                Log.d(TAG, "READ ID command received")
                handleReadIdCommand()
            }
            
            else -> {
                Log.d(TAG, "Unknown command: ${commandApdu.toHexString()}")
                UNKNOWN_CMD_SW
            }
        }
    }
    
    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Service deactivated, reason: $reason")
        when (reason) {
            DEACTIVATION_LINK_LOSS -> {
                Log.d(TAG, "Link lost")
            }
            DEACTIVATION_DESELECTED -> {
                Log.d(TAG, "App deselected")
            }
        }
    }
    
    /**
     * Verifica se è un comando di lettura dati
     */
    private fun isReadDataCommand(commandApdu: ByteArray): Boolean {
        return commandApdu.size >= 4 &&
               commandApdu[0] == 0x00.toByte() &&
               commandApdu[1] == 0xB0.toByte() // READ BINARY
    }
    
    /**
     * Verifica se è un comando di lettura ID
     */
    private fun isReadIdCommand(commandApdu: ByteArray): Boolean {
        return commandApdu.size >= 4 &&
               commandApdu[0] == 0x00.toByte() &&
               commandApdu[1] == 0xCA.toByte() // GET DATA
    }
    
    /**
     * Gestisce comando di lettura dati
     */
    private fun handleReadDataCommand(): ByteArray {
        val tag = emulatedTag ?: return UNKNOWN_CMD_SW
        
        return try {
            // Costruisci risposta con i dati del tag
            val data = buildTagDataResponse(tag)
            data + SELECT_OK_SW
        } catch (e: Exception) {
            Log.e(TAG, "Error building data response", e)
            UNKNOWN_CMD_SW
        }
    }
    
    /**
     * Gestisce comando di lettura ID
     */
    private fun handleReadIdCommand(): ByteArray {
        val tag = emulatedTag ?: return UNKNOWN_CMD_SW
        
        return try {
            val idBytes = tag.id.toByteArray()
            idBytes + SELECT_OK_SW
        } catch (e: Exception) {
            Log.e(TAG, "Error building ID response", e)
            UNKNOWN_CMD_SW
        }
    }
    
    /**
     * Costruisce risposta con dati del tag
     */
    private fun buildTagDataResponse(tag: NFCTag): ByteArray {
        val response = StringBuilder()
        
        // Aggiungi informazioni base del tag
        response.append("ID:${tag.id}\n")
        response.append("Type:${tag.type}\n")
        response.append("UID:${tag.uid}\n")
        
        // Aggiungi record NDEF
        tag.records.forEachIndexed { index, record ->
            response.append("Record$index:${record.recordType}:${record.data}\n")
        }
        
        return response.toString().toByteArray()
    }
    
    /**
     * Avvia emulazione di un tag specifico
     */
    fun startEmulation(tagId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            emulatedTag = repository.getTagById(tagId)
            isEmulating = emulatedTag != null
            Log.d(TAG, "Started emulation for tag: $tagId")
        }
    }
    
    /**
     * Ferma emulazione
     */
    fun stopEmulation() {
        emulatedTag = null
        isEmulating = false
        Log.d(TAG, "Stopped emulation")
    }
}

/**
 * Estensione per convertire ByteArray in stringa esadecimale
 */
private fun ByteArray.toHexString(): String {
    return joinToString("") { "%02x".format(it) }
}