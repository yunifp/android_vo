package com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository

import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.ListVoteResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.PostVoteRequest
import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.PostVoteResponse
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilih
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilihan
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihanEntity
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SuaraSah
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SyncRekap
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SyncRow
import kotlinx.coroutines.flow.Flow

interface PemilihanRepository {

    // Untuk mendapatkan data (cache-first)
    fun getKandidat(): Flow<List<Kandidat>>
    fun getPemilih(): Flow<List<Pemilih>>
    fun getPemilihan(): Flow<List<Pemilihan>>
    suspend fun getPemilihByNik(nik: String): Pemilih?

    // Untuk download dan sync data
    suspend fun downloadKandidat(token: String): CommonStatus
    suspend fun downloadPemilih(token: String): CommonStatus

    suspend fun hasAlreadyVoted(nik: String): Boolean

    suspend fun detailPemilihan(nik: String): Pemilihan?

    suspend fun insertPemilihan(pemilihan: PemilihanEntity)

    suspend fun postVote(token: String, pemilihan: PostVoteRequest): PostVoteResponse

    suspend fun getListVote(token: String, deviceId: String): ListVoteResponse

    fun getJumlahSuaraSahPerKandidat(): Flow<List<SuaraSah>>

    fun getJumlahPemilih(): Flow<Int>

    fun getJumlahSah(): Flow<Int>

    fun getJumlahSahLaki(): Flow<Int>

    fun getJumlahSahPerempuan(): Flow<Int>

    fun getJumlahTidakSah(): Flow<Int>

    fun getJumlahTidakSahLaki(): Flow<Int>

    fun getJumlahTidakSahPerempuan(): Flow<Int>

    fun getJumlahAbstain(): Flow<Int>

    fun getJumlahAbstainLaki(): Flow<Int>

    fun getJumlahAbstainPerempuan(): Flow<Int>

    fun getPemilihByKandidat(noUrut: Int): Flow<List<PemilihanEntity>>

    suspend fun getTotalPemilihan(): Int

    suspend fun syncRekap(token: String, data: SyncRekap): CommonStatus

    suspend fun syncRow(token: String, data: SyncRow): CommonStatus

    suspend fun resetPemilihan(): CommonStatus

    suspend fun checkHasPrintUlang(nik: String): Boolean

    suspend fun updateHasPrintUlang(nik: String): CommonStatus

    fun getLastRowPemilihan(): Flow<String?>

}