package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Constant
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihanEntity
import com.bit.bilikdigitalkarawang.utils.PaillierHE
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
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

        // Tentukan Status (1 = Sah, 2 = Tidak Sah, 3 = Abstain)
        val idStatus = when {
            noUrut.isEmpty() -> 3
            noUrut.size > 1 -> 2
            else -> 1
        }

        // ====================================================================
        // PROSES HOMOMORPHIC ENCRYPTION SAAT PEMILIH MENCOBLOS
        // ====================================================================
        PaillierHE.generateKeys() // Pastikan Kunci HE sudah siap di memory

        val mapSuaraHE = mutableMapOf<String, String>()
        val chosenKandidatId = if (idStatus == 1) noUrut.first() else null

        // Ambil daftar kandidat dari lokal DB untuk membangun Peta (Map) Suara
        val daftarKandidat = repository.getKandidat().first()

        daftarKandidat.forEach { kandidat ->
            if (kandidat.noUrut == chosenKandidatId) {
                // Jika kandidat ini yang dipilih, simpan angka 1 terenkripsi
                mapSuaraHE[kandidat.noUrut] = PaillierHE.encrypt(1)
            } else {
                // Jika tidak dipilih (atau jika suara tidak sah/abstain), simpan 0 terenkripsi
                mapSuaraHE[kandidat.noUrut] = PaillierHE.encrypt(0)
            }
        }

        // Konversi Map Enkripsi ke bentuk JSON String
        val heVotesMapJson = Gson().toJson(mapSuaraHE)
        // ====================================================================

        // Simpan Entitas Baru dengan format Homomorfik (heVotesMap)
        val pemilihan = PemilihanEntity(
            nik = nik,
            heVotesMap = heVotesMapJson, // Gantikan noUrut dan namaKandidat dengan Peta HE
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