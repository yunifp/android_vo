package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.qr_scanner

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.core.Screen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.qr_scanner.components.CameraPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScannerScreen(
    navController: NavController,
    onNavigateBack: () -> Unit,
    onNavigateToPemilihan: (nik: String) -> Unit,
    viewModel: QRScannerViewModel = hiltViewModel()
) {
    BackHandler {
        Log.d("PemilihanScreen", "Back button pressed!")
        navController.navigate(
            Screen.KonfirmasiPin.createRoute(Screen.Home.route)
        )
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        val device = usbManager.deviceList.values.firstOrNull {
            it.deviceClass == UsbConstants.USB_CLASS_PRINTER
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == "${context.packageName}.USB_PERMISSION") {
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    // granted/deny bisa di-handle di sini jika perlu
                }
            }
        }

        if (device != null && !usbManager.hasPermission(device)) {
            val permissionIntent = PendingIntent.getBroadcast(
                context, 0,
                Intent("${context.packageName}.USB_PERMISSION"),
                PendingIntent.FLAG_IMMUTABLE
            )
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter("${context.packageName}.USB_PERMISSION"),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
            usbManager.requestPermission(device, permissionIntent)
        }

        onDispose {
            try { context.unregisterReceiver(receiver) } catch (_: Exception) {}
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    // Check permission on first composition
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                viewModel.onPermissionGranted()
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Navigate when NIK is scanned
    LaunchedEffect(state.scannedNik) {
        state.scannedNik?.let { nik ->
            onNavigateToPemilihan(nik)
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !state.hasPermission -> {
                    // Permission request UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(96.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Permission Kamera Diperlukan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 24.dp)
                        )

                        Text(
                            text = "Aplikasi memerlukan akses kamera untuk scan QR code yang berisi NIK",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Berikan Permission Kamera")
                        }
                    }
                }

                state.hasPermission -> {
                    // Camera preview
                    CameraPreview(
                        onQRCodeScanned = { qrContent ->
                            viewModel.onQRCodeScanned(qrContent)
                        },
                        onCameraReady = {
                            viewModel.onCameraReady()
                        },
                        isScanning = true
                    )

                    // Overlay UI
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f)
                    ) {
                        // Instruksi di atas
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Arahkan kamera ke QR code",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Pastikan QR code terlihat jelas dan dalam frame",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                // Total pemilihan di bawah instruksi
                                Text(
                                    text = "Total pemilihan: ${state.totalPemilihan}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Scanning frame di tengah
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(280.dp)
                        ) {
                            // Corner indicators
                            repeat(4) { index ->
                                val alignment = when (index) {
                                    0 -> Alignment.TopStart
                                    1 -> Alignment.TopEnd
                                    2 -> Alignment.BottomStart
                                    else -> Alignment.BottomEnd
                                }
                                Box(
                                    modifier = Modifier
                                        .align(alignment)
                                        .size(40.dp, 6.dp)
                                        .background(
                                            if (state.isScanning) Color.Green else MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(3.dp)
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .align(alignment)
                                        .size(6.dp, 40.dp)
                                        .background(
                                            if (state.isScanning) Color.Green else MaterialTheme.colorScheme.outline,
                                            RoundedCornerShape(3.dp)
                                        )
                                )
                            }

                            if (state.isScanning) {
                                Text(
                                    text = "Letakkan QR code di dalam frame",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .background(
                                            Color.Black.copy(alpha = 0.7f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Loading indicator
                        if (state.isLoading) {
                            Card(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(32.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Memvalidasi NIK...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 16.dp)
                                    )
                                }
                            }
                        }

                        // Status di bawah
                        Card(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            )
                        ) {
                            Text(
                                text = when {
                                    state.isLoading -> "Memvalidasi QR code..."
                                    state.isScanning -> "Siap untuk scan"
                                    !state.isCameraReady -> "Menyiapkan kamera..."
                                    else -> "Scanning dihentikan"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Error message
            state.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .zIndex(2f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text(
                                "Coba Lagi",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}