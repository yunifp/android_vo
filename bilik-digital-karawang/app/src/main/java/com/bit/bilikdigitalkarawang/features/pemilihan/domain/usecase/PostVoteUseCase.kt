package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import android.util.Log
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.PostVoteRequest
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class PostVoteUseCase @Inject constructor(
    private val repository: PemilihanRepository,
    private val getDetailPemilihUseCase: GetDetailPemilihUseCase,
    private val dataStoreDiv: DataStoreDiv
) {
    operator fun invoke(
        nik: String,
        noUrut: List<String>,
        namaKandidat: List<String>
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        val token = dataStoreDiv.getData("sesi_token").first() ?: ""

        var idDpt: String? = null
        getDetailPemilihUseCase(nik).collect { result ->
            if (result is Resource.Success && result.data != null) {
                idDpt = result.data.idDpt
            } else if (result is Resource.Error) {
                emit(Resource.Error("Gagal mengambil data pemilih."))
                return@collect
            }
        }

        val idStatus = when {
            noUrut.isEmpty() -> 3
            noUrut.size > 1 -> 2
            else -> 1
        }

        val pemilihan = PostVoteRequest(
            nik = nik,
            noUrut = if (idStatus == 1) noUrut.first() else null,
            idStatus = idStatus,
            idDpt = idDpt ?: ""
        )

        val response = repository.postVote(token, pemilihan)

        Log.d("ALDDDY", response.toString())

        emit(Resource.Success(Unit))
    }.catch { e ->
        val message = when (e) {
            is java.net.UnknownHostException ->
                "Tidak ada koneksi internet. Periksa jaringan Anda dan coba lagi."
            is java.net.SocketTimeoutException ->
                "Waktu koneksi habis. Server tidak merespons, silakan coba lagi."
            is retrofit2.HttpException -> {
                when (e.code()) {
                    400 -> "Permintaan tidak valid. Silakan periksa data yang dikirim."
                    401 -> "Sesi Anda telah berakhir. Silakan login kembali."
                    403 -> "Anda tidak memiliki izin untuk melakukan tindakan ini."
                    404 -> "Data yang diminta tidak ditemukan di server."
                    500 -> "Terjadi kesalahan pada server. Coba lagi nanti."
                    else -> "Terjadi kesalahan server (${e.code()}). Coba lagi nanti."
                }
            }
            is com.google.gson.JsonSyntaxException ->
                "Terjadi kesalahan saat memproses data dari server."
            else ->
                e.localizedMessage ?: "Terjadi kesalahan yang tidak diketahui."
        }

        emit(Resource.Error(message))
    }
}
