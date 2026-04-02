package com.bit.bilikdigitalkarawang.shared.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("data_bilik_digital")
        }
    }

    @Provides
    @Singleton
    fun provideStoreUserInfo(dataStore: DataStore<Preferences>): DataStoreDiv {
        return DataStoreDiv(dataStore)
    }
}