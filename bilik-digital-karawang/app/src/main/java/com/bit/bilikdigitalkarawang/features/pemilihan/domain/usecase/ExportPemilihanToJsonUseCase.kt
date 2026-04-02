package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.util.Log
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.repository.PemilihanRepository
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import com.bit.bilikdigitalkarawang.utils.Encrypt
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStreamWriter
import javax.inject.Inject

data class ExportData(
    val metadata: ExportMetadata,
    val pemilihanList: List<PemilihanExport>
)

data class ExportMetadata(
    val version: Int = 1,
    val exportDate: Long = System.currentTimeMillis(),
    val totalPemilihan: Int,
    val deviceInfo: String
)

data class PemilihanExport(
    val nik: String,
    val noUrut: String?,
    val namaKandidat: String?,
    val idStatus: String,
    val idDpt: String,
    val jenisKelamin: String,
    val hasPrintUlang: String
)

// ============================================
// 2. USE CASE - Export to JSON
// ============================================

class ExportPemilihanToJsonUseCase @Inject constructor(
    private val repository: PemilihanRepository,
    private val dataStoreDiv: DataStoreDiv,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    operator fun invoke(): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            // 1. Get export URI from DataStore
            val exportUri = dataStoreDiv.getData("export_path").first()

            if (exportUri.isNullOrEmpty()) {
                emit(Resource.Error("Lokasi export belum diatur"))
                return@flow
            }

            val uri = Uri.parse(exportUri)

            // 2. Get all pemilihan data from repository
            val pemilihanList = repository.getPemilihan().first()

            if (pemilihanList.isEmpty()) {
                emit(Resource.Error("Tidak ada data untuk di-export"))
                return@flow
            }

            // 3. Process di IO thread untuk operasi berat
            withContext(Dispatchers.IO) {
                // Create export data with metadata
                val exportData = ExportData(
                    metadata = ExportMetadata(
                        version = 1,
                        exportDate = System.currentTimeMillis(),
                        totalPemilihan = pemilihanList.size,
                        deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}"
                    ),
                    pemilihanList = pemilihanList.map { entity ->
                        PemilihanExport(
                            nik = Encrypt.encrypt(entity.nik),
                            noUrut = Encrypt.encrypt(entity.noUrut),
                            namaKandidat = Encrypt.encrypt(entity.namaKandidat),
                            idStatus = Encrypt.encrypt(entity.idStatus),
                            idDpt = Encrypt.encrypt(entity.idDpt),
                            jenisKelamin = Encrypt.encrypt(entity.jenisKelamin),
                            hasPrintUlang = Encrypt.encrypt(entity.hasPrintUlang)
                        )
                    }
                )

                // 4. Convert to JSON
                val json = gson.toJson(exportData)

                // 5. Write to temporary file first (atomic write)
                val tempUri = createTempFile(context, uri)

                context.contentResolver.openOutputStream(tempUri, "wt")?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write(json)
                        writer.flush()
                    }
                } ?: throw IOException("Tidak dapat menulis ke file")

                // 6. Verify file size (optional but recommended)
                context.contentResolver.openInputStream(tempUri)?.use { input ->
                    val fileSize = input.available()
                    if (fileSize < 100) { // Too small, likely failed
                        throw IOException("File export terlalu kecil, kemungkinan gagal")
                    }
                }

                // 7. Move temp to final destination (atomic operation)
                moveTempToFinal(context, tempUri, uri)
            }

            emit(Resource.Success("Berhasil export ${pemilihanList.size} data pemilihan"))

        } catch (e: SecurityException) {
            emit(Resource.Error("Akses ditolak. Silakan pilih ulang lokasi export"))
        } catch (e: FileNotFoundException) {
            emit(Resource.Error("File tidak ditemukan. Silakan pilih ulang lokasi export"))
        } catch (e: IOException) {
            emit(Resource.Error("Gagal menulis file: ${e.message}"))
        } catch (e: OutOfMemoryError) {
            emit(Resource.Error("Data terlalu besar. Coba kurangi jumlah data"))
        } catch (e: Exception) {
            Log.e("ExportJSON", "Unexpected error", e)
            emit(Resource.Error(e.localizedMessage ?: "Gagal export data"))
        }
    }

    private fun createTempFile(context: Context, originalUri: Uri): Uri {
        // Create temp file in same directory
        val fileName = DocumentsContract.getDocumentId(originalUri)
        val tempFileName = "${fileName}.tmp"
        // Implementation depends on your storage strategy
        return originalUri // Simplified for example
    }

    private fun moveTempToFinal(context: Context, tempUri: Uri, finalUri: Uri) {
        // Atomic move operation
        // Implementation depends on your storage strategy
    }
}