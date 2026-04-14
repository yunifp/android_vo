package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.SuaraSah
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.utils.PaillierHE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetSuaraPerKandidatUseCase @Inject constructor(
    private val repository: PemilihanRepository
) {
    operator fun invoke(): Flow<Resource<List<SuaraSah>>> = flow {
        emit(Resource.Loading())
        try {
            // Ambil semua data pemilihan sah dan daftar kandidat
            val semuaPemilihan = repository.getPemilihan().first().filter { it.idStatus == 1 }
            val daftarKandidat = repository.getKandidat().first()

            // Siapkan Map untuk mengakumulasi Ciphertext per kategori
            val accumulatedCipherTotal = mutableMapOf<String, String>()
            val accumulatedCipherLaki = mutableMapOf<String, String>()
            val accumulatedCipherPerempuan = mutableMapOf<String, String>()

            val gson = Gson()
            val type = object : TypeToken<Map<String, String>>() {}.type

            // LAKUKAN HOMOMORPHIC ADDITION
            semuaPemilihan.forEach { pemilihan ->
                try {
                    // Parsing JSON peta suara dari heVotesMap
                    val mapSuaraPemilih: Map<String, String> = gson.fromJson(pemilihan.heVotesMap, type)

                    val isLaki = pemilihan.jenisKelamin == "L"
                    val isPerempuan = pemilihan.jenisKelamin == "P"

                    mapSuaraPemilih.forEach { (kandidatNoUrut, ciphertextVote) ->
                        // 1. Tambahkan ke Total Cipher
                        if (accumulatedCipherTotal.containsKey(kandidatNoUrut)) {
                            accumulatedCipherTotal[kandidatNoUrut] = PaillierHE.addEncryptedVotes(
                                accumulatedCipherTotal[kandidatNoUrut]!!,
                                ciphertextVote
                            )
                        } else {
                            accumulatedCipherTotal[kandidatNoUrut] = ciphertextVote
                        }

                        // 2. Tambahkan ke Cipher Laki-Laki
                        if (isLaki) {
                            if (accumulatedCipherLaki.containsKey(kandidatNoUrut)) {
                                accumulatedCipherLaki[kandidatNoUrut] = PaillierHE.addEncryptedVotes(
                                    accumulatedCipherLaki[kandidatNoUrut]!!,
                                    ciphertextVote
                                )
                            } else {
                                accumulatedCipherLaki[kandidatNoUrut] = ciphertextVote
                            }
                        }

                        // 3. Tambahkan ke Cipher Perempuan
                        if (isPerempuan) {
                            if (accumulatedCipherPerempuan.containsKey(kandidatNoUrut)) {
                                accumulatedCipherPerempuan[kandidatNoUrut] = PaillierHE.addEncryptedVotes(
                                    accumulatedCipherPerempuan[kandidatNoUrut]!!,
                                    ciphertextVote
                                )
                            } else {
                                accumulatedCipherPerempuan[kandidatNoUrut] = ciphertextVote
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Abaikan jika ada 1 baris yang corrupt agar tidak crash
                    e.printStackTrace()
                }
            }

            // DEKRIPSI HASIL AKHIR REKAPITULASI
            val hasilAkhir = mutableListOf<SuaraSah>()

            daftarKandidat.forEach { kandidat ->
                val finalCipherTotal = accumulatedCipherTotal[kandidat.noUrut]
                val finalCipherLaki = accumulatedCipherLaki[kandidat.noUrut]
                val finalCipherPerempuan = accumulatedCipherPerempuan[kandidat.noUrut]

                // Dekripsi Total
                val totalSuaraPlaintext = if (finalCipherTotal != null) {
                    try { PaillierHE.decrypt(finalCipherTotal) } catch (e: Exception) { 0 }
                } else { 0 }

                // Dekripsi Laki-Laki
                val totalLakiPlaintext = if (finalCipherLaki != null) {
                    try { PaillierHE.decrypt(finalCipherLaki) } catch (e: Exception) { 0 }
                } else { 0 }

                // Dekripsi Perempuan
                val totalPerempuanPlaintext = if (finalCipherPerempuan != null) {
                    try { PaillierHE.decrypt(finalCipherPerempuan) } catch (e: Exception) { 0 }
                } else { 0 }

                // Masukkan ke Model Asli Anda
                hasilAkhir.add(
                    SuaraSah(
                        noUrut = kandidat.noUrut,
                        nama = kandidat.namaCalon,
                        localFoto = kandidat.localFoto,
                        jumlah = totalSuaraPlaintext,
                        jumlahLaki = totalLakiPlaintext,
                        jumlahPerempuan = totalPerempuanPlaintext
                    )
                )
            }

            // Sortir urutan: Jumlah terbanyak di atas, jika sama urutkan berdasarkan noUrut
            emit(Resource.Success(hasilAkhir.sortedWith(compareByDescending<SuaraSah> { it.jumlah }.thenBy { it.noUrut })))

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal menghitung suara HE"))
        }
    }
}