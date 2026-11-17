package com.example.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.core.state.AmanahLedgerViewModel
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.LocalAppThemeMode
import com.example.ui.theme.ShareTechMonoFontFamily
import com.example.ui.theme.isTactical

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val badges: List<String>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: AmanahLedgerViewModel,
    onComplete: () -> Unit
) {
    val themeMode = LocalAppThemeMode.current
    val isTactical = themeMode.isTactical

    // Gunakan warna kontras yang selaras dengan themeMode aktif (WCAG AA & AAA compliant)
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val onSecondaryColor = MaterialTheme.colorScheme.onSecondary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

    val coralRed = Color(0xFFD32F2F)

    val pages = remember(primaryColor, secondaryColor, isTactical) {
        listOf(
            OnboardingPage(
                title = "Ahlan wa Sahlan",
                subtitle = "Panduan Keuangan Syariah Amanah",
                description = "Selamat datang di Infaqan Syariah (Amanah Ledger).\nAsisten cerdas untuk memandu pencatatan keuangan pribadi & keluarga yang patuh syariah, terhindar dari riba, disiplin zakat, serta menumbuhkan keberkahan harta.",
                icon = Icons.Default.MenuBook,
                iconColor = primaryColor,
                badges = listOf("✨ Fiqh Muamalah Teruji", "🕊️ Bebas Riba", "🕌 Berkah Finansial")
            ),
            OnboardingPage(
                title = "100% Privat & Aman",
                subtitle = "Keamanan Tanpa Kompromi",
                description = "Aplikasi ini beroperasi 100% offline (lokal) pada perangkat Anda.\nKami tidak pernah terhubung ke bank atau e-wallet, dan tidak pernah mengunggah data keuangan Anda ke server manapun.",
                icon = Icons.Default.Security,
                iconColor = if (isTactical) secondaryColor else coralRed,
                badges = listOf("🔒 100% Offline Lokal", "🛡️ Tanpa Akses Cloud Luar", "📱 Data Milik Anda")
            ),
            OnboardingPage(
                title = "Hisab Maliyah Berpasangan",
                subtitle = "Kaidah Double-Entry & Evaluasi Diri",
                description = "Sesuai adab Islam, Anda mencatat setiap pemasukan dan pengeluaran secara mandiri.\nHal ini melatih kesadaran (mindfulness) terhadap dari mana harta didapat dan ke mana harta dibelanjakan.",
                icon = Icons.Default.SelfImprovement,
                iconColor = primaryColor,
                badges = listOf("🧮 Double-Entry Terpadu", "⚖️ Debit-Kredit Seimbang", "🌱 Sadar Finansial")
            ),
            OnboardingPage(
                title = "Data Simulasi (Sandbox)",
                subtitle = "Eksplorasi Fitur Tanpa Ragu",
                description = "Tersedia beberapa transaksi dan kantong rekening contoh (dummy) agar Anda leluasa menguji fitur.\nAnda bebas mengubah, menghapus, atau me-reset seluruh data ini di menu 'Pengaturan' saat siap mencatat data riil.",
                icon = Icons.Default.Science,
                iconColor = if (isTactical) secondaryColor else coralRed,
                badges = listOf("🧪 Data Contoh Siap Pakai", "🔄 Reset Mudah di Pengaturan", "🎯 Bebas Uji Coba")
            ),
            OnboardingPage(
                title = "Siap Mengelola Amanah?",
                subtitle = "Langkah Awal Ketenangan Harta",
                description = "Langkah pertama: Buka menu 'Multi-Kantong Rekening' untuk mendaftarkan dompet dan saldo awal Anda, lalu pantau haul & nisab zakat dengan tenang.",
                icon = Icons.Default.CheckCircle,
                iconColor = secondaryColor,
                badges = listOf("💼 Multi-Kantong & Dompet", "💰 Pelacak Nisab Zakat", "🚀 Siap Beraksi")
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    // Animasi Pulse & Floating Glow pada Hero Icon
    val infiniteTransition = rememberInfiniteTransition(label = "onboarding_pulse")
    val heroPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_pulse"
    )
    val haloPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_alpha"
    )
    val floatYOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )

    // Animated Progress Bar di bagian atas
    val targetProgress = (pagerState.currentPage + 1).toFloat() / pages.size
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "onboarding_progress"
    )

    fun handleCloseOrSkip() {
        viewModel.completeOnboarding()
        onComplete()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("onboarding_screen_root"),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ====================================================================
            // HEADER BAR: BRANDING + PROGRESS + OPSI TUTUP / LEWATI LANGSUNG
            // ====================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sisi Kiri: Branding App
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp))
                            .background(primaryColor.copy(alpha = 0.15f))
                            .border(
                                1.dp,
                                primaryColor.copy(alpha = 0.5f),
                                if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isTactical) Icons.Default.Bolt else Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isTactical) "PROTOKOL // ONBOARDING" else "INFAQAN SYARIAH",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            color = primaryColor,
                            letterSpacing = if (isTactical) 1.sp else 1.2.sp
                        )
                        Text(
                            text = if (isTactical) "PROTOKOL KEUANGAN AMANAH" else "Amanah Ledger • Fiqh Muamalah",
                            fontSize = 10.sp,
                            fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Sisi Kanan: TOMBOL TUTUP / LEWATI (Direct Enter Option) dengan kontras tinggi
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .clickable { handleCloseOrSkip() }
                        .testTag("onboarding_skip_button")
                        .testTag("onboarding_close_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Lewati",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup & Langsung Masuk ke Aplikasi",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Linear Progress Bar dengan Animasi Halus
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(if (isTactical) CutCornerShape(1.dp) else RoundedCornerShape(2.dp)),
                color = primaryColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )

            // Step Indicator Badge ("Langkah 1 dari 5") dengan animasi pergantian teks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = primaryColor.copy(alpha = 0.15f),
                    shape = if (isTactical) CutCornerShape(2.dp) else RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, primaryColor.copy(alpha = 0.5f))
                ) {
                    AnimatedContent(
                        targetState = pagerState.currentPage + 1,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn()) togetherWith
                                    (slideOutVertically { height -> -height } + fadeOut())
                        },
                        label = "step_badge_anim"
                    ) { step ->
                        Text(
                            text = if (isTactical) "NODE 0$step // 0${pages.size}" else "Langkah $step dari ${pages.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "${(animatedProgress * 100).toInt()}% Selesai",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ====================================================================
            // SLIDER UTAMA DENGAN KARTU DAN MIKRO-ANIMASI INTUITIF
            // ====================================================================
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("onboarding_pager")
            ) { position ->
                val page = pages[position]
                val cardShape = if (isTactical) CutCornerShape(10.dp) else RoundedCornerShape(20.dp)

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isTactical) primaryColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Hero Icon Box dengan Efek Pulsing Glow & Floating Offset
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .offset(y = floatYOffset.dp)
                                .size(130.dp)
                        ) {
                            // Cincin Efek Halo Luar (Breathing Ambient Ring)
                            Box(
                                modifier = Modifier
                                    .size(126.dp)
                                    .scale(heroPulseScale)
                                    .clip(if (isTactical) CutCornerShape(12.dp) else CircleShape)
                                    .background(page.iconColor.copy(alpha = haloPulseAlpha))
                            )

                            // Cincin Dalam Berisi Ikon
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(if (isTactical) CutCornerShape(8.dp) else CircleShape)
                                    .background(page.iconColor.copy(alpha = 0.16f))
                                    .border(
                                        1.5.dp,
                                        page.iconColor.copy(alpha = 0.6f),
                                        if (isTactical) CutCornerShape(8.dp) else CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = page.icon,
                                    contentDescription = page.title,
                                    modifier = Modifier.size(50.dp),
                                    tint = page.iconColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Subtitle Kecil
                        Text(
                            text = page.subtitle.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                            color = secondaryColor,
                            letterSpacing = 0.8.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Judul Utama Halaman
                        Text(
                            text = page.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Deskripsi Narasi
                        Text(
                            text = page.description,
                            fontSize = 13.5.sp,
                            fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 21.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Chip Penanda Nilai Tambah (Badges Row)
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                page.badges.forEach { badge ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                        shape = if (isTactical) CutCornerShape(3.dp) else RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            0.5.dp,
                                            page.iconColor.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Text(
                                            text = badge,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = if (isTactical) ShareTechMonoFontFamily else null,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Khusus Halaman 0 ("Ahlan wa Sahlan"): Tombol Cepat Langsung Masuk (Tonal Filled Button dengan Kontras Tinggi)
                        if (position == 0) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Button(
                                onClick = { handleCloseOrSkip() },
                                shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryContainer,
                                    contentColor = onPrimaryContainer
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 44.dp)
                                    .testTag("onboarding_direct_enter_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Langsung Masuk ke Aplikasi",
                                    fontSize = 12.5.sp,
                                    fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                                    fontWeight = FontWeight.Bold,
                                    color = onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ====================================================================
            // ANIMATED PAGE INDICATOR (INTERACTIVE & EXPANDING BARS)
            // ====================================================================
            Row(
                modifier = Modifier
                    .height(36.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration

                    val indicatorWidth by animateDpAsState(
                        targetValue = if (isSelected) 26.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "indicator_width"
                    )

                    val indicatorColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        animationSpec = tween(durationMillis = 250),
                        label = "indicator_color"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(7.dp)
                            .width(indicatorWidth)
                            .clip(if (isTactical) CutCornerShape(1.dp) else RoundedCornerShape(4.dp))
                            .background(indicatorColor)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(iteration)
                                }
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ====================================================================
            // BOTTOM ACTION NAVIGATION BUTTONS DENGAN KONTRAST TINGGI (WCAG COMPLIANT)
            // ====================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Kembali (atau Tutup/Lewati pada Halaman Awal)
                if (pagerState.currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("onboarding_prev_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Sebelumnya",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kembali",
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    TextButton(
                        onClick = { handleCloseOrSkip() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = primaryColor
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("onboarding_skip_bottom_button")
                    ) {
                        Text(
                            text = "Lewati Semua",
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }

                // Tombol Selanjutnya / Mulai Sekarang dengan kontras tinggi (onPrimary pada primary)
                if (pagerState.currentPage < pages.size - 1) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimaryColor
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("onboarding_next_button")
                    ) {
                        Text(
                            text = "Selanjutnya",
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = onPrimaryColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Berikutnya",
                            tint = onPrimaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Button(
                        onClick = { handleCloseOrSkip() },
                        shape = if (isTactical) CutCornerShape(4.dp) else RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = onPrimaryColor
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("onboarding_finish_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = onPrimaryColor,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mulai Sekarang",
                            fontFamily = if (isTactical) ChakraPetchFontFamily else null,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = onPrimaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
