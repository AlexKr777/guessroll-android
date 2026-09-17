package com.guessroll.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.guessroll.domain.invite.RoomInviteParser
import com.guessroll.domain.invite.RoomInviteShareBuilder
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun RoomQrInviteDialog(
    roomCode: String,
    onCopyCode: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val payload = remember(roomCode) { RoomInviteParser.payloadForRoomCode(roomCode) }
    val inviteShare = remember(roomCode) { RoomInviteShareBuilder.build(roomCode) }
    val qrBitmap = remember(payload) { QrBitmapFactory.create(payload, 768) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF203050C))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            RevealBox {
                ConsentGlassSheet {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GuessRollLogoMark(size = 40.dp, contentDescription = null)
                        SectionTitle("QR комнаты", "сканируй, чтобы войти", color = Amber)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(248.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFFF4EBD8),
                                        Color(0xFFE8D9B7),
                                    ),
                                ),
                            )
                            .border(BorderStroke(1.dp, Amber.copy(alpha = 0.46f)), RoundedCornerShape(28.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR-код комнаты $roomCode",
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = roomCode,
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Сканируй, чтобы зайти в комнату.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Скопировать код",
                        onClick = onCopyCode,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SecondaryButton(
                        text = "Поделиться ссылкой",
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, inviteShare.text)
                            }
                            context.startActivity(
                                Intent.createChooser(sendIntent, "Поделиться приглашением"),
                            )
                        },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SecondaryButton(
                        text = "Закрыть",
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
}

@Composable
fun QrScannerDialog(
    onScanned: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xF403050C))
                .padding(horizontal = 18.dp, vertical = 30.dp),
            contentAlignment = Alignment.Center,
        ) {
            GlassCard(
                accent = Amber,
                compact = true,
                featured = true,
                dense = true,
            ) {
                SectionTitle("Сканировать QR", "наведи камеру на код комнаты", color = Sky)
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(PanelDeep)
                        .border(1.dp, Amber.copy(alpha = 0.22f), RoundedCornerShape(30.dp)),
                ) {
                    QrCameraPreview(
                        modifier = Modifier.matchParentSize(),
                        onScanned = onScanned,
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clearAndSetSemantics {},
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(236.dp)
                                .border(2.dp, Amber.copy(alpha = 0.58f), RoundedCornerShape(28.dp)),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Код заполнится автоматически после скана.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(modifier = Modifier.height(14.dp))
                SecondaryButton(
                    text = "Отмена",
                    onClick = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun QrCameraPreview(
    modifier: Modifier = Modifier,
    onScanned: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnScanned by rememberUpdatedState(onScanned)
    var previewView: PreviewView? by remember { mutableStateOf(null) }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                previewView = this
            }
        },
    )

    DisposableEffect(context, lifecycleOwner, previewView) {
        val view = previewView ?: return@DisposableEffect onDispose {}
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val analyzerExecutor = Executors.newSingleThreadExecutor()
        val scanner = BarcodeScanning.getClient()
        val consumed = AtomicBoolean(false)

        val listener = Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(view.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analysis.setAnalyzer(analyzerExecutor) { imageProxy ->
                analyzeQrFrame(
                    imageProxy = imageProxy,
                    scanner = scanner,
                    consumed = consumed,
                    onScanned = currentOnScanned,
                )
            }
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis,
            )
        }
        cameraProviderFuture.addListener(listener, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
            scanner.close()
            analyzerExecutor.shutdown()
        }
    }
}

@OptIn(ExperimentalGetImage::class)
private fun analyzeQrFrame(
    imageProxy: androidx.camera.core.ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    consumed: AtomicBoolean,
    onScanned: (String) -> Unit,
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }
    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            if (consumed.get()) return@addOnSuccessListener
            val qrValue = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }?.rawValue
            if (!qrValue.isNullOrBlank() && consumed.compareAndSet(false, true)) {
                onScanned(qrValue)
            }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}

private object QrBitmapFactory {
    fun create(payload: String, size: Int): Bitmap {
        val matrix = QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val dark = AndroidColor.rgb(3, 7, 15)
        val light = AndroidColor.rgb(244, 235, 216)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (matrix[x, y]) dark else light)
            }
        }
        return bitmap
    }
}
