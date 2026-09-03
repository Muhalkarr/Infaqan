package com.example.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.calendar.HijriDate
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DualCalendarCard(
    selectedHijriOffset: Int = 0,
    modifier: Modifier = Modifier,
    onDateSelected: (Date, HijriDate) -> Unit = { _, _ -> }
) {
    var currentDateOffsetDays by remember { mutableStateOf(0) }

    val calendar = remember(currentDateOffsetDays) {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, currentDateOffsetDays)
        }
    }
    val activeDate = remember(calendar) { calendar.time }

    val hijriDate = remember(activeDate, selectedHijriOffset) {
        HijriCalendarEngine.fromGregorian(date = activeDate, manualOffsetDays = selectedHijriOffset)
    }

    // Gregorian Date Formats
    val masehiDayName = remember(activeDate) {
        SimpleDateFormat("EEEE", Locale("id", "ID")).format(activeDate)
    }
    val masehiDayNum = remember(activeDate) {
        SimpleDateFormat("d", Locale("id", "ID")).format(activeDate)
    }
    val masehiMonthYear = remember(activeDate) {
        SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(activeDate)
    }

    // Islamic / Hijri metadata
    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
    val isFriday = dayOfWeek == Calendar.FRIDAY
    val isMondayOrThursday = dayOfWeek == Calendar.MONDAY || dayOfWeek == Calendar.THURSDAY
    val isAyyamulBidh = hijriDate.day in 13..15

    val hijriDayArabic = when (dayOfWeek) {
        Calendar.SUNDAY -> "Yaumul Ahad (الأحد)"
        Calendar.MONDAY -> "Yaumul Itsnayn (الإثنين)"
        Calendar.TUESDAY -> "Yaumuts Tsulatsa (الثلاثاء)"
        Calendar.WEDNESDAY -> "Yaumul Arba'a (الأربعاء)"
        Calendar.THURSDAY -> "Yaumul Khamis (الخميس)"
        Calendar.FRIDAY -> "Yaumul Jumu'ah (الجمعة)"
        Calendar.SATURDAY -> "Yaumus Sabt (السبت)"
        else -> ""
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dual_calendar_card"),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Title and Stepper Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Kalender Ganda",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Dual-Kalender Masehi & Hijriah",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Sinkronisasi Tarikh Ibadah & Muamalah",
                            fontSize = 11.sp,
                            color = GoldLight
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Date Navigator Steppers (Fixed-structure pill container)
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { currentDateOffsetDays-- },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Hari Sebelumnya",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        Surface(
                            onClick = { currentDateOffsetDays = 0 },
                            color = if (currentDateOffsetDays != 0) EmeraldPrimary.copy(alpha = 0.25f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = when {
                                    currentDateOffsetDays == 0 -> "Hari Ini"
                                    currentDateOffsetDays > 0 -> "+${currentDateOffsetDays}h"
                                    else -> "${currentDateOffsetDays}h"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentDateOffsetDays != 0) EmeraldLight else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { currentDateOffsetDays++ },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Hari Berikutnya",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Side-by-Side Dual Calendar Container
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // LEFT PANE: Kalender Masehi (Gregorian)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    DarkSurfaceVariant,
                                    Color(0xFF162529)
                                )
                            )
                        )
                        .border(1.dp, Color(0xFF264653).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Kalender Masehi",
                                tint = Color(0xFF00B4D8),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "MASEHI",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00B4D8),
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = masehiDayNum,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = masehiDayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Text(
                            text = masehiMonthYear,
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // RIGHT PANE: Kalender Hijriah (Islamic Tarikh)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    EmeraldDark.copy(alpha = 0.35f),
                                    Color(0xFF0D251D)
                                )
                            )
                        )
                        .border(1.dp, EmeraldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = "Kalender Hijriah",
                                tint = GoldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "HIJRIAH",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${hijriDate.day}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldAccent
                        )

                        Text(
                            text = hijriDate.monthName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Text(
                            text = "${hijriDate.year} H",
                            fontSize = 11.sp,
                            color = EmeraldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Islamic Badges & Sunnah Reminder Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = hijriDayArabic,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = when {
                            isFriday -> "Hari Sayyidul Ayyam • Sunnah Sedekah & Baca Al-Kahfi"
                            isAyyamulBidh -> "Fase Ayyamul Bidh (13-15 H) • Sunnah Puasa Tengah Bulan"
                            isMondayOrThursday -> "Jadwal Sunnah Puasa Senin-Kamis & Amal Shalih"
                            else -> "Ketetapan Haul & Zakat Maal mengikuti tarikh Hijriah"
                        },
                        fontSize = 10.sp,
                        color = if (isFriday || isAyyamulBidh || isMondayOrThursday) GoldLight else Color.White.copy(alpha = 0.6f)
                    )
                }

                if (isFriday || isAyyamulBidh || isMondayOrThursday) {
                    Surface(
                        color = GoldAccent.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isFriday) "Jumat Berkah" else "Sunnah",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
