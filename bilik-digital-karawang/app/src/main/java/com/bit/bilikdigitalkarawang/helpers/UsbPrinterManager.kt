package com.bit.bilikdigitalkarawang.helpers

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.util.Log
import com.dantsu.escposprinter.connection.usb.UsbOutputStream
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import javax.inject.Inject

class UsbPrinterManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var usbConnection: UsbDeviceConnection? = null
    private var usbOutputStream: OutputStream? = null
    private var usbInterface: UsbInterface? = null

    fun connectIfNeeded(): Result<Unit> {
        if (usbOutputStream != null) return Result.success(Unit)

        return try {
            val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

            Log.d("USB", "Total device: ${usbManager.deviceList.size}")

            val device = usbManager.deviceList.values.firstOrNull()
                ?: return Result.failure(Exception("Printer USB tidak ditemukan"))

            Log.d("USB", "Device: ${device.deviceName}, Class: ${device.deviceClass}")
            Log.d("USB", "Has permission: ${usbManager.hasPermission(device)}")

            if (!usbManager.hasPermission(device))
                return Result.failure(Exception("Permission USB belum diberikan"))

            val connection = usbManager.openDevice(device)
            Log.d("USB", "Connection: $connection")

            connection ?: return Result.failure(Exception("Gagal buka koneksi USB"))

            val iface = device.getInterface(0)
            Log.d("USB", "Interface class: ${iface.interfaceClass}")
            Log.d("USB", "Endpoint count: ${iface.endpointCount}")

            val endpoint = (0 until iface.endpointCount)
                .map { iface.getEndpoint(it) }
                .also { endpoints ->
                    endpoints.forEach { Log.d("USB", "Endpoint direction: ${it.direction}, type: ${it.type}") }
                }
                .firstOrNull { it.direction == UsbConstants.USB_DIR_OUT }

            Log.d("USB", "Endpoint OUT: $endpoint")

            endpoint ?: return Result.failure(Exception("Endpoint OUT tidak ditemukan"))

            connection.claimInterface(iface, true)
            usbConnection = connection
            usbInterface = iface
            usbOutputStream = UsbPrinterOutputStream(connection, endpoint)

            Log.d("USB", "connectIfNeeded SUCCESS")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.d("USB", "connectIfNeeded ERROR: ${e.message}")
            Result.failure(e)
        }
    }

    fun getOutputStream(): OutputStream? = usbOutputStream

    fun testPrinter(): Result<Unit> {
        return try {
            val stream = usbOutputStream
                ?: return Result.failure(Exception("Printer USB tidak terkoneksi"))
            stream.write(byteArrayOf(0x1B, 0x40))
            stream.flush()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        try { usbOutputStream?.close() } catch (_: Exception) {}
        try { usbInterface?.let { usbConnection?.releaseInterface(it) } } catch (_: Exception) {}
        try { usbConnection?.close() } catch (_: Exception) {}
        usbOutputStream = null
        usbConnection = null
        usbInterface = null
    }
}