package com.bit.bilikdigitalkarawang.shared.domain.usecase

import android.content.Context
import javax.inject.Inject
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext

class GetDeviceIdUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}