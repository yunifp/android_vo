package com.bit.bilikdigitalkarawang.features.pemilihan.di

import android.content.Context
import com.bit.bilikdigitalkarawang.features.pemilihan.data.repository.PemilihanRepositoryImpl
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.KandidatDao
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.PemilihDao
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.PemilihanDao
import com.bit.bilikdigitalkarawang.shared.data.source.remote.ApiService
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
    fun providePemilihanRepository(
        api: ApiService,
        kandidatDao: KandidatDao,
        pemilihDao: PemilihDao,
        pemilihanDao: PemilihanDao,
        @ApplicationContext context: Context
    ): PemilihanRepository {
        return PemilihanRepositoryImpl(api, kandidatDao, pemilihDao, pemilihanDao, context)
    }

}