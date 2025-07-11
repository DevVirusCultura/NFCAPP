package com.nfcscanner.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nfcscanner.app.data.models.AppSettings
import com.nfcscanner.app.data.models.NFCRecord
import com.nfcscanner.app.data.models.NFCStats
import com.nfcscanner.app.data.models.NFCTag
import java.util.Date

/**
 * Database Room per NFC Scanner
 */
@Database(
    entities = [NFCTag::class, NFCStats::class, AppSettings::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NFCDatabase : RoomDatabase() {
    
    abstract fun nfcTagDao(): NFCTagDao
    abstract fun statsDao(): NFCStatsDao
    abstract fun settingsDao(): AppSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: NFCDatabase? = null
        
        fun getDatabase(context: Context): NFCDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NFCDatabase::class.java,
                    "nfc_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Type converters per Room
 */
class Converters {
    
    private val gson = Gson()
    
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromNFCRecordList(value: List<NFCRecord>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toNFCRecordList(value: String): List<NFCRecord> {
        val listType = object : TypeToken<List<NFCRecord>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
    
    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toStringIntMap(value: String): Map<String, Int> {
        val mapType = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(value, mapType) ?: emptyMap()
    }
}