package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Komponen Edukasi Khusus: Kotak Dalil Al-Qur'an & Hadits Shahih
 */
@Composable
fun IslamicDalilCard(
    source: String,
    arabicText: String? = null,
    translation: String,
    fiqhNote: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1E20)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Rujukan Dalil Syariah: $source",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }

            if (!arabicText.isNullOrBlank()) {
                Text(
                    text = arabicText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    lineHeight = 22.sp
                )
            }

            Text(
                text = "“$translation”",
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                color = Color.White80,
                lineHeight = 17.sp
            )

            if (!fiqhNote.isNullOrBlank()) {
                HorizontalDivider(color = DarkBorder)
                Row(verticalAlignment = Alignment.Top) {
                    Text("💡 ", fontSize = 11.sp)
                    Text(
                        text = fiqhNote,
                        fontSize = 11.sp,
                        color = EmeraldLight,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Komponen Edukasi Khusus: Kotak Hikmah & Kaidah Fiqih Muamalah
 */
@Composable
fun IslamicHikmahBox(
    title: String,
    points: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = EmeraldLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight
                )
            }

            points.forEach { point ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("•", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        text = point,
                        fontSize = 11.sp,
                        color = Color.White70,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Komponen Panduan Langkah Praktis Modul
 */
@Composable
fun IslamicStepByStepCard(
    title: String = "Langkah Praktis Penggunaan Modul:",
    steps: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E20)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = EmeraldLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldLight
                )
            }
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldPrimary.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent
                            )
                        }
                    }
                    Text(
                        text = step,
                        fontSize = 11.sp,
                        color = Color.White80,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

/**
 * Komponen Tanya Jawab Fiqih Muamalah (FAQ Syariah)
 */
@Composable
fun IslamicQACard(
    question: String,
    answer: String,
    reference: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161522)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF5E35B1).copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Text("❓ ", fontSize = 12.sp)
                Text(
                    text = question,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 16.sp
                )
            }
            Row(verticalAlignment = Alignment.Top) {
                Text("💡 ", fontSize = 12.sp)
                Text(
                    text = answer,
                    fontSize = 11.sp,
                    color = Color.White70,
                    lineHeight = 16.sp
                )
            }
            if (!reference.isNullOrBlank()) {
                Text(
                    text = "📚 Rujukan: $reference",
                    fontSize = 10.sp,
                    fontStyle = FontStyle.Italic,
                    color = GoldAccent
                )
            }
        }
    }
}

/**
 * Komponen Adab-Adab Syariah
 */
