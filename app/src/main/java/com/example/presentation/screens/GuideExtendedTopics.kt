package com.example.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// -------------------------------------------------------------------------------------------------
// TOPIC 25: ANALISIS & AUDIT KOMPREHENSIF SLI & PERINGATAN ISRAF
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicSliAnalyticsGuideContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. Al-Isra': 26-27 & QS. Al-Furqan: 67",
            arabicText = "﴿وَالَّذِينَ إِذَا أَنْفَقُوا لَمْ يُسْرِفُوا وَلَمْ يَقْتُرُوا وَكَانَ بَيْنَ ذَٰلِكَ قَوَامًا﴾",
            translation = "Dan orang-orang yang apabila membelanjakan (harta), mereka tidak berlebihan, dan tidak (pula) kikir, dan adalah (pembelanjaan itu) di tengah-tengah antara yang demikian.",
            fiqhNote = "Evaluasi arus pengeluaran dan perbandingan sedekah terhadap gaya hidup adalah ikhtiar menjaga keseimbangan (qawama) serta menjauhi sifat israf (berlebih-lebihan)."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Audit Komprehensif SLI & Pelacakan Angka", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• Spiritual Liquidity Index (SLI): Infaq Vault ÷ Belanja Konsumsi — mengukur porsi sedekah dibanding gaya hidup Anda.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Rasio dari Total Pendapatan: Infaq Vault ÷ Total Pendapatan (Kasab + Non-Kasab) — memudahkan verifikasi manual dari total pemasukan.", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text("• Rincian Pembentuk Nominal Infaq: Melacak asal angka infaq (alokasi pemasukan, round-up belanja, infaq syukur, dan sedekah langsung).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Kalkulator Simulasi Uji Hitung Manual: Uji coba dan cocokkan langsung angka pendapatan, konsumsi, serta target tarif infaq Anda.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Cara Menelusuri Audit SLI & Peringatan Israf:",
            steps = listOf(
                "Buka menu 'Analisis & Israf' dari sidebar atau tombol di bawah.",
                "Bandingkan kartu 'SLI (vs Konsumsi)' dan 'Rasio dr Pendapatan' untuk memahami dua sudut pandang rasio infaq.",
                "Buka bagian 'Rincian Pembentuk Nominal Infaq' untuk melihat sumber transaksi pembentuk saldo Vault.",
                "Gunakan 'Simulasi & Uji Hitung Audit Manual' jika ingin mencocokkan perhitungan pribadi dengan rekan atau penasihat keuangan.",
                "Periksa rasio konsumsi primer vs sekunder untuk mencegah pemborosan (israf)."
            )
        )

        IslamicQACard(
            question = "Mengapa persentase SLI bisa berbeda dengan hasil bagi Infaq ÷ Total Pendapatan?",
            answer = "Karena SLI membagi total alokasi Infaq terhadap total Belanja Konsumsi (gaya hidup) agar terlihat proporsi amal terhadap konsumsi, sedangkan hitungan manual umumnya membagi Infaq terhadap Total Pendapatan. Keduanya ditampilkan berdampingan di layar Analisis.",
            reference = "Metodologi Audit Amanah Ledger & Prinsip Hisab Al-Qawama"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Analisis & Audit SLI", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 26: MODUL LAPORAN ZAKAT LENGKAP (ZAKAT MAAL & ZAKAT FITRAH)
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicZakatReportGuideContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "HR. Bukhari No. 1503 & Muslim No. 984",
            arabicText = "«فَرَضَ رَسُولُ اللَّهِ ﷺ زَكَاةَ الْفِطْرِ صَاعًا مِنْ تَمْرٍ أَوْ صَاعًا مِنْ شَعِيرٍ عَلَى الْعَبْدِ وَالْحُرِّ وَالذَّكَرِ وَالْأُنْثَى وَالصَّغِيرِ وَالْكَبِيرِ مِنَ الْمُسْلِمِينَ»",
            translation = "Rasulullah SAW mewajibkan zakat fitrah satu sha' kurma atau satu sha' gandum atas hamba sahaya maupun orang merdeka, laki-laki maupun perempuan, kecil maupun besar dari kaum muslimin.",
            fiqhNote = "Zakat Fitrah wajib ditunaikan atas diri sendiri dan seluruh anggota keluarga yang menjadi tanggungan nafkah sebelum shalat Idul Fitri, sebagai penyuci jiwa dan santunan fakir miskin."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rekapitulasi Zakat Maal & Zakat Fitrah Keluarga", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• Zakat Maal Otomatis: Menggabungkan status nisab 85 gram emas dan masa kepemilikan haul (354 hari Hijriyah).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Kalkulator Zakat Fitrah Dinamis: Hitung total kewajiban berdasarkan jumlah tanggungan jiwa (termasuk diri sendiri) × harga makanan pokok/beras lokal (2,5 kg atau 3,5 liter/jiwa).", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text("• Total Gabungan Kewajiban Zakat: Menyajikan estimasi total zakat siap salur agar seluruh kewajiban keluarga tidak terlewat.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Menggunakan Modul Laporan Zakat:",
            steps = listOf(
                "Buka menu 'Laporan Zakat (Maal & Fitrah)' di sidebar atau dari Zakat Hub.",
                "Periksa ringkasan status Zakat Maal (kekayaan bersih dibanding nilai nisab & progres haul).",
                "Masukkan jumlah anggota keluarga/tanggungan jiwa yang wajib dibayarkan Zakat Fitrahnya.",
                "Masukkan harga beras atau bahan pokok lokal per kg/liter sesuai domisili Anda.",
                "Simpan konfigurasi dan gunakan hasil laporan sebagai acuan penyaluran zakat keluarga."
            )
        )

        IslamicQACard(
            question = "Bolehkah Zakat Fitrah ditunaikan dalam bentuk uang senilai harga beras lokal?",
            answer = "Menurut madzhab Hanafi dan ketetapan BAZNAS/MUI di Indonesia, menunaikan Zakat Fitrah dengan uang yang senilai harga makanan pokok setempat diperbolehkan demi kemaslahatan mustahiq, dengan takaran standar 2,5 kg (atau 3,5 liter) beras per jiwa.",
            reference = "SK Ketua BAZNAS & Panduan Zakat Fitrah Kemenag RI"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Laporan Zakat (Maal & Fitrah)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 27: NOTIFIKASI PENGINGAT PENYALURAN VIRTUAL INFAQ VAULT
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicInfaqNotificationGuideContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. Al-Baqarah: 148 & HR. Bukhari No. 1430",
            arabicText = "﴿فَاسْتَبِقُوا الْخَيْرَاتِ﴾",
            translation = "Maka berlomba-lombalah kamu dalam berbuat kebaikan.",
            fiqhNote = "Harta yang telah diniatkan untuk infaq dan masuk ke Virtual Infaq Vault pada hakikatnya adalah hak mustahiq. Menyegerakan penyalurannya mencegah kelalaian serta mempercepat manfaat bagi penerima."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pengingat Jadwal Penyaluran Dana Vault", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• Frekuensi Harian atau Mingguan: Pilih pengingat setiap hari atau setiap hari Jumat/akhir pekan sesuai kebiasaan beramal Anda.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Jam Kustom Presisi: Tentukan pukul berapa notifikasi muncul (misal pukul 05:30 usai Subuh atau pukul 11:30 jelang Jumat).", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text("• Uji Coba Instan: Tersedia tombol kirim tes notifikasi di Pengaturan untuk memastikan izin notifikasi perangkat aktif.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Mengatur Pengingat Notifikasi Vault:",
            steps = listOf(
                "Buka 'Pengaturan Terpusat' lalu pilih tab 'Infaq & Notifikasi'.",
                "Aktifkan sakelar 'Pengingat Penyaluran Infaq Vault' (izinkan akses notifikasi jika diminta sistem Android).",
                "Pilih frekuensi pengingat: Harian atau Mingguan (pilih hari pilihan seperti Jumat).",
                "Atur jam dan menit pengingat yang paling nyaman bagi Anda.",
                "Ketuk tombol uji notifikasi untuk memastikan pengingat berjalan lancar."
            )
        )

        IslamicQACard(
            question = "Apakah notifikasi akan tetap mengingatkan jika saldo Virtual Infaq Vault masih nol?",
            answer = "Pengingat dirancang untuk mendorong disiplin penyaluran saat terdapat dana infaq terakumulasi dan menjaga kebiasaan evaluasi berkala agar dana titipan tidak mengendap terlalu lama.",
            reference = "Adab Penyaluran Amanah & Kebijakan Pengingat Amanah Ledger"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Atur Jadwal Notifikasi di Pengaturan", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 28: ZAKAT PERNIAGAAN & PORTOFOLIO SAHAM SYARIAH
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicBusinessZakatGuideContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "HR. Abu Dawud No. 1562 (Hadits Samurah bin Jundab RA)",
            arabicText = "«أَمَّا بَعْدُ، فَإِنَّ رَسُولَ اللَّهِ ﷺ كَانَ يَأْمُرُنَا أَنْ نُخْرِجَ الصَّدَقَةَ مِنَ الَّذِي نُعِدُّ لِلْبَيْعِ»",
            translation = "Sesungguhnya Rasulullah SAW memerintahkan kami untuk mengeluarkan zakat dari barang-barang yang kami persiapkan untuk diperjualbelikan.",
            fiqhNote = "Zakat Tijarah (perniagaan) dihitung dari aset lancar usaha yang berputar (kas dagang + persediaan barang + piutang lancar − hutang jangka pendek jatuh tempo) dengan kadar 2,5% bila mencapai nisab 85 gram emas dan haul 1 tahun."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Instrumen Zakat Usaha & Efek Syariah (DES)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• Rumus Aset Bersih Dagang: (Modal Kerja Tunai + Nilai Stok Barang + Piutang Lancar) − Hutang Jatuh Tempo.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Aset Tetap Tidak Dizakati: Ruko, mesin produksi, dan kendaraan operasional tidak masuk hitungan objek zakat perniagaan.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Saham & Investasi Syariah: Evaluasi portofolio Daftar Efek Syariah (DES) agar bersih dari pendapatan non-halal melalui cleansing.", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Cara Menghisab Zakat Perniagaan & Saham:",
            steps = listOf(
                "Buka menu 'Zakat Perniagaan & Saham' di sidebar Mode Lengkap.",
                "Masukkan saldo kas usaha, nilai pasar stok barang siap jual, dan piutang dagang tertagih.",
                "Kurangi dengan kewajiban hutang operasional jangka pendek yang jatuh tempo saat haul.",
                "Bandingkan total aset bersih usaha terhadap nisab 85 gram emas untuk melihat kewajiban zakat 2,5%."
            )
        )

        IslamicQACard(
            question = "Apakah nilai stok barang dihitung berdasarkan harga beli atau harga jual?",
            answer = "Mayoritas ulama dan standar akuntansi zakat (AAOIFI & BAZNAS) menilai persediaan barang dagang berdasarkan harga jual pasar wajar pada tanggal jatuh tempo haul.",
            reference = "Standar Akuntansi Zakat AAOIFI No. 9 & Panduan BAZNAS"
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.BusinessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Zakat Perniagaan & Saham", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 29: PELACAK EMAS BAZNAS & AUDIT KEPATUHAN SYARIAH
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicGoldAndShariahAuditGuideContent(
    onOpenGoldTracker: () -> Unit,
    onOpenShariahAudit: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. Al-Baqarah: 275 & HR. Muslim No. 1015",
            arabicText = "﴿وَأَحَلَّ اللَّهُ الْبَيْعَ وَحَرَّمَ الرِّبَا﴾",
            translation = "Padahal Allah telah menghalalkan jual beli dan mengharamkan riba.",
            fiqhNote = "Menjaga kemurnian harta meliputi dua aspek: memastikan akurasi batas nisab (acuan harga emas) dan mengaudit seluruh catatan agar bebas dari unsur riba, gharar, dan maysir."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dua Modul Pengawasan: Pelacak Emas & Audit Syariah", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• Pelacak Emas & Nisab BAZNAS: Pantau dan perbarui harga emas murni per gram sesuai ketetapan BAZNAS terbaru agar ambang batas 85g selalu akurat.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Audit Kepatuhan Syariah (Health Check): Memindai transaksi buku kas untuk memeriksa kecukupan alokasi infaq, keseimbangan neraca, pemenuhan akad, dan deteksi kategori bermasalah (syubhat/riba).", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Menjalankan Audit & Pembaruan Nisab:",
            steps = listOf(
                "Buka 'Pelacak Emas & Nisab BAZNAS' secara berkala untuk menyesuaikan harga emas acuan terkini.",
                "Buka 'Audit Kepatuhan Syariah' untuk menjalankan pemeriksaan kesehatan finansial syariah secara menyeluruh.",
                "Tinjau skor kepatuhan dan rekomendasi perbaikan bila ada transaksi non-kasab atau dana Vault yang belum disalurkan."
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenGoldTracker,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Pelacak Emas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
            }
            Button(
                onClick = onOpenShariahAudit,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Audit Syariah", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 30: KUSTOMISASI MODUL, KATEGORI & LOGIKA SYARIAH
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicRulesAndCustomizationGuideContent(
    onOpenShariahRules: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "Kaidah Fiqhiyyah Al-Taisir",
            arabicText = "«المَشَقَّةُ تَجْلِبُ التَّيْسِيرَ»",
            translation = "Kesulitan itu mendatangkan kemudahan.",
            fiqhNote = "Setiap rumah tangga dan usaha memiliki kebutuhan pembukuan yang unik. Aplikasi membolehkan Anda menyesuaikan parameter hisab, menyembunyikan modul yang tidak dipakai, serta menambah pos pengeluaran pribadi."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("3 Lapisan Personalisasi Amanah Ledger", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("1. Pusat Kustomisasi Logika Syariah (Rules Studio): Sesuaikan parameter kadar zakat, pendekatan bruto/netto, batas nisab, dan ambang batas israf.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("2. Kustomisasi Modul & Fitur (24 Sakelar): Pilih preset Simpel/Perniagaan/Komprehensif atau aktifkan fitur tertentu saja sesuai kebutuhan.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("3. Kategori Pengeluaran Kustom: Tambah atau sembunyikan akun pos pengeluaran agar buku kas lebih ringkas dan akurat.", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenShariahRules,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Rules Studio", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Pengaturan Modul", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 31: ARSIP BUKTI STRUK & KUITANSI DIGITAL
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicReceiptArchiveGuideContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "QS. Al-Baqarah: 282",
            arabicText = "﴿ذَٰلِكُمْ أَقْسَطُ عِنْدَ اللَّهِ وَأَقْوَمُ لِلشَّهَادَةِ وَأَدْنَىٰ أَلَّا تَرْتَابُوا﴾",
            translation = "Yang demikian itu lebih adil di sisi Allah dan lebih menguatkan persaksian serta lebih dekat kepada tidak (menimbulkan) keraguanmu.",
            fiqhNote = "Menyimpan bukti transaksi berupa nota, faktur, atau kuitansi digital memperkuat akuntabilitas pembukuan pribadi, keluarga, maupun usaha."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galeri Bukti Struk & Kuitansi Terintegrasi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Text(
                    text = "Saat mencatat transaksi baru atau menyunting jurnal, Anda dapat melampirkan foto struk langsung dari kamera atau galeri tanpa izin penyimpanan berlebih. Semua lampiran terhimpun rapi di menu Arsip Bukti Struk.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }

        IslamicStepByStepCard(
            title = "Langkah Mengarsipkan Struk Belanja:",
            steps = listOf(
                "Buka form 'Catat Transaksi Baru' lalu pilih lampirkan foto struk/nota.",
                "Ambil gambar dengan kamera atau pilih foto nota melalui Photo Picker aman.",
                "Simpan transaksi; gambar akan tertaut otomatis ke entri jurnal terkait.",
                "Kunjungi menu 'Arsip Bukti Struk Transaksi' kapan saja untuk meninjau kembali dokumen belanja Anda."
            )
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Arsip Bukti Struk", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// TOPIC 32: WIDGET LAYAR UTAMA & TEMA TAMPILAN
// -------------------------------------------------------------------------------------------------
@Composable
fun TopicWidgetsAndThemeGuideContent(onTryAction: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        IslamicDalilCard(
            source = "HR. Muslim No. 91",
            arabicText = "«إِنَّ اللَّهَ جَمِيلٌ يُحِبُّ الْجَمَالَ»",
            translation = "Sesungguhnya Allah itu Maha Indah dan mencintai keindahan.",
            fiqhNote = "Tampilan yang nyaman dipandang, rapi, dan akses cepat dari layar utama ponsel membantu menjaga semangat konsistensi dalam beramal serta mencatat keuangan."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("3 Widget Layar Utama & Pilihan Tema Visual", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("• Widget Sedekah Subuh: Catat dan pantau streak sedekah fajar langsung dari layar depan ponsel.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Widget Target Ibadah: Pantau progres tabungan Qurban, Aqiqah, atau Umroh sekilas tanpa membuka aplikasi.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Widget Ringkasan Kas: Pantau arus pemasukan, pengeluaran, dan saldo Vault (mendukung sensor saldo).", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("• Tema & Skala Teks: Pilih tema Terang, Gelap, Sistem, atau Protokol Taktis (Gelap/Terang) dan atur ukuran huruf sesuai kenyamanan mata.", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        IslamicStepByStepCard(
            title = "Cara Memasang Widget & Mengubah Tema:",
            steps = listOf(
                "Tekan lama area kosong di layar utama (Home Screen) Android Anda, lalu pilih 'Widget'.",
                "Cari 'AmanahLedger Syariah' dan seret widget Sedekah Subuh, Target Ibadah, atau Ringkasan Kas ke layar.",
                "Untuk mengganti tema atau skala teks, buka 'Pengaturan Terpusat' → 'Tema & Privasi' atau 'Skala Teks'."
            )
        )

        Button(
            onClick = onTryAction,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Buka Pengaturan Tema & Tampilan", fontWeight = FontWeight.Bold)
        }
    }
}
