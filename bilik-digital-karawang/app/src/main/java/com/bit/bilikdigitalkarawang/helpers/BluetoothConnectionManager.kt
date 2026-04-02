package com.bit.bilikdigitalkarawang.helpers

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import com.bit.bilikdigitalkarawang.common.Constant
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothConnectionManager @Inject constructor(
    private val dataStoreDiv: DataStoreDiv,
    @ApplicationContext private val context: Context
) {
    private var bluetoothSocket: BluetoothSocket? = null
    private var connectedMac: String? = null
    private val mutex = Mutex() // Untuk thread-safety

    suspend fun connectIfNeeded(): Result<Unit> = mutex.withLock {
        Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Memulai connectIfNeeded")
        Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Android SDK = ${Build.VERSION.SDK_INT}")

        // Cek apakah socket masih benar-benar connected
        if (bluetoothSocket?.isConnected == true) {
            try {
                // Test koneksi dengan mencoba akses outputStream
                bluetoothSocket?.outputStream?.let {
                    Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Socket sudah terkoneksi dan valid")
                    return Result.success(Unit)
                }
            } catch (e: IOException) {
                Log.w(Constant.LOG_TAG, "BluetoothConnectionManager: Socket sepertinya sudah mati, reconnect")
                close()
            }
        }

        // Ambil MAC address dari DataStore
        val mac = dataStoreDiv.getData("selected_printer_mac").firstOrNull()
        if (mac.isNullOrBlank()) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: MAC address tidak ditemukan di DataStore")
            return Result.failure(Exception("MAC address tidak ditemukan"))
        }
        Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: MAC address = $mac")

        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: BluetoothAdapter null")
            return Result.failure(Exception("Bluetooth adapter tidak tersedia"))
        }

        if (!adapter.isEnabled) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Bluetooth tidak aktif")
            return Result.failure(Exception("Bluetooth tidak aktif"))
        }

        // Cek permission berdasarkan versi Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Cek permission untuk Android 12+")
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Permission BLUETOOTH_CONNECT belum diberikan")
                return Result.failure(Exception("Permission BLUETOOTH_CONNECT belum diberikan"))
            }
        } else {
            Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Cek permission untuk Android < 12")
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Permission BLUETOOTH belum diberikan")
                return Result.failure(Exception("Permission BLUETOOTH belum diberikan"))
            }
        }

        Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Mencari device dengan MAC $mac")
        val device = adapter.bondedDevices?.find { it.address == mac }
        if (device == null) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Device tidak ditemukan di bonded devices")
            return Result.failure(Exception("Device tidak ditemukan atau belum di-pair"))
        }
        Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Device ditemukan - ${device.name}")

        return try {
            Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Cancel discovery")
            adapter.cancelDiscovery()

            // Beri jeda setelah cancel discovery
            delay(200)

            Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Membuat RFCOMM socket")
            val socket = device.createRfcommSocketToServiceRecord(
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            )

            Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Mencoba connect ke socket")

            // Jalankan connect di IO dispatcher dengan timeout
            withTimeout(5000L) { // 5 detik timeout
                withContext(Dispatchers.IO) {
                    socket.connect()
                }
            }

            bluetoothSocket = socket
            connectedMac = mac

            Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Koneksi berhasil!")

            // Beri jeda setelah connect untuk stabilitas
            delay(300)

            Result.success(Unit)

        } catch (e: TimeoutCancellationException) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Timeout saat connect", e)
            bluetoothSocket = null
            Result.failure(Exception("Timeout koneksi ke printer"))

        } catch (e: IOException) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: IOException saat connect - ${e.message}", e)
            bluetoothSocket = null

            // Coba fallback method untuk beberapa printer yang bermasalah
            tryFallbackConnection(device, mac)

        } catch (e: Exception) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Exception saat connect - ${e.message}", e)
            bluetoothSocket = null
            Result.failure(e)
        }
    }

    // Fallback connection method untuk printer yang bermasalah
    @SuppressLint("MissingPermission")
    private suspend fun tryFallbackConnection(device: BluetoothDevice, mac: String): Result<Unit> {
        return try {
            Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Mencoba fallback method")

            // Method reflection untuk createRfcommSocket
            val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
            val socket = method.invoke(device, 1) as BluetoothSocket

            withTimeout(5000L) {
                withContext(Dispatchers.IO) {
                    socket.connect()
                }
            }

            bluetoothSocket = socket
            connectedMac = mac

            Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Fallback connection berhasil!")
            delay(300)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Fallback juga gagal - ${e.message}", e)
            Result.failure(Exception("Gagal terhubung ke printer"))
        }
    }

    fun getOutputStream(): OutputStream? {
        return try {
            val stream = bluetoothSocket?.outputStream
            if (stream == null) {
                Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: OutputStream null")
            } else {
                Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: OutputStream berhasil didapat")
            }
            stream
        } catch (e: IOException) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Error getting OutputStream - ${e.message}")
            null
        }
    }

    fun isConnected(): Boolean {
        return try {
            bluetoothSocket?.isConnected == true && bluetoothSocket?.outputStream != null
        } catch (e: Exception) {
            false
        }
    }

    fun close() {
        Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Menutup koneksi")
        try {
            bluetoothSocket?.close()
            Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Socket ditutup")
        } catch (e: IOException) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Error saat close socket - ${e.message}")
        } finally {
            bluetoothSocket = null
            connectedMac = null
        }
    }

    suspend fun testPrinter(): Result<Unit> = mutex.withLock {
        return try {
            if (!isConnected()) {
                return Result.failure(Exception("Tidak terhubung ke printer"))
            }

            val os = bluetoothSocket?.outputStream
                ?: return Result.failure(Exception("OutputStream null"))

            // Kirim ESC command untuk test
            withContext(Dispatchers.IO) {
                os.write(byteArrayOf(0x1B, 0x40)) // ESC @ (Initialize printer)
                os.flush()
            }

            delay(100) // Beri waktu printer memproses

            Log.d(Constant.LOG_TAG, "BluetoothConnectionManager: Test printer berhasil")
            Result.success(Unit)

        } catch (e: IOException) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Test printer gagal - ${e.message}")
            close() // Tutup koneksi yang bermasalah
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(Constant.LOG_TAG, "BluetoothConnectionManager: Exception saat test - ${e.message}")
            Result.failure(e)
        }
    }
}