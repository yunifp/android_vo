package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilihan.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Kandidat
import com.bit.bilikdigitalkarawang.shared.presentation.components.CButton

@Composable
fun KandidatConfirmationDialog(
    selectedCandidates: List<Kandidat>, // Candidate: data class dengan nama & foto
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    // Full screen overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 1f))
            .clickable{

            },
        contentAlignment = Alignment.Center,
    )
    {
        // Card sebagai dialog
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { /* DO NOTHING */ }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Konfirmasi Pemilihan",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Peringatan di atas kandidat
                val warningMessage = when {
                    selectedCandidates.isEmpty() || selectedCandidates.size > 1 -> {
                        buildAnnotatedString {
                            append("Apakah Anda yakin? Anda memilih lebih dari satu kandidat, jika ya suara Anda akan dianggap ")

                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("TIDAK SAH")
                            }
                        }
                    }

                    else -> {
                        buildAnnotatedString {
                            append("Apa Anda yakin akan memilih kandidat ini?")
                        }
                    }
                }

                Text(
                    text = warningMessage,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Normal
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedCandidates.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        selectedCandidates.forEach { candidate ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(320.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(candidate.localFoto),
                                    contentDescription = candidate.namaCalon,
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            width = 1.dp,
                                            color = Color.LightGray,
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = candidate.namaCalon,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Tombol besar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CButton(
                        label = "ULANGI",
                        backgroundColor = MaterialTheme.colorScheme.error,
                        textColor = Color.White,
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp) // tinggi tombol lebih besar
                    )
                    CButton(
                        label = "PILIH",
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                    )
                }
            }
        }
    }
}
