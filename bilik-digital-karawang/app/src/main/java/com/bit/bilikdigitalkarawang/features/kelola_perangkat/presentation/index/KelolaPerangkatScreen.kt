package com.bit.bilikdigitalkarawang.features.kelola_perangkat.presentation.index

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.common.ConnectionStatus
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.domain.model.BluetoothDeviceInfo
import com.bit.bilikdigitalkarawang.shared.presentation.components.CAlert
import com.bit.bilikdigitalkarawang.shared.presentation.components.CHeader
import com.bit.bilikdigitalkarawang.shared.presentation.components.CLoadingDialog
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaPerangkatScreen(
    navController: NavController,
    viewModel: KelolaPerangkatViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 👉 Launcher untuk request permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.refreshBluetoothState()
        } else {
//            viewModel.setError("Izin Bluetooth diperlukan untuk melanjutkan.")
        }
    }

    LaunchedEffect(Unit) {
        val permissionsNeeded = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 ke atas
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            } else {
                // Android 11 ke bawah (termasuk Android 8)
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        permissionLauncher.launch(permissionsNeeded.toTypedArray())

        viewModel.loadSavedUsbPrinter()

        viewModel.loadMekanismePrint()
        viewModel.loadSavedUsbPrinter()
    }

    LaunchedEffect(state.isBluetoothEnabled) {
        if (state.isBluetoothEnabled) {
            viewModel.refreshBluetoothState()
        }
    }

    // Handle error messages
    state.errorMessage?.let { error ->
        LaunchedEffect(error) {
            delay(3000)
            viewModel.clearError()
        }
    }

    if(state.isConnecting) {
        CLoadingDialog()
    }

    if(state.connectionStatus == ConnectionStatus.ERROR) {
        CAlert(
            status = CommonStatus.Error,
            title = "Gagal",
            message = state.errorMessage ?: "Gagal terhubung",
            onDismiss = {
                viewModel.hideAlert()
            }
        )
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            CHeader(onBack = { navController.navigateUp() })

            Spacer(modifier = Modifier.height(16.dp))

            // Ganti semua yang lama dengan ini
            PrinterSection(state = state, viewModel = viewModel)

            // Error/Success Message tetap ada
            state.errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (message.contains("berhasil"))
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = if (message.contains("berhasil"))
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}


@Composable
private fun StatusSection(
    state: KelolaPerangkatState,
    viewModel: KelolaPerangkatViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusCard(
            title = "Bluetooth",
            status = if (state.isBluetoothEnabled) "Aktif" else "Tidak Aktif",
            isActive = state.isBluetoothEnabled,
            modifier = Modifier.weight(1f)
        )

        StatusCard(
            title = "Printer",
            status = when (state.connectionStatus) {
                ConnectionStatus.CONNECTED -> "Terhubung"
                ConnectionStatus.CONNECTING -> "Menghubungkan..."
                ConnectionStatus.ERROR -> "Error"
                else -> "Tidak Terhubung"
            },
            isActive = state.connectionStatus == ConnectionStatus.CONNECTED,
            modifier = Modifier.weight(1f)
        )
    }

    state.selectedPrinter?.let { printer ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Printer Terpilih:",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = printer.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = printer.macAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    status: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    state: KelolaPerangkatState,
    viewModel: KelolaPerangkatViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                if (state.isScanning) {
                    viewModel.stopScan()
                } else {
                    viewModel.startScan()
                }
            },
            enabled = state.isBluetoothEnabled,
            modifier = Modifier.weight(1f)
        ) {
            if (state.isScanning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Scan")
            } else {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Device")
            }
        }

        Button(
            onClick = { viewModel.print() },
            enabled = state.connectionStatus == ConnectionStatus.CONNECTED,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Print, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Print")
        }
    }
}

@Composable
private fun DeviceListsSection(
    state: KelolaPerangkatState,
    viewModel: KelolaPerangkatViewModel
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Paired Devices
        if (state.pairedDevices.isNotEmpty()) {
            item {
                Text(
                    text = "Perangkat Terpasang",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            items(state.pairedDevices.filter { it.isPrinter }) { device ->
                DeviceCard(
                    device = device,
                    isSelected = device.macAddress == state.selectedPrinter?.macAddress,
                    isConnecting = state.isConnecting,
                    onDeviceClick = { viewModel.selectPrinter(device) }
                )
            }
        }

        // Discovered Devices
        if (state.discoveredDevices.isNotEmpty()) {
            item {
                Text(
                    text = "Perangkat Ditemukan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            items(state.discoveredDevices.filter { it.isPrinter }) { device ->
                DeviceCard(
                    device = device,
                    isSelected = device.macAddress == state.selectedPrinter?.macAddress,
                    isConnecting = state.isConnecting,
                    onDeviceClick = { viewModel.selectPrinter(device) }
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: BluetoothDeviceInfo,
    isSelected: Boolean,
    isConnecting: Boolean,
    onDeviceClick: () -> Unit
) {
    Card(
        onClick = onDeviceClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Bluetooth,
                contentDescription = null,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = device.macAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isConnecting && isSelected) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PrinterSection(
    state: KelolaPerangkatState,
    viewModel: KelolaPerangkatViewModel
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Koneksi Printer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle Bluetooth / Cable
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            // Tombol Bluetooth
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (state.mekanismePrint == "bt")
                            MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { viewModel.setMekanismePrint("bt") }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Bluetooth,
                        contentDescription = null,
                        tint = if (state.mekanismePrint == "bt")
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Bluetooth",
                        color = if (state.mekanismePrint == "bt")
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (state.mekanismePrint == "bt")
                            FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            // Tombol Cable
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (state.mekanismePrint == "cbl")
                            MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable { viewModel.setMekanismePrint("cbl") }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Usb,
                        contentDescription = null,
                        tint = if (state.mekanismePrint == "cbl")
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Kabel",
                        color = if (state.mekanismePrint == "cbl")
                            MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (state.mekanismePrint == "cbl")
                            FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Konten sesuai pilihan
        when (state.mekanismePrint) {
            "bt" -> {
                // Tampilkan section bluetooth yang sudah ada
                state.savedPrinterInfo?.let { printer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Printer tersimpan:", style = MaterialTheme.typography.labelSmall)
                            Text(printer.name, fontWeight = FontWeight.SemiBold)
                            Text(printer.macAddress, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } ?: Text(
                    "Belum ada printer Bluetooth yang tersimpan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))
                StatusSection(state = state, viewModel = viewModel)
                Spacer(modifier = Modifier.height(8.dp))
                ActionButtonsSection(state = state, viewModel = viewModel)
                Spacer(modifier = Modifier.height(8.dp))
                DeviceListsSection(state = state, viewModel = viewModel)
            }

            "cbl" -> {
                state.savedUsbPrinterInfo?.let { printer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Printer tersimpan:", style = MaterialTheme.typography.labelSmall)
                            Text(printer.deviceName, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } ?: Text(
                    "Belum ada printer USB yang tersimpan",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { viewModel.scanUsbPrinters(context) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Usb, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cari Printer USB")
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (state.usbPrinterList.isEmpty()) {
                    Text(
                        "Tidak ada printer USB terdeteksi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                state.usbPrinterList.forEach { printer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selectUsbPrinter(context, printer) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (state.savedUsbPrinterInfo?.vendorId == printer.vendorId)
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(printer.deviceName, fontWeight = FontWeight.Medium)
                                Text(
                                    "VendorID: ${printer.vendorId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (state.savedUsbPrinterInfo?.vendorId == printer.vendorId) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}