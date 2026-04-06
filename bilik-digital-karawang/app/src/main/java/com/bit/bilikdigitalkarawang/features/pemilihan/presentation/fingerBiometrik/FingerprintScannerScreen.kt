package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.fingerBiometrik

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun FingerprintScannerScreen(
    isConnected: Boolean,
    capturedBitmap: Bitmap?,
    capturedTemplate: ByteArray?,
    scanTrigger: Int, // 🌟 INI NYAWA UTAMA KITA 🌟
    onBack: () -> Unit,
    onConnect: () -> Unit,
    onSuccessFingerprint: (String) -> Unit,
    viewModel: FingerBiometrikViewModel = hiltViewModel()
) {
    val scanState = viewModel.scanState

    // SINKRONISASI AWAL
    LaunchedEffect(Unit) {
        if (!isConnected) onConnect()
        viewModel.loadFingerprintData()
    }

    // 🌟 OBAT ANTI-TAP 2 KALI & ANTI LOOPING 🌟
    // Menyimpan angka trigger terakhir yang diproses. Berada di luar Crossfade agar ingatannya tidak amnesia saat pindah layar.
    var lastProcessedTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(scanTrigger, viewModel.isDataLoaded, isConnected, capturedTemplate) {
        // Jangan proses jika data belum siap
        if (!isConnected || !viewModel.isDataLoaded || capturedTemplate == null) return@LaunchedEffect

        // Jika angka scanTrigger dari ZKTeco LEBIH BESAR dari yang terakhir kali kita proses, itu PASTI jari baru!
        if (scanTrigger > lastProcessedTrigger) {
            if (scanState is FingerScanState.Idle) {
                // Catat angkanya agar sistem tidak memproses hal yang sama 2 kali (Anti Looping)
                lastProcessedTrigger = scanTrigger

                // Jeda 50ms ke alat ZKTeco buat nulis byte array-nya dengan sempurna
                delay(50)

                // Gunakan copyOf() agar memori aman
                viewModel.processFingerprint(capturedTemplate.copyOf())
            }
        }
    }

    // Tampilan Transisi Layar
    Crossfade(targetState = scanState, label = "Screen Transition") { state ->
        when (state) {
            is FingerScanState.Success -> {
                FingerprintResultScreen(
                    user = state.user,
                    errorMessage = null,
                    isConnected = isConnected,
                    capturedBitmap = capturedBitmap,
                    isDataLoaded = viewModel.isDataLoaded,
                    serverTemplatesCount = viewModel.serverTemplates.size,
                    onRetry = {
                        viewModel.resetScan()
                    },
                    onContinue = { onSuccessFingerprint(state.user.nik) },
                    onBack = onBack,
                    onConnect = onConnect
                )
            }
            is FingerScanState.Error -> {
                FingerprintResultScreen(
                    user = null,
                    errorMessage = state.message,
                    isConnected = isConnected,
                    capturedBitmap = capturedBitmap,
                    isDataLoaded = viewModel.isDataLoaded,
                    serverTemplatesCount = viewModel.serverTemplates.size,
                    onRetry = {
                        if (viewModel.syncError != null) viewModel.loadFingerprintData()
                        viewModel.resetScan()
                    },
                    onContinue = {},
                    onBack = onBack,
                    onConnect = onConnect
                )
            }
            else -> {
                // View ini sekarang 100% murni UI saja, pemrosesannya sudah di-handle di atas
                FingerprintScannerView(
                    isConnected = isConnected,
                    capturedBitmap = capturedBitmap,
                    viewModel = viewModel,
                    onBack = onBack,
                    onConnect = onConnect
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FingerprintScannerView(
    isConnected: Boolean,
    capturedBitmap: Bitmap?,
    viewModel: FingerBiometrikViewModel,
    onBack: () -> Unit,
    onConnect: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val scanState = viewModel.scanState

    val statusText = when {
        !isConnected -> "ALAT TIDAK TERDETEKSI"
        viewModel.syncError != null -> "GAGAL UNDUH DATA"
        !viewModel.isDataLoaded -> "SINKRONISASI DATABASE..."
        scanState is FingerScanState.Verifying -> "MENCOCOKKAN..."
        else -> "MENUNGGU JARI..."
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .zIndex(2f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Text(
                        text = "Fingerprint Check",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Box(modifier = Modifier.size(48.dp))
                }

                // Bagian Tengah
                Column(
                    modifier = Modifier.align(Alignment.Center).zIndex(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ConnectionStatusCard(
                        isConnected = isConnected,
                        isDataLoaded = viewModel.isDataLoaded,
                        templateCount = viewModel.serverTemplates.size,
                        onConnect = onConnect
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    ScannerPreview(
                        capturedBitmap = capturedBitmap,
                        isConnected = isConnected,
                        isDataLoaded = viewModel.isDataLoaded,
                        isVerifying = scanState is FingerScanState.Verifying
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = statusText,
                        color = if (scanState is FingerScanState.Verifying) MaterialTheme.colorScheme.primary else if (viewModel.syncError != null) MaterialTheme.colorScheme.error else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    isConnected: Boolean,
    isDataLoaded: Boolean,
    templateCount: Int,
    onConnect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isConnected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isConnected) "Alat Terhubung" else "Alat Terputus",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isDataLoaded) "Koneksi Aman ($templateCount Jari)" else "Sinkronisasi...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isConnected) {
                Spacer(Modifier.width(16.dp))
                TextButton(onClick = onConnect) {
                    Text("HUBUNGKAN", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ScannerPreview(
    capturedBitmap: Bitmap?,
    isConnected: Boolean,
    isDataLoaded: Boolean,
    isVerifying: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val borderColor = if (isVerifying) primaryColor.copy(alpha = alpha) else Color.DarkGray

    Surface(
        modifier = Modifier
            .size(200.dp, 260.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = Color(0xFF1A1A1A),
        border = BorderStroke(width = if (isVerifying) 4.dp else 2.dp, color = borderColor),
        shadowElevation = if (isVerifying) 12.dp else 4.dp
    ) {
        if (capturedBitmap != null && isConnected && isDataLoaded && !isVerifying) {
            Image(
                bitmap = capturedBitmap.asImageBitmap(),
                contentDescription = "Fingerprint Preview",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.Gray,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = if (!isDataLoaded && isConnected) "LOADING..." else if (isVerifying) "MEMPROSES..." else "STANDBY",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}