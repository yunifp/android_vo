import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.bit.bilikdigitalkarawang.common.PrintMode
import com.bit.bilikdigitalkarawang.common.StatusSuara
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.experimental.or


object PrinterHelper {

    fun generateTestPrintBytes(): ByteArray {
        return generateFormattedVotingBytes(
            printMode = PrintMode.TESTING,
            namaPemilihan = "Pemilihan Kepala Desa Testing",
            idStatus = 1,
            noUrut = "24",
            namaKandidat = "Testing Kandidat",
            deviceId = "xxx",
            noTps = "zzz",
            noBilik = "yyy"
        )
    }

    fun generateFormattedVotingBytes(
        printMode: PrintMode,
        namaPemilihan: String,
        noTps: String,
        noBilik: String,
        idStatus: Int,
        noUrut: String?,
        namaKandidat: String?,
        deviceId: String
    ): ByteArray {
        val output = mutableListOf<Byte>()

        // Init & center
        output += listOf(0x1B, 0x40) // init

        // PENTING: Beri jeda setelah init
        repeat(2) { output += 0x0A }

        output += listOf(0x1B, 0x61, 1) // align center

        when (printMode) {
            PrintMode.RESI_CETAK_ULANG -> {
                output += "[ RESI CETAK ULANG ]\n".toByteArray().toList()
            }
            PrintMode.TESTING -> {
                output += "[ RESI TESTING ]\n".toByteArray().toList()
            }
            PrintMode.NORMAL -> {
                // Warming up dengan whitespace minimal
                output += "\n".toByteArray().toList()
            }
        }

        output += "SURAT AUDIT\n".toByteArray().toList()
        output += "$namaPemilihan\n\n".toByteArray().toList()

        // Bold ON
        output += listOf(0x1B, 0x45, 1)

        output += "TPS $noTps / Bilik $noBilik".toByteArray().toList()

        // Bold OFF
        output += listOf(0x1B, 0x45, 0)

        output += "\n\n".toByteArray().toList()

        // QR Code
        var qrData = "$namaPemilihan\n"
        if (idStatus == 1) {
            qrData += "$namaKandidat"
        } else {
            val status = if (idStatus == 2) "Tidak Sah" else "Tidak Sah"
            qrData += "Status Suara: $status"
        }
        output += generateQRCodeBytesSuperClear(qrData, deviceId).toList()

        output += "\n".toByteArray().toList()

        // Box teks hasil voting
        val statusString = getStatusSuaraText(idStatus)
        output += generateDecoratedBoxBytes(statusString, noUrut, namaKandidat).toList()

        repeat(7) {
            output += 0x0A
        }

        return output.toByteArray()
    }

    fun getStatusSuaraText(id: Int): String {
        val status = StatusSuara.fromId(id)
        return status?.name ?: "TIDAK_DISET"
    }

    fun generateDecoratedBoxBytes(status: String, noUrut: String?, namaKandidat: String?): ByteArray {
        val nonAlphanumericChars = listOf('#', '@', '$', '%', '&', '*', '+', '=', '~')
        val boxWidth = 32
        val boxHeight = 3

        val statusLine = when (status) {
            "SAH" -> ""
            "TIDAK_SAH" -> "Suara Tidak Sah"
            "ABSTAIN" -> "Suara Tidak Sah"
            else -> "Status Suara: $status"
        }
        val kandidatLine = if (status == "SAH") "${namaKandidat ?: "-"}" else ""

        fun placeRandomly(text: String, used: MutableSet<Pair<Int, Int>>): Pair<Int, Int> {
            val maxRow = boxHeight - 1
            val maxCol = boxWidth - text.length
            if (maxCol < 0) return 0 to 0
            while (true) {
                val row = (0..maxRow).random()
                val col = (0..maxCol).random()
                val overlaps = (col until col + text.length).any { pos -> used.contains(row to pos) }
                if (!overlaps) {
                    (col until col + text.length).forEach { pos -> used.add(row to pos) }
                    return row to col
                }
            }
        }

        val usedPositions = mutableSetOf<Pair<Int, Int>>()
        val contentLines = mutableListOf<Pair<String, Pair<Int, Int>>>()

        if (statusLine.isNotEmpty()) contentLines.add(statusLine to placeRandomly(statusLine, usedPositions))
        if (kandidatLine.isNotEmpty()) contentLines.add(kandidatLine to placeRandomly(kandidatLine, usedPositions))

        val decoratedBox = buildString {
            for (row in 0 until boxHeight) {
                for (col in 0 until boxWidth) {
                    val char = contentLines.firstNotNullOfOrNull { (text, pos) ->
                        val (r, c) = pos
                        if (row == r && col in c until c + text.length) {
                            text[col - c]
                        } else null
                    } ?: nonAlphanumericChars.random()
                    append(char)
                }
                append("\n")
            }
        }

        return decoratedBox.toByteArray()
    }

