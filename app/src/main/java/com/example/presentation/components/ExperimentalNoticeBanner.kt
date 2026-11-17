package com.example.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.theme.LocalAppThemeMode
import com.example.ui.theme.isTactical
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/**
 * Badge penanda fitur eksperimen / lab untuk komponen ringkas (TopAppBar, Drawer, Tab, Card).
 */
@Composable
fun ExperimentalBadge(
    modifier: Modifier = Modifier,
    text: String = "🧪 Eksperimen",
    containerColor: Color? = null,
    contentColor: Color? = null,
    testTag: String = "experimental_badge"
) {
    val isTactical = LocalAppThemeMode.current.isTactical
    val bgColor = containerColor ?: MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
    val textColor = contentColor ?: MaterialTheme.colorScheme.onTertiaryContainer

    Surface(
        color = bgColor,
        shape = if (isTactical) CutCornerShape(3.dp) else RoundedCornerShape(6.dp),
        border = BorderStroke(0.8.dp, textColor.copy(alpha = 0.35f)),
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 0.2.sp
            )
        }
    }
}

/**
 * Kartu banner peringatan & edukasi fitur berstatus eksperimen (Lab/Beta)
 * yang memerlukan ketelitian ekstra dari pengguna.
 */
@Composable
fun ExperimentalNoticeCard(
    title: String = "Fitur Eksperimen (Lab)",
    subtitle: String = "Perlu Ketelitian & Verifikasi",
    description: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = true,
    testTag: String = "experimental_notice_card"
) {
    val isTactical = LocalAppThemeMode.current.isTactical
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Card(
        shape = if (isTactical) CutCornerShape(8.dp) else RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(if (isTactical) CutCornerShape(4.dp) else CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Fitur Eksperimen",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = title,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            ExperimentalBadge(text = "🧪 Lab")
                        }
                        Text(
                            text = subtitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Ciutkan Peringatan" else "Buka Peringatan",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f))
                            .padding(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 1.dp)
                            )
                            Text(
                                text = description,
                                fontSize = 11.5.sp,
                                lineHeight = 16.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "💡 Panduan Ihtiyath (Kehati-hatian): Hasil modul ini adalah simulasi awal. Selalu tinjau ulang sebelum menetapkan keputusan hukum atau pembagian aset riil.",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
