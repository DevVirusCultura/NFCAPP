package com.nfcscanner.app.di

import android.content.Context
import androidx.room.Room
import com.nfcscanner.app.data.database.AppSettingsDao
import com.nfcscanner.app.data.database.NFCDatabase
import com.nfcscanner.app.data.database.NFCStatsDao
import com.nfcscanner.app.data.database.NFCTagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Modulo Hilt per dependency injection del database
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideNFCDatabase(@ApplicationContext context: Context): NFCDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            NFCDatabase::class.java,
            "nfc_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    
    @Provides
    fun provideNFCTagDao(database: NFCDatabase): NFCTagDao {
        return database.nfcTagDao()
    }
    
    @Provides
    fun provideNFCStatsDao(database: NFCDatabase): NFCStatsDao {
        return database.statsDao()
    }
    
    @Provides
    fun provideAppSettingsDao(database: NFCDatabase): AppSettingsDao {
        return database.settingsDao()
    }
}