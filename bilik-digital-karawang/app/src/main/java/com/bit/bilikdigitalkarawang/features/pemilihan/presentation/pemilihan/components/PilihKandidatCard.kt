package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.bit.bilikdigitalkarawang.R
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat
import com.bit.bilikdigitalkarawang.ui.theme.Typography
import java.io.File

@Composable
fun PilihKandidatCard(
    kandidat: Kandidat,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.error
    } else {
        Color.LightGray.copy(alpha = .4F)
    }

    // 🔁 Animasi denyut (aktif hanya saat selected)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Card(
        modifier = modifier
            .border(
                width = if (selected) 6.dp else 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.2f),
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(File(kandidat.localFoto)),
                    contentDescription = "Foto Kandidat",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                )

                // Badge nomor urut
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = kandidat.noUrut,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // 🪛 Paku mencoblos + animasi denyut
                if (selected) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_hole),
                        contentDescription = "Dipilih",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(128.dp)
                            .scale(pulseScale)
                            .zIndex(1f)
                    )
                }
            }
            NamaKandidatText(kandidat.namaCalon)
        }
    }
}

@Composable
fun NamaKandidatText(
    nama: String,
    modifier: Modifier = Modifier
) {
    var displayText by remember { mutableStateOf(nama) }

    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        text = displayText,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        style = Typography.bodyMedium.copy(
            fontSize = 18.sp
        ),
        onTextLayout = { textLayoutResult ->
            val lineCount = textLayoutResult.lineCount

            // 👇 Jika cuma 1 baris, tambahkan baris kosong
            if (lineCount == 1 && !displayText.contains("\n")) {
                displayText = "$nama\n"
            }
        }
    )
}

