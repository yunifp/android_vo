package com.bit.bilikdigitalkarawang.helpers

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import java.io.IOException
import java.io.OutputStream

// Buat file baru UsbPrinterOutputStream.kt
class UsbPrinterOutputStream(
    private val connection: UsbDeviceConnection,
    private val endpoint: UsbEndpoint,
    private val timeout: Int = 5000
) : OutputStream() {

    override fun write(b: Int) = write(byteArrayOf(b.toByte()))

    override fun write(b: ByteArray, off: Int, len: Int) {
        val data = if (off == 0 && len == b.size) b else b.copyOfRange(off, off + len)
        val transferred = connection.bulkTransfer(endpoint, data, data.size, timeout)
        if (transferred < 0) throw IOException("USB bulk transfer gagal")
    }

    override fun flush() {}
    override fun close() {}
}