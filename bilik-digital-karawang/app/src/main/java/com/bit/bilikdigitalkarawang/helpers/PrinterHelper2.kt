package com.bit.bilikdigitalkarawang.helpers

class PrinterHelper2 {
    companion object {
        // ESC/POS Commands
        private const val ESC = "\u001B"
        private const val GS = "\u001D"

        // Initialize printer
        const val INIT = "${ESC}@"
        const val ALIGN_CENTER = "${ESC}a\u0001"
        const val BOLD_ON = "${ESC}E\u0001"
        const val BOLD_OFF = "${ESC}E\u0000"

        const val CUT_PAPER = "${GS}V\u0042\u0000"
        const val FEED_LINES = "${ESC}d\u0005"
        fun generateTestPrint(): String {
            val nonAlphanumericChars = listOf('!', '@', '#', '$', '%', '&', '*', '+', '=', '~', '?')

            val boxWidth = 32
            val boxHeight = 7

            val status = "Status Suara: SAH"
            val kandidat = "Kandidat Terpilih: Testing"

            // Fungsi untuk buat posisi acak tanpa tabrakan
            fun placeRandomly(text: String, used: MutableSet<Pair<Int, Int>>): Pair<Int, Int> {
                val maxRow = boxHeight - 1
                val maxCol = boxWidth - text.length
                while (true) {
                    val row = (0..maxRow).random()
                    val col = (0..maxCol).random()
                    // pastikan tidak tabrakan dengan yang sudah dipakai
                    val overlaps = (col until col + text.length).any { pos -> used.contains(row to pos) }
                    if (!overlaps) {
                        (col until col + text.length).forEach { pos -> used.add(row to pos) }
                        return row to col
                    }
                }
            }

            val usedPositions = mutableSetOf<Pair<Int, Int>>()
            val (statusRow, statusCol) = placeRandomly(status, usedPositions)
            val (kandidatRow, kandidatCol) = placeRandomly(kandidat, usedPositions)

            val decoratedBox = buildString {
                for (row in 0 until boxHeight) {
                    for (col in 0 until boxWidth) {
                        val textChar = when {
                            row == statusRow && col in statusCol until statusCol + status.length ->
                                status[col - statusCol]
                            row == kandidatRow && col in kandidatCol until kandidatCol + kandidat.length ->
                                kandidat[col - kandidatCol]
                            else -> nonAlphanumericChars.random()
                        }
                        append(textChar)
                    }
                    append("\n")
                }
            }

            return buildString {
                append(INIT)
                append(ALIGN_CENTER)
                append(BOLD_ON)
                append("SURAT AUDIT\n\n")
                append("Pemilihan Kepala Desa\nLembah Sari\n\n")
                append(BOLD_OFF)

                append(ALIGN_CENTER)
                append(decoratedBox)

                append(FEED_LINES)
                append(CUT_PAPER)
            }
        }
    }
}