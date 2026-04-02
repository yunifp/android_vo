package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.rekap

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.rekap.components.StatusSuaraCard
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.rekap.components.SuaraSahCard
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.rekap.components.SuaraSingleCard
import com.bit.bilikdigitalkarawang.helpers.formatNumber
import com.bit.bilikdigitalkarawang.shared.presentation.components.CHeader
import com.bit.bilikdigitalkarawang.ui.theme.Typography
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.AreaBreakType
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun RekapScreen(
    navController: NavController,
    viewModel: RekapViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var showOpenDialog by remember { mutableStateOf(false) }
    var pdfFilePath by remember { mutableStateOf("") }
    var savedFile by remember { mutableStateOf<File?>(null) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            CHeader(onBack = { navController.navigateUp() })

            Spacer(modifier = Modifier.height(16.dp))

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            Text(
                                text = "Statistik Pemilihan",
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                        item {
                            SuaraSingleCard(
                                title = "Jumlah Pemilihan",
                                subtitle = String.format("%.2f%%", (state.jumlahPemilihan.toDouble() / state.jumlahPemilih.toDouble()) * 100),
                                jumlah = "${state.jumlahPemilihan.formatNumber()} / ${state.jumlahPemilih.formatNumber()}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        item {
                            Text(
                                text = "Jumlah Suara per Status  ",
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        item {
                            StatusSuaraCard(
                                title = "Suara Sah",
                                jumlahLaki = state.jumlahSahLaki,
                                jumlahPerempuan = state.jumlahSahPerempuan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        item {
                            StatusSuaraCard(
                                title = "Suara Tidak Sah",
                                jumlahLaki = state.jumlahTidakSahLaki + state.jumlahAbstainLaki,
                                jumlahPerempuan = state.jumlahTidakSahPerempuan + state.jumlahAbstainPerempuan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                        item {
                            Text(
                                text = "Jumlah Suara per Kandidat",
                                style = Typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        items(state.listSuaraSah) {
                            SuaraSahCard(it)
                        }
                        item {
                            Button(onClick = {
                                viewModel.exportJson()
                            }) {
                                Text("Export JSON")
                            }
                        }
                        item {
                            // Export Button
                            Button(
                                onClick = {
                                    scope.launch {
                                        isExporting = true
                                        val result = exportPdfWithIText(context, state)
                                        isExporting = false
                                        if (result != null) {
                                            savedFile = result
                                            pdfFilePath = result.absolutePath
                                            showOpenDialog = true
                                        }
                                    }
                                },
                                enabled = !isExporting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                if (isExporting) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Mengexport PDF ...")
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Export ke PDF", fontSize = 16.sp)
                                }
                            }
                        }
                    }

                }
            }

            if (showOpenDialog) {
                AlertDialog(
                    onDismissRequest = { showOpenDialog = false },
                    icon = {
                        Text("✅", fontSize = 48.sp)
                    },
                    title = {
                        Text("Export Berhasil!")
                    },
                    text = {
                        Column {
                            Text("PDF telah disimpan di:")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                pdfFilePath,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Apakah Anda ingin membuka PDF sekarang?",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showOpenDialog = false
                                savedFile?.let { openPdfFile(context, it) }
                            }
                        ) {
                            Text("Buka PDF")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showOpenDialog = false }) {
                            Text("Nanti Saja")
                        }
                    }
                )
            }

        }
    }
}

suspend fun exportPdfWithIText(
    context: Context,
    state: RekapState
): File? = withContext(Dispatchers.IO) {
    try {
        val fileName = "Laporan_Rekapitulasi_${System.currentTimeMillis()}.pdf"
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            fileName
        )

        // Inisialisasi PDF dengan iText
        val writer = PdfWriter(file)
        val pdfDoc = PdfDocument(writer)
        val document = Document(pdfDoc)

        // Font
        val timesFont = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN)

        // Tambahkan Title
        val title = Paragraph("REKAPITULASI JUMLAH PEMILIH SURAT SUARA SECARA DIGITAL")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(title)

        val subtitle1 = Paragraph("PEMILIHAN KEPALA DESA " + state.userInfo?.namaKel)
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(subtitle1)

        val subtitle2 = Paragraph("KECAMATAN ${state.userInfo?.namaKec} KABUPATEN KARAWANG")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(subtitle2)

        val yearInfo = Paragraph("TAHUN 2025")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(yearInfo)

        val tpsBilikInfo = Paragraph("NO. TPS ${state.userInfo?.tpsNo} / NO. BILIK ${state.userInfo?.bilikNo}")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(tpsBilikInfo)

        val deviceIdInfo = Paragraph("DEVICE ID ${state.userInfo?.deviceId}")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20f)
        document.add(deviceIdInfo)

        // ========== SUARA ==========
        val sectionSuara = Paragraph("REKAPITULASI SUARA")
            .setFontSize(12f)
            .setFont(timesFont)
            .setMarginBottom(10f)
        document.add(sectionSuara)

        val tableSuara = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f)))
        tableSuara.setWidth(UnitValue.createPercentValue(100f))

        tableSuara.addHeaderCell(
            Cell()
                .add(Paragraph("STATUS").setBold().setFontSize(11f))
                .setFont(timesFont)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8f)
        )

        tableSuara.addHeaderCell(
            Cell()
                .add(Paragraph("JUMLAH").setBold().setFontSize(11f))
                .setFont(timesFont)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8f)
        )

        // Suara Sah
        tableSuara.addCell(
            Cell().add(Paragraph("Suara Sah"))
                .setFont(timesFont)
                .setPadding(6f)
        )

        tableSuara.addCell(
            Cell().add(
                Paragraph(
                    (state.jumlahSahLaki + state.jumlahSahPerempuan).formatNumber()
                )
            )
                .setFont(timesFont)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6f)
        )

        // Suara Tidak Sah
        tableSuara.addCell(
            Cell().add(Paragraph("Suara Tidak Sah"))
                .setFont(timesFont)
                .setPadding(6f)
        )

        tableSuara.addCell(
            Cell().add(
                Paragraph(
                    (
                            state.jumlahTidakSahLaki +
                                    state.jumlahTidakSahPerempuan +
                                    state.jumlahAbstainLaki +
                                    state.jumlahAbstainPerempuan
                            ).formatNumber()
                )
            )
                .setFont(timesFont)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(6f)
        )

        document.add(tableSuara)


        // ========== SAKSI - SAKSI ==========
        val saksiSection = Paragraph("Saksi – saksi")
            .setFontSize(12f)
            .setFont(timesFont)
            .setMarginTop(30f)
            .setMarginBottom(10f)
        document.add(saksiSection)

        // Tabel untuk saksi dengan 2 kolom
        val saksiColumnWidths = floatArrayOf(1f, 1f)
        val saksiTable = Table(UnitValue.createPercentArray(saksiColumnWidths))
        saksiTable.setWidth(UnitValue.createPercentValue(100f))

        // Baris 1 - Saksi 1 dan tahun
        saksiTable.addCell(
            Cell()
                .add(Paragraph("1. ............................."))
                .setFontSize(10f)
                .setFont(timesFont)
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )

        saksiTable.addCell(
            Cell()
                .add(Paragraph("................................. 2025"))
                .setFontSize(10f)
                .setFont(timesFont)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5f)
        )

        // Baris 2 - Saksi 2 dan Ketua Panitia
        saksiTable.addCell(
            Cell()
                .add(Paragraph("2. ............................."))
                .setFontSize(10f)
                .setFont(timesFont)
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )

        saksiTable.addCell(
            Cell()
                .add(Paragraph("Ketua Panitia"))
                .setFontSize(10f)
                .setFont(timesFont)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5f)
        )

        // Baris 3 - Saksi 3 (kosong di kanan)
        saksiTable.addCell(
            Cell()
                .add(Paragraph("3. ............................."))
                .setFontSize(10f)
                .setFont(timesFont)
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )

        saksiTable.addCell(
            Cell()
                .add(Paragraph(""))
                .setFont(timesFont)
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )

        // Baris 4 - Saksi 4 (kosong di kanan)
        saksiTable.addCell(
            Cell()
                .add(Paragraph("4. ............................."))
                .setFontSize(10f)
                .setFont(timesFont)
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )

        saksiTable.addCell(
            Cell()
                .add(Paragraph(""))
                .setFont(timesFont)
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )

        // Baris 5 - Saksi 5 (kosong di kanan)
        saksiTable.addCell(
            Cell()
                .add(Paragraph("5. ............................."))
                .setFontSize(10f)
                .setFont(timesFont)
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )

        saksiTable.addCell(
            Cell()
                .add(Paragraph(""))
                .setFont(timesFont)
                .setBorder(Border.NO_BORDER)
                .setPadding(5f)
        )

        document.add(saksiTable)

