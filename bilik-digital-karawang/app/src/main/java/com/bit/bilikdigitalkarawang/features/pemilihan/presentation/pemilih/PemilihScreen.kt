
package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilih

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.R
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilih.components.PemilihCard
import com.bit.bilikdigitalkarawang.shared.presentation.components.CAlert
import com.bit.bilikdigitalkarawang.shared.presentation.components.CButton
import com.bit.bilikdigitalkarawang.shared.presentation.components.CHeader
import com.bit.bilikdigitalkarawang.shared.presentation.components.CLoadingDialog
import com.bit.bilikdigitalkarawang.shared.presentation.components.CTextField

@Composable
fun PemilihScreen(
    navController: NavController,
    viewModel: PemilihViewModel = hiltViewModel()
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

    val filteredList = state.pemilihList.filter {
        it.namaPenduduk.contains(state.searchQuery, ignoreCase = true) ||
                it.nik.contains(state.searchQuery)
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

                state.pemilihList.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CButton(
                            label = "Download Data Pemilih",
                            onClick = { viewModel.download() },
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        ambientColor = Color.Black.copy(alpha = 0.5f),
                                        spotColor = Color.Black.copy(alpha = 0.2f),
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "* Terakhir unduh data: ${state.lastTimeGetData}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )

                                    CButton(
                                        icon = R.drawable.ic_download,
                                        label = "Unduh Ulang",
                                        onClick = { viewModel.download() },
                                        backgroundColor = MaterialTheme.colorScheme.onPrimary,
                                        textColor = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        item {
                            CTextField(
                                value = state.searchQuery,
                                onValueChange = { newValue ->
                                    viewModel.updateSearchQuery(newValue)
                                },
                                label = "Cari NIK atau Nama",
                                placeholder = "Masukkan NIK atau Nama",
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                        itemsIndexed(filteredList) { index, item ->
                            PemilihCard(pemilih = item, index = index)
                        }
                    }
                }
            }
        }
    }
}