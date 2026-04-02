package com.bit.bilikdigitalkarawang.shared.di

import android.content.Context
import androidx.room.Room
import com.bit.bilikdigitalkarawang.shared.data.source.local.LocalDbService
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.KandidatDao
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.PemilihDao
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.PemilihanDao
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDbModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LocalDbService {
        return Room.databaseBuilder(
            context,
            LocalDbService::class.java,
            "hitung_suara_db"
        )
            .fallbackToDestructiveMigration() // Drops and rebuilds DB if migration is missing
            .build()
    }

    @Provides
    @Singleton
    fun provideKandidatDao(database: LocalDbService): KandidatDao {
        return database.kandidatDao()
    }

    @Provides
    @Singleton
    fun providePemilihDao(database: LocalDbService): PemilihDao {
        return database.pemilihDao()
    }

    @Provides
    @Singleton
    fun providePemilihanDao(database: LocalDbService): PemilihanDao {
        return database.pemilihanDao()
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .create()
    }
}