package com.nfcscanner.app.data.repository

import androidx.lifecycle.LiveData
import com.nfcscanner.app.data.database.AppSettingsDao
import com.nfcscanner.app.data.database.NFCStatsDao
import com.nfcscanner.app.data.database.NFCTagDao
import com.nfcscanner.app.data.models.AppSettings
import com.nfcscanner.app.data.models.NFCStats
import com.nfcscanner.app.data.models.NFCTag
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository per gestione dati NFC - Bridge tra UI e Database
 */
@Singleton
class NFCRepository @Inject constructor(
    private val nfcTagDao: NFCTagDao,
    private val statsDao: NFCStatsDao,
    private val settingsDao: AppSettingsDao
) {
    
    // ==================== TAG OPERATIONS ====================
    
    fun getAllTags(): Flow<List<NFCTag>> = nfcTagDao.getAllTags()
    
    fun getAllTagsLiveData(): LiveData<List<NFCTag>> = nfcTagDao.getAllTagsLiveData()
    
    suspend fun getTagById(tagId: String): NFCTag? = nfcTagDao.getTagById(tagId)
    
    suspend fun insertTag(tag: NFCTag) {
        nfcTagDao.insertTag(tag)
        updateScanStats()
    }
    
    suspend fun updateTag(tag: NFCTag) = nfcTagDao.updateTag(tag)
    
    suspend fun deleteTag(tag: NFCTag) = nfcTagDao.deleteTag(tag)
    
    suspend fun deleteTagById(tagId: String) = nfcTagDao.deleteTagById(tagId)
    
    suspend fun deleteAllTags() {
        nfcTagDao.deleteAllTags()
        clearStats()
    }
    
    suspend fun getTagCount(): Int = nfcTagDao.getTagCount()
    
    // ==================== STATS OPERATIONS ====================
    
    suspend fun getStats(): NFCStats {
        return statsDao.getStats() ?: NFCStats()
    }
    
    private suspend fun updateScanStats() {
        val currentStats = getStats()
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        
        val updatedStats = currentStats.copy(
            totalScans = currentStats.totalScans + 1,
            lastScanDate = java.util.Date(),
            firstScanDate = currentStats.firstScanDate ?: java.util.Date(),
            tagsPerDay = currentStats.tagsPerDay.toMutableMap().apply {
                this[today] = (this[today] ?: 0) + 1
            }
        )
        
        statsDao.insertStats(updatedStats)
    }
    
    suspend fun clearStats() = statsDao.clearStats()
    
    // ==================== SETTINGS OPERATIONS ====================
    
    suspend fun getSettings(): AppSettings {
        return settingsDao.getSettings() ?: AppSettings()
    }
    
    suspend fun updateSettings(settings: AppSettings) = settingsDao.insertSettings(settings)
    
    // ==================== EXPORT/IMPORT ====================
    
    suspend fun exportData(): String {
        val tags = getAllTags()
        val stats = getStats()
        val settings = getSettings()
        
        val exportData = mapOf(
            "exportDate" to java.util.Date().toString(),
            "version" to "1.0.0",
            "tags" to tags.toString(), // In real implementation, collect flow
            "stats" to stats,
            "settings" to settings
        )
        
        return com.google.gson.Gson().toJson(exportData)
    }
    
    suspend fun importData(jsonData: String): Int {
        try {
            val gson = com.google.gson.Gson()
            val importData = gson.fromJson(jsonData, Map::class.java)
            
            // Parse and import tags
            // Implementation depends on JSON structure
            
            return 0 // Return number of imported tags
        } catch (e: Exception) {
            throw Exception("Invalid import data format")
        }
    }
}