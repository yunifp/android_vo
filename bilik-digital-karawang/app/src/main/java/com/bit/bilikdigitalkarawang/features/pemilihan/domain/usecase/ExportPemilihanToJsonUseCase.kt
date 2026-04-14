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
import com.bit.bilikdigitalkarawang.utils.PaillierHE
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
    val deviceInfo: String,
    val paillierKeysAes: String // <--- TAMBAHKAN INI (Kunci ikut dibackup)
)

data class PemilihanExport(
    val nik: String,
    val heVotesMap: String,
    val idStatus: String,
    val idDpt: String,
    val jenisKelamin: String,
    val hasPrintUlang: String
)

class ExportPemilihanToJsonUseCase @Inject constructor(
    private val repository: PemilihanRepository,
    private val dataStoreDiv: DataStoreDiv,
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    operator fun invoke(): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())

            val exportUri = dataStoreDiv.getData("export_path").first()
            if (exportUri.isNullOrEmpty()) {
                emit(Resource.Error("Lokasi export belum diatur"))
                return@flow
            }
            val uri = Uri.parse(exportUri)

            // Mengambil data berbentuk List<Pemilihan> (model)
            val pemilihanList = repository.getPemilihan().first()

            if (pemilihanList.isEmpty()) {
                emit(Resource.Error("Tidak ada data untuk di-export"))
                return@flow
            }

            withContext(Dispatchers.IO) {
                val exportData = ExportData(
                    metadata = ExportMetadata(
                        version = 1,
                        exportDate = System.currentTimeMillis(),
                        totalPemilihan = pemilihanList.size,
                        deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL}",
                        // Export & Encrypt kunci Paillier ke Micro SD
                        paillierKeysAes = Encrypt.encrypt(PaillierHE.exportKeysToJson())
                    ),
                    pemilihanList = pemilihanList.map { model ->
                        PemilihanExport(
                            nik = Encrypt.encrypt(model.nik),
                            heVotesMap = Encrypt.encrypt(model.heVotesMap),
                            idStatus = Encrypt.encrypt(model.idStatus.toString()),
                            idDpt = Encrypt.encrypt(model.idDpt),
                            jenisKelamin = Encrypt.encrypt(model.jenisKelamin),
                            hasPrintUlang = Encrypt.encrypt(model.hasPrintUlang.toString())
                        )
                    }
                )

                val json = gson.toJson(exportData)
                val tempUri = createTempFile(context, uri)

                context.contentResolver.openOutputStream(tempUri, "wt")?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write(json)
                        writer.flush()
                    }
                } ?: throw IOException("Tidak dapat menulis ke file")

                context.contentResolver.openInputStream(tempUri)?.use { input ->
                    if (input.available() < 100) {
                        throw IOException("File export terlalu kecil, kemungkinan gagal")
                    }
                }

                moveTempToFinal(context, tempUri, uri)
            }

            emit(Resource.Success("Berhasil export ${pemilihanList.size} data pemilihan"))

        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal export data"))
        }
    }

    private fun createTempFile(context: Context, originalUri: Uri): Uri { return originalUri }
    private fun moveTempToFinal(context: Context, tempUri: Uri, finalUri: Uri) { }
}