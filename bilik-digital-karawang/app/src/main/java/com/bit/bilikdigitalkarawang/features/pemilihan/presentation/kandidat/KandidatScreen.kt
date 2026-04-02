package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.kandidat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.kandidat.components.KandidatCard
import com.bit.bilikdigitalkarawang.shared.presentation.components.CAlert
import com.bit.bilikdigitalkarawang.shared.presentation.components.CButton
import com.bit.bilikdigitalkarawang.shared.presentation.components.CHeader
import com.bit.bilikdigitalkarawang.shared.presentation.components.CLoadingDialog

@Composable
fun KandidatScreen(
    navController: NavController,
    viewModel: KandidatViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    if(state.downloadStatus == CommonStatus.Loading) {
        CLoadingDialog()
    }

    if(state.showAlert){
        CAlert(
            status = state.downloadStatus,
            title = if(state.downloadStatus == CommonStatus.Success) "Berhasil" else "Gagal",
            message = state.downloadStatusMsg,
            onDismiss = { viewModel.hideAlert() }
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

            when {
                state.isInitialLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.kandidatList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CButton(
                            label = "Download Data Kandidat",
                            onClick = { viewModel.download() },
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.kandidatList) {
                            KandidatCard(kandidat = it)
                        }
                    }
                }
            }

        }
    }

}