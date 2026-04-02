package com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.repository

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.model.BluetoothDeviceInfo
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KelolaPerangkatRepository @Inject constructor(
    private val context: Context,
    private val dataStoreDiv: DataStoreDiv
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

    private var isReceiverRegistered = false

    private val _discoveredDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDeviceInfo>> = _discoveredDevices.asStateFlow()

    private var bluetoothSocket: BluetoothSocket? = null
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val deviceDiscoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    if (!hasBluetoothPermissions()) return

                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        try {
                            val deviceInfo = BluetoothDeviceInfo(
                                name = if (hasConnectPermission()) it.name ?: "Unknown Device" else "Unknown Device",
                                macAddress = it.address,
                                isPrinter = isPrinterDevice(if (hasConnectPermission()) it.name else null)
                            )

                            val currentList = _discoveredDevices.value.toMutableList()
                            if (!currentList.any { existing -> existing.macAddress == deviceInfo.macAddress }) {
                                currentList.add(deviceInfo)
                                _discoveredDevices.value = currentList
                            }
                        } catch (e: SecurityException) {
                            // Permission tidak ada, skip device ini
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // Discovery selesai
                }
            }
        }
    }

    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasConnectPermission() && hasScanPermission()
        } else {
            hasLegacyBluetoothPermissions()
        }
    }

    private fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasLegacyBluetoothPermissions()
        }
    }

    private fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            hasLegacyBluetoothPermissions()
        }
    }

    private fun hasLegacyBluetoothPermissions(): Boolean {
        val bluetoothPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED

        val bluetoothAdminPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.BLUETOOTH_ADMIN
        ) == PackageManager.PERMISSION_GRANTED

        val locationPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return bluetoothPermission && bluetoothAdminPermission && locationPermission
    }

    private fun isPrinterDevice(deviceName: String?): Boolean {
//        val printerKeywords = listOf("printer", "pos", "receipt", "thermal", "epson", "canon", "hp")
//        return deviceName?.lowercase()?.let { name ->
//            printerKeywords.any { keyword -> name.contains(keyword) }
//        } ?: false

        return true
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun startDiscovery(): Boolean {
        if (!isBluetoothEnabled() || !hasBluetoothPermissions()) return false

        try {
            if (!isReceiverRegistered) {
                val filter = IntentFilter().apply {
                    addAction(BluetoothDevice.ACTION_FOUND)
                    addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
                }
                context.registerReceiver(deviceDiscoveryReceiver, filter)
                isReceiverRegistered = true
            }

            _discoveredDevices.value = emptyList()
            return bluetoothAdapter?.startDiscovery() == true
        } catch (e: SecurityException) {
            return false
        }
    }

    fun stopDiscovery() {
        try {
            if (isReceiverRegistered) {
                context.unregisterReceiver(deviceDiscoveryReceiver)
                isReceiverRegistered = false
            }
        } catch (e: Exception) {
            Log.e("BluetoothRepo", "Receiver unregister error", e)
        }

        try {
            if (hasBluetoothPermissions()) {
                bluetoothAdapter?.cancelDiscovery()
            }
        } catch (e: SecurityException) {
            // Ignore
        }
    }

    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        if (!hasConnectPermission()) return emptyList()

        return try {
            bluetoothAdapter?.bondedDevices?.map { device ->
                BluetoothDeviceInfo(
                    name = device.name ?: "Unknown Device",
                    macAddress = device.address,
                    isPrinter = isPrinterDevice(device.name)
                )
            } ?: emptyList()
        } catch (e: SecurityException) {
            emptyList()
        }
    }

//    suspend fun connectToPrinter(macAddress: String): Boolean {
//        if (!hasConnectPermission()) return false
//
//        return try {
//            val device = bluetoothAdapter?.getRemoteDevice(macAddress)
//            bluetoothSocket = device?.createRfcommSocketToServiceRecord(SPP_UUID)
//            bluetoothAdapter?.cancelDiscovery()
//            bluetoothSocket?.connect()
//            true
//        } catch (e: IOException) {
//            bluetoothSocket?.close()
//            bluetoothSocket = null
//            false
//        } catch (e: SecurityException) {
//            bluetoothSocket?.close()
//            bluetoothSocket = null
//            false
//        }
//    }

