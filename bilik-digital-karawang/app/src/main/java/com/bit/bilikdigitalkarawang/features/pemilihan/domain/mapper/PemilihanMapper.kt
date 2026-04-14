package com.bit.bilikdigitalkarawang.features.pemilihan.domain.mapper

import com.bit.bilikdigitalkarawang.features.pemilihan.data.resource.remote.dto.VoteDto
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilihan
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihanEntity
import com.bit.bilikdigitalkarawang.utils.PaillierHE
import com.google.gson.Gson

fun PemilihanEntity.toPemilihan(): Pemilihan {
    return Pemilihan(
        id = id,
        nik = nik,
        idDpt = idDpt,
        heVotesMap = heVotesMap, // Gunakan heVotesMap
        idStatus = idStatus,
        jenisKelamin = jenisKelamin,
        hasPrintUlang = hasPrintUlang
    )
}

// Catatan Penting: Untuk mapPemilihanToSyncFormat, jika server Anda TIDAK MENDUKUNG
// menerima data terenkripsi homomorfik (heVotesMap), maka Anda tidak bisa men-sync data ini ke server.
// Jika server mendukung, kirim heVotesMap. Di sini saya asumsikan server akan menerima string json heVotesMap
// di dalam field "no_urut" sebagai workaround sementara.
fun mapPemilihanToSyncFormat(pemilihanList: List<Pemilihan>): String {
    val mappedList = pemilihanList.map {
        mapOf(
            "id_dpt" to (it.idDpt ?: ""),
            "nik" to (it.nik ?: ""),
            // Mengirim string HE ke server melalui parameter no_urut
            "no_urut" to (it.heVotesMap ?: ""),
            "status" to (it.idStatus?.toString() ?: ""),
            "jenis_kelamin" to (it.jenisKelamin?.toString() ?: "")
        )
    }
    return Gson().toJson(mappedList)
}

// Fungsi ini membutuhkan list ID kandidat untuk membuat HE Map.
// Jika Anda memanggil ini dari DownloadListVoteUseCase, pastikan Anda juga
// men-fetch daftar kandidat dan melempar ID-nya ke sini.
fun VoteDto.toPemilihanEntity(daftarKandidatIds: List<String>): PemilihanEntity {
    // Pastikan key HE sudah di-generate
    PaillierHE.generateKeys()

    // Buat map suara HE (1 untuk paslon yang dipilih, 0 untuk sisanya)
    val mapSuaraHE = mutableMapOf<String, String>()

    daftarKandidatIds.forEach { kandidatId ->
        if (this.noUrut == kandidatId) {
            mapSuaraHE[kandidatId] = PaillierHE.encrypt(1)
        } else {
            mapSuaraHE[kandidatId] = PaillierHE.encrypt(0)
        }
    }

    return PemilihanEntity(
        nik = this.nik ?: "",
        idDpt = this.idDpt ?: "",
        heVotesMap = Gson().toJson(mapSuaraHE),
        idStatus = this.status.toIntOrNull() ?: 0,
        jenisKelamin = this.jenisKelamin ?: "",
        hasPrintUlang = 0
    )
}

// FUNGSI OVERLOAD: Jika Anda terpaksa tidak bisa memberikan daftarKandidatIds,
// misal saat download awal belum ada kandidat, simpan noUrut di-enkripsi tunggal,
// NAMUN ini AKAN MERUSAK GetSuaraPerKandidatUseCase.
// SANGAT DISARANKAN untuk selalu mengirim daftarKandidatIds.
fun VoteDto.toPemilihanEntitySingleHE(): PemilihanEntity {
    PaillierHE.generateKeys()
    // Hanya enkripsi satu angka saja
    val encryptedSingleVote = PaillierHE.encrypt(this.noUrut.toIntOrNull() ?: 0)

    return PemilihanEntity(
        nik = this.nik ?: "",
        idDpt = this.idDpt ?: "",
        heVotesMap = encryptedSingleVote, // Bahaya: ini format berbeda dengan Map di atas!
        idStatus = this.status.toIntOrNull() ?: 0,
        jenisKelamin = this.jenisKelamin ?: "",
        hasPrintUlang = 0
    )
}