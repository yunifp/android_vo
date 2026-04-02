package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.qr_scanner.components

import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bit.bilikdigitalkarawang.common.Constant
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    onQRCodeScanned: (String) -> Unit,
    onCameraReady: () -> Unit,
    isScanning: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .setTargetResolution(android.util.Size(1280, 720))
                    .build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(android.util.Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setImageQueueDepth(1)
                    .build()

                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                val barcodeScanner = BarcodeScanning.getClient(options)

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    if (!isScanning) {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    val mediaImage = imageProxy.image ?: run {
                        imageProxy.close()
                        return@setAnalyzer
                    }

                    val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )

                    barcodeScanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull()?.rawValue?.let { qrContent ->
                                Log.d(Constant.LOG_TAG, "QR detected: $qrContent")
                                onQRCodeScanned(qrContent)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(Constant.LOG_TAG, "Barcode scanning failed", exception)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }

                try {
                    cameraProvider.unbindAll()

                    // 🔍 Pilih kamera yang tersedia (prioritas: belakang -> depan)
                    val cameraSelector = when {
                        cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ->
                            CameraSelector.DEFAULT_BACK_CAMERA
                        cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ->
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        else -> null
                    }

                    if (cameraSelector == null) {
                        Toast.makeText(ctx, "Tidak ada kamera yang tersedia di perangkat ini", Toast.LENGTH_LONG).show()
                        Log.e(Constant.LOG_TAG, "No camera available on this device")
                        return@addListener
                    }

                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )

                    camera.cameraControl.enableTorch(false)
                    onCameraReady()
                    Log.d(Constant.LOG_TAG, "Camera initialized successfully with $cameraSelector")

                } catch (exc: Exception) {
                    Log.e(Constant.LOG_TAG, "Camera binding failed", exc)
                }

            }, executor)

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}
