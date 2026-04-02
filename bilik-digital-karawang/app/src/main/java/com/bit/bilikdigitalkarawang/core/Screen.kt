package com.bit.bilikdigitalkarawang.core

sealed class Screen(val route: String) {
    object Login: Screen("login_screen")
    object Home: Screen("home_screen")
    object Kandidat: Screen("kandidat_screen")
    object Pemilih: Screen("pemilih_screen")

    object Pemilihan: Screen("pemilihan_screen/{nik}") {
        fun createRoute(nik: String) = "pemilihan_screen/$nik"
    }

    object QrScanner: Screen("qr_scanner_screen")
    object KelolaPerangkat: Screen("kelola_perangkat_screen")
    object Setting: Screen("setting_screen")
    object Pin: Screen("pin_screen")

    // Update ini untuk menerima parameter
    object KonfirmasiPin: Screen("konfirmasi_pin_screen/{targetRoute}") {
        fun createRoute(targetRoute: String) = "konfirmasi_pin_screen/$targetRoute"
    }
    object PrintUlangScanner: Screen("print_ulang_scanner_screen")
    object PrintUlang: Screen("print_ulang_screen/{nik}") {
        fun createRoute(nik: String) = "print_ulang_screen/$nik"
    }
    object PrintUlang2: Screen("print_ulang_2_screen")
    object Rekap: Screen("rekap_screen")

    object SystemCheck: Screen("system_check_screen")

    object FaceRecognition: Screen("face_recognition_screen")

}