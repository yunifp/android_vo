package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.face_recognition

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner

import com.bit.bilikdigitalkarawang.helpers.FaceAnalyzer
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaceDetectorScreen(
    viewModel: BiometricViewModel = hiltViewModel(), // Diubah menggunakan hiltViewModel
    onBack: () -> Unit,
    onSuccessLiveness: (String) -> Unit // Menerima NIK (String)
) {
    BackHandler {
        onBack()
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scanState = viewModel.scanState

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_FRONT) }
    var faceStatusMessage by remember { mutableStateOf("Posisikan wajah Anda\ndi dalam area") }
    var isFaceInsideFrame by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // --- LOGIKA ANALYZER MENDUKUNG ROTASI DINAMIS ---
    val analyzer = remember {
        FaceAnalyzer { bitmap, rotation, isValid, message ->
            isFaceInsideFrame = isValid
            faceStatusMessage = message

            // Hanya proses gambar jika dalam state Scanning
            if (isValid && scanState is ScanState.Scanning) {
                viewModel.processFaceImage(context, bitmap, rotation)
            }
        }
    }

    LaunchedEffect(lensFacing, scanState) {
        if (scanState is ScanState.Scanning) {
            analyzer.resetCooldown()
            faceStatusMessage = "Posisikan wajah Anda\ndi dalam area"
            isFaceInsideFrame = false
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(lensFacing, hasCameraPermission) {
        if (hasCameraPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also {
                        it.setAnalyzer(Executors.newSingleThreadExecutor(), analyzer)
                    }

                val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            when {
                !hasCameraPermission -> {
                    // Permission request UI
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(96.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Permission Kamera Diperlukan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 24.dp)
                        )

                        Text(
                            text = "Aplikasi memerlukan akses kamera untuk melakukan verifikasi wajah",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )

                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Berikan Permission Kamera")
                        }
                    }
                }

                hasCameraPermission -> {
                    // 1. Layer Preview Kamera
                    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

                    // 2. Layer Overlay KYC Oval Area
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val ovalWidth = canvasWidth * 0.70f
                        val ovalHeight = ovalWidth * 1.35f
                        val topLeft = Offset((canvasWidth - ovalWidth) / 2, (canvasHeight - ovalHeight) / 2.2f)

                        drawRect(color = Color.Black.copy(alpha = 0.65f))

                        drawRoundRect(
                            color = Color.Transparent,
                            topLeft = topLeft,
                            size = Size(ovalWidth, ovalHeight),
                            cornerRadius = CornerRadius(ovalWidth / 2, ovalHeight / 2),
                            blendMode = BlendMode.Clear
                        )

                        val strokeColor = when {
                            scanState is ScanState.Loading -> primaryColor
                            scanState is ScanState.Success -> Color(0xFF4CAF50)
                            scanState is ScanState.Error -> errorColor
                            isFaceInsideFrame -> Color(0xFF4CAF50)
                            else -> Color.White.copy(alpha = 0.5f)
                        }

                        drawRoundRect(
                            color = strokeColor,
                            topLeft = topLeft,
                            size = Size(ovalWidth, ovalHeight),
                            cornerRadius = CornerRadius(ovalWidth / 2, ovalHeight / 2),
                            style = Stroke(width = if (scanState is ScanState.Loading || scanState is ScanState.Success) 10f else 4f)
                        )
                    }

                    // 3. UI Layer Atas & Interaksi
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f)
                    ) {
                        // Header Transparan
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }

                            Text(
                                text = "Liveness Check",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )

                            IconButton(
                                onClick = {
                                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                                        CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                                },
                                modifier = Modifier.background(Color.Black.copy(0.4f), CircleShape)
                            ) {
                                Icon(Icons.Default.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White)
                            }
                        }

                        // Pesan Status Panduan (Saat sedang scanning)
                        if (scanState is ScanState.Scanning) {
                            Text(
                                text = faceStatusMessage,
                                color = if (isFaceInsideFrame) Color(0xFF4CAF50) else Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 90.dp)
                            )
                        }

                        // Indikator Loading
                        if (scanState is ScanState.Loading) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Memproses wajah...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        // 4. BANNER HASIL SUKSES (TAMPIL DI ATAS)
                        AnimatedVisibility(
                            visible = scanState is ScanState.Success,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 80.dp, start = 24.dp, end = 24.dp)
                        ) {
                            if (scanState is ScanState.Success) {
                                val user = scanState.user
                                val namaTampil = user.nama_penduduk ?: user.nama_lengkap ?: "Tanpa Nama"

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Ikon Success Menarik
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .background(Color(0xFFE8F5E9), CircleShape), // Warna hijau soft
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Success",
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // Informasi Data Diri Atas
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Verifikasi Berhasil!",
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = namaTampil,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "NIK: ${user.nik}",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // TOMBOL LANJUTKAN (TAMPIL DI BAWAH SAAT SUKSES)
                        AnimatedVisibility(
                            visible = scanState is ScanState.Success,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(24.dp)
                        ) {
                            if (scanState is ScanState.Success) {
                                val user = scanState.user
                                Button(
                                    onClick = { onSuccessLiveness(user.nik) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(100)
                                ) {
                                    Text("LANJUTKAN KE PEMILIHAN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }

                        // 5. BANNER HASIL GAGAL (TAMPIL DI ATAS)
                        AnimatedVisibility(
                            visible = scanState is ScanState.Error,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 80.dp, start = 24.dp, end = 24.dp)
                        ) {
                            if (scanState is ScanState.Error) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                    elevation = CardDefaults.cardElevation(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Ikon Error
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = "Error",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // Informasi Error
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Verifikasi Gagal",
                                                color = MaterialTheme.colorScheme.error,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = scanState.message,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                fontSize = 13.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // TOMBOL COBA LAGI (TAMPIL DI BAWAH SAAT GAGAL)
                        AnimatedVisibility(
                            visible = scanState is ScanState.Error,
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(24.dp)
                        ) {
                            Button(
                                onClick = { viewModel.resetScan() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(100),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text("COBA LAGI", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                    }
                }
            }
        }
    }
}