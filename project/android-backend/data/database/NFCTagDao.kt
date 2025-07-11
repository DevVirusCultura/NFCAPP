package com.nfcscanner.app.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.nfcscanner.app.data.models.NFCTag
import kotlinx.coroutines.flow.Flow

/**
 * DAO per operazioni sui tag NFC
 */
@Dao
interface NFCTagDao {
    
    @Query("SELECT * FROM nfc_tags ORDER BY timestamp DESC")
    fun getAllTags(): Flow<List<NFCTag>>
    
    @Query("SELECT * FROM nfc_tags ORDER BY timestamp DESC")
    fun getAllTagsLiveData(): LiveData<List<NFCTag>>
    
    @Query("SELECT * FROM nfc_tags WHERE id = :tagId")
    suspend fun getTagById(tagId: String): NFCTag?
    
    @Query("SELECT * FROM nfc_tags WHERE id = :tagId")
    fun getTagByIdLiveData(tagId: String): LiveData<NFCTag?>
    
    @Query("""
        SELECT * FROM nfc_tags 
        WHERE id LIKE '%' || :query || '%' 
        OR type LIKE '%' || :query || '%'
        OR uid LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchTags(query: String): Flow<List<NFCTag>>
    
    @Query("SELECT * FROM nfc_tags WHERE isVirtual = 1 ORDER BY timestamp DESC")
    fun getVirtualTags(): Flow<List<NFCTag>>
    
    @Query("SELECT COUNT(*) FROM nfc_tags")
    suspend fun getTagCount(): Int
    
    @Query("SELECT COUNT(*) FROM nfc_tags WHERE DATE(timestamp/1000, 'unixepoch') = DATE('now')")
    suspend fun getTodayTagCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: NFCTag)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<NFCTag>)
    
    @Update
    suspend fun updateTag(tag: NFCTag)
    
    @Delete
    suspend fun deleteTag(tag: NFCTag)
    
    @Query("DELETE FROM nfc_tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: String)
    
    @Query("DELETE FROM nfc_tags")
    suspend fun deleteAllTags()
    
    @Query("DELETE FROM nfc_tags WHERE isVirtual = 1")
    suspend fun deleteAllVirtualTags()
    
    @Query("""
        DELETE FROM nfc_tags 
        WHERE id NOT IN (
            SELECT id FROM nfc_tags 
            ORDER BY timestamp DESC 
            LIMIT :maxSize
        )
    """)
    suspend fun limitHistorySize(maxSize: Int)
    
    @Query("SELECT * FROM nfc_tags ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentTags(limit: Int): List<NFCTag>
    
    @Query("""
        SELECT type, COUNT(*) as count 
        FROM nfc_tags 
        WHERE type IS NOT NULL 
        GROUP BY type 
        ORDER BY count DESC
    """)
    suspend fun getTagTypeStats(): Map<String, Int>
}

/**
 * DAO per statistiche
 */
@Dao
interface NFCStatsDao {
    
    @Query("SELECT * FROM nfc_stats WHERE id = 1")
    suspend fun getStats(): com.nfcscanner.app.data.models.NFCStats?
    
    @Query("SELECT * FROM nfc_stats WHERE id = 1")
    fun getStatsLiveData(): LiveData<com.nfcscanner.app.data.models.NFCStats?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: com.nfcscanner.app.data.models.NFCStats)
    
    @Update
    suspend fun updateStats(stats: com.nfcscanner.app.data.models.NFCStats)
    
    @Query("DELETE FROM nfc_stats")
    suspend fun clearStats()
}

/**
 * DAO per impostazioni
 */
@Dao
interface AppSettingsDao {
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): com.nfcscanner.app.data.models.AppSettings?
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettingsLiveData(): LiveData<com.nfcscanner.app.data.models.AppSettings?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: com.nfcscanner.app.data.models.AppSettings)
    
    @Update
    suspend fun updateSettings(settings: com.nfcscanner.app.data.models.AppSettings)
    
    @Query("DELETE FROM app_settings")
    suspend fun clearSettings()
}