//        val catatan = Paragraph("Catatan :")
//            .setFontSize(12f)
//            .setFont(timesFont)
//            .setMarginTop(30f)
//        document.add(catatan)
//
//        val noTpsSection = Paragraph("- No TPS: " + state.userInfo?.tpsNo)
//            .setFontSize(12f)
//            .setFont(timesFont)
//        document.add(noTpsSection)
//
//        val deviceIdSection = Paragraph("- Device ID Tablet: " + state.userInfo?.deviceId)
//            .setFontSize(12f)
//            .setFont(timesFont)
//        document.add(deviceIdSection)

        // ===== HALAMAN KEDUA =====
        document.add(AreaBreak(AreaBreakType.NEXT_PAGE))
        document.setMargins(20f, 20f, 20f, 20f)

        // Header halaman kedua
        val title2 = Paragraph("REKAPITULASI PERHITUNGAN SUARA CALON KEPALA DESA SECARA DIGITAL")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(title2)

        val subtitle21 = Paragraph("PEMILIHAN KEPALA DESA ${state.userInfo?.namaKel}")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(subtitle21)

        val subtitle22 = Paragraph("KECAMATAN ${state.userInfo?.namaKec} KABUPATEN KARAWANG")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(subtitle22)

        val yearInfo2 = Paragraph("TAHUN 2025")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(yearInfo2)

        val tpsBilikInfo2 = Paragraph("NO. TPS ${state.userInfo?.tpsNo} / NO. BILIK ${state.userInfo?.bilikNo}")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
        document.add(tpsBilikInfo2)

        val deviceIdInfo2 = Paragraph("DEVICE ID ${state.userInfo?.deviceId}")
            .setFontSize(12f)
            .setFont(timesFont)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(20f)
        document.add(deviceIdInfo2)

        // ========== TABEL REKAP PER CALON ==========
        val columnWidths2 = floatArrayOf(1.5f, 4f, 2f)
        val tableRekap = Table(UnitValue.createPercentArray(columnWidths2))
        tableRekap.setWidth(UnitValue.createPercentValue(100f))

