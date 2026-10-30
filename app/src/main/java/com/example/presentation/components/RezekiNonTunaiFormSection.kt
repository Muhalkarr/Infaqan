package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.rezeki.RezekiCategory
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RezekiNonTunaiFormSection(
    title: String,
    onTitleChange: (String) -> Unit,
    giverName: String,
    onGiverNameChange: (String) -> Unit,
    category: RezekiCategory,
    onCategoryChange: (RezekiCategory) -> Unit,
    estimatedValueText: String,
    onEstimatedValueTextChange: (String) -> Unit,
    infaqRate: Double,
    onInfaqRateChange: (Double) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier
) {
    val nf = remember { NumberFormat.getNumberInstance(Locale("id", "ID")) }
    val estimatedValue = estimatedValueText.toDoubleOrNull() ?: 0.0
    val suggestedInfaq = estimatedValue * infaqRate

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rezeki_non_tunai_form_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Kartu Edukasi & Esensi Rezeki Non-Tunai
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Hakikat Rezeki Non-Tunai",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rezeki makanan yang ditraktir, barang hadiah, atau fasilitas tumpangan pada hakikatnya berasal dari Allah S.W.T melalui perantara sesama. Pencatatan ini tidak memotong atau menambah saldo kas riil Anda.",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. Nama Pemberian / Manfaat
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Nama Rezeki / Pemberian *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rezeki_title_input"),
                placeholder = { Text("Contoh: Ditraktir Makan Siang, Parsel Buah, Hadiah Buku") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Preset saran cepat
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val presets = listOf("Ditraktir Makan Siang", "Hadiah / Kado", "Parsel & Bingkisan", "Tumpangan Mobil", "Bantuan Tenaga")
                presets.forEach { preset ->
                    SuggestionChip(
                        onClick = { onTitleChange(preset) },
                        label = { Text(preset, fontSize = 11.sp) }
                    )
                }
            }
        }

        // 3. Nama Pemberi / Sahabat
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Nama Pemberi / Sumber (Opsional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = giverName,
                onValueChange = onGiverNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rezeki_giver_input"),
                placeholder = { Text("Nama sahabat, kerabat, atau kosongkan (Hamba Allah)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
        }

        // 4. Kategori Rezeki
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Kategori Rezeki Non-Tunai",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RezekiCategory.values().forEach { cat ->
                    val isSelected = cat == category
                    val icon = when (cat) {
                        RezekiCategory.FOOD_BEVERAGE -> Icons.Default.Restaurant
                        RezekiCategory.GIFT_GOODS -> Icons.Default.CardGiftcard
                        RezekiCategory.SERVICE_RIDE -> Icons.Default.DirectionsCar
                        RezekiCategory.VOUCHER_PERK -> Icons.Default.ConfirmationNumber
                        RezekiCategory.OTHER -> Icons.Default.VolunteerActivism
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(cat) },
                        label = { Text(cat.displayName, fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(),
                        modifier = Modifier.testTag("rezeki_cat_${cat.name}")
                    )
                }
            }
        }

        // 5. Estimasi Nilai Manfaat
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Estimasi Nilai Manfaat (Rp) *",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = estimatedValueText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        onEstimatedValueTextChange(input)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rezeki_estimated_value_input"),
                leadingIcon = {
                    Text(
                        text = "Rp",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                placeholder = { Text("Berapa kira-kira harga manfaat pemberian ini?") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Preset nominal cepat
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(25000L, 50000L, 100000L, 200000L, 500000L).forEach { amount ->
                    SuggestionChip(
                        onClick = { onEstimatedValueTextChange(amount.toString()) },
                        label = { Text("Rp ${nf.format(amount)}", fontSize = 11.sp) }
                    )
                }
            }
        }

        // 6. Saran Infaq Syukur
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Saran Infaq Syukur",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Saran: Rp ${nf.format(suggestedInfaq.toLong())}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Text(
                    text = "Pilih rasio persentase infaq syukur yang direkomendasikan atas rezeki ini:",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val rates = listOf(0.025 to "2.5% (Zakat)", 0.05 to "5% (Standar)", 0.10 to "10% (Sedekah)")
                    rates.forEach { (rate, label) ->
                        val isSelected = kotlin.math.abs(infaqRate - rate) < 0.001
                        FilterChip(
                            selected = isSelected,
                            onClick = { onInfaqRateChange(rate) },
                            label = { Text(label, fontSize = 11.5.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors()
                        )
                    }
                }
            }
        }

        // 7. Catatan / Doa Kebaikan
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Catatan & Doa untuk Pemberi (Opsional)",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rezeki_notes_input"),
                placeholder = { Text("Contoh: Jazakallahu khairan, semoga Allah melipatgandakan rezeki beliau...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(10.dp)
            )

            SuggestionChip(
                onClick = {
                    val prayer = "Jazakallahu khairan katsiran atas pemberiannya. Semoga Allah membalas dengan keberkahan yang berlipat ganda."
                    onNotesChange(if (notes.isBlank()) prayer else "$notes\n$prayer")
                },
                label = { Text("✨ Sisipkan Doa Jazakallahu Khairan", fontSize = 11.sp) }
            )
        }

        // 8. Tombol Simpan
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("submit_rezeki_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSaving && title.isNotBlank() && estimatedValue > 0
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Catat Rezeki & Tawarkan Infaq",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