    // ======== QR Code Super Clear ========
    fun generateQRCodeBytesSuperClear(data: String, deviceId: String): ByteArray {
        val targetWidth = 360
        val targetHeight = targetWidth

        val qrWriter = QRCodeWriter()
        val hints = hashMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
            put(EncodeHintType.MARGIN, 0)
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
        }

        val bitMatrix = qrWriter.encode(data, BarcodeFormat.QR_CODE, targetWidth, targetHeight, hints)

        val bmp = createBitmap(targetWidth, targetHeight)
        for (x in 0 until targetWidth) {
            for (y in 0 until targetHeight) {
                bmp[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        // Tambahkan teks di tengah QR
        val finalQr = addCenterTextOverlayDynamic(bmp, deviceId)

        return bitmapToESCPOSBytesCrisp(finalQr)
    }

    // ======== Convert Bitmap to ESC/POS Bytes ========
    fun bitmapToESCPOSBytesCrisp(bitmap: Bitmap): ByteArray {
        val targetWidth = 384
        val targetHeight = (bitmap.height * targetWidth / bitmap.width.toFloat()).toInt()

        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false) // no blur
        val widthBytes = (scaledBitmap.width + 7) / 8
        val height = scaledBitmap.height

        val bytes = mutableListOf<Byte>()

        bytes += 0x1D.toByte()
        bytes += 0x76.toByte()
        bytes += 0x30.toByte()
        bytes += 0x00.toByte()
        bytes += (widthBytes and 0xFF).toByte()
        bytes += ((widthBytes shr 8) and 0xFF).toByte()
        bytes += (height and 0xFF).toByte()
        bytes += ((height shr 8) and 0xFF).toByte()

        for (y in 0 until height) {
            var bitIndex = 0
            var currentByte: Byte = 0
            for (x in 0 until scaledBitmap.width) {
                val pixel = scaledBitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val luminance = (0.299 * r + 0.587 * g + 0.114 * b)
                val isBlack = luminance < 128
                if (isBlack) {
                    currentByte = currentByte or (1 shl (7 - bitIndex)).toByte()
                }
                bitIndex++
                if (bitIndex == 8) {
                    bytes += currentByte
                    currentByte = 0
                    bitIndex = 0
                }
            }
            if (bitIndex > 0) bytes += currentByte
        }

        scaledBitmap.recycle()
        return bytes.toByteArray()
    }
}

fun addCenterTextOverlayDynamic(qrBitmap: Bitmap, text: String): Bitmap {
    val canvas = Canvas(qrBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Kotak background (25% dari QR)
    val boxSize = qrBitmap.width * 0.26f
    val left = (qrBitmap.width - boxSize) / 2
    val top = (qrBitmap.height - boxSize) / 2
    val right = left + boxSize
    val bottom = top + boxSize

    // kotak putih
    paint.color = Color.WHITE
    paint.style = Paint.Style.FILL
    canvas.drawRect(left, top, right, bottom, paint)

    // Persiapkan teks
    paint.color = Color.BLACK
    paint.textAlign = Paint.Align.CENTER

    // Cek apakah muat 1 baris
    paint.textSize = boxSize * 0.22f
    val oneLineWidth = paint.measureText(text)

    val centerX = qrBitmap.width / 2f

    if (oneLineWidth <= boxSize * 0.9f) {
        // Jika muat 1 baris → tulis langsung
        val centerY = qrBitmap.height / 2f - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(text, centerX, centerY, paint)
        return qrBitmap
    }

    // Jika terlalu panjang → pecah jadi 2 baris
    val mid = text.length / 2
    val line1 = text.substring(0, mid)
    val line2 = text.substring(mid)

    val centerY = qrBitmap.height / 2f

    // Baris 1
    canvas.drawText(line1, centerX, centerY - paint.textSize * 0.6f, paint)
    // Baris 2
    canvas.drawText(line2, centerX, centerY + paint.textSize * 0.6f, paint)

    return qrBitmap
}

