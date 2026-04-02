package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.R
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.core.Screen
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.home.components.BigUserInfoCard
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.home.components.GantiKertasCard
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.home.components.MenuCard
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan.components.CConfirmationDialog
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.rekap.components.CPinConfirmationDialog
import com.bit.bilikdigitalkarawang.shared.presentation.components.CAlert
import com.bit.bilikdigitalkarawang.shared.presentation.components.Logo
import com.canopas.lib.showcase.IntroShowcase
import com.canopas.lib.showcase.component.ShowcaseStyle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val bolehBukaRekap = state.jumlahPemilihan == 0

    val menuList = listOf(
        // 🗳️ DATA PEMILIHAN
        MenuItem(
            "Data Kandidat",
            "Profil dan daftar kandidat",
            "Berisi informasi lengkap mengenai calon yang berpartisipasi dalam pemilihan, seperti nama, foto dan nomor urut",
            R.drawable.ic_leader,
            { navController.navigate(Screen.KonfirmasiPin.createRoute(Screen.Kandidat.route)) },
            MenuCategory.DATA_PEMILIHAN
        ),
        MenuItem(
            "Data Pemilih",
            "Daftar pemilih tetap",
            "Merupakan daftar pemilih tetap yang telah terverifikasi untuk mengikuti proses pemilihan secara digital",
            R.drawable.ic_people,
            { navController.navigate(Screen.KonfirmasiPin.createRoute(Screen.Pemilih.route)) },
            MenuCategory.DATA_PEMILIHAN
        ),

        // 🟢 PROSES PEMILIHAN
        MenuItem(
            "Pemilihan",
            "Mulai proses pemungutan suara",
            "Mulai proses pemungutan suara secara langsung setelah QR pemilih diverifikasi",
            R.drawable.ic_vote,
            {
                if (state.sudahGantiKertas == "Y") {
                    navController.navigate(Screen.KonfirmasiPin.createRoute(Screen.SystemCheck.route))
                } else {
                    viewModel.alertGantiKertas()
                }
            },
            MenuCategory.PROSES_PEMILIHAN
        ),
        MenuItem(
            "Rekap Data",
            "Lihat hasil rekapitulasi suara",
            "Lihat total suara per kandidat dan jumlah pemilih secara real-time",
            R.drawable.ic_report,
            {
                if (bolehBukaRekap) {
                    navController.navigate(Screen.KonfirmasiPin.createRoute(Screen.Rekap.route))
                } else {
                    viewModel.confirmBukaRekap()
                }
            },
            MenuCategory.PROSES_PEMILIHAN
        ),
        MenuItem(
            "Cetak Ulang",
            "Mencetak ulang resi suara",
            "Cetak ulang hasil suara menggunakan printer Bluetooth",
            R.drawable.ic_reprint,
            { navController.navigate(Screen.KonfirmasiPin.createRoute(Screen.PrintUlang2.route)) },
            MenuCategory.PROSES_PEMILIHAN
        ),

        // ⚙️ PENGATURAN
        MenuItem(
            "Setting",
            "Konfigurasi aplikasi dan sistem",
            "Atur preferensi aplikasi, backup & restore, sinkronisasi, dan pengaturan sistem lainnya",
            R.drawable.ic_setting,
            { navController.navigate(Screen.KonfirmasiPin.createRoute(Screen.Setting.route)) },
            MenuCategory.PENGATURAN
        ),
        MenuItem(
            "Kelola Printer",
            "Hubungkan printer Bluetooth",
            "Hubungkan printer Bluetooth untuk mencetak hasil suara",
            R.drawable.ic_printer,
            { navController.navigate(Screen.KonfirmasiPin.createRoute(Screen.KelolaPerangkat.route)) },
            MenuCategory.PENGATURAN
        ),
    )


    val scope = rememberCoroutineScope()

    if (state.confirmBukaRekap) {
        CPinConfirmationDialog(
            title = "Konfirmasi Buka Rekapitulasi",
            onConfirm = { pinPanitia, pinPengembang ->
                scope.launch {
                    val result = viewModel.bukaRekap(pinPanitia, pinPengembang)

                    if (result) {
                        navController.navigate(Screen.Rekap.route)
                    } else {
                        viewModel.alertBukaRekap()
                    }
                }
            },
            onCancel = { viewModel.hideAlert() }
        )
    }


    if(state.showAlertTidakBisaBukaRekap) {
        CAlert(
            status = CommonStatus.Error,
            title = "Gagal",
            message = "Kredensial Salah",
            onDismiss = { viewModel.hideAlert() }
        )
    }

    if(state.showAlertGantiKertas) {
        CAlert(
            status = CommonStatus.Error,
            title = "Gagal",
            message = "Silakan konfirmasi bahwa Anda telah mengganti kertas printer sebelum melanjutkan",
            onDismiss = { viewModel.hideAlert() }
        )
    }

    if(state.showConfirmGantiKertas) {
        CConfirmationDialog(
            title = "Konfirmasi Pergantian Kertas",
            message = "Apakah Anda yakin kertas pada printer sudah diganti?",
            confirmText = "Ya, sudah diganti",
            cancelText = "Belum, batal",
            onConfirm = { viewModel.setSudahGantiKertas() },
            onCancel = { viewModel.hideAlert() }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.getUserInfo()
        viewModel.getSudahGantiKertas()
        viewModel.getTotalPemilihan()
    }

    Scaffold { innerPadding ->
        IntroShowcase(
            showIntroShowCase = false,
            onShowCaseCompleted = {
                viewModel.markShowcaseAsShown()
            },
            dismissOnClickOutside = false
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Logo()
                    }
                }

                item {
                    BigUserInfoCard(userInfo = state.userInfo)
                }

                if (state.sudahGantiKertas == "N") {
                    item {
                        GantiKertasCard(onGantiKertas = { viewModel.confirmGantiKertas() })
                    }
                }

                // Spacer sebelum grid
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val dataPemilihanMenus =
                    menuList.filter { it.category == MenuCategory.DATA_PEMILIHAN }

                val prosesPemilihanMenus =
                    menuList.filter { it.category == MenuCategory.PROSES_PEMILIHAN }

                val pengaturanMenus =
                    menuList.filter { it.category == MenuCategory.PENGATURAN }

                // Grid Menu
                MenuSection(
                    title = "Informasi Pemilihan",
                    menus = dataPemilihanMenus
                ) { menu ->
                    MenuCard(
                        title = menu.title,
                        subtitle = menu.subtitle,
                        icon = menu.icon,
                        onClick = menu.onClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                MenuSection(
                    title = "Proses Pemungutan Suara",
                    menus = prosesPemilihanMenus
                ) { menu ->
                    MenuCard(
                        title = menu.title,
                        subtitle = menu.subtitle,
                        icon = menu.icon,
                        onClick = menu.onClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                MenuSection(
                    title = "Pengaturan Sistem",
                    menus = pengaturanMenus
                ) { menu ->
                    MenuCard(
                        title = menu.title,
                        subtitle = menu.subtitle,
                        icon = menu.icon,
                        onClick = menu.onClick
                    )
                }
            }
        }

    }
}

fun <T> LazyListScope.gridItems(
    data: List<T>,
    columns: Int,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    itemContent: @Composable BoxScope.(T) -> Unit
) {
    val rows = if (data.isNotEmpty()) (data.size + columns - 1) / columns else 0
    items(rows) { rowIndex ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = horizontalArrangement
        ) {
            for (columnIndex in 0 until columns) {
                val itemIndex = rowIndex * columns + columnIndex
                if (itemIndex < data.size) {
                    Box(modifier = Modifier.weight(1f)) {
                        itemContent(data[itemIndex])
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

fun LazyListScope.MenuSection(
    title: String,
    menus: List<MenuItem>,
    onBuildItem: @Composable (MenuItem) -> Unit
) {
    if (menus.isEmpty()) return

    // Judul section
    item {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
        )
    }

    // Grid menu
    gridItems(menus, columns = 3) { menu ->
        onBuildItem(menu)
    }
}


data class MenuItem(
    val title: String,
    val subtitle: String,
    val highlightLabel: String,
    val icon: Int,
    val onClick: () -> Unit,
    val category: MenuCategory
)

enum class MenuCategory {
    DATA_PEMILIHAN,
    PROSES_PEMILIHAN,
    PENGATURAN
}
