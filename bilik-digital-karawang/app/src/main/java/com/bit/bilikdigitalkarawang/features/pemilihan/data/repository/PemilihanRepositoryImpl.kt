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
            CommonStatus.Error
        }
    }

    override suspend fun downloadPemilih(token: String): CommonStatus {
        return try {
            val response = apiService.getPemilih(token)
            if (response.success) {
                pemilihDao.clearAll()
                val entities = response.pemilihSumDto.pemilihDto.map { it.toEntity() }
                pemilihDao.insertAllPemilih(entities)
                CommonStatus.Success
            } else {
                CommonStatus.Error
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
            ?: "".toRequestBody("text/plain".toMediaType())
        val statusBody = pemilihan.idStatus.toString().toRequestBody("text/plain".toMediaType())

        return apiService.postVote(token, idDptBody, nikBody, noUrutBody, statusBody)
    }

    override suspend fun getListVote(token: String, deviceId: String): ListVoteResponse {
        return apiService.getListVote(token, deviceId.toRequestBody())
    }

    override fun getJumlahPemilih(): Flow<Int> = pemilihDao.countAllPemilih()
    override fun getJumlahSah(): Flow<Int> = pemilihanDao.getJumlahSah()
    override fun getJumlahSahLaki(): Flow<Int> = pemilihanDao.getJumlahSahLaki()
    override fun getJumlahSahPerempuan(): Flow<Int> = pemilihanDao.getJumlahSahPerempuan()
    override fun getJumlahTidakSah(): Flow<Int> = pemilihanDao.getJumlahTidakSah()
    override fun getJumlahTidakSahLaki(): Flow<Int> = pemilihanDao.getJumlahTidakSahLaki()
    override fun getJumlahTidakSahPerempuan(): Flow<Int> = pemilihanDao.getJumlahTidakSahPerempuan()
    override fun getJumlahAbstain(): Flow<Int> = pemilihanDao.getJumlahAbstain()
    override fun getJumlahAbstainLaki(): Flow<Int> = pemilihanDao.getJumlahAbstainLaki()
    override fun getJumlahAbstainPerempuan(): Flow<Int> = pemilihanDao.getJumlahAbstainPerempuan()
    override suspend fun getTotalPemilihan(): Int = pemilihanDao.getTotalPemilihan()

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
            if (response.success) CommonStatus.Success else CommonStatus.Error
        } catch (e: Exception) {
            CommonStatus.Error
        }
    }

    override suspend fun syncRow(token: String, data: SyncRow): CommonStatus {
        return try {
            val response = apiService.syncRow(token, data.toRequest().votes)
            if (response.success) CommonStatus.Success else CommonStatus.Error
        } catch (e: Exception) {
            CommonStatus.Error
        }
    }

    override suspend fun resetPemilihan(): CommonStatus {
        return try {
            pemilihanDao.resetPemilihan()
            CommonStatus.Success
        } catch (e: Exception) {
            CommonStatus.Error
        }
    }

    override suspend fun checkHasPrintUlang(nik: String): Boolean = pemilihanDao.hasPrintUlang(nik) == 1

    override suspend fun updateHasPrintUlang(nik: String): CommonStatus {
        return try {
            pemilihanDao.updateHasPrintUlang(nik)
            CommonStatus.Success
        } catch (e: Exception) {
            CommonStatus.Error
        }
    }

    override fun getLastRowPemilihan(): Flow<String?> = pemilihanDao.getNikLastRowPemilihan().catch { emit(null) }
}