//    suspend fun connectToPrinter(macAddress: String): Boolean {
//        if (!hasConnectPermission()) return false
//
//        return withContext(Dispatchers.IO) {
//            try {
//                val device = bluetoothAdapter?.getRemoteDevice(macAddress)
//                bluetoothAdapter?.cancelDiscovery()
//
//                // Buat socket dengan timeout yang bisa dikontrol
//                bluetoothSocket = device?.createRfcommSocketToServiceRecord(SPP_UUID)
//
//                // Cara 1: Gunakan fallback socket method (lebih reliable)
//                try {
//                    val method = device?.javaClass?.getMethod(
//                        "createRfcommSocket",
//                        Int::class.javaPrimitiveType
//                    )
//                    bluetoothSocket = method?.invoke(device, 1) as? BluetoothSocket
//                } catch (e: Exception) {
//                    // Fallback ke method biasa
//                    bluetoothSocket = device?.createRfcommSocketToServiceRecord(SPP_UUID)
//                }
//
//                // Connect dengan timeout handling
//                val connectJob = async(Dispatchers.IO) {
//                    bluetoothSocket?.connect()
//                }
//
//                // Tunggu dengan timeout 10 detik
//                withTimeout(10_000L) {
//                    connectJob.await()
//                }
//
//                true
//            } catch (e: TimeoutCancellationException) {
//                bluetoothSocket?.close()
//                bluetoothSocket = null
//                false
//            } catch (e: IOException) {
//                bluetoothSocket?.close()
//                bluetoothSocket = null
//                false
//            } catch (e: SecurityException) {
//                bluetoothSocket?.close()
//                bluetoothSocket = null
//                false
//            }
//        }
//    }

    suspend fun connectToPrinter(macAddress: String): Boolean {
        if (!hasConnectPermission()) return false

        return withContext(Dispatchers.IO) {
            try {
                val device = bluetoothAdapter?.getRemoteDevice(macAddress) ?: return@withContext false
                bluetoothAdapter?.cancelDiscovery()

                // Tutup socket lama jika ada
                try {
                    bluetoothSocket?.close()
                } catch (ignored: Exception) {}
                bluetoothSocket = null

                // Buat socket baru
                bluetoothSocket = try {
                    // Coba method yang lebih reliable dulu
                    val method = device.javaClass.getMethod(
                        "createRfcommSocket",
                        Int::class.javaPrimitiveType
                    )
                    method.invoke(device, 1) as? BluetoothSocket
                } catch (e: Exception) {
                    device.createRfcommSocketToServiceRecord(SPP_UUID)
                }

                val socket = bluetoothSocket ?: return@withContext false

                // Gunakan async untuk bisa di-cancel
                val result = withTimeoutOrNull(8_000L) { // Kurangi timeout jadi 8 detik
                    try {
                        socket.connect()
                        socket.isConnected
                    } catch (e: IOException) {
                        false
                    }
                }

                if (result == true && socket.isConnected) {
                    true
                } else {
                    // Gagal atau timeout - cleanup
                    cleanupSocket()
                    false
                }
            } catch (e: Exception) {
                cleanupSocket()
                false
            }
        }
    }

    private fun cleanupSocket() {
        try {
            bluetoothSocket?.let { socket ->
                try {
                    socket.close()
                } catch (e: Exception) {
                    // Force close menggunakan reflection jika perlu
                    try {
                        val method = socket.javaClass.getMethod("close")
                        method.invoke(socket)
                    } catch (ignored: Exception) {}
                }
            }
        } catch (ignored: Exception) {
        } finally {
            bluetoothSocket = null
            // Beri jeda untuk cleanup sistem
            Thread.sleep(500)
        }
    }

    fun disconnectPrinter() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
        } catch (e: IOException) {
            // Handle error
        }
    }

    fun printText(text: String): Boolean {
        return try {
            bluetoothSocket?.outputStream?.let { outputStream ->
                // ESC/POS commands
                val initCommand = byteArrayOf(0x1B, 0x40) // Initialize printer
                val textBytes = text.toByteArray(Charsets.UTF_8)
                val cutCommand = byteArrayOf(0x1D, 0x56, 0x41, 0x10) // Cut paper

                outputStream.write(initCommand)
                outputStream.write(textBytes)
                outputStream.write(cutCommand)
                outputStream.flush()
                true
            } ?: false
        } catch (e: IOException) {
            false
        }
    }

    fun printBytes(bytes: ByteArray): Boolean = try {
        bluetoothSocket?.outputStream?.let { os ->
            os.write(byteArrayOf(0x1B, 0x40)) // init printer
            os.write(bytes)
            os.write(byteArrayOf(0x1D, 0x56, 0x00)) // full cut
            os.flush()
            true
        } ?: false
    } catch (e: IOException) {
        false
    }

    fun printRawData(data: ByteArray): Boolean {
        return try {
            bluetoothSocket?.outputStream?.let { outputStream ->
                outputStream.write(data)
                outputStream.flush()
                true
            } ?: false
        } catch (e: IOException) {
            false
        }
    }

    suspend fun saveSelectedPrinter(device: BluetoothDeviceInfo) {
        val dataMap = mapOf(
            "selected_printer_mac" to device.macAddress,
            "selected_printer_name" to device.name
        )
        dataStoreDiv.bulkSaveData(dataMap)
    }

    fun getSelectedPrinterMac() = dataStoreDiv.getData("selected_printer_mac")
    fun getSelectedPrinterName() = dataStoreDiv.getData("selected_printer_name")

    fun checkPermissions(): List<String> {
        val missingPermissions = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!hasConnectPermission()) {
                missingPermissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (!hasScanPermission()) {
                missingPermissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(android.Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADMIN)
                != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(android.Manifest.permission.BLUETOOTH_ADMIN)
            }
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        return missingPermissions
    }
}