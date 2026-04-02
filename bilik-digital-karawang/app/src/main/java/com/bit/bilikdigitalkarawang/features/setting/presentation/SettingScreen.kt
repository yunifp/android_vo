package com.bit.bilikdigitalkarawang.features.setting.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan.components.CConfirmationDialog
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.rekap.components.CPinConfirmationDialog
import com.bit.bilikdigitalkarawang.features.setting.presentation.components.BackupRestoreSection
import com.bit.bilikdigitalkarawang.features.setting.presentation.components.LastSyncCard
import com.bit.bilikdigitalkarawang.features.setting.presentation.components.VotingMethodSection
import com.bit.bilikdigitalkarawang.shared.presentation.components.CAlert
import com.bit.bilikdigitalkarawang.shared.presentation.components.CHeader
import com.bit.bilikdigitalkarawang.shared.presentation.components.CLoadingDialog

@Composable
fun SettingScreen(
    navController: NavController,
    viewModel: SettingViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    if(state.showConfirmSyncRekap) {
        CConfirmationDialog(
            title = "Konfirmasi Sinkronsisasi Data",
            message = "Apakah anda yakin ingin melakukan sinkronisasi data ke server ?",
            onConfirm = { viewModel.syncRekap() },
            onCancel = { viewModel.toggleConfirmSyncRekap(false) }
        )
    }

    if(state.showConfirmSyncRow) {
        CConfirmationDialog(
            title = "Konfirmasi Sinkronsisasi Data",
            message = "Apakah anda yakin ingin melakukan sinkronisasi data ke server ?",
            onConfirm = { viewModel.syncRow() },
            onCancel = { viewModel.toggleConfirmSyncRow(false) }
        )
    }

    if(state.showConfirmDownloadRekap) {
        CConfirmationDialog(
            title = "Konfirmasi Download Data",
            message = "Apakah anda yakin ingin melakukan download data dari server ?",
            onConfirm = { viewModel.downloadListVote() },
            onCancel = { viewModel.toggleConfirmDownloadListVote(false) }
        )
    }

    if (state.showConfirmResetPemilihan) {
        CPinConfirmationDialog(
            title = "Konfirmasi Reset Pemilihan",
            onConfirm = { pinPanitia, pinPengembang ->
                viewModel.resetPemilihan(pinPanitia, pinPengembang)
            },
            onCancel = { viewModel.toggleConfirmResetPemilihan(false) }
        )
    }

    if(state.isSyncingRekap || state.isSyncingRow  || state.isDownloadingListVote || state.isResetingPemilihan || state.isRestoring) {
        CLoadingDialog()
    }

    if (state.statusSyncRekap != null) {
        CAlert(
            status = state.statusSyncRekap!!,
            title = if(state.statusSyncRekap == CommonStatus.Success) "Berhasil Sinkronisasi" else "Gagal Sinkronisasi",
            message = state.statusSyncMessageRekap,
            onDismiss = { viewModel.hideAlert() }
        )
    }

    if (state.statusSyncRow != null) {
        CAlert(
            status = state.statusSyncRow!!,
            title = if(state.statusSyncRow == CommonStatus.Success) "Berhasil Sinkronisasi" else "Gagal Sinkronisasi",
            message = state.statusSyncMessageRow,
            onDismiss = { viewModel.hideAlert() }
        )
    }

    if (state.statusDownloadListVote != null) {
        CAlert(
            status = state.statusDownloadListVote!!,
            title = if(state.statusDownloadListVote == CommonStatus.Success) "Berhasil Download data" else "Gagal Download data",
            message = state.statusMessageDownloadListVote,
            onDismiss = { viewModel.hideAlert() }
        )
    }

    if (state.statusResetPemilihan != null) {
        CAlert(
            status = state.statusResetPemilihan!!,
            title = if(state.statusResetPemilihan == CommonStatus.Success) "Berhasil hapus data" else "Gagal hapus data",
            message = state.statusMessageResetPemilihan,
            onDismiss = { viewModel.hideAlert() }
        )
    }

    if (state.statusSavingExportLocation != null) {
        CAlert(
            status = state.statusSavingExportLocation!!,
            title = if(state.statusSavingExportLocation == CommonStatus.Success) "Berhasil menyimpan path" else "Gagal menyimpan path",
            message = state.saveExportLocationMsg,
            onDismiss = { viewModel.hideAlert() }
        )
    }

    if (state.statusRestoring != null) {
        CAlert(
            status = state.statusRestoring!!,
            title = if(state.statusRestoring == CommonStatus.Success) "Berhasil restoring" else "Gagal restoring",
            message = state.restoringMsg,
            onDismiss = { viewModel.hideAlert() }
        )
    }

    if (state.statusSavingVotingMethod != null) {
        CAlert(
            status = state.statusSavingVotingMethod!!,
            title = if(state.statusSavingVotingMethod == CommonStatus.Success) "Berhasil" else "Gagal",
            message = state.saveVotingMethodMsg,
            onDismiss = { viewModel.hideAlert() }
        )
    }

    // Launcher untuk memilih lokasi file
    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.onSelectLocation(it) }
    }

    // Launcher untuk import file
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.importData(it)
        }
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

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // -> 1. Tampilkan Voting Method Section di sini
                item {
                    VotingMethodSection(
                        selectedMethod = state.votingMethod,
                        onMethodSelected = { method ->
                            viewModel.saveVotingMethod(method)
                        }
                    )
                }
                item {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }

                // -> 2. Baru Backup Restore Section
                item {
                    BackupRestoreSection(
                        exportLocation = state.exportLocation,
                        onSelectExportPath = {
                            createFileLauncher.launch("backup_${System.currentTimeMillis()}.json")
                        },
                        onRestore = {
                            importLauncher.launch(arrayOf("application/json", "application/octet-stream"))
                        }
                    )
                }
                item {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }

                // -> 3. Baru Last Sync Card
                item {
                    LastSyncCard(
                        lastSyncTime = state.lastSyncPemilihan,
                        onSyncRekap = { viewModel.toggleConfirmSyncRekap(true) },
                        onSyncRow = { viewModel.toggleConfirmSyncRow(true) },
                        onDownloadListVote = { viewModel.toggleConfirmDownloadListVote(true) },
                        onResetPemilihan = { viewModel.toggleConfirmResetPemilihan(true) }
                    )
                }
            }
        }
    }
}