package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.face_recognition

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bit.bilikdigitalkarawang.common.Resource
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase.CekNikValidUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    object Loading : ScanState()
    data class Success(val user: UserData) : ScanState()
    data class Error(val message: String) : ScanState()
}

@HiltViewModel
class BiometricViewModel @Inject constructor(
    private val cekNikValidUseCase: CekNikValidUseCase
) : ViewModel() {
    var scanState by mutableStateOf<ScanState>(ScanState.Scanning)
        private set

    private val TAG = "BiometricVM"

    fun resetScan() {
        scanState = ScanState.Scanning
    }

    // TERIMA ROTATION DEGREES DINAMIS DARI SENSOR
    fun processFaceImage(context: Context, bitmap: Bitmap, rotationDegrees: Int, expectedNik: String? = null) {
        if (scanState is ScanState.Loading || scanState is ScanState.Success) return
        scanState = ScanState.Loading

        viewModelScope.launch {
            try {
                // Rotasi Dinamis (Anti Miring di Tablet)
                val matrix = Matrix()
                matrix.postRotate(rotationDegrees.toFloat())
                val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

                val file = File(context.cacheDir, "face_capture.jpg")
                val os = FileOutputStream(file)
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, os)
                os.flush()
                os.close()

                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // MEMANGGIL API RetrofitClient
                val response = RetrofitClient.instance.recognizeFace(body)

                if (response.success && response.data != null) {
                    val detectedUser = response.data

                    if (expectedNik != null) {
                        if (detectedUser.nik == expectedNik) {
                            validateNikInDpt(detectedUser) // Cek NIK ke DPT
                        } else {
                            scanState = ScanState.Error("Wajah terdeteksi sebagai ${detectedUser.nama_lengkap}. Gunakan wajah yang sesuai NIK Anda!")
                        }
                    } else {
                        validateNikInDpt(detectedUser) // Cek NIK ke DPT
                    }

                } else {
                    scanState = ScanState.Error(response.message)
                }
            } catch (e: HttpException) {
                val err = e.response()?.errorBody()?.string()
                Log.e(TAG, "HTTP Error: $err")
                scanState = ScanState.Error("Wajah tidak jelas atau posisi miring.")
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}")
                scanState = ScanState.Error("Koneksi gagal atau wajah tidak terdaftar di sistem.")
            }
        }
    }

    // Fungsi tambahan untuk memvalidasi NIK hasil deteksi wajah dengan data pemilih menggunakan onEach
    private fun validateNikInDpt(detectedUser: UserData) {
        cekNikValidUseCase(detectedUser.nik).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    // Biarkan state tetap Loading
                }
                is Resource.Success -> {
                    if (result.data == true) {
                        scanState = ScanState.Success(detectedUser)
                    } else {
                        scanState = ScanState.Error("Wajah dikenali, namun NIK (${detectedUser.nik}) tidak terdaftar dalam Data Pemilih.")
                    }
                }
                is Resource.Error -> {
                    scanState = ScanState.Error(result.message ?: "Terjadi kesalahan saat memvalidasi NIK di Data Pemilih.")
                }
            }
        }.launchIn(viewModelScope)
    }
}