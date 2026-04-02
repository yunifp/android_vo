package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Constant
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihanEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class InsertPemilihanUseCase @Inject constructor(
    private val repository: PemilihanRepository,
    private val getTotalPemilihanUseCase: GetTotalPemilihanUseCase,
    private val getDetailPemilihUseCase: GetDetailPemilihUseCase,
    private val dataStoreDiv: DataStoreDiv,
) {
    operator fun invoke(
        nik: String,
        noUrut: List<String>,
        namaKandidat: List<String>
    ): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        if (repository.hasAlreadyVoted(nik)) {
            emit(Resource.Error("Anda sudah melakukan pemilihan"))
            return@flow
        }

        var idDpt: String? = null
        var jenisKelamin: String = ""
        getDetailPemilihUseCase(nik).collect { result ->
            if (result is Resource.Success && result.data != null) {
                idDpt = result.data.idDpt
                jenisKelamin = result.data.jenisKelamin
            } else if (result is Resource.Error) {
                emit(Resource.Error("Gagal mengambil data pemilih"))
                return@collect
            }
        }

        val idStatus = when {
            noUrut.isEmpty() -> 3
            noUrut.size > 1 -> 2
            else -> 1
        }

        val pemilihan = PemilihanEntity(
            nik = nik,
            noUrut = if (idStatus == 1) noUrut.first() else null,
            namaKandidat = if (idStatus == 1) namaKandidat.first() else null,
            idStatus = idStatus,
            idDpt = idDpt ?: "",
            jenisKelamin = jenisKelamin
        )

        repository.insertPemilihan(pemilihan)

        getTotalPemilihanUseCase().collect { totalResult ->
            if (
                totalResult is Resource.Success &&
                totalResult.data?.rem(Constant.TOTAL_JUMLAH_PEMILIH_PER_SESI_KERTAS) == 0
            ) {
                dataStoreDiv.saveData("sudah_ganti_kertas", "N")
            }
        }
        // LANGSUNG EMIT SUCCESS TANPA MENUNGGU EXPORT SELESAI
        emit(Resource.Success(Unit))

    }.catch { e ->
        emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan"))
    }
}