// Header tabel (TANPA JENIS)
        listOf("NO URUT", "NAMA CALON", "JUMLAH").forEach {
            tableRekap.addHeaderCell(
                Cell()
                    .add(Paragraph(it).setBold().setFontSize(11f))
                    .setFont(timesFont)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                    .setPadding(8f)
            )
        }

// Isi data
        state.listSuaraSah
            .sortedBy { it.noUrut.toIntOrNull() ?: Int.MAX_VALUE }
            .forEach { calon ->

                tableRekap.addCell(
                    Cell().add(Paragraph(calon.noUrut))
                        .setFont(timesFont)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6f)
                )

                tableRekap.addCell(
                    Cell().add(Paragraph(calon.nama))
                        .setFont(timesFont)
                        .setPadding(6f)
                )

                tableRekap.addCell(
                    Cell().add(
                        Paragraph(
                            (calon.jumlahLaki + calon.jumlahPerempuan).formatNumber()
                        )
                    )
                        .setFont(timesFont)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(6f)
                )
            }

        // Tambahkan tabel ke halaman
        document.add(tableRekap)

        // Tutup dokumen
        document.close()

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "PDF berhasil disimpan!", Toast.LENGTH_SHORT).show()
        }

        file
    } catch (e: Exception) {
        e.printStackTrace()
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Gagal export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
        null
    }
}

fun openPdfFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Cek apakah ada aplikasi yang bisa handle PDF
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // Coba buka dengan chooser - pakai startActivity langsung
            context.startActivity(Intent.createChooser(intent, "Buka PDF dengan").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    } catch (e: Exception) {
        e.printStackTrace()

        // Fallback: coba buka dengan intent chooser tanpa mime type spesifik
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "*/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(Intent.createChooser(intent, "Buka dengan").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e2: Exception) {
            Toast.makeText(
                context,
                "Gagal membuka PDF: ${e2.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}