package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.print_ulang2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.core.Screen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan.components.CConfirmationDialog
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.print_ulang2.PrintUlang2ViewModel
import com.bit.bilikdigitalkarawang.shared.presentation.components.CAlert
import com.bit.bilikdigitalkarawang.shared.presentation.components.CLoadingDialog
import kotlinx.coroutines.delay

@Composable
fun PrintUlang2Screen(
    navController: NavController,
    viewModel: PrintUlang2ViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if(state.checkingNikStatus == false) {
        CAlert(
            status = CommonStatus.Error,
            title = "Gagal",
            message = state.checkingNikStatusMsg,
            onDismiss = {}
        )
    }

    if(state.printingStatus != null) {
        CAlert(
            status = if(state.printingStatus == true) CommonStatus.Success else CommonStatus.Error,
            title =  if(state.printingStatus == true) "Berhasil" else "Gagal",
            message = state.printingMsg,
            onDismiss = {}
        )
    }

//    if(state.checkingHasPrintUlangStatus == true) {
//        CAlert(
//            status = CommonStatus.Error,
//            title = "Gagal",
//            message = state.checkingHasPrintUlangMsg,
//            onDismiss = {}
//        )
//    }

    if(state.updatingHasPrintUlangStatus == true) {
        CAlert(
            status = CommonStatus.Success,
            title = "Berhasil",
            message = state.updatingHasPrintUlangMsg,
            onDismiss = {}
        )
    }

//    LaunchedEffect(state.checkingNikStatus) {
//        if(state.checkingNikStatus == false) {
//            delay(2000)
//            navController.navigate(Screen.PrintUlangScanner.route) {
//                popUpTo(Screen.PrintUlang.route) {
//                    inclusive = true
//                }
//            }
//        }
//    }
//
//    LaunchedEffect(state.checkingHasPrintUlangStatus) {
//        if(state.checkingHasPrintUlangStatus == true) {
//            delay(2000)
//            navController.navigate(Screen.PrintUlangScanner.route) {
//                popUpTo(Screen.PrintUlang.route) {
//                    inclusive = true
//                }
//            }
//        }
//    }

    LaunchedEffect(state.updatingHasPrintUlangStatus) {
        if(state.updatingHasPrintUlangStatus == true) {
            delay(2000)
            viewModel.toggleSelesaiPrintUlang(false)
            viewModel.hideAlert()
        }
    }

    LaunchedEffect(state.printingStatus) {
        if(state.printingStatus == false || state.printingStatus == true) {
            delay(2000)
            viewModel.resetPrintStatus()
            viewModel.toggleSelesaiPrintUlang(true)
        }
    }

    if(state.isPrinting == true) {
        CLoadingDialog()
    }

    if(state.showConfirmSelesaiPrintUlang == true) {
        CConfirmationDialog(
            title = "Konfirmasi",
            message = "Cetak ulang sudah selesai dilakukan, jika resi telah keluar maka boleh tekan \"Selesai\", jika belum tekan \"Ulangi\"",
            confirmText = "Selesai",
            cancelText = "Ulangi",
            onConfirm = { viewModel.selesaiPrintUlang() },
            onCancel = { viewModel.toggleSelesaiPrintUlang(false) },
        )
    }

    when(state.isCheckingNik == null || state.isCheckingHasPrintUlang == null) {
        true -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "Memverifikasi data...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
        false -> {
            if(state.checkingNikStatus == true) {
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        MaterialTheme.colorScheme.background,
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                            .padding(innerPadding)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Header Section
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (state.checkingHasPrintUlangStatus == true) "Sudah Pernah Dicetak" else "Verifikasi Berhasil",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = if (state.checkingHasPrintUlangStatus == true)
                                        "Data pemilih ini sudah pernah melakukan cetak ulang sebelumnya"
                                    else
                                        "Data pemilih ditemukan dan siap untuk dicetak ulang",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }

                            // Voter Information Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                                                )
                                            )
                                        )
                                        .padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    // Title with Icon
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Data Pemilih Terakhir",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }

                                    Divider(
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                                        thickness = 1.dp
                                    )

                                    // Name Field
                                    InfoRow(
                                        label = "Nama Lengkap",
                                        value = state.informasiPemilih?.namaPenduduk ?: "-",
                                        icon = Icons.Default.Person
                                    )

                                    // NIK Field
                                    InfoRow(
                                        label = "Nomor Induk Kependudukan",
                                        value = state.informasiPemilih?.nik ?: "-",
                                        icon = Icons.Default.CreditCard
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Conditional Info Banner
                            if (state.checkingHasPrintUlangStatus == true) {
                                // Warning Banner for Already Printed
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFFF3CD) // Warna kuning peringatan
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFF856404), // Warna kuning gelap
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Pemilih ini sudah pernah melakukan cetak ulang sebelumnya. Tidak dapat melakukan cetak ulang lagi.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF856404)
                                        )
                                    }
                                }
                            } else {
                                // Info Banner
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Pastikan data sudah benar sebelum melakukan cetak ulang",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }

                            // Conditional Buttons
                            if (state.checkingHasPrintUlangStatus == true) {
                                // Only Back Button
                                Button(
                                    onClick = {
                                        navController.navigate(Screen.Home.route) {
                                            popUpTo(Screen.PrintUlang2.route) {
                                                inclusive = true
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp,
                                        pressedElevation = 8.dp
                                    )
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Kembali",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    }
                                }
                            } else {
                                // Print and Complete Buttons
                                Row {
                                    // Print Button
                                    Button(
                                        onClick = { viewModel.print() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 6.dp,
                                            pressedElevation = 8.dp
                                        )
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Print,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Text(
                                                text = "Cetak Ulang Sekarang",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        null -> {}
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}