@Composable
fun IslamicAdabCard(
    title: String = "Adab-Adab Syariah Terkait:",
    adabList: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent
                )
            }
            adabList.forEach { adab ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("•", color = EmeraldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(
                        text = adab,
                        fontSize = 11.sp,
                        color = Color.White70,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 11: KALKULATOR WARIS ISLAM (FARAIDH & MAWARITH)
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicFaraidhContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. An-Nisa: 11 & HR. Ibnu Majah",
            arabicText = "يُوصِيكُمُ اللَّهُ فِي أَوْلَادِكُمْ ۖ لِلذَّكَرِ مِثْلُ حَظِّ الْأُنثَيَيْنِ",
            translation = "Allah mensyariatkan (mewajibkan) kepadamu tentang pembagian warisan untuk anak-anakmu, yaitu bagian seorang anak laki-laki sama dengan bagian dua orang anak perempuan...",
            fiqhNote = "Rasulullah SAW bersabda: 'Pelajarilah faraidh dan ajarkanlah, karena ia adalah setengah dari ilmu dan ilmu yang pertama kali dicabut dari umatku.' (HR. Ibnu Majah)."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Calculate, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Urutan Wajib Sebelum Waris Dibagikan (Tirkah)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "Dalam fiqih mawarith, harta peninggalan jenazah (Tirkah) tidak boleh langsung dibagi ke ahli waris sebelum 3 hak terdahulu dituntaskan:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 17.sp
                )

                Surface(
                    color = Color(0xFF0B1718),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1️⃣ Biaya Pengurusan Jenazah (Tajhiz al-Jana'iz): Kafan, makam, dan pemakaman secara wajar tanpa berlebihan.", fontSize = 11.sp, color = Color.White)
                        Text("2️⃣ Pelunasan Hutang Jenazah (Qardh/Dayn): Wajib dilunasi dari harta almarhum sebelum wasiat dan warisan!", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                        Text("3️⃣ Penunaian Wasiat: Maksimal 1/3 dari sisa harta bersih, dan tidak boleh ditujukan kepada penerima ahli waris.", fontSize = 11.sp, color = Color.White)
                        Text("4️⃣ Pembagian Ahli Waris: Sisa bersih dibagikan secara adil berdasarkan Ashabul Furudh dan Ashabah.", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        IslamicHikmahBox(
            title = "Fitur Kalkulator Faraidh di Amanah Ledger",
            points = listOf(
                "Hisab Otomatis: Menghitung porsi pasti Ashabul Furudh (1/2, 1/3, 2/3, 1/4, 1/6, 1/8) sesuai nas Al-Qur'an.",
                "Rasio Anak Laki & Perempuan: Menerapkan rasio 2:1 secara tepat bagi ashabah bil ghair.",
                "Hak Pasangan & Orang Tua: Porsi suami (1/2 atau 1/4), istri (1/4 atau 1/8), ayah dan ibu (1/6) otomatis terkalkulasi.",
                "Cegah Sengketa Keluarga: Hasil hisab transparan lengkap dengan nilai rupiah per ahli waris."
            )
        )

        IslamicStepByStepCard(
            title = "Langkah Praktis Menghitung Waris Faraidh:",
            steps = listOf(
                "Buka Kalkulator Waris Faraidh dari tombol di bawah.",
                "Masukkan total nilai harta peninggalan kotor (Tirkah).",
                "Input biaya tajhiz jenazah, total hutang almarhum, dan wasiat (maks 1/3).",
                "Pilih komposisi ahli waris yang ditinggalkan (suami/istri, anak laki-laki, anak perempuan, ayah, ibu).",
                "Tekan 'Hitung Waris Syariah' untuk melihat rincian nominal dan fraksi pembagian per ahli waris secara transparan."
            )
        )

        IslamicQACard(
            question = "Apakah anak angkat atau kerabat non-muslim berhak atas warisan?",
            answer = "Secara nasab, anak angkat dan kerabat beda agama tidak termasuk Ashabul Furudh. Namun, Islam memberikan solusi melalui Wasiat Wajibah maksimal sepertiga (1/3) dari harta peninggalan bersih sebelum dibagi ke ahli waris nasab.",
            reference = "Kompilasi Hukum Islam (KHI) Pasal 209 & Ijma Ulama"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Kalkulator Waris Faraidh", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 12: AKAD QARDH HASAN (HUTANG PIUTANG SYARIAH)
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicQardhContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. Al-Baqarah: 282 (Ayat Mudayanah)",
            arabicText = "يَا أَيُّهَا الَّذِينَ آمَنُوا إِذَا تَدَايَنتُم بِدَيْنٍ إِلَىٰ أَجَلٍ مُّسَمًّى فَاكْتُبُوهُ",
            translation = "Wahai orang-orang yang beriman! Apabila kamu bermuamalah tidak secara tunai untuk waktu yang ditentukan, hendaklah kamu menuliskannya...",
            fiqhNote = "Ayat terpanjang dalam Al-Qur'an ini secara tegas mewajibkan dokumentasi pencatatan hutang piutang beserta saksi agar terhindar dari perselisihan dan riba."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kaidah Utama: Qardh Hasan Bebas Riba", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "Qardh Hasan adalah pinjaman kebajikan tanpa meminta kelebihan sedikitpun:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 17.sp
                )

                Surface(
                    color = Color(0xFF081415),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ExpenseCoral.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("⚠️ Kaidah Fiqih Pengharam Bunga Pinjaman:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ExpenseCoral)
                        Text("« كُلُّ قَرْضٍ جَرَّ مَنْفَعَةً فَهُوَ رِبًا »", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("'Setiap pinjaman yang menarik manfaat/keuntungan tambahan (bagi pemberi pinjaman) adalah riba.'", fontSize = 11.sp, fontStyle = FontStyle.Italic, color = Color.White80)
                        Text("Pinjam Rp 1.000.000 wajib kembali persis Rp 1.000.000 tanpa bunga, tanpa potongan biaya terselubung, dan tanpa denda keterlambatan berbunga.", fontSize = 10.sp, color = Color.White70)
                    }
                }
            }
        }

        IslamicHikmahBox(
            title = "Manajemen Qardh Hasan di Amanah Ledger",
            points = listOf(
                "Pemisahan Piutang vs Hutang: Mencatat siapa yang berhutang dan kepada siapa kita berhutang secara rapi.",
                "Jatuh Tempo & Pengingat: Memantau tanggal tenggat pembayaran agar amanah tertunai tepat waktu.",
                "Pencatatan Cicilan: Pelunasan bertahap dicatat otomatis tanpa merusak neraca buku besar kas.",
                "Pemberian Kelonggaran: Sesuai QS Al-Baqarah: 280, jika peminjam dalam kesempitan, beri tangguh atau sedekahkan sebagai penghapus dosa."
            )
        )

        IslamicStepByStepCard(
            title = "Langkah Praktis Pencatatan Qardh Hasan:",
            steps = listOf(
                "Pilih tab Piutang (jika kita meminjamkan) atau Hutang (jika kita meminjam).",
                "Ketik nama pihak kedua, kontak, dan nominal pokok pinjaman murni.",
                "Tentukan tanggal akad dan batas waktu jatuh tempo pelunasan.",
                "Simpan catatan akad; sistem akan menampilkan status lunas/aktif dan tanggal tempo di beranda.",
                "Saat ada pembayaran cicilan, klik 'Bayar/Terima Cicilan' untuk memperbarui sisa hutang secara otomatis."
            )
        )

        IslamicQACard(
            question = "Bolehkah peminjam memberikan hadiah/tambahan saat melunasi hutang?",
            answer = "Boleh dan bahkan disunnahkan (Husnul Qadha), DENGAN SYARAT hadiah tersebut murni kerelaan peminjam, TIDAK dipersyaratkan di awal akad, dan TIDAK menjadi kebiasaan yang mengikat sebelum pelunasan.",
            reference = "HR. Bukhari No. 2393 & Fiqih Sunnah Sayyid Sabiq"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Pencatatan Qardh Hasan", fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 13: ZAKAT HUB & PENYALURAN 8 ASNAF
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicZakatHubContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. At-Taubah: 60 (Mustahiq 8 Asnaf)",
            arabicText = "إِنَّمَا الصَّدَقَاتُ لِلْفُقَرَاءِ وَالْمَسَاكِينِ وَالْعَامِلِينَ عَلَيْهَا وَالْمُؤَلَّفَةِ قُلُوبُهُمْ وَفِي الرِّقَابِ وَالْغَارِمِينَ وَفِي سَبِيلِ اللَّهِ وَابْنِ السَّبِيلِ ۖ فَرِيضَةً مِّنَ اللَّهِ",
            translation = "Sesungguhnya zakat itu hanyalah untuk orang-orang fakir, orang miskin, amil zakat, mualaf yang dilunakkan hatinya, untuk memerdekakan hamba sahaya, orang yang berutang, untuk jalan Allah, dan untuk orang yang sedang dalam perjalanan, sebagai kewajiban dari Allah...",
            fiqhNote = "Zakat wajib disalurkan secara spesifik kepada 8 asnaf ini, berbeda dengan infaq/sedekah umum yang cakupannya lebih luas."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pusat Penyaluran Zakat & Infaq Amanah", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "Zakat Hub mengintegrasikan perhitungan hisab zakat dengan eksekusi penyaluran langsung dari Virtual Infaq Vault:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 17.sp
                )

                Surface(
                    color = Color(0xFF0B1718),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(EmeraldPrimary), contentAlignment = Alignment.Center) {
                                Text("1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kalkulator Zakat Profesi: Dihitung 2.5% dari rezeki kasab bersih.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(EmeraldPrimary), contentAlignment = Alignment.Center) {
                                Text("2", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kalkulator Zakat Maal: Berdasarkan aset haul & nisab 85g emas.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(EmeraldPrimary), contentAlignment = Alignment.Center) {
                                Text("3", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Disburse Dana Infaq Vault: Penyaluran langsung ke amil dengan kwitansi syariah.", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        IslamicHikmahBox(
            title = "Keutamaan Berzakat Tepat Sasaran",
            points = listOf(
                "Membersihkan Harta & Jiwa: Firman Allah 'Ambillah zakat dari harta mereka guna membersihkan dan menyucikan mereka' (QS. At-Taubah: 103).",
                "Menjaga Keseimbangan Sosial: Mencegah perputaran harta hanya di kalangan orang kaya (QS. Al-Hasyr: 7).",
                "Kwitansi Digital: Setiap penyaluran zakat menghasilkan tanda terima amanah yang dapat diaudit kapan saja."
            )
        )

        IslamicStepByStepCard(
            title = "Langkah Praktis Penyaluran Zakat & Infaq:",
            steps = listOf(
                "Pilih kalkulator: Zakat Profesi (penghasilan bulanan) atau Zakat Maal (harta simpanan haul 1 tahun).",
                "Periksa apakah nilai harta telah melampaui nisab (setara 85 gram emas murni).",
                "Jika wajib zakat, sistem menghitung kewajiban 2.5% (atau 2.577% untuk haul solar/masehi).",
                "Klik 'Salurkan ke Amil / Asnaf' untuk memilih lembaga amil resmi (BAZNAS/Dompet Dhuafa/dll).",
                "Simpan bukti penyerahan; saldo kas dan Virtual Vault otomatis disesuaikan dengan prinsip akuntansi syariah."
            )
        )

        IslamicQACard(
            question = "Kapan zakat profesi/gaji wajib dikeluarkan?",
            answer = "Berdasarkan Fatwa MUI No. 3 Tahun 2003, zakat penghasilan dikeluarkan pada saat menerima gaji jika total pendapatan setahun mencapai nisab (85 gram emas). Mengeluarkannya per bulan saat gajian adalah cara termudah dan paling aman.",
            reference = "Fatwa MUI No. 3 Tahun 2003 tentang Zakat Penghasilan"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Zakat Hub & 8 Asnaf", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 14: MULTI-WALLET & REKENING KAS SYARIAH
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicMultiWalletContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "Prinsip Amanah & Ketertiban Pengelolaan Kas",
            arabicText = "إِنَّ اللَّهَ يَأْمُرُكُمْ أَن تُؤَدُّوا الْأَمَانَاتِ إِلَىٰ أَهْلِهَا",
            translation = "Sesungguhnya Allah menyuruh kamu menyampaikan amanat kepada yang berhak menerimanya... (QS. An-Nisa: 58)",
            fiqhNote = "Memisahkan rekening pribadi, rekening tabungan ibadah, kas operasional, dan dana titipan infaq adalah wujud ihsan agar tidak terjadi syubhat percampuran dana."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kelola Berbagai Rekening & Kantong Kas", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "Amanah Ledger memungkinkan Anda mengelola seluruh rekening keuangan dalam satu tempat:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 17.sp
                )

                Surface(
                    color = Color(0xFF0B1718),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏦 ", fontSize = 14.sp)
                            Text("Rekening Bank Syariah: Tabungan Wadiah/Mudharabah BSI, Muamalat, BCA Syariah.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📱 ", fontSize = 14.sp)
                            Text("Dompet Digital / E-Wallet: Saldo transaksi harian bebas bunga.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💵 ", fontSize = 14.sp)
                            Text("Kas Tunai (Dompet Fisik): Pengeluaran uang kertas sehari-hari.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔄 ", fontSize = 14.sp)
                            Text("Transfer Antar Kantong: Pindah dana antar akun kas tanpa memengaruhi laba rugi.", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        IslamicHikmahBox(
            title = "Manfaat Pemisahan Akun Kas",
            points = listOf(
                "Proteksi Privasi: Tersedia tombol Sensor Saldo (Masking) pada kartu beranda demi menjaga kerahasiaan saat di tempat umum.",
                "Audit Saldo Riil: Setiap mutasi debit/kredit langsung meng-update saldo kas akun bersangkutan secara real-time.",
                "Bebas Selisih: Transparansi penuh atas setiap rupiah yang tersimpan di berbagai tempat."
            )
        )

        IslamicStepByStepCard(
            title = "Langkah Praktis Mengelola Multi-Wallet:",
            steps = listOf(
                "Buka menu Multi-Wallet dari tombol di bawah.",
                "Klik '+ Tambah Akun Kas' untuk mendaftarkan rekening bank syariah, e-wallet, atau kas tunai baru.",
                "Pilih jenis akun (Aset Kas, Tabungan Mudharabah, atau Titipan Amanah).",
                "Gunakan fitur 'Transfer Kas' untuk memindahkan dana antar rekening tanpa mempengaruhi laba rugi.",
                "Gunakan tombol Sensor Mata (Masking) di beranda saat berada di keramaian demi menjaga privasi finansial."
            )
        )

        IslamicQACard(
            question = "Bagaimana jika ada bunga dari rekening bank konvensional yang terlanjur masuk?",
            answer = "Bunga bank konvensional adalah riba yang wajib dibersihkan. Catat sebagai 'Dana Syubhat' dan salurkan 100% untuk fasilitas umum (toilet umum, perbaikan jalan, jembatan), tanpa niat mengharap sedekah pahala pribadi.",
            reference = "Fatwa DSN-MUI No. 123/DSN-MUI/XI/2018 tentang Pemanfaatan Dana Sosial Keagamaan"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kelola Multi-Wallet & Kas", fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 15: OTOMASI TRANSAKSI BERULANG (RECURRING)
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicRecurringContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "HR. Bukhari & Muslim (Keutamaan Istiqamah)",
            arabicText = "أَحَبُّ الأَعْمَالِ إِلَى اللَّهِ أَدْوَمُهَا وَإِنْ قَلَّ",
            translation = "Amalan yang paling dicintai oleh Allah adalah amalan yang berkelanjutan (terus menerus/istiqamah) walaupun sedikit.",
            fiqhNote = "Mengatur pengeluaran infaq, sedekah bulanan, dan pemenuhan nafkah secara terencana dan konsisten mencerminkan keistiqamahan dalam bermuamalah."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoMode, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Otomasi Transaksi Berulang yang Cerdas", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "Amanah Ledger menjamin Anda tidak pernah melewatkan kewajiban berkala:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 17.sp
                )

                Surface(
                    color = Color(0xFF0B1718),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📅 Frekuensi Fleksibel: Atur jadwal harian, mingguan, atau bulanan.", fontSize = 11.sp, color = Color.White)
                        Text("🔔 Deteksi Jatuh Tempo Otomatis: Muncul di beranda saat tanggal pembayaran tiba.", fontSize = 11.sp, color = GoldAccent, fontWeight = FontWeight.SemiBold)
                        Text("⚡ Eksekusi 1-Klik: Klik 'Jalankan Transaksi' untuk langsung mencatat jurnal ganda tanpa ketik ulang.", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.Bold)
                        Text("🛡️ Pemenuhan Hak Nafkah: Memastikan nafkah wajib keluarga (QS. Al-Baqarah: 233) tertunaikan tepat waktu.", fontSize = 11.sp, color = Color.White70)
                    }
                }
            }
        }

        IslamicHikmahBox(
            title = "Contoh Transaksi Rutin yang Disarankan",
            points = listOf(
                "Infaq Bulanan Anak Yatim / Pesantren: Menjaga kesinambungan sedekah jariyah.",
                "SPP Pendidikan Agama Anak: Prioritas investasi ilmu syar'i bagi keluarga.",
                "Nafkah Wajib Orang Tua & Pasangan: Pemenuhan kewajiban syar'i yang berpahala besar.",
                "Iuran Kebersihan Lingkungan & Masjid: Menjaga ukhuwah dan kebersihan tempat ibadah."
            )
        )

        IslamicStepByStepCard(
            title = "Langkah Praktis Otomasi Transaksi Rutin:",
            steps = listOf(
                "Buka menu Otomasi Transaksi dari tombol di bawah.",
                "Klik 'Tambah Jadwal Rutin' dan masukkan deskripsi transaksi (misal: 'Nafkah Orang Tua', 'SPP Anak').",
                "Pilih tipe (Pengeluaran Rutin, Infaq Berkala, atau Transfer Kas) dan nominalnya.",
                "Pilih frekuensi (Harian, Mingguan, atau Bulanan) dan tetapkan tanggal eksekusi berikutnya.",
                "Saat jatuh tempo tiba, notifikasi akan muncul di beranda dan Anda cukup menekan 'Jalankan Transaksi'."
            )
        )

        IslamicQACard(
            question = "Mengapa nafkah wajib keluarga lebih utama dari sedekah sunnah?",
            answer = "Rasulullah SAW bersabda: 'Dinar yang engkau infaqkan di jalan Allah, dinar yang engkau infaqkan untuk memerdekakan budak, dinar yang engkau sedekahkan kepada orang miskin, dan dinar yang engkau nafkahkan kepada keluargamu; yang paling besar pahalanya adalah yang engkau nafkahkan kepada keluargamu.'",
            reference = "HR. Muslim No. 995"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoMode, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kelola Otomasi Transaksi Rutin", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 16: TABUNGAN TARGET IBADAH (IBADAH GOALS)
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicIbadahGoalsContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. Al-Baqarah: 197 & Fiqih Istitha'ah",
            arabicText = "وَتَزَوَّدُوا فَإِنَّ خَيْرَ الزَّادِ التَّقْوَىٰ ۚ وَاتَّقُونِ يَا أُولِي الْأَلْبَابِ",
            translation = "Dan berbekallah kamu, sesungguhnya sebaik-baik bekal adalah takwa, dan bertakwalah kepada-Ku wahai orang-orang yang mempunyai akal sehat...",
            fiqhNote = "Ibadah besar seperti Haji, Umroh, dan Qurban membutuhkan persiapan bekal finansial yang matang (istitha'ah). Merencanakannya sejak dini adalah wujud ikhtiar takwa."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rencanakan Target Ibadah Finansial", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "Fitur Ibadah Goals membantu Anda mengumpulkan dana untuk target ibadah mulia:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 17.sp
                )

                Surface(
                    color = Color(0xFF0B1718),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🐑 ", fontSize = 14.sp)
                            Text("Qurban Idul Adha: Menabung rutin bulanan untuk pengadaan kambing/sapi qurban.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🕋 ", fontSize = 14.sp)
                            Text("Umroh & Haji: Menghimpun dana porsi haji atau paket perjalanan umroh keluarga.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👶 ", fontSize = 14.sp)
                            Text("Aqiqah Anak: Mempersiapkan dana syukuran aqiqah kelahiran putra/putri tercinta.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🕌 ", fontSize = 14.sp)
                            Text("Wakaf Produktif / Masjid: Berpartisipasi dalam wakaf pembangunan tempat ibadah.", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        IslamicHikmahBox(
            title = "Fitur Cerdas Ibadah Goals",
            points = listOf(
                "Visual Progress Bar: Memantau persentase ketercapaian target tabungan secara visual dan membangkitkan semangat.",
                "Rekomendasi Setoran Harian/Bulanan: Aplikasi otomatis menghitung berapa nominal yang harus disisihkan setiap bulan agar tercapai sebelum tenggat waktu.",
                "Penyimpanan Aman: Dana target ibadah dialokasikan khusus dan terlindungi dari pengeluaran konsumtif yang tidak perlu."
            )
        )

        IslamicStepByStepCard(
            title = "Langkah Praktis Target Tabungan Ibadah:",
            steps = listOf(
                "Buka menu Tabungan Ibadah Goals dari tombol di bawah.",
                "Pilih kategori target ibadah (Qurban, Umroh/Haji, Aqiqah, atau Wakaf).",
                "Tentukan target total dana yang dibutuhkan dan estimasi tanggal pelaksanaan.",
                "Aplikasi akan otomatis memproyeksikan target setoran tabungan bulanan yang realistis.",
                "Setiap menabung, klik 'Setor Tabungan' untuk menyisihkan kas ke rekening amanah ibadah tersebut."
            )
        )

        IslamicQACard(
            question = "Bolehkah mencicil atau berhutang untuk berangkat Haji/Umroh?",
            answer = "Kewajiban haji/umroh hanya bagi yang istitha'ah (mampu secara finansial riil). Para ulama membolehkan pembiayaan talangan syariah asalkan: menggunakan akad syariah murabahah/ijarah tanpa bunga, nasabah memiliki kemampuan riil mencicil, dan tidak menyusahkan nafkah wajib keluarga.",
            reference = "Fatwa DSN-MUI No. 29/DSN-MUI/VI/2002 tentang Pembiayaan Pengurusan Haji"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Tabungan Ibadah Goals", fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 17: ENSIKLOPEDIA FATWA & RUJUKAN DSN-MUI
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicIslamicGroundingContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. An-Nahl: 43 & Otoritas Fatwa DSN-MUI",
            arabicText = "فَاسْأَلُوا أَهْلَ الذِّكْرِ إِن كُنتُمْ لَا تَعْلَمُونَ",
            translation = "Maka bertanyalah kepada orang-orang yang mempunyai pengetahuan jika kamu tidak mengetahui...",
            fiqhNote = "Dewan Syariah Nasional Majelis Ulama Indonesia (DSN-MUI) adalah otoritas rujukan hukum Islam resmi dalam bidang muamalah maliyyah di Indonesia."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ensiklopedia Fatwa & Solusi Fiqih Muamalah", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "Amanah Ledger menyediakan basis pengetahuan interaktif untuk menjawab keraguan muamalah kontemporer:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 17.sp
                )

                Surface(
                    color = Color(0xFF0B1718),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📜 Fatwa Paylater & Denda Keterlambatan: Penjelasan status riba denda vs ta'zir/gharamat sosial.", fontSize = 11.sp, color = Color.White)
                        Text("💳 Fatwa Uang Elektronik & Cashback: Fatwa DSN No. 116 mengenai batas kehalalan diskon e-wallet.", fontSize = 11.sp, color = Color.White)
                        Text("⚖️ Screening Bebas Riba, Gharar, Maysir: Kriteria transaksi halal yang wajib dipenuhi dalam setiap akad.", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.SemiBold)
                        Text("🔍 Pencarian Cerdas: Temukan dalil dan jawaban hukum syariah dengan cepat.", fontSize = 11.sp, color = GoldAccent)
                    }
                }
            }
        }

        IslamicHikmahBox(
            title = "Mengapa Literasi Fiqih Muamalah Penting?",
            points = listOf(
                "Kehati-hatian dalam Mencari Rezeki: Menghindarkan diri dan keluarga dari memakan harta syubhat atau riba yang dapat menghalangi terkabulnya doa.",
                "Ketenangan Jiwa: Bertransaksi dengan tenang karena mengetahui akad yang digunakan telah sesuai dengan fatwa ulama terpercaya.",
                "Panduan Terus Diperbarui: Rujukan fatwa diselaraskan dengan perkembangan instrumen keuangan syariah terkini."
            )
        )

        IslamicStepByStepCard(
            title = "Langkah Praktis Menggunakan Ensiklopedia Fatwa:",
            steps = listOf(
                "Buka menu Ensiklopedia Fatwa dari tombol di bawah.",
                "Ketik kata kunci muamalah yang ingin dicari (contoh: 'Paylater', 'E-Wallet', 'Emas Online', 'Asuransi').",
                "Baca intisari hukum halal/haram, syarat akad sah, dan dalil nash yang melandasinya.",
                "Gunakan fitur filter berdasarkan kategori (Perbankan, Investasi, Zakat, Transaksi Digital).",
                "Terapkan panduan kepatuhan syariah pada pencatatan transaksi sehari-hari Anda."
            )
        )

        IslamicQACard(
            question = "Apa beda Riba Nasi'ah dan Riba Fadhl dalam transaksi modern?",
            answer = "Riba Nasi'ah adalah kelebihan nilai pinjaman karena penangguhan waktu bayar (seperti bunga kredit/denda paylater). Sedangkan Riba Fadhl adalah kelebihan kuantitas dalam tukar-menukar barang ribawi sejenis (seperti barter emas batangan dengan emas perhiasan beda berat tanpa kontan).",
            reference = "Fatwa DSN-MUI & Fiqih Muamalah Standar AAOIFI"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Ensiklopedia Fatwa DSN-MUI", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 18: EKSPOR LAPORAN KEUANGAN SYARIAH (PDF/CSV)
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicExportReportContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "Atsar Umar bin Khattab RA (Muhasabah & Hisab)",
            arabicText = "حَاسِبُوا أَنْفُسَكُمْ قَبْلَ أَنْ تُحَاسَبُوا ، وَزِنُوا أَنْفُسَكُمْ قَبْلَ أَنْ تُوزَنُوا",
            translation = "Hisablah (introspeksi/evaluasilah) diri kalian sebelum kalian dihisab di hari kiamat, dan timbanglah amal kalian sebelum amal kalian ditimbang...",
            fiqhNote = "Membuat laporan keuangan yang transparan dan rapi adalah bentuk hisab maliyyah (audit harta) agar kita siap mempertanggungjawabkan setiap rezeki dari mana diperoleh dan ke mana dibelanjakan."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Laporan Keuangan & Audit Syariah Resmi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    text = "Amanah Ledger menyediakan fitur ekspor dokumen yang rapi, profesional, dan siap dibagikan:",
                    fontSize = 12.sp,
                    color = Color.White70,
                    lineHeight = 17.sp
                )

                Surface(
                    color = Color(0xFF0B1718),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📄 ", fontSize = 14.sp)
                            Text("Format PDF Resmi: Laporan arus kas, laba rugi, dan rasio kedermawanan lengkap dengan kop syariah.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📊 ", fontSize = 14.sp)
                            Text("Format CSV / Excel: Data mutasi mentah yang kompatibel untuk analisis lebih lanjut di komputer.", fontSize = 11.sp, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📈 ", fontSize = 14.sp)
                            Text("Spiritual Liquidity Index (SLI): Statistik persentase infaq terhadap pemasukan kasab.", fontSize = 11.sp, color = EmeraldLight, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🔒 ", fontSize = 14.sp)
                            Text("Privasi 100% Offline: Dokumen dibuat langsung di perangkat tanpa diunggah ke server pihak ketiga.", fontSize = 11.sp, color = GoldAccent)
                        }
                    }
                }
            }
        }

        IslamicHikmahBox(
            title = "Pertanggungjawaban Dua Pertanyaan Harta",
            points = listOf(
                "Hadits Rasulullah SAW: 'Kedua kaki seorang hamba tidak akan bergeser pada hari kiamat sampai ditanya tentang 4 hal... salah satunya tentang hartanya: dari mana ia peroleh dan untuk apa ia belanjakan' (HR. Tirmidzi).",
                "Dengan laporan keuangan syariah yang tercatat rapi, kita senantiasa memantau apakah rezeki kita bersih dan dibelanjakan pada jalan yang diridhai Allah SWT."
            )
        )

        IslamicStepByStepCard(
            title = "Langkah Praktis Ekspor Laporan Keuangan:",
            steps = listOf(
                "Buka menu Ekspor Laporan dari tombol di bawah.",
                "Pilih rentang tanggal audit keuangan (Bulan Ini, Tahun Ini, atau Kustom).",
                "Pilih format yang diinginkan: 'Cetak PDF Syariah' untuk dokumen siap baca atau 'Ekspor CSV' untuk olah data spreadsheet.",
                "Periksa metrik Spiritual Liquidity Index (SLI) dan rasio kedermawanan Anda.",
                "File dokumen langsung tersimpan di folder unduhan perangkat secara aman dan 100% privat offline."
            )
        )

        IslamicQACard(
            question = "Mengapa hisab keuangan pribadi di dunia sangat dianjurkan dalam Islam?",
            answer = "Karena setiap rupiah harta yang kita miliki akan dipertanggungjawabkan dua kali di akhirat: dari mana asal sumbernya (halal/haram) dan ke mana ia dibelanjakan. Menghisab keuangan di dunia secara transparan menjauhkan kita dari kelalaian dan harta batil.",
            reference = "HR. At-Tirmidzi No. 2417 & Atsar Khalifah Umar bin Khattab RA"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Ekspor Laporan PDF/CSV", fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}
