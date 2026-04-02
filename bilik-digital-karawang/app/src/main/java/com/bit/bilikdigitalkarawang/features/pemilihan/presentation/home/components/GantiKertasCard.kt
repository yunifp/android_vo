package com.bit.bilikdigitalkarawang.features.pemilihan.presentation.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bit.bilikdigitalkarawang.common.Constant
import com.bit.bilikdigitalkarawang.shared.presentation.components.CButton

@Composable
fun GantiKertasCard(
    onGantiKertas: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.2f),
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Perhatian",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onError
            )
            Text(
                text = "Total pemilih mencapai ${Constant.TOTAL_JUMLAH_PEMILIH_PER_SESI_KERTAS}. Harap segera cek kertas printer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f)
            )
            CButton(
                label = "Klik di Sini Setelah Mengganti Kertas",
                onClick = { onGantiKertas() },
                backgroundColor = MaterialTheme.colorScheme.onError,
                textColor = MaterialTheme.colorScheme.error
            )
        }
    }
}
