package com.nfcscanner.app.data.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.nfcscanner.app.data.database.Converters
import kotlinx.parcelize.Parcelize
import java.util.Date

/**
 * Modello dati per un tag NFC
 */
@Entity(tableName = "nfc_tags")
@TypeConverters(Converters::class)
@Parcelize
data class NFCTag(
    @PrimaryKey
    val id: String,
    val uid: String?,
    val type: String?,
    val timestamp: Date,
    val technologies: List<String>,
    val records: List<NFCRecord>,
    val size: Int?,
    val isWritable: Boolean,
    val maxSize: Int?,
    val isVirtual: Boolean = false,
    val isEmulated: Boolean = false
) : Parcelable

/**
 * Record NDEF contenuto nel tag
 */
@Parcelize
data class NFCRecord(
    val recordType: String,
    val data: String,
    val encoding: String? = "UTF-8",
    val language: String? = "en",
    val mimeType: String? = null
) : Parcelable

/**
 * Statistiche di utilizzo
 */
@Entity(tableName = "nfc_stats")
data class NFCStats(
    @PrimaryKey
    val id: Int = 1,
    val totalScans: Int = 0,
    val firstScanDate: Date? = null,
    val lastScanDate: Date? = null,
    val tagsPerDay: Map<String, Int> = emptyMap(),
    val recordTypes: Map<String, Int> = emptyMap()
)

/**
 * Impostazioni app
 */
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val notifications: Boolean = true,
    val autoSave: Boolean = true,
    val vibration: Boolean = true,
    val soundEnabled: Boolean = true,
    val theme: String = "auto",
    val scanTimeout: Int = 30,
    val maxHistorySize: Int = 100
)

/**
 * Dati per scrittura NFC
 */
data class NFCWriteData(
    val records: List<NFCRecord>
)

/**
 * Risultato operazione NFC
 */
sealed class NFCResult<out T> {
    data class Success<T>(val data: T) : NFCResult<T>()
    data class Error(val exception: Throwable) : NFCResult<Nothing>()
    object Loading : NFCResult<Nothing>()
}

/**
 * Stato scanner NFC
 */
enum class NFCScannerState {
    IDLE,
    SCANNING,
    TAG_DETECTED,
    ERROR
}

/**
 * Tipo di operazione NFC
 */
enum class NFCOperationType {
    READ,
    WRITE,
    EMULATE
}