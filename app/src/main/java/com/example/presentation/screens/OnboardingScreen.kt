package com.example.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.core.state.AmanahLedgerViewModel

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: AmanahLedgerViewModel,
    onComplete: () -> Unit
) {
    val emeraldPrimary = Color(0xFF00897B)
    val coralRed = Color(0xFFD32F2F)
    val goldAccent = Color(0xFFDAA520)
    
    val pages = listOf(
        OnboardingPage(
            title = "Ahlan wa Sahlan",
            description = "Selamat datang di Infaqan Syariah (Amanah Ledger).\nSebuah asisten cerdas untuk memandu pencatatan keuangan pribadi & keluarga yang sesuai syariah, terhindar dari riba, dan disiplin dalam zakat & infaq.",
            icon = Icons.Default.MenuBook,
            iconColor = emeraldPrimary
        ),
        OnboardingPage(
            title = "100% Privat & Aman",
            description = "Aplikasi ini beroperasi 100% offline (lokal) pada perangkat Anda.\nKami tidak pernah terhubung ke bank atau e-wallet, dan tidak pernah mengunggah data keuangan Anda ke server manapun.",
            icon = Icons.Default.Security,
            iconColor = coralRed
        ),
        OnboardingPage(
            title = "Hisab Maliyah (Evaluasi Mandiri)",
            description = "Sesuai adab Islam, Anda mencatat setiap pemasukan dan pengeluaran secara mandiri (manual).\nHal ini melatih kesadaran (mindfulness) terhadap dari mana harta didapat dan ke mana harta dibelanjakan.",
            icon = Icons.Default.SelfImprovement,
            iconColor = emeraldPrimary
        ),
        OnboardingPage(
            title = "Data Simulasi (Dummy)",
            description = "Kami telah menyediakan beberapa data transaksi dan dompet contoh (dummy) agar Anda bisa menjelajahi fitur aplikasi.\nAnda bebas mengubah, menghapus, atau me-reset seluruh data ini di menu 'Pengaturan' saat siap mencatat data asli Anda.",
            icon = Icons.Default.Science,
            iconColor = coralRed
        ),
        OnboardingPage(
            title = "Siap Mulai Pencatatan?",
            description = "Langkah pertama: Buka menu 'Multi-Wallet & Rekening Kas' untuk mendaftarkan dompet dan saldo awal Anda, lalu Anda siap mengelola keuangan harian dengan tenang.",
            icon = Icons.Default.CheckCircle,
            iconColor = goldAccent
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "INFAQAN SYARIAH",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Text(
                text = "Amanah Ledger",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(5f)
            ) { position ->
                val page = pages[position]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(page.iconColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = page.iconColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = page.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = page.description,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Page indicators
            Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    val size = if (pagerState.currentPage == iteration) 10.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(size)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (pagerState.currentPage > 0) {
                    TextButton(onClick = { 
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }) {
                        Text("Kembali")
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (pagerState.currentPage < pages.size - 1) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Selanjutnya")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.completeOnboarding()
                            onComplete()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = emeraldPrimary)
                    ) {
                        Text("Mulai Sekarang")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
