package com.example.presentation.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.grounding.CustomGroundedRuling
import com.example.core.grounding.CustomRulingRepository
import com.example.core.grounding.FatwaConsultationMessage
import com.example.core.grounding.GeminiFatwaRagService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditCustomRulingDialog(
    initialRuling: CustomGroundedRuling? = null,
    onDismiss: () -> Unit,
    onSave: (CustomGroundedRuling) -> Unit
) {
    var title by remember { mutableStateOf(initialRuling?.title ?: "") }
    var authority by remember { mutableStateOf(initialRuling?.authority ?: "Fatwa DSN-MUI / BAZNAS") }
    var referenceNumber by remember { mutableStateOf(initialRuling?.referenceNumber ?: "") }
    var category by remember { mutableStateOf(initialRuling?.category ?: "ZAKAT") }
    var summary by remember { mutableStateOf(initialRuling?.summary ?: "") }
    var detailedRuling by remember { mutableStateOf(initialRuling?.detailedRuling ?: "") }
    var dalilArabic by remember { mutableStateOf(initialRuling?.dalilArabic ?: "") }
    var dalilTranslation by remember { mutableStateOf(initialRuling?.dalilTranslation ?: "") }
    var dalilSource by remember { mutableStateOf(initialRuling?.dalilSource ?: "") }
    var calculationFormula by remember { mutableStateOf(initialRuling?.calculationFormula ?: "") }
    var tagsText by remember { mutableStateOf(initialRuling?.tags?.joinToString(", ") ?: "") }

    val categories = listOf(
        "ZAKAT" to "Zakat & Rikaz 20%",
        "TATA_KELOLA" to "Tata Kelola & Pembukuan",
        "RIBA_DAN_HUTANG" to "Riba & Qardh",
        "INVESTASI" to "Investasi Syariah",
        "ISRAF_KONSUMSI" to "Israf & Belanja",
        "SYUBHAT" to "Penyucian Syubhat"
    )

    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (initialRuling != null) Icons.Default.Edit else Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (initialRuling != null) "Ubah Ketentuan Fatwa" else "Tambah Sumber Fatwa Baru",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Lengkapi data hukum fiqih muamalah atau fatwa baru lengkap dengan teks dalil Arab dan rujukan kitab.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Judul
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Ketentuan / Fatwa *", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: Fatwa Investasi Kripto / P2P Syariah", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("custom_ruling_title_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Lembaga / Otoritas
                OutlinedTextField(
                    value = authority,
                    onValueChange = { authority = it },
                    label = { Text("Lembaga / Otoritas Fatwa *", fontSize = 12.sp) },
                    placeholder = { Text("DSN-MUI / BAZNAS / Majelis Tarjih", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("custom_ruling_authority_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Nomor Rujukan Resmi
                OutlinedTextField(
                    value = referenceNumber,
                    onValueChange = { referenceNumber = it },
                    label = { Text("Nomor Dokumen / Fatwa Resmi", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: Fatwa DSN No. 140/DSN-MUI/VIII/2021", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Kategori Selector
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.first == category }?.second ?: category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori Fiqih", fontSize = 12.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { (catKey, catLabel) ->
                            DropdownMenuItem(
                                text = { Text(catLabel, fontSize = 12.sp) },
                                onClick = {
                                    category = catKey
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Ringkasan Kaidah
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Ringkasan Kaidah Fiqih *", fontSize = 12.sp) },
                    placeholder = { Text("Intisari hukum singkat (1-2 kalimat)", fontSize = 11.sp) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag("custom_ruling_summary_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Ulasan Rinci
                OutlinedTextField(
                    value = detailedRuling,
                    onValueChange = { detailedRuling = it },
                    label = { Text("Ulasan Hukum & Penjelasan Rinci", fontSize = 12.sp) },
                    placeholder = { Text("Ketentuan syarat, rukun, dan batasan syariat...", fontSize = 11.sp) },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Teks Dalil Arab
                OutlinedTextField(
                    value = dalilArabic,
                    onValueChange = { dalilArabic = it },
                    label = { Text("Teks Dalil Al-Qur'an / Hadits (Arab Berharakat)", fontSize = 12.sp) },
                    placeholder = { Text("«... وَفِي الرِّكَازِ الْخُمُسُ»", fontSize = 11.sp) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Terjemahan Dalil
                OutlinedTextField(
                    value = dalilTranslation,
                    onValueChange = { dalilTranslation = it },
                    label = { Text("Terjemahan Dalil Indonesia", fontSize = 12.sp) },
                    placeholder = { Text("Dan pada rikaz, wajib dikeluarkan seperlima (20%)...", fontSize = 11.sp) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Sumber Kitab / Rujukan
                OutlinedTextField(
                    value = dalilSource,
                    onValueChange = { dalilSource = it },
                    label = { Text("Sumber Kitab / Hadits / PMA", fontSize = 12.sp) },
                    placeholder = { Text("HR. Bukhari No. 1499 / Fathul Bari / PMA 52/2014", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Rumus Hisab (Opsional)
                OutlinedTextField(
                    value = calculationFormula,
                    onValueChange = { calculationFormula = it },
                    label = { Text("Rumus Hisab / Persentase (Opsional)", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: Nilai Aset x 20% (Khumus)", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Tag Pencarian
                OutlinedTextField(
                    value = tagsText,
                    onValueChange = { tagsText = it },
                    label = { Text("Tag Pencarian (Pisahkan dengan koma)", fontSize = 12.sp) },
                    placeholder = { Text("rikaz, 20%, investasi, aset kripto", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || summary.isBlank()) {
                        return@Button
                    }
                    val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val ruling = (initialRuling ?: CustomGroundedRuling(
                        title = title.trim(),
                        category = category,
                        authority = authority.trim(),
                        referenceNumber = referenceNumber.trim().ifBlank { "-" },
                        summary = summary.trim(),
                        detailedRuling = detailedRuling.trim().ifBlank { summary.trim() },
                        calculationFormula = calculationFormula.trim().ifBlank { null },
                        tags = tags,
                        dalilSource = dalilSource.trim().ifBlank { null },
                        dalilArabic = dalilArabic.trim().ifBlank { null },
                        dalilTranslation = dalilTranslation.trim().ifBlank { null }
                    )).copy(
                        title = title.trim(),
                        category = category,
                        authority = authority.trim(),
                        referenceNumber = referenceNumber.trim().ifBlank { "-" },
                        summary = summary.trim(),
                        detailedRuling = detailedRuling.trim().ifBlank { summary.trim() },
                        calculationFormula = calculationFormula.trim().ifBlank { null },
                        tags = tags,
                        dalilSource = dalilSource.trim().ifBlank { null },
                        dalilArabic = dalilArabic.trim().ifBlank { null },
                        dalilTranslation = dalilTranslation.trim().ifBlank { null }
                    )
                    onSave(ruling)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("save_custom_ruling_confirm_button")
            ) {
                Text("Simpan Rujukan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Batal", fontSize = 12.sp)
            }
        }
    )
}

@Composable
fun GeminiApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSaveKey: (String) -> Unit
) {
    var apiKeyText by remember { mutableStateOf(currentKey) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Kunci API Gemini AI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Aplikasi menggunakan model gemini-3.5-flash untuk konsultasi fatwa berdalil. Jika Anda memiliki API Key Google AI Studio sendiri, Anda dapat memasukkannya di bawah ini.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
                OutlinedTextField(
                    value = apiKeyText,
                    onValueChange = { apiKeyText = it },
                    label = { Text("Gemini API Key", fontSize = 12.sp) },
                    placeholder = { Text("AIzaSy...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("gemini_api_key_input"),
                    shape = RoundedCornerShape(10.dp)
                )
                Text(
                    text = "Catatan: Jika dikosongkan, sistem tetap beroperasi menggunakan rujukan grounding lokal cerdas.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveKey(apiKeyText) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Simpan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                Text("Tutup", fontSize = 12.sp)
            }
        }
    )
}

@Composable
fun FatwaAiConsultationTab(
    context: Context,
    messages: List<FatwaConsultationMessage>,
    inputText: String,
    onInputTextChange: (String) -> Unit,
    isConsulting: Boolean,
    onSendMessage: (String) -> Unit,
    onOpenApiKeyDialog: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = listOf(
        "Berapa persen zakat rikaz dan apa dalil haditsnya?",
        "Bagaimana hisab zakat profesi bruto vs netto?",
        "Apakah bunga bank riba dan bagaimana cara menyucikannya?",
        "Kaidah pencatatan hutang piutang QS Al-Baqarah 282"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // AI Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Amanah Syariah Advisor", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                        }
                        Text("Grounding Fatwa DSN-MUI & Fiqih Muamalah", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                IconButton(
                    onClick = onOpenApiKeyDialog,
                    modifier = Modifier.size(32.dp).testTag("open_gemini_key_dialog_button")
                ) {
                    Icon(Icons.Default.Key, contentDescription = "Konfigurasi Kunci API", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(quickQuestions) { q ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { onSendMessage(q) }
                ) {
                    Text(
                        text = q,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chat Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                if (msg.sender == "USER") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Card(
                            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text(
                                text = msg.text,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(12.dp),
                                lineHeight = 17.sp
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth(0.95f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Text("Amanah Syariah Advisor", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                Text(
                                    text = msg.text,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                                if (msg.groundedReferences.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        msg.groundedReferences.forEach { ref ->
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = "✓ $ref",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isConsulting) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                                Text("Menelaah dalil hadits & fatwa DSN-MUI...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input & Send Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                placeholder = { Text("Tanyakan fatwa atau hukum muamalah...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("fatwa_consultation_input_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { onSendMessage(inputText) },
                enabled = inputText.isNotBlank() && !isConsulting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("send_fatwa_query_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Kirim", modifier = Modifier.size(16.dp))
            }
        }
    }
}
