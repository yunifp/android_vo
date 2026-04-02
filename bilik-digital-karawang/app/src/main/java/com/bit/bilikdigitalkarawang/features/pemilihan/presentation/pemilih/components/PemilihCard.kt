package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.pemilih.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bit.bilikdigitalkarawang.features.pemilihan.domain.model.Pemilih

@Composable
fun PemilihCard(
    pemilih: Pemilih,
    index: Int
) {
    // Ambil tahun lahir dari tanggal lahir
    val tahunLahir = pemilih.tanggalLahir.takeIf { it.length >= 4 }?.substring(0, 4) ?: "-"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.2f),
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${index + 1}.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 16.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "NIK: ${pemilih.nik}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = pemilih.namaPenduduk,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "NKK: ${pemilih.nkk}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Tahun Lahir: $tahunLahir",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
