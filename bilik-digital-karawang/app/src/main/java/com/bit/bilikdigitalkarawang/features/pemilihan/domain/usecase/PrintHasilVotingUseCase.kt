package com.bit.bilikdigitalkarawang.features.pemilihan.domain.usecase

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.bit.bilikdigitalkarawang.common.PrintMode
import com.bit.bilikdigitalkarawang.helpers.BluetoothConnectionManager
import com.bit.bilikdigitalkarawang.helpers.UsbPrinterManager
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import com.bit.bilikdigitalkarawang.shared.domain.usecase.GetDeviceIdUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

class PrintHasilVotingUseCase @Inject constructor(
    private val bluetoothManager: BluetoothConnectionManager,
    private val usbPrinterManager: UsbPrinterManager,
    private val getNamaPemilihanUseCase: GetNamaPemilihanUseCase,
    private val dataStoreDiv: DataStoreDiv,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        printMode: PrintMode,
        idStatus: Int,
        noUrut: String?,
        namaKandidat: String?
    ): Result<Unit> {
        val namaPemilihan: String = getNamaPemilihanUseCase().firstOrNull() ?: ""
        val bilikNo = dataStoreDiv.getData("sesi_bilik_no").first() ?: ""
        val tpsNo = dataStoreDiv.getData("sesi_tps_no").first() ?: ""
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )

        // Baca mekanisme dari datastore
        val mekanisme = dataStoreDiv.getData("mekanisme_print").first()

        // Pilih koneksi
        val connectResult: Result<Unit>
        val outputStream: OutputStream?

        Log.d("TEST CABLE", mekanisme.toString())

        if (mekanisme == "cbl") {
            Log.d("USB_FLOW", "Masuk branch cable")
            connectResult = usbPrinterManager.connectIfNeeded()
            Log.d("USB_FLOW", "connectResult: ${connectResult}")
            if (connectResult.isFailure) {
                Log.d("USB_FLOW", "GAGAL: ${connectResult.exceptionOrNull()?.message}")
                return connectResult
            }
            outputStream = usbPrinterManager.getOutputStream()
            Log.d("USB_FLOW", "outputStream: $outputStream")
        } else {
            connectResult = bluetoothManager.connectIfNeeded()
            if (connectResult.isFailure) return connectResult
            outputStream = bluetoothManager.getOutputStream()
        }

        outputStream ?: return Result.failure(Exception("Tidak bisa akses printer"))

        return try {
            outputStream.write(byteArrayOf(0x1B, 0x40))

            val formattedBytes = PrinterHelper.generateFormattedVotingBytes(
                printMode = printMode,
                namaPemilihan = namaPemilihan,
                noTps = tpsNo,
                noBilik = bilikNo,
                idStatus = idStatus,
                noUrut = noUrut,
                namaKandidat = namaKandidat,
                deviceId = deviceId
            )
            outputStream.write(formattedBytes)
            outputStream.write(byteArrayOf(0x1D, 0x56, 0x00))
            outputStream.flush()

            Result.success(Unit)
        } catch (e: IOException) {
            // Tutup koneksi yang aktif
            if (mekanisme == "cbl") usbPrinterManager.close()
            else bluetoothManager.close()

            Result.failure(e)
        }
    }
}


