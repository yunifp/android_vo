package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.bit.bilikdigitalkarawang.R
import com.bit.bilikdigitalkarawang.common.CommonStatus
import com.bit.bilikdigitalkarawang.core.Screen
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan.components.KandidatConfirmationDialog
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan.components.PilihKandidatCard
import com.bit.bilikdigitalkarawang.shared.presentation.components.CAlert
import com.bit.bilikdigitalkarawang.shared.presentation.components.CButton
import com.bit.bilikdigitalkarawang.shared.presentation.components.CLoadingDialog
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PemilihanScreen(
    navController: NavController,
    nik: String,
    viewModel: PemilihanViewModel = hiltViewModel(),
) {

    BackHandler {
        navController.navigate(Screen.QrScanner.route) {
            popUpTo(Screen.Pemilihan.route) {
                inclusive = true
            }
        }
    }

    val context = LocalContext.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val speak = rememberTextToSpeech(context)

    if(state.checkingNikStatus == true) {
        LaunchedEffect(speak) {
            speak("Silahkan pilih kandidat yang Anda inginkan")
        }
    }

    var previousSelection by remember { mutableStateOf<List<Kandidat>>(emptyList()) }

    LaunchedEffect(state.kandidatTerpilih) {
        if (state.kandidatTerpilih != previousSelection) {
            speak("Tekan tombol di bawah untuk melakukan pemilihan")
            previousSelection = state.kandidatTerpilih
        }
    }

    if(state.checkingNikStatus == false) {
        CAlert(
            status = CommonStatus.Error,
            title = "Gagal",
            message = state.checkingNikStatusMsg,
            onDismiss = {}
        )
    }

    LaunchedEffect(state.checkingNikStatus) {
        if(state.checkingNikStatus == false) {
            delay(2000)
            navController.navigate(Screen.QrScanner.route) {
                popUpTo(Screen.Pemilihan.route) {
                    inclusive = true
                }
            }
        }
    }

    if (state.voteStatus == CommonStatus.Success || state.voteStatus == CommonStatus.Error) {
        CAlert(
            status = state.voteStatus,
            title = if(state.voteStatus == CommonStatus.Success) "Terima Kasih" else "Gagal",
            message = state.voteStatusMsg,
            onDismiss = {}
        )
    }

    if (state.printStatus == CommonStatus.Error) {
        CAlert(
            status = state.printStatus,
            title = "Gagal Print",
            message = state.printStatusMsg,
            onDismiss = {}
        )
    }

    LaunchedEffect(state.voteStatus) {
        if (state.voteStatus == CommonStatus.Success || state.voteStatus == CommonStatus.Error) {
            if(state.voteStatus == CommonStatus.Success) {
                speak("Silahkan ambil struk")
            }
            delay(5000)
            viewModel.resetSuccessVote()
            navController.navigate(Screen.QrScanner.route) {
                popUpTo(Screen.Pemilihan.route) {
                    inclusive = true
                }
            }
        }
    }

    LaunchedEffect(nik) {
        viewModel.setNik(nik)
    }

    LaunchedEffect(Unit) {
        viewModel.getUserInfo()
    }

    if(state.voteStatus == CommonStatus.Loading) {
        CLoadingDialog(state.voteStatusMsg)
    }

    if(state.isCheckingNik == true) {
        CLoadingDialog(state.checkingNikStatusMsg)
    } else if(state.isCheckingNik == false && state.checkingNikStatus == true) {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    // Logo
                    Image(
                        painter = painterResource(R.drawable.logo_karawang),
                        contentDescription = "Logo Karawang",
                        modifier = Modifier
                            .width(80.dp)
                            .height(80.dp)
                    )

                    // Teks Header
                    Text(
                        text = "SURAT SUARA",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )


                    Text(
                        text = "PEMILIHAN KEPALA DESA ${state.userInfo?.namaKel}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "KECAMATAN ${state.userInfo?.namaKec}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "KABUPATEN KARAWANG",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "TAHUN 2025",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }


                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val kandidatList = state.kandidatList.take(5)

                    kandidatList.forEach { kandidat ->
                        PilihKandidatCard(
                            kandidat = kandidat,
                            selected = state.kandidatTerpilih.any { it.noUrut == kandidat.noUrut },
                            onClick = { viewModel.toggleKandidatSelection(kandidat) },
                            modifier = Modifier
                                .width(240.dp)  // lebar tetap, bisa kamu sesuaikan
                                .height(330.dp) // tinggi tetap, proporsional dengan lebar
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

                // Tambahkan animasi di button
                val shouldAnimate = state.kandidatTerpilih.isNotEmpty()

                val infiniteTransition = rememberInfiniteTransition(label = "pulse")

                val alpha by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 800,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.95f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 800,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )

                // Animasi untuk icon tangan (bounce effect)
                val iconOffset by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 600,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "iconOffset"
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CButton(
                        label = "PILIH KANDIDAT INI",
                        onClick = { viewModel.showConfirm(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .graphicsLayer {
                                if (shouldAnimate) {
                                    this.alpha = alpha
                                    scaleX = scale
                                    scaleY = scale
                                }
                            },
                        backgroundColor = MaterialTheme.colorScheme.error,
                        textColor = Color.White,
                        enabled = state.voteStatus != CommonStatus.Loading
                    )


                    if (shouldAnimate) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_finger_pointing),
                            contentDescription = "Tap here",
                            modifier = Modifier
                                .size(96.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (-44).dp) // Posisi di atas button (negatif untuk ke atas)
                                .graphicsLayer {
                                    translationY = -iconOffset // Negatif agar bounce ke bawah (menunjuk button)
                                    rotationZ = 180f // Rotasi 180 derajat agar jari menunjuk ke bawah
                                }
                        )
                    }
                }
            }
        }
    }

    if (state.showConfirmation) {
        KandidatConfirmationDialog(
            selectedCandidates = state.kandidatTerpilih,
            onConfirm = {
                viewModel.vote()
            },
            onCancel = {
                viewModel.resetKandidat()
                viewModel.showConfirm(false)
            }
        )
    }
}

@Composable
fun rememberTextToSpeech(context: Context): (String) -> Unit {
    var isReady by remember { mutableStateOf(false) }
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        tts.value = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts.value?.language = Locale("id", "ID")
                isReady = true
            }
        }

        onDispose {
            tts.value?.shutdown()
            tts.value = null
        }
    }

    return remember(isReady) {
        { text: String ->
            if (isReady) {
                tts.value?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            } else {
            }
        }
    }
}