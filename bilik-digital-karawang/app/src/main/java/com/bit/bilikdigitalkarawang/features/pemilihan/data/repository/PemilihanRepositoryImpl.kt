package com.bit.bilikdigitalkarawang.features.pemilihan.data.repository

import android.content.Context
import android.util.Log
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.Constant
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.ListVoteResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.PostVoteRequest
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.PostVoteResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper.toEntity
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper.toKandidat
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper.toPemilih
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper.toPemilihan
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper.toRequest
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper.toSuaraSah
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilih
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilihan
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.helpers.FotoHelper
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.KandidatDao
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.PemilihDao
import com.bit.bilikdigitalkarawang.shared.data.source.local.dao.PemilihanDao
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihanEntity
import com.bit.bilikdigitalkarawang.shared.data.source.remote.ApiService
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SuaraSah
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SyncRekap
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SyncRow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class PemilihanRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val kandidatDao: KandidatDao,
    private val pemilihDao: PemilihDao,
    private val pemilihanDao: PemilihanDao,
    @ApplicationContext private val context: Context
) : PemilihanRepository {

    override fun getKandidat(): Flow<List<Kandidat>> =
        kandidatDao.getAllKandidat().map { entities ->
            entities.map { it.toKandidat() }
        }

    override fun getPemilih(): Flow<List<Pemilih>> =
        pemilihDao.getAllPemilih().map { entities ->
            entities.map { it.toPemilih() }
        }

    override fun getPemilihan(): Flow<List<Pemilihan>> =
        pemilihanDao.getAllPemilihan().map { entities ->
            entities.map { it.toPemilihan() }
        }

    override suspend fun getPemilihByNik(nik: String): Pemilih? =
        pemilihDao.getPemilihByNik(nik)?.toPemilih()

    override suspend fun downloadKandidat(token: String): CommonStatus {
        return try {
            val response = apiService.getKandidat(token)

            if (response.success) {
                // Clear old data and insert new data
                kandidatDao.clearAll()
                val entities = response.kandidatSumDto.kandidatDtos.map { dto ->
                    val localImagePath = withContext(Dispatchers.IO) {
                        FotoHelper.saveImageToInternalStorage(context, dto.foto)
                    }
                    dto.toEntity(localImagePath)
                }
                kandidatDao.insertAllKandidat(entities)

                CommonStatus.Success
            } else {
                CommonStatus.Error
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(Constant.LOG_TAG, e.localizedMessage)
            CommonStatus.Error
        }
    }

    override suspend fun downloadPemilih(token: String): CommonStatus {
        return try {
            val response = apiService.getPemilih(token)

            if (response.success) {
                // Clear old data and insert new data
                pemilihDao.clearAll()
                val entities = response.pemilihSumDto.pemilihDto.map { it.toEntity() }
                pemilihDao.insertAllPemilih(entities)

                CommonStatus.Success
            } else {
                CommonStatus.Error
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(Constant.LOG_TAG, e.toString())
            CommonStatus.Error
        }
    }

    override suspend fun hasAlreadyVoted(nik: String): Boolean {
        return pemilihanDao.getPemilihanByNik(nik) != null
    }

    override suspend fun detailPemilihan(nik: String): Pemilihan? {
        return pemilihanDao.getPemilihanByNik(nik)?.toPemilihan()
    }

    override suspend fun insertPemilihan(pemilihan: PemilihanEntity) {
        pemilihanDao.insertOrUpdateByIdDpt(pemilihan)
    }

    override suspend fun postVote(token: String, pemilihan: PostVoteRequest): PostVoteResponse {
        val idDptBody = pemilihan.idDpt.toRequestBody("text/plain".toMediaType())
        val nikBody = pemilihan.nik.toRequestBody("text/plain".toMediaType())
        val noUrutBody = pemilihan.noUrut?.toRequestBody("text/plain".toMediaType())
            ?: "".toRequestBody("text/plain".toMediaType()) // kosong jika null
        val statusBody = pemilihan.idStatus.toString().toRequestBody("text/plain".toMediaType())

        return apiService.postVote(
            authorization = token,
            idDpt = idDptBody,
            nik = nikBody,
            noUrut = noUrutBody,
            status = statusBody
        )
    }

    override suspend fun getListVote(token: String, deviceId: String): ListVoteResponse {
        return apiService.getListVote(token, deviceId.toRequestBody())
    }

    override fun getJumlahSuaraSahPerKandidat(): Flow<List<SuaraSah>> {
        return pemilihanDao.getJumlahSuaraSahPerKandidat()
            .map { list -> list.map { it.toSuaraSah() } }
    }

    override fun getJumlahPemilih(): Flow<Int> {
        return pemilihDao.countAllPemilih()
    }

    override fun getJumlahSah(): Flow<Int> {
        return pemilihanDao.getJumlahSah()
    }

    override fun getJumlahSahLaki(): Flow<Int> {
        return pemilihanDao.getJumlahSahLaki()
    }

    override fun getJumlahSahPerempuan(): Flow<Int> {
        return pemilihanDao.getJumlahSahPerempuan()
    }

    override fun getJumlahTidakSah(): Flow<Int> {
        return pemilihanDao.getJumlahTidakSah()
    }

    override fun getJumlahTidakSahLaki(): Flow<Int> {
        return pemilihanDao.getJumlahTidakSahLaki()
    }

    override fun getJumlahTidakSahPerempuan(): Flow<Int> {
        return pemilihanDao.getJumlahTidakSahPerempuan()
    }

    override fun getJumlahAbstain(): Flow<Int> {
        return pemilihanDao.getJumlahAbstain()
    }

    override fun getJumlahAbstainLaki(): Flow<Int> {
        return pemilihanDao.getJumlahAbstainLaki()
    }

    override fun getJumlahAbstainPerempuan(): Flow<Int> {
        return pemilihanDao.getJumlahAbstainPerempuan()
    }

    override fun getPemilihByKandidat(noUrut: Int): Flow<List<PemilihanEntity>> {
        return pemilihanDao.getPemilihByKandidat(noUrut)
    }

    override suspend fun getTotalPemilihan(): Int {
        return pemilihanDao.getTotalPemilihan()
    }

    override suspend fun syncRekap(token: String, data: SyncRekap): CommonStatus {
        return try {
            val request = data.toRequest()

            val response = apiService.syncRekap(
                authorization = token,
                jumlahPartisipasi = request.jumlahPartisipasi,
                suaraSah = request.suaraSah,
                suaraTidakSah = request.suaraTidakSah,
                suaraAbstain = request.suaraAbstain,
                jumlahPemilihNoUrut1 = request.suaraPerKandidat.getOrNull(0) ?: "0".toRequestBody("text/plain".toMediaType()),
                jumlahPemilihNoUrut2 = request.suaraPerKandidat.getOrNull(1) ?: "0".toRequestBody("text/plain".toMediaType()),
                jumlahPemilihNoUrut3 = request.suaraPerKandidat.getOrNull(2) ?: "0".toRequestBody("text/plain".toMediaType()),
                jumlahPemilihNoUrut4 = request.suaraPerKandidat.getOrNull(3) ?: "0".toRequestBody("text/plain".toMediaType()),
                jumlahPemilihNoUrut5 = request.suaraPerKandidat.getOrNull(4) ?: "0".toRequestBody("text/plain".toMediaType()),
                jumlahPemilihNoUrut6 = request.suaraPerKandidat.getOrNull(5) ?: "0".toRequestBody("text/plain".toMediaType()),
                jumlahPemilihNoUrut7 = request.suaraPerKandidat.getOrNull(6) ?: "0".toRequestBody("text/plain".toMediaType()),
                jumlahPemilihNoUrut8 = request.suaraPerKandidat.getOrNull(7) ?: "0".toRequestBody("text/plain".toMediaType()),
            )

            if (response.success) {
                CommonStatus.Success
            } else {
                CommonStatus.Error
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CommonStatus.Error
        }
    }

    override suspend fun syncRow(token: String, data: SyncRow): CommonStatus {
        return try {
            val request = data.toRequest()

            val response = apiService.syncRow(
                authorization = token,
                votes = request.votes
            )

            Log.d(Constant.LOG_TAG, response.message)

            if (response.success) {
                CommonStatus.Success
            } else {
                CommonStatus.Error
            }
        } catch (e: Exception) {
            e.printStackTrace()

            CommonStatus.Error
        }
    }

    override suspend fun resetPemilihan(): CommonStatus {
        try {
            pemilihanDao.resetPemilihan()
            return CommonStatus.Success
        } catch (e: Exception) {
            return CommonStatus.Error
        }
    }

    override suspend fun checkHasPrintUlang(nik: String): Boolean {
        return pemilihanDao.hasPrintUlang(nik) == 1
    }

    override suspend fun updateHasPrintUlang(nik: String): CommonStatus {
        try {
            pemilihanDao.updateHasPrintUlang(nik)
            return CommonStatus.Success
        } catch (e: Exception) {
            return CommonStatus.Error
        }
    }

    override fun getLastRowPemilihan(): Flow<String?> {
        return pemilihanDao.getNikLastRowPemilihan()
            .catch { emit(null) } // atau emit("Gagal") jika mau
    }


}