package com.bit.bilikdigitalkarawang.features.auth.di

import com.bit.bilikdigitalkarawang.features.auth.data.repository.AuthRepositoryImpl
import com.bit.bilikdigitalkarawang.features.auth.domain.repository.AuthRepository
import com.bit.bilikdigitalkarawang.shared.data.source.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: ApiService
    ): AuthRepository {
        return AuthRepositoryImpl(api)
    }

}