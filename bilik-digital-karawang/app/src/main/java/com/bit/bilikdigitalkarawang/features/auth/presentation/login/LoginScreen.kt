package com.bit.bilikdigitalkarawang.features.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.core.Screen
import com.bit.bilikdigitalkarawang.shared.presentation.components.CAlert
import com.bit.bilikdigitalkarawang.shared.presentation.components.CButton
import com.bit.bilikdigitalkarawang.shared.presentation.components.CTextField
import com.bit.bilikdigitalkarawang.ui.theme.Typography
import com.bit.bilikdigitalkarawang.shared.presentation.components.Logo
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val isFormEnable = state.status != CommonStatus.Loading

    LaunchedEffect(state.status) {
        if (state.status == CommonStatus.Success) {
            delay(1000)
            navController.navigate(Screen.Pin.route) {
                popUpTo(Screen.Login.route) {
                    inclusive = true
                }
            }
        }
    }

    if (state.showAlert) {
        CAlert(
            status = state.status,
            title = if(state.status == CommonStatus.Success) "Berhasil Masuk" else "Gagal Masuk",
            message = state.statusMsg,
            onDismiss = { viewModel.hideAlert() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            Logo()

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Selamat Datang, \ndi Aplikasi Bilik Digital",
                style = Typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            CTextField(
                label = "Username",
                value = state.formData.username,
                placeholder = "Masukkan Username",
                onValueChange = { str -> viewModel.updateForm("username", str) },
                enabled = isFormEnable
            )

            Spacer(modifier = Modifier.height(16.dp))

            CTextField(
                label = "Password",
                value = state.formData.password,
                placeholder = "Masukkan Password",
                onValueChange = { str -> viewModel.updateForm("password", str) },
                enabled = isFormEnable
            )

            Spacer(modifier = Modifier.height(32.dp))

            CButton(
                modifier = Modifier.fillMaxWidth(),
                label = if(isFormEnable) "Masuk" else "Mohon Tunggu",
                onClick = { viewModel.submit() },
                enabled = isFormEnable
            )
        }
    }
}