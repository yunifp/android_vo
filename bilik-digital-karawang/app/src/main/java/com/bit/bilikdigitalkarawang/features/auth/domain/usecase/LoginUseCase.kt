package com.bit.bilikdigitalkarawang.features.auth.domain.usecase

import android.content.Context
import android.provider.Settings
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.auth.data.source.remote.dto.LoginResponse
import com.bit.bilikdigitalkarawang.features.auth.domain.model.form_data.LoginFormData
import com.bit.bilikdigitalkarawang.features.auth.domain.repository.AuthRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val dataStoreDiv: DataStoreDiv,
    @ApplicationContext private val context: Context
) {
    operator fun invoke(formDatax: LoginFormData): Flow<Resource<LoginResponse>> = flow {
        try {
            emit(Resource.Loading())

            val deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            val formData = formDatax.copy(deviceId = deviceId)

            val result = repository.login(formData)
            if (!result.success) {
                throw Exception(result.message)
            } else {

                dataStoreDiv.bulkSaveData(mapOf(
                    "has_login" to "Y",
                    "sesi_token" to result.dataDto.token,
                    "sesi_kode_kec" to result.dataDto.userDto.kodeKec,
                    "sesi_nama_kec" to result.dataDto.userDto.namaKec,
                    "sesi_kode_kel" to result.dataDto.userDto.kodeKel,
                    "sesi_nama_kel" to result.dataDto.userDto.namaKel,
                    "sesi_nama" to result.dataDto.userDto.nama,
                    "sesi_nama_pemilihan" to result.dataDto.userDto.namaPemilihan,
                    "sesi_id_tps" to result.dataDto.userDto.idTps,
                    "sesi_tps_no" to result.dataDto.userDto.tpsNo,
                    "sesi_bilik_no" to result.dataDto.userDto.bilikNo,
                    "sudah_ganti_kertas" to "Y"
                ))

                emit(Resource.Success(result))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Jaringan internet tidak tersedia"))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "Tidak dapat menjangkau server"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
        }
    }

}