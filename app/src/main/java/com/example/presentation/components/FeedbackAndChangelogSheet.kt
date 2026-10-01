package com.example.presentation.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.changelog.ChangelogRegistry
import com.example.core.feedback.FeedbackRepository
import com.example.core.feedback.FeedbackType
import com.example.core.feedback.UserFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackAndChangelogSheet(
    onDismissRequest: () -> Unit,
    initialTab: Int = 0
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        FeedbackRepository.loadAll(context)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("feedback_and_changelog_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pusat Masukan & Catatan Rilis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Pengembang: Muhalkarr • Amanah Ledger v2.5",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.testTag("close_feedback_sheet_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Tabs
            val tabs = listOf(
                "Kirim Masukan",
                "Riwayat Anda",
                "Changelog (v0-v2.5)"
            )

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            var editingFeedback by remember { mutableStateOf<UserFeedback?>(null) }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            when (selectedTab) {
                0 -> SendFeedbackTabContent(
                    editingFeedback = editingFeedback,
                    onClearEditing = { editingFeedback = null },
                    onFeedbackSubmitted = {
                        editingFeedback = null
                        selectedTab = 1 // Switch to history tab
                    }
                )
                1 -> FeedbackHistoryTabContent(
                    onEditFeedback = { feedback ->
                        editingFeedback = feedback
                        selectedTab = 0 // Switch to Send / Edit tab
                    }
                )
                2 -> ChangelogTabContent()
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SendFeedbackTabContent(
    editingFeedback: UserFeedback? = null,
    onClearEditing: () -> Unit = {},
    onFeedbackSubmitted: () -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(editingFeedback?.type ?: FeedbackType.FEATURE_SUGGESTION) }
    var rating by remember { mutableIntStateOf(editingFeedback?.rating ?: 5) }
    var messageText by remember { mutableStateOf(editingFeedback?.message ?: "") }
    var contactText by remember { mutableStateOf(editingFeedback?.contact ?: "") }

    LaunchedEffect(editingFeedback) {
        if (editingFeedback != null) {
            selectedType = editingFeedback.type
            rating = editingFeedback.rating
            messageText = editingFeedback.message
            contactText = editingFeedback.contact ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Banner if in editing / revising mode
        AnimatedVisibility(visible = editingFeedback != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (editingFeedback?.status == com.example.core.feedback.FeedbackStatus.DRAFT)
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    else
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (editingFeedback?.status == com.example.core.feedback.FeedbackStatus.DRAFT)
                                "📝 Mode Edit Draf Masukan"
                            else
                                "🔄 Mode Ubah & Kirim Ulang Masukan",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (editingFeedback?.status == com.example.core.feedback.FeedbackStatus.DRAFT)
                                "Draf disimpan: ${editingFeedback?.formattedUpdatedDate()}"
                            else
                                "Terkirim sebelumnya via ${editingFeedback?.channelSent} (Revisi #${(editingFeedback?.revisionCount ?: 0) + 1})",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            onClearEditing()
                            messageText = ""
                            contactText = ""
                            rating = 5
                            selectedType = FeedbackType.FEATURE_SUGGESTION
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Batal", fontSize = 11.sp)
                    }
                }
            }
        }

        Text(
            text = "Kategori Masukan:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeedbackType.values().forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type.label, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Rating Stars
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tingkat Kepuasan:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row {
                for (star in 1..5) {
                    IconButton(
                        onClick = { rating = star },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Bintang $star",
                            tint = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message Input
        OutlinedTextField(
            value = messageText,
            onValueChange = { messageText = it },
            label = { Text(if (editingFeedback != null) "Perbaiki isi masukan Anda..." else "Tuliskan masukan, ide, atau saran Anda...") },
            placeholder = { Text("Contoh: Mohon tambahkan ekspor Excel atau fitur pengingat...") },
            minLines = 4,
            maxLines = 6,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("feedback_message_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Optional Contact
        OutlinedTextField(
            value = contactText,
            onValueChange = { contactText = it },
            label = { Text("Nama atau Nomor Kontak (Opsional)") },
            placeholder = { Text("Nama Anda atau email/nomor WA") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("feedback_contact_input"),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons: Send via WhatsApp & Email
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (messageText.isBlank()) {
                        Toast.makeText(context, "Mohon tuliskan isi masukan Anda", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val feedback = if (editingFeedback != null) {
                        editingFeedback.copy(
                            type = selectedType,
                            rating = rating,
                            message = messageText.trim(),
                            contact = contactText.trim().ifBlank { null },
                            channelSent = "WhatsApp",
                            status = com.example.core.feedback.FeedbackStatus.SENT,
                            updatedAt = System.currentTimeMillis(),
                            revisionCount = if (editingFeedback.status == com.example.core.feedback.FeedbackStatus.SENT) editingFeedback.revisionCount + 1 else editingFeedback.revisionCount
                        )
                    } else {
                        UserFeedback(
                            type = selectedType,
                            rating = rating,
                            message = messageText.trim(),
                            contact = contactText.trim().ifBlank { null }
                        )
                    }
                    val ok = FeedbackRepository.sendViaWhatsApp(context, feedback)
                    if (ok) {
                        Toast.makeText(context, if (editingFeedback != null && editingFeedback.status == com.example.core.feedback.FeedbackStatus.SENT) "Perubahan masukan berhasil dikirim ulang via WhatsApp" else "Masukan disimpan dan diteruskan ke WhatsApp", Toast.LENGTH_SHORT).show()
                        messageText = ""
                        contactText = ""
                        onFeedbackSubmitted()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("send_feedback_whatsapp_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (editingFeedback != null && editingFeedback.status == com.example.core.feedback.FeedbackStatus.SENT) "Kirim Ulang WA" else "Kirim WA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = {
                    if (messageText.isBlank()) {
                        Toast.makeText(context, "Mohon tuliskan isi masukan Anda", Toast.LENGTH_SHORT).show()
                        return@OutlinedButton
                    }
                    val feedback = if (editingFeedback != null) {
                        editingFeedback.copy(
                            type = selectedType,
                            rating = rating,
                            message = messageText.trim(),
                            contact = contactText.trim().ifBlank { null },
                            channelSent = "Email",
                            status = com.example.core.feedback.FeedbackStatus.SENT,
                            updatedAt = System.currentTimeMillis(),
                            revisionCount = if (editingFeedback.status == com.example.core.feedback.FeedbackStatus.SENT) editingFeedback.revisionCount + 1 else editingFeedback.revisionCount
                        )
                    } else {
                        UserFeedback(
                            type = selectedType,
                            rating = rating,
                            message = messageText.trim(),
                            contact = contactText.trim().ifBlank { null }
                        )
                    }
                    val ok = FeedbackRepository.sendViaEmail(context, feedback)
                    if (ok) {
                        Toast.makeText(context, if (editingFeedback != null && editingFeedback.status == com.example.core.feedback.FeedbackStatus.SENT) "Perubahan masukan berhasil dikirim ulang via Email" else "Masukan disimpan dan diteruskan ke Email", Toast.LENGTH_SHORT).show()
                        messageText = ""
                        contactText = ""
                        onFeedbackSubmitted()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("send_feedback_email_button"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (editingFeedback != null && editingFeedback.status == com.example.core.feedback.FeedbackStatus.SENT) "Kirim Ulang Email" else "Kirim Email",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save Draft Button
        OutlinedButton(
            onClick = {
                if (messageText.isBlank()) {
                    Toast.makeText(context, "Mohon tuliskan isi masukan terlebih dahulu", Toast.LENGTH_SHORT).show()
                    return@OutlinedButton
                }
                val feedback = if (editingFeedback != null && editingFeedback.status == com.example.core.feedback.FeedbackStatus.DRAFT) {
                    editingFeedback.copy(
                        type = selectedType,
                        rating = rating,
                        message = messageText.trim(),
                        contact = contactText.trim().ifBlank { null },
                        status = com.example.core.feedback.FeedbackStatus.DRAFT,
                        channelSent = "Draf",
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    UserFeedback(
                        type = selectedType,
                        rating = rating,
                        message = messageText.trim(),
                        contact = contactText.trim().ifBlank { null },
                        channelSent = "Draf",
                        status = com.example.core.feedback.FeedbackStatus.DRAFT,
                        updatedAt = System.currentTimeMillis()
                    )
                }
                FeedbackRepository.save(context, feedback)
                Toast.makeText(context, "Draf masukan berhasil disimpan di riwayat! Anda dapat merevisinya kapan saja.", Toast.LENGTH_SHORT).show()
                messageText = ""
                contactText = ""
                onFeedbackSubmitted()
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("save_feedback_draft_button"),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (editingFeedback != null && editingFeedback.status == com.example.core.feedback.FeedbackStatus.DRAFT)
                    "Simpan Perubahan Draf"
                else
                    "Simpan sebagai Draf (Revisi Nanti)",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun FeedbackHistoryTabContent(
    onEditFeedback: (UserFeedback) -> Unit = {}
) {
    val context = LocalContext.current
    val feedbacks by FeedbackRepository.feedbacksState.collectAsState()
    var selectedFilter by remember { mutableStateOf("SEMUA") } // "SEMUA", "DRAFT", "SENT"

    val draftCount = remember(feedbacks) { feedbacks.count { it.status == com.example.core.feedback.FeedbackStatus.DRAFT } }
    val sentCount = remember(feedbacks) { feedbacks.count { it.status == com.example.core.feedback.FeedbackStatus.SENT } }

    val filteredFeedbacks = remember(feedbacks, selectedFilter) {
        when (selectedFilter) {
            "DRAFT" -> feedbacks.filter { it.status == com.example.core.feedback.FeedbackStatus.DRAFT }
            "SENT" -> feedbacks.filter { it.status == com.example.core.feedback.FeedbackStatus.SENT }
            else -> feedbacks
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "SEMUA",
                onClick = { selectedFilter = "SEMUA" },
                label = { Text("Semua (${feedbacks.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            )
            FilterChip(
                selected = selectedFilter == "DRAFT",
                onClick = { selectedFilter = "DRAFT" },
                label = { Text("Draf (${draftCount})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            )
            FilterChip(
                selected = selectedFilter == "SENT",
                onClick = { selectedFilter = "SENT" },
                label = { Text("Terkirim (${sentCount})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }

        if (filteredFeedbacks.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedFilter == "DRAFT") "Belum Ada Draf Masukan"
                        else if (selectedFilter == "SENT") "Belum Ada Masukan Terkirim"
                        else "Belum Ada Masukan Tersimpan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (selectedFilter == "DRAFT")
                            "Gunakan tombol 'Simpan sebagai Draf' pada form untuk menyimpan ide Anda."
                        else
                            "Masukan yang Anda kirimkan atau simpan akan tercatat rapi di sini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFeedbacks, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (item.status == com.example.core.feedback.FeedbackStatus.DRAFT)
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ),
                        border = if (item.status == com.example.core.feedback.FeedbackStatus.DRAFT)
                            BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                        else null
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Top Row: Type, Status Badge, Delete Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = item.type.label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (item.status == com.example.core.feedback.FeedbackStatus.DRAFT)
                                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                                else
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (item.status == com.example.core.feedback.FeedbackStatus.DRAFT) "DRAF" else "TERKIRIM",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.status == com.example.core.feedback.FeedbackStatus.DRAFT)
                                                MaterialTheme.colorScheme.tertiary
                                            else
                                                MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (item.revisionCount > 0) {
                                        Text(
                                            text = "Revisi #${item.revisionCount}",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { FeedbackRepository.delete(context, item.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "★".repeat(item.rating) + "☆".repeat(5 - item.rating),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${item.message}\"",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (item.status == com.example.core.feedback.FeedbackStatus.DRAFT)
                                        "Tersimpan: ${item.formattedUpdatedDate()}"
                                    else
                                        "Kanal: ${item.channelSent} • ${item.formattedDate()}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Action Button: Edit or Resend
                                OutlinedButton(
                                    onClick = { onEditFeedback(item) },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text(
                                        text = if (item.status == com.example.core.feedback.FeedbackStatus.DRAFT) "Edit Draf ➔" else "Kirim Ulang / Ubah ➔",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
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

@Composable
fun ChangelogTabContent() {
    val versions = ChangelogRegistry.versions

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(versions, key = { it.version }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = if (item.isLatest) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (item.isLatest)
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (item.isLatest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.version,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.tag,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.isLatest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = item.releaseDate,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    item.highlights.forEach { hl ->
                        Text(
                            text = hl,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
