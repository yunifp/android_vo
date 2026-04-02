package com.bit.bilikdigitalkarawang.features.setting.domain

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SaveExportLocationUseCase @Inject constructor(
    private val dataStoreDiv: DataStoreDiv,
    @ApplicationContext private val context: Context
) {
    operator fun invoke(uri: Uri): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            // Take persistable permission
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)

            // Verify we can write to this URI
            context.contentResolver.openOutputStream(uri, "wt")?.close()

            // Save URI
            dataStoreDiv.saveData("export_path", uri.toString())

            emit(Resource.Success("Berhasil menyimpan path"))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal menyimpan path"))
        }
    }
}