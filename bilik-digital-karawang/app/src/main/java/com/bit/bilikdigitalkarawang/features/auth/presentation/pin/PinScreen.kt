package com.bit.bilikdigitalkarawang.features.auth.presentation.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.core.Screen
import com.bit.bilikdigitalkarawang.features.auth.presentation.pin.components.PinInput
import com.bit.bilikdigitalkarawang.shared.presentation.components.CButton

@Composable
fun PinScreen(
    navController: NavController,
    viewModel: PinViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.successMessage) {
        if (state.successMessage == "PIN berhasil disimpan") {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Pin.route) { inclusive = true }
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (!state.isPinCreated) "Buat PIN" else "Konfirmasi PIN",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PinInput(
                    pinValue = if (!state.isPinCreated) state.pin else state.confirmPin,
                    onPinValueChange = {
                        if (!state.isPinCreated) {
                            viewModel.onPinChanged(it)
                        } else {
                            viewModel.onConfirmPinChanged(it)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CButton(
                modifier = Modifier.fillMaxWidth(),
                label = if (!state.isPinCreated) "Lanjut" else "Simpan PIN",
                onClick = {
                    if (!state.isPinCreated) {
                        viewModel.markPinCreated()
                    } else {
                        viewModel.savePinIfMatched()
                    }
                }
            )
            if (state.isPinCreated) {
                TextButton(
                    onClick = { viewModel.resetPinCreation() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Kembali ke Buat PIN")
                }
            }

            if (state.errorMessage.isNotEmpty()) {
                Text(
                    text = state.errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (state.successMessage.isNotEmpty()) {
                Text(
                    text = state.successMessage,
                    color = Color.Green,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }

}