package com.bit.bilikdigitalkarawang.features.kelola_perangkat.di

import android.content.Context
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.repository.KelolaPerangkatRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBluetoothRepository(
        @ApplicationContext context: Context,
        dataStoreDiv: DataStoreDiv
    ): KelolaPerangkatRepository {
        return KelolaPerangkatRepository(context, dataStoreDiv)
    }
}