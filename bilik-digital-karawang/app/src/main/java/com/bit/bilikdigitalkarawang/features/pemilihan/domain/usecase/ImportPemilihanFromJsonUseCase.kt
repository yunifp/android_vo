package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.entity.PemilihanEntity
import com.bit.bilikdigitalkarawang.utils.Encrypt
import com.bit.bilikdigitalkarawang.utils.PaillierHE
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.io.FileNotFoundException
import javax.inject.Inject

data class ImportResult(
    val totalImported: Int,
    val totalFailed: Int,
    val previousCount: Int,
    val importDate: Long,
    val sourceDevice: String
)

class ImportPemilihanFromJsonUseCase @Inject constructor(
    private val repository: PemilihanRepository,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    operator fun invoke(uri: Uri): Flow<Resource<ImportResult>> = flow {
        try {
            emit(Resource.Loading())

            // 1. Read JSON from URI
            val json = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: run {
                emit(Resource.Error("Tidak dapat membaca file"))
                return@flow
            }



            // 2. Parse JSON
            val exportData = try {
                gson.fromJson(json, ExportData::class.java)
            } catch (e: JsonSyntaxException) {
                emit(Resource.Error("Format JSON tidak valid"))
                return@flow
            } catch (e: Exception) {
                emit(Resource.Error("Gagal parse JSON: ${e.localizedMessage}"))
                return@flow
            }

            if (!exportData.metadata.paillierKeysAes.isNullOrEmpty()) {
                val decryptedKeysJson = Encrypt.decryptToStringOrEmpty(exportData.metadata.paillierKeysAes)
                if (decryptedKeysJson.isNotEmpty()) {
                    // Tanamkan kunci lama ke memory device baru!
                    PaillierHE.importKeysFromJson(decryptedKeysJson)
                }
            }

            // 3. Validate data
            if (exportData.pemilihanList.isEmpty()) {
                emit(Resource.Error("File tidak berisi data pemilihan"))
                return@flow
            }

            // 4. Validate version compatibility
            if (exportData.metadata.version > 1) {
                emit(Resource.Error("Versi file tidak kompatibel. Silakan update aplikasi."))
                return@flow
            }

            // 5. Get existing data count
            val existingCount = repository.getPemilihan().first().size

            // 6. Clear existing data
            repository.resetPemilihan()

            // 7. Insert imported data
            var successCount = 0
            var failedCount = 0

            exportData.pemilihanList.forEach { pemilihanExport ->
                try {
                    val entity = PemilihanEntity(
                        nik = Encrypt.decryptToStringOrEmpty(pemilihanExport.nik),
                        heVotesMap = Encrypt.decryptToStringOrEmpty(pemilihanExport.heVotesMap), // AES Dekripsi -> Kembali jadi String HE
                        idStatus = Encrypt.decryptToIntOrDefault(pemilihanExport.idStatus, 0),
                        idDpt = Encrypt.decryptToStringOrEmpty(pemilihanExport.idDpt),
                        jenisKelamin = Encrypt.decryptToStringOrEmpty(pemilihanExport.jenisKelamin),
                        hasPrintUlang = Encrypt.decryptToIntOrDefault(pemilihanExport.hasPrintUlang, 0)
                    )
                    repository.insertPemilihan(entity)
                    successCount++
                } catch (e: Exception) {
                    failedCount++
                    Log.e("ImportPemilihan", "Failed to insert: ${pemilihanExport.nik}", e)
                }
            }

            // 8. Create result
            val result = ImportResult(
                totalImported = successCount,
                totalFailed = failedCount,
                previousCount = existingCount,
                importDate = exportData.metadata.exportDate,
                sourceDevice = exportData.metadata.deviceInfo
            )

            emit(Resource.Success(result))

        } catch (e: SecurityException) {
            emit(Resource.Error("Akses file ditolak"))
        } catch (e: FileNotFoundException) {
            emit(Resource.Error("File tidak ditemukan"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal import data"))
        }
    }
}