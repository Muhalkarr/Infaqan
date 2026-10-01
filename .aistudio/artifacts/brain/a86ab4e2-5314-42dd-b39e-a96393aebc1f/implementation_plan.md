# Rencana Implementasi Terpadu: Integrasi Emas BAZNAS, Hisab Saham & Perniagaan, Audit Syariah, & Laporan Zakat Tahunan Resmi

Dokumen arsitektur komprehensif untuk peningkatan fitur Amanah Ledger tahap berikutnya berdasarkan preferensi yang telah dikonfirmasi oleh pengguna:

1. **Integrasi Harga Emas Real-time & Nisab SK BAZNAS Terkini**:
   - Pelacak harga emas dinamis dengan **grafik tren harga historis interaktif**.
   - **Tabel konversi moneter syariah klasik**: Dinar Emas (4.25 gram emas 24k) dan Dirham Perak (2.975 gram perak murni).
   - **Peringatan cerdas (*Nisab Alert Banner*)**: Indikator otomatis saat total likuiditas kas pengguna menyentuh atau melampaui batas ambang nisab 85 gram emas.
2. **Kalkulator Zakat Perniagaan (*'Urudh At-Tijarah*) & Portofolio Saham Syariah**:
   - **Zakat Perniagaan**: Formulir hisab standar DSN-MUI (Aktiva Lancar: Kas + Piutang Lancar + Nilai Persediaan Dagang - Hutang Jangka Pendek Jatuh Tempo) x 2.5%.
   - **Zakat Saham Syariah**: Skrining emiten syariah (DES OJK / ISSI), hisab porsi *market value* atau laba dividen, serta fitur pembersihan dividen non-halal (*cleansing to infaq*).
3. **Audit Kepatuhan Syariah Mutasi Transaksi & Pengingat Ibadah Keuangan**:
   - **Audit Kepatuhan Otomatis (*Shariah Transaction Health Check*)**: Pemeriksaan otomatis terhadap transaksi harian untuk mendeteksi transaksi terindikasi syubhat, denda bunga (*riba nasi'ah*), atau pos belanja yang melampaui batas wajar (*israf/tabdzir*).
   - **Pengingat Siklus Haul Hijriah & Jadwal Sedekah Subuh**: Notifikasi terjadwal saat masa haul 354 hari mendekati jatuh tempo dan pengingat harian sedekah subuh di waktu fajar.
4. **Laporan Hisab Zakat Tahunan Resmi PDF & Analisis Rasio Kedermawanan**:
   - **Cetak Laporan Zakat Format Resmi Standar BAZNAS**: Dokumen PDF komprehensif memuat ringkasan neraca hisab, rincian per kantong kas, status haul, dan tanda tangan pernyataan muzakki.
   - **Grafik Analisis Rasio Kedermawanan & Tren Infaq Bulanan**: Visualisasi metrik *Spiritual Liquidity Index* (SLI) dan rasio kedermawanan terhadap pendapatan kasab.
   - **Pemisahan Zakat per Kantong Kas**: Rincian porsi zakat yang dibebankan pada masing-masing rekening/kantong kas aktif secara proporsional.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Keputusan dan preferensi pengguna yang telah dikonfirmasi:
> - **Cakupan Fitur Prioritas**: Integrasi harga emas real-time BAZNAS, simulasi zakat perniagaan & saham syariah, pengingat haul & sedekah subuh, serta audit kepatuhan syariah mutasi kas.
> - **Penyajian Data Emas**: Menggunakan grafik tren harga emas, tabel konversi Dinar-Dirham klasik, dan kartu pengingat status nisab otomatis.
> - **Format Pelaporan**: Ekspor PDF resmi format hisab BAZNAS, grafik tren kedermawanan bulanan, dan hisab zakat yang dipisah per masing-masing kantong kas.

---

## 1. Arsitektur Komponen & Alur Data

```
┌────────────────────────────────────────────────────────────────────────┐
│                          Amanah Application                            │
├───────────────────────────────────┬────────────────────────────────────┤
│   Modul Emas & Nisab BAZNAS       │  Kalkulator Zakat Bisnis & Saham   │
│   (GoldNisabTrackerScreen)        │   (BusinessZakatCalculatorScreen)  │
├───────────────────────────────────┼────────────────────────────────────┤
│ • Harga Emas Live & Input Manual  │ • Zakat Perniagaan ('Urudh Tijarah)│
│ • Grafik Tren Harga Emas (Canvas) │   - Kas + Piutang + Stok - Hutang  │
│ • Tabel Konversi Dinar & Dirham   │ • Zakat Saham Syariah & Reksadana  │
│ • Indikator Melampaui Nisab       │   - Skrining DES OJK & Cleansing   │
├───────────────────────────────────┴────────────────────────────────────┤
│              Audit Kepatuhan Syariah & Pengingat Ibadah                │
│                   (ShariahComplianceAuditEngine)                       │
├───────────────────────────────────┬────────────────────────────────────┤
│ • Audit Mutasi Harian Otomatis    │ • Pengingat Haul Kalender Hijriah  │
│ • Deteksi Potensi Riba & Israf    │ • Pengingat Sedekah Subuh Harian   │
├───────────────────────────────────┴────────────────────────────────────┤
│            Laporan Zakat Tahunan Resmi & Multi-Pocket Kas              │
│                (AnnualZakatReportPdfGenerator)                         │
├────────────────────────────────────────────────────────────────────────┤
│ • Laporan PDF Resmi Standar BAZNAS (Kop Surat, Neraca, Tanda Tangan)   │
│ • Grafik Spiritual Liquidity Index (SLI) & Tren Kedermawanan          │
│ • Alokasi Kewajiban Zakat Proporsional per Kantong Kas                 │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Rincian Teknis & Tahapan Implementasi

### 2.1 Modul Pelacak Emas, Nisab BAZNAS & Konversi Dinar-Dirham
1. **Model Data Emas & Log Harga**:
   - `GoldPriceRecord`: Mencatat riwayat harga emas harian (tanggal Masehi/Hijriah, harga per gram, sumber rujukan SK BAZNAS).
   - Sinkronisasi otomatis ke state global `AmanahLedgerState.goldPricePerGram`.
2. **Grafik Tren Harga Emas Interaktif (Canvas)**:
   - Visualisasi kurva fluktuasi harga emas 7 hari / 30 hari terakhir dengan indikator harga tertinggi, terendah, dan rata-rata.
3. **Tabel Konversi Moneter Syariah Klasik**:
   - 1 Dinar = 4.25 gram emas 24k (Nisab Zakat Maal = 20 Dinar = 85 gram emas).
   - 1 Dirham = 2.975 gram perak murni (Nisab Perak = 200 Dirham = 595 gram perak).
   - Kalkulator timbangan emas fisik (batangan, dinar, perhiasan) ke mata uang rupiah.
4. **Smart Nisab Status Banner**:
   - Mengevaluasi saldo riil seluruh kantong kas aktif terhadap ambang batas Rp (85 x harga emas).
   - Menampilkan status *"Alhamdulillah, Saldo Melewati Nisab"* atau *"Menuju Batas Nisab (X% Tercapai)"*.

### 2.2 Modul Zakat Perniagaan & Portofolio Saham Syariah
1. **Kalkulator Zakat Perniagaan (*'Urudh At-Tijarah*)**:
   - Mengikuti kaidah Fiqih DSN-MUI No. 3/2003 & Muktamar Tarjih:
   - Rumus: `[Uang Kas Lancar Bisnis + Piutang Lancar Tagih + Nilai Pasar Persediaan Barang] - [Hutang Usaha Jatuh Tempo] x 2.5%`.
   - Mengambil saldo kantong kas bisnis secara otomatis.
2. **Kalkulator Zakat Saham & Investasi Syariah**:
   - Mode Trading Aktif: Dihitung dari total nilai pasar portofolio akhir tahun + dividen x 2.5%.
   - Mode Investasi Jangka Panjang: Dihitung dari dividen bersih yang diterima.
   - Fitur *Pembersihan Dividen Non-Halal* (*Tathhirul Amwal*): Menghitung persentase pendapatan non-halal emiten untuk disalurkan ke pos Infaq/Kebajikan.

### 2.3 Mesin Audit Kepatuhan Syariah (*Shariah Compliance Audit Engine*)
1. **Audit Mutasi Transaksi Kas Harian**:
   - Mendeteksi transaksi dengan kata kunci mencurigakan atau deskripsi yang berpotensi memuat bunga/denda (seperti denda keterlambatan paylater, bunga pinjaman konvensional).
   - Mendeteksi transaksi pengeluaran konsumtif yang melonjak melebihi alokasi anggaran bulanan (*israf/tabdzir*).
   - Memberikan lencana peringatan: `Kepatuhan Penuh (Syariah Compliant)`, `Perlu Ditinjau (Syubhat)`, atau `Harus Disucikan (Non-Halal)`.
2. **Pengingat Siklus Haul & Jadwal Sedekah Subuh**:
   - Penghitung mundur hari menuju genapnya 1 tahun Hijriah (354 hari) sejak saldo pertama kali mencapai nisab.
   - Jadwal pengingat harian sedekah subuh berbasis kalender lokal.

### 2.4 Generator Dokumen PDF Laporan Zakat Tahunan BAZNAS & Analisis SLI
1. **Laporan Hisab Zakat Resmi (PDF)**:
   - Header resmi berorientasi syariah dengan kop dokumen hisab.
   - Rincian identitas muzakki, tahun buku Hijriah dan Masehi.
   - Tabel neraca harta simpanan, emas, kas usaha, piutang, dan pengurang hutang.
   - Rincian pemisahan porsi zakat per masing-masing kantong kas (*Dompet Utama, Kas Simpanan, Bank Syariah, Kas Bisnis*).
   - Lembar pernyataan ikhlas berzakat dan kolom tanda tangan penyerahan amil BAZNAS.
2. **Grafik Analisis Rasio Kedermawanan & Tren Infaq**:
   - Visualisasi persentase pengeluaran sosial (*Spiritual Liquidity Index*) per bulan.
   - Metrik komparasi: Pertumbuhan harta kasab vs Pertumbuhan infaq/sedekah.

---

## 3. Rencana Pengujian & Verifikasi

1. **Pengujian Unit (`gradle :app:testDebugUnitTest`)**:
   - Validasi rumus konversi Dinar/Dirham dan ambang nisab emas 85 gram.
   - Validasi rumus zakat perniagaan dan pemisahan per kantong kas.
   - Validasi logika audit kepatuhan syariah transaksi.
2. **Verifikasi Kompilasi (`compile_applet`)**:
   - Memastikan seluruh modul UI Jetpack Compose, Canvas charting, dan PDF generator lulus kompilasi tanpa error.
3. **Pengujian Antarmuka Pengguna**:
   - Memeriksa kelancaran navigasi layar emas & nisab, kalkulator saham/perniagaan, audit mutasi harian, dan ekspor laporan PDF BAZNAS.
