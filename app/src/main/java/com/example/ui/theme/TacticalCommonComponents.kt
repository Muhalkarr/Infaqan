package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 1. HEADER KONSOL SUB-LAYAR TAKTIS (PROTOKOL TAKTIS SUB-ROUTINE TOP BAR)
 * Menyelaraskan seluruh layar sub-fitur dengan gaya tema Protokol Taktis:
 * Menggantikan TopAppBar biasa dengan konsol taktis bersudut potong, kode unit, dan telemetri.
 */
@Composable
fun TacticalSubScreenHeader(
    title: String,
    subCode: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingActions: @Composable RowScope.() -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "header_blink")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "header_alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), Color.Transparent)
                    )
                )
            )
    ) {
        // Baris status telemetri mini paling atas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = alphaAnim), CutCornerShape(1.dp))
                )
                Text(
                    text = "SYS://NODE-04 // $subCode",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    letterSpacing = 0.5.sp
                )
            }
            Text(
                text = "SECURE LINK: ACTIVE [100% OK]",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Baris Konten Header Utama
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Tombol Kembali Taktis [ESC//KEMBALI]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp))
                    .clickable { onBack() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("tactical_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ESC//BACK",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Judul Layar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title.uppercase(),
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "SYSTEM SUB-ROUTINE // SHARIAH PROTOCOL",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Aksi tambahan di sisi kanan (jika ada)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                content = trailingActions
            )
        }

        // Garis batas bawah dengan aksen neon berkedip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.primary.copy(alpha = alphaAnim),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                )
        )
    }
}

/**
 * 2. KARTU TAKTIS (TACTICAL CHAMFERED CARD)
 * Menstandarkan bentuk seluruh kartu di sub-layar dengan sudut potong miring chamfered
 * dan border neon beraksen tech.
 */
@Composable
fun TacticalCard(
    modifier: Modifier = Modifier,
    borderStroke: BorderStroke? = null,
    backgroundColor: Color? = null,
    cutCornerSize: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = CutCornerShape(
        topStart = cutCornerSize,
        topEnd = 0.dp,
        bottomStart = 0.dp,
        bottomEnd = cutCornerSize
    )

    val actualBg = backgroundColor ?: MaterialTheme.colorScheme.surface
    val actualBorder = borderStroke ?: BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = actualBg),
        border = actualBorder
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

/**
 * 3. TOMBOL TAKTIS (TACTICAL ACTION BUTTON)
 */
@Composable
fun TacticalButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    testTag: String = "tactical_button"
) {
    val shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp, topEnd = 2.dp, bottomStart = 2.dp)
    val bgColor = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isPrimary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    val borderColor = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .clip(shape)
            .background(if (enabled) bgColor else bgColor.copy(alpha = 0.4f))
            .border(1.dp, borderColor, shape)
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text.uppercase(),
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = contentColor
            )
        }
    }
}

/**
 * 4. BADGE STATUS TELEMETRI TAKTIS
 */
@Composable
fun TacticalBadge(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .background(accentColor.copy(alpha = 0.15f), CutCornerShape(3.dp))
            .border(0.8.dp, accentColor.copy(alpha = 0.8f), CutCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text.uppercase(),
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor
        )
    }
}

/**
 * 5. BANNER AUDIT SYARIAH TAKTIS (SHARIAH INTEGRITY BANNER)
 */
@Composable
fun TacticalShariahIntegrityBanner(
    modifier: Modifier = Modifier,
    subtitle: String = "100% TRANSAKSI TERVALIDASI KAIDAH FIQIH MUAMALAH"
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CutCornerShape(6.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary, CutCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "PROTOKOL // INTEGRITAS SYARIAH",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TacticalBadge(text = "VERIFIED", accentColor = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 6. PROTOKOL TAKTIS BOTTOM NAVIGATION BAR
 * Menggantikan NavigationBar biasa secara total ketika Protokol Taktis aktif.
 */
@Composable
fun TacticalCommsBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onAddTransaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dock_anim")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), Color.Transparent)
                    )
                )
            )
            .navigationBarsPadding()
    ) {
        // Micro Telemetry Line di atas bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "COMMUNICATIONS DOCK // SECURE CHANNEL",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 7.5.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CutCornerShape(1.dp)))
                Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.secondary, CutCornerShape(1.dp)))
                Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CutCornerShape(1.dp)))
            }
        }

        // 5 Item Bar (Beranda, Buku Kas, [REC], Kantong, Pengaturan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Beranda
            TacticalNavItem(
                label = "BERANDA",
                isSelected = currentRoute == com.example.navigation.AmanahRoutes.DASHBOARD,
                icon = Icons.Default.Home,
                onClick = { onNavigate(com.example.navigation.AmanahRoutes.DASHBOARD) },
                testTag = "nav_tactical_dashboard"
            )

            // 2. Buku Kas
            TacticalNavItem(
                label = "JURNAL",
                isSelected = currentRoute == com.example.navigation.AmanahRoutes.LEDGER,
                icon = Icons.Default.Shield,
                onClick = { onNavigate(com.example.navigation.AmanahRoutes.LEDGER) },
                testTag = "nav_tactical_ledger"
            )

            // 3. Tombol Utama Tengah (REC - Diamond Tactical Center Node)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CutCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = pulseGlow), CutCornerShape(12.dp))
                    .clickable { onAddTransaction() }
                    .testTag("nav_tactical_add_transaction"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Catat Transaksi",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "REC",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // 4. Kantong
            TacticalNavItem(
                label = "DOMPET",
                isSelected = currentRoute == com.example.navigation.AmanahRoutes.MULTI_WALLET,
                icon = Icons.Default.AccountBalanceWallet,
                onClick = { onNavigate(com.example.navigation.AmanahRoutes.MULTI_WALLET) },
                testTag = "nav_tactical_wallets"
            )

            // 5. Pengaturan
            TacticalNavItem(
                label = "SISTEM",
                isSelected = currentRoute == com.example.navigation.AmanahRoutes.CENTRAL_SETTINGS,
                icon = Icons.Default.Settings,
                onClick = { onNavigate(com.example.navigation.AmanahRoutes.CENTRAL_SETTINGS) },
                testTag = "nav_tactical_settings"
            )
        }
    }
}

@Composable
private fun TacticalNavItem(
    label: String,
    isSelected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CutCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Micro LED Indicator di atas item
        Box(
            modifier = Modifier
                .width(16.dp)
                .height(2.5.dp)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    CutCornerShape(1.dp)
                )
        )
        Spacer(modifier = Modifier.height(5.dp))

        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            fontFamily = ShareTechMonoFontFamily,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 7. TELEMETRY DIAGNOSTIC FLOATING ORB (ELEMEN KUSTOM TEMA TAKTIS)
 * Menampilkan Node Mengambang di sisi layar dengan kemampuan membuka status telemetri
 * syariah live dan akses cepat ganti tema dari seluruh layar.
 */
@Composable
fun TacticalDiagnosticOrb(
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_glow"
    )

    Box(
        modifier = modifier
            .clip(CutCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = pulse),
                CutCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 0.dp, bottomEnd = 0.dp)
            )
            .clickable { onOpenDiagnostics() }
            .padding(horizontal = 8.dp, vertical = 7.dp)
            .testTag("system_diagnostic_orb")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(MaterialTheme.colorScheme.primary, CutCornerShape(1.dp))
            )
            Text(
                text = "SYS//STATUS",
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
        }
    }
}

