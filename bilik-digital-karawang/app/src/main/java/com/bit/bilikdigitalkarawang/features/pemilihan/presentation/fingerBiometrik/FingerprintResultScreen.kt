package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.fingerBiometrik

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.face_recognition.RetrofitClient
import com.bit.bilikdigitalkarawang.features.pemilihan.presentation.face_recognition.UserData
import kotlinx.coroutines.delay

private val PrimaryBlue = Color(0xFF4361EE)
private val BackgroundGray = Color(0xFFF4F6F9)
private val SuccessGreen = Color(0xFF2ECC71)
private val ErrorRed = Color(0xFFE74C3C)

@Composable
fun FingerprintResultScreen(
    user: UserData?,
    errorMessage: String?,
    isConnected: Boolean,
    capturedBitmap: Bitmap?,
    isDataLoaded: Boolean,
    serverTemplatesCount: Int,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onConnect: () -> Unit
) {
    val isRecognized = user != null

    BackHandler {
        if (isRecognized) onBack() else onRetry()
    }

    var showResult by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        showResult = true
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            // ===============================================
            // BACKGROUND (Menyamar sebagai halaman scanner)
            // ===============================================
            Box(modifier = Modifier.fillMaxSize().zIndex(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("Fingerprint Check", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Box(modifier = Modifier.size(48.dp))
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ConnectionStatusCard(
                        isConnected = isConnected,
                        isDataLoaded = isDataLoaded,
                        templateCount = serverTemplatesCount,
                        onConnect = onConnect
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    ScannerPreview(
                        capturedBitmap = capturedBitmap,
                        isConnected = isConnected,
                        isDataLoaded = isDataLoaded,
                        isVerifying = false
                    )
                }
            }

            // Layer Penggelap (Dim) agar Card Melayang lebih pop-out
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .zIndex(2f)
            )

            // ===============================================
            // FOREGROUND (Card Hasil Melayang)
            // ===============================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .zIndex(3f)
            ) {
                // Card Hasil di Atas
                AnimatedVisibility(
                    visible = showResult,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 60.dp)
                ) {
                    if (isRecognized) SuccessResultContent(user!!) else ErrorResultContent(errorMessage)
                }

                // Tombol Aksi di Bawah
                AnimatedVisibility(
                    visible = showResult,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                ) {
                    ActionButtons(
                        isRecognized = isRecognized,
                        onRetry = {
                            showResult = false
                            onRetry()
                        },
                        onContinue = onContinue,
                        onBack = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun SuccessResultContent(user: UserData) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(24.dp)
        ) {
            val safeBaseUrl = RetrofitClient.BASE_URL.trimEnd('/')
            val safePath = user.foto_profil?.replace("\\", "/")?.trimStart('/') ?: ""
            val imageUrl = "$safeBaseUrl/$safePath"

            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(4.dp, PrimaryBlue.copy(alpha = 0.3f)),
                shadowElevation = 8.dp
            ) {
                if (!user.foto_profil.isNullOrEmpty()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Foto Profil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.LightGray)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            val namaTampil = user.nama_penduduk ?: user.nama_lengkap ?: "Tanpa Nama"
            Text(namaTampil, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue, textAlign = TextAlign.Center)
            Text("NIK: ${user.nik}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

            Spacer(Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                DetailResultRow("Tempat Lahir", user.tempat_lahir)
                DetailResultRow("Tanggal Lahir", user.tanggal_lahir)
                DetailResultRow("Jenis Kelamin", user.jenis_kelamin)
                DetailResultRow("Alamat", user.alamat, showDivider = false)
            }
        }
    }
}

@Composable
private fun ErrorResultContent(errorMessage: String?) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(32.dp)
        ) {
            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(100.dp), tint = ErrorRed)
            Spacer(Modifier.height(24.dp))
            Text("VERIFIKASI GAGAL", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = ErrorRed, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                text = errorMessage ?: "Pastikan sidik jari Anda bersih, menempel sempurna, atau sudah terdaftar.",
                fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActionButtons(
    isRecognized: Boolean,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f).height(55.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, PrimaryBlue)
        ) {
            Text("KEMBALI", fontWeight = FontWeight.Bold, color = PrimaryBlue)
        }

        if (isRecognized) {
            Button(
                onClick = onContinue,
                modifier = Modifier.weight(1f).height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("LANJUTKAN", fontWeight = FontWeight.Bold)
            }
        } else {
            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f).height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("COBA LAGI", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DetailResultRow(label: String, value: String?, showDivider: Boolean = true) {
    Column(modifier = Modifier.padding(bottom = if (showDivider) 12.dp else 0.dp)) {
        Text(label.uppercase(), fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(2.dp))
        Text(value ?: "-", fontSize = 16.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = BackgroundGray)
        }
    }
}