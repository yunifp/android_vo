package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import android.content.Context
import android.provider.Settings
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.UserInfo
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val dataStoreDiv: DataStoreDiv,
    @ApplicationContext private val context: Context
) {
    operator fun invoke(): Flow<Resource<UserInfo>> = flow {
        try {
            emit(Resource.Loading())

            val nama = dataStoreDiv.getData("sesi_nama").first() ?: ""
            val namaKec = dataStoreDiv.getData("sesi_nama_kec").first() ?: ""
            val namaKel = dataStoreDiv.getData("sesi_nama_kel").first() ?: ""
            val tpsNo = dataStoreDiv.getData("sesi_tps_no").first() ?: ""
            val bilikNo = dataStoreDiv.getData("sesi_bilik_no").first() ?: ""

            val deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )

            val userInfo = UserInfo(
                namaKec = namaKec,
                namaKel = namaKel,
                nama = nama,
                deviceId = deviceId,
                tpsNo = tpsNo,
                bilikNo = bilikNo
            )

            emit(Resource.Success(userInfo))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal mengambil data user"))
        }
    }
}
