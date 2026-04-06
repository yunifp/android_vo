package com.bit.bilikdigitalkarawang.core

import android.graphics.Bitmap
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.bit.bilikdigitalkarawang.features.auth.presentation.konfirmasi_pin.KonfirmasiPinScreen
import com.bit.bilikdigitalkarawang.features.auth.presentation.login.LoginScreen
import com.bit.bilikdigitalkarawang.features.auth.presentation.pin.PinScreen
import com.bit.bilikdigitalkarawang.features.kelola_perangkat.presentation.index.KelolaPerangkatScreen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.home.HomeScreen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.kandidat.KandidatScreen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilih.PemilihScreen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan.PemilihanScreen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.print_ulang.PrintUlangScreen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.print_ulang2.PrintUlang2Screen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.qr_scanner.QRScannerScreen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.rekap.RekapScreen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.system_check.SystemCheckScreen
import com.bit.bilikdigitalkarawang.features.setting.presentation.SettingScreen
import com.bit.bilikdigitalkarawang.shared.data.source.local.datastore.DataStoreDiv

// IMPORT FACE DETECTOR SCREEN DI SINI
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.face_recognition.FaceDetectorScreen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.fingerBiometrik.FingerprintScannerScreen
import kotlinx.coroutines.flow.first

@Composable
fun Navigation(
    navController: NavHostController,
    dataStoreDiv: DataStoreDiv,
    // --- PARAMETER TAMBAHAN UNTUK HARDWARE ZKTECO ---
    isConnected: Boolean,
    capturedBitmap: Bitmap?,
    capturedTemplate: ByteArray?,
    scanTrigger: Int,
    onConnectRequest: () -> Unit,
    onResetHardwareData: () -> Unit
) {

    var hasLogin by remember { mutableStateOf<Boolean?>(null) }
    var hasPin by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        hasLogin = (dataStoreDiv.getData("has_login").first() ?: "") == "Y"
        hasPin = (dataStoreDiv.getData("has_pin").first() ?: "") == "Y"
    }

    if (hasLogin == null || hasPin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = if(hasLogin == true) (if(hasPin == true) Screen.Home.route else Screen.Pin.route) else Screen.Login.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            composable(route = Screen.Login.route) {
                LoginScreen(navController)
            }
            composable(route = Screen.Pin.route) {
                PinScreen(navController)
            }
            composable(route = Screen.Home.route) {
                HomeScreen(navController)
            }
            composable(route = Screen.Kandidat.route) {
                KandidatScreen(navController)
            }
            composable(route = Screen.Pemilih.route) {
                PemilihScreen(navController)
            }
            composable(
                route = Screen.Pemilihan.route,
                arguments = listOf(
                    navArgument("nik") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val nik = backStackEntry.arguments?.getString("nik") ?: ""
                PemilihanScreen(
                    navController = navController,
                    nik = nik
                )
            }
            composable(route = Screen.QrScanner.route) {
                QRScannerScreen(
                    navController = navController,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPemilihan = { nik ->
                        navController.navigate(Screen.Pemilihan.createRoute(nik)) {
                            popUpTo(Screen.QrScanner.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(route = Screen.KelolaPerangkat.route) {
                KelolaPerangkatScreen(navController)
            }
            composable(route = Screen.Setting.route) {
                SettingScreen(navController)
            }
            composable(
                route = Screen.KonfirmasiPin.route,
                arguments = listOf(
                    navArgument("targetRoute") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                val targetRoute = backStackEntry.arguments?.getString("targetRoute") ?: ""

                KonfirmasiPinScreen(
                    navController = navController,
                    targetRoute = targetRoute,
                )
            }
            composable(route = Screen.PrintUlangScanner.route) {
                QRScannerScreen(
                    navController = navController,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToPemilihan = { nik ->
                        navController.navigate(Screen.PrintUlang.createRoute(nik)) {
                            popUpTo(Screen.PrintUlangScanner.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Screen.PrintUlang.route,
                arguments = listOf(
                    navArgument("nik") {
                        type = NavType.StringType
                        nullable = false
                    }
                )
            ) { backStackEntry ->
                val nik = backStackEntry.arguments?.getString("nik") ?: ""
                PrintUlangScreen(
                    navController = navController,
                    nik = nik
                )
            }
            composable(
                route = Screen.PrintUlang2.route,
            ) { backStackEntry ->
                PrintUlang2Screen(
                    navController = navController,
                )
            }
            composable(route = Screen.Rekap.route) {
                RekapScreen(navController)
            }
            composable(route = Screen.SystemCheck.route) {
                SystemCheckScreen(navController)
            }

            // ===== INI ADALAH BLOK FACE RECOGNITION =====
            composable(route = Screen.FaceRecognition.route) {
                FaceDetectorScreen(
                    onBack = { navController.popBackStack() },
                    onSuccessLiveness = { nik ->
                        navController.navigate(Screen.Pemilihan.createRoute(nik)) {
                            popUpTo(Screen.FaceRecognition.route) { inclusive = true }
                        }
                    }
                )
            }
            // =======================================

            // ===== INI ADALAH BLOK FINGERPRINT YANG DITAMBAHKAN =====
            composable(route = Screen.FingerBiometrik.route) {
                // Mereset cache data jari setiap kali masuk ke halaman ini
                LaunchedEffect(Unit) {
                    onResetHardwareData()
                }

                FingerprintScannerScreen(
                    isConnected = isConnected,
                    capturedBitmap = capturedBitmap,
                    capturedTemplate = capturedTemplate,
                    onConnect = onConnectRequest,
                    scanTrigger = scanTrigger,
                    onBack = { navController.popBackStack() },
                    onSuccessFingerprint = { nik ->
                        navController.navigate(Screen.Pemilihan.createRoute(nik)) {
                            popUpTo(Screen.FingerBiometrik.route) { inclusive = true }
                        }
                    }
                )
            }
            // =======================================
        }
    }
}