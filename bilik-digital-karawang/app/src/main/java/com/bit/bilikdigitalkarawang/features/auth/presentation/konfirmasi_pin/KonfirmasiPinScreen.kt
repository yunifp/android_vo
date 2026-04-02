package com.bit.bilikdigitalkarawang.features.auth.presentation.konfirmasi_pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.core.Screen
import com.bit.bilikdigitalkarawang.features.auth.presentation.konfirmasi_pin.components.KonfirmasiPinInput

@Composable
fun KonfirmasiPinScreen(
    navController: NavController,
    targetRoute: String,
    viewModel: KonfirmasiPinViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Navigate ketika PIN berhasil diverifikasi
    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            navController.navigate(targetRoute) {
                popUpTo(Screen.KonfirmasiPin.route) { inclusive = true }
            }
        }
    }

    // Reset error message setelah beberapa detik
    LaunchedEffect(state.errorMessage) {
        if (state.errorMessage.isNotEmpty()) {
            kotlinx.coroutines.delay(2000)
            viewModel.resetError()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Masukkan PIN",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Konfirmasi PIN untuk melanjutkan",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )

            Spacer(modifier = Modifier.height(32.dp))

            KonfirmasiPinInput(
                pinValue = state.pin,
                onPinValueChange = viewModel::onPinChanged,
                isError = state.errorMessage.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loading indicator
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Error message
            if (state.errorMessage.isNotEmpty()) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol kembali (opsional)
            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("Kembali")
            }
        }
    }
}