package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.system_check

// ===== Composable Screen =====
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.core.Screen
import com.bit.bilikdigitalkarawang.shared.presentation.components.Logo

data class CheckItem(
    val title: String,
    val isLoading: Boolean = false,
    val isChecked: Boolean = false,
    val isSuccess: Boolean = false,
    val message: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemCheckScreen(
    navController: NavController,
    viewModel: SystemCheckViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.startSystemCheck()
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box (
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ){
                Logo()
            }

            // Check Items
            CheckItemCard(
                title = "Cek Data Kandidat",
                status = state.kandidatCheck,
                message = state.kandidatCheckMsg
            )
            CheckItemCard(
                title = "Cek Data Pemilih",
                status = state.pemilihCheck,
                message = state.pemilihCheckMsg
            )
            CheckItemCard(
                title = "Cek SD Card",
                status = state.sdCardCheck,
                message = state.sdCardCheckMsg
            )
            CheckItemCard(
                title = "Cek Setting Backup",
                status = state.backupPathCheck,
                message = state.backupPathCheckMsg
            )

            CheckItemCard(
                title = "Cek Koneksi Ke Printer",
                status = state.printerCheck,
                message = state.printerCheckMsg
            )


            // Action Buttons
            val allChecked = state.kandidatCheck !is CommonStatus.Idle &&
                    state.pemilihCheck !is CommonStatus.Idle &&
                    state.sdCardCheck !is CommonStatus.Idle &&
                    state.backupPathCheck !is CommonStatus.Idle
                    && state.printerCheck !is CommonStatus.Idle // DIABAIKAN SEMENTARA
            val allSuccess = state.kandidatCheck is CommonStatus.Success &&
                    state.pemilihCheck is CommonStatus.Success &&
                    state.sdCardCheck is CommonStatus.Success &&
                    state.backupPathCheck is CommonStatus.Success
                    && state.printerCheck is CommonStatus.Success // DIABAIKAN SEMENTARA

            if (allChecked) {
                Spacer(modifier = Modifier.height(8.dp))

                if (allSuccess) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // ----- LOGIKA BARU NAVIGASI BERDASARKAN MODE -----
                                when (state.votingMethod) {
                                    "Face Recognition" -> {
                                        navController.navigate(Screen.FaceRecognition.route) {
                                            popUpTo(Screen.SystemCheck.route) { inclusive = true }
                                        }
                                    }
                                    "Fingerprint" -> {
                                        navController.navigate(Screen.FingerBiometrik.route) {
                                            popUpTo(Screen.SystemCheck.route) { inclusive = true }
                                        }
                                    }
                                    else -> {
                                        navController.navigate(Screen.QrScanner.route) {
                                            popUpTo(Screen.SystemCheck.route) { inclusive = true }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lanjutkan")
                        }

                        OutlinedButton(
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.SystemCheck.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kembali ke Home")
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Beberapa pengecekan gagal. Silakan perbaiki masalah yang ada.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.retryCheck() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cek Ulang")
                        }

                        OutlinedButton(
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.SystemCheck.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kembali ke Home")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckItemCard(
    title: String,
    status: CommonStatus,
    message: String = ""
) {
    Card(
        modifier = Modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.2f),
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                when (status) {
                    is CommonStatus.Success -> {
                        if (message.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    is CommonStatus.Error -> {
                        if (message.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Status Icon/Loading
            when (status) {
                is CommonStatus.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                is CommonStatus.Success -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                is CommonStatus.Error -> {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
                is CommonStatus.Idle -> {
                    Icon(
                        imageVector = Icons.Default.HourglassEmpty,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}