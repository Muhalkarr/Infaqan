# Fitur Pencatatan Rezeki Non-Tunai (In-Kind Blessings) & Kalkulasi Infaq Syukur

Solusi pencatatan langsung untuk penerimaan rezeki berupa makanan, barang hadiah, bingkisan, maupun fasilitas manfaat tanpa harus mencatat ganda (masuk lalu keluar) di dompet kas, dilengkapi kalkulasi otomatis dan penawaran instan penunaian Infaq Syukur.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> Berdasarkan konfirmasi pengguna pada sesi tanya-jawab sebelumnya, berikut adalah keputusan inti yang menjadi fondasi fitur:
> 1. **Dampak Saldo**: Rezeki non-tunai dicatat sebagai entitas nilai manfaat (*memo off-balance*) dan **TIDAK mengubah saldo uang kas riil** di kantong pengguna.
> 2. **Kalkulasi Infaq Syukur**: Sistem secara otomatis menghitung saran nominal infaq syukur (berdasarkan persentase seperti 2.5% atau 5%) dan langsung memberikan opsi 1-klik untuk mencatat infaq dari kantong kas riil jika pengguna berkenan.
> 3. **Penempatan UX**: Tab khusus pada formulir transaksi (`AddTransactionScreen`) dan kartu ringkasan di beranda utama (`DashboardScreen`).

---

## 1. Overview & Core Concept

- **Problem Statement**: Sering kali seorang mukmin menerima rezeki berupa makanan ditraktir, bingkisan, hadiah, atau fasilitas cuma-cuma yang bernilai ekonomis. Dalam pembukuan syariah berpasangan (*double-entry*) biasa, mencatatnya membutuhkan langkah ganda yang merepotkan (mencatat estimasi harga sebagai pemasukan kas lalu segera mencatatnya sebagai pengeluaran konsumsi agar kas riil tidak fiktif).
- **Core Value**: Memberikan wadah pencatatan langsung atas rezeki non-tunai sebagai bentuk syukur (*tahadduts bin ni'mah*), mencatat estimasi nilai manfaatnya, menghitung total rezeki hakiki yang didapat dari Allah SWT, serta mempermudah pengeluaran infaq syukur atas nikmat tersebut.

---

## 2. User Experience & Visual Design

### 2.1 Alur Pengguna (User Flows)
1. **Pencatatan Cepat di Formulir Transaksi**:
   - Pengguna membuka layar **Catat Transaksi** (`AddTransactionScreen`).
   - Terdapat tab transaksi baru: **Rezeki Non-Tunai** (bersebelahan dengan Pemasukan, Pengeluaran, Transfer).
   - Pengguna memasukkan:
     - Nama Rezeki / Manfaat (misal: *"Traktir Makan Siang Sate Padang"*, *"Bingkisan Kurma & Madu"*)
     - Pemberi / Sumber (misal: *"Rekan Kerja"*, *"Keluarga"*, *"Hamba Allah"*)
     - Kategori Manfaat (*Makanan & Minuman*, *Barang & Hadiah*, *Jasa & Tumpangan*, *Voucher & Manfaat Lain*)
     - Estimasi Nilai Manfaat (Rp)
     - Persentase Saran Infaq Syukur (Pilihan cepat: 2.5%, 5%, 10%, atau Kustom)
     - Catatan / Niat Syukur
2. **Dialog Konfirmasi Infaq Syukur Instan**:
   - Setelah menekan tombol **"Simpan Rezeki Non-Tunai"**, sistem menampilkan lembar/dialog apresiasi syukur:
     - *"Alhamdulillah! Nilai manfaat rezeki: Rp 100.000. Saran Infaq Syukur (5%): Rp 5.000. Apakah Anda ingin menunaikan infaq syukur sekarang dari kantong kas?"*
     - Jika Pengguna memilih **"Tunaikan Infaq Sekarang"**: Pengguna memilih kantong kas (misal *Dompet Tunai* / *Bank Syariah*), dan transaksi infaq otomatis tercatat resmi ke jurnal kas dengan tag *"Infaq Syukur atas [Nama Rezeki]"*.
     - Jika Pengguna memilih **"Nanti Saja"**: Catatan rezeki tetap tersimpan di arsip rezeki non-tunai.
3. **Kartu Dashboard & Rekapitulasi (Dashboard Widget)**:
   - Di `DashboardScreen`, ditampilkan kartu **"Rekap Rezeki Non-Tunai & Syukur"**:
     - Total estimasi nilai manfaat yang diterima bulan ini (misal: `Rp 420.000`).
     - Jumlah pemberian/hadiah yang tercatat (misal: `5 Kali`).
     - Total Infaq Syukur yang sudah disalurkan atas rezeki non-tunai tersebut.
     - Riwayat 3 penerimaan terakhir dan tombol *"Catat Rezeki Non-Tunai"*.

---

## 3. Technical Architecture & Data Strategy

### 3.1 Diagram Arsitektur & Alur Data

```
┌────────────────────────────────────────────────────────────────────────┐
│                        UI / Presentation Layer                         │
├───────────────────────────────┬────────────────────────────────────────┤
│     AddTransactionScreen      │            DashboardScreen             │
│   [Tab: Rezeki Non-Tunai]     │   [Kartu: Rekap Rezeki Non-Tunai]      │
└───────────────┬───────────────┴────────────────────┬───────────────────┘
                │                                    │
                ▼                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        AmanahLedgerViewModel                           │
│  - recordRezekiNonTunai(...)                                           │
│  - executeInfaqSyukurFromWallet(...)                                   │
│  - rezekiNonTunaiList: StateFlow<List<RezekiNonTunaiRecord>>           │
└───────────────────────────────┬────────────────────────────────────────┘
                                │
                                ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      AmanahDatabase (Room SQLite)                      │
├────────────────────────────────────────────────────────────────────────┤
│  RezekiNonTunaiEntity                JournalEntryEntity (Kas Riil)     │
│  - id: String (UUID)                 - lines: Debit Kas / Kredit Infaq │
│  - title: String                     - (Hanya dibuat jika pengguna     │
│  - giverName: String                   memilih tunaikan infaq syukur)  │
│  - category: String                                                    │
│  - estimatedValue: Double                                              │
│  - suggestedInfaqAmount: Double                                        │
│  - isPaidInfaq: Boolean                                                │
│  - paidJournalEntryId: String?                                         │
└────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Struktur Entitas Data

```kotlin
data class RezekiNonTunaiRecord(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val giverName: String,
    val category: RezekiCategory, // FOOD_BEVERAGE, GIFT_GOODS, SERVICE_RIDE, VOUCHER_PERK
    val estimatedValue: Double,
    val suggestedInfaqRate: Double = 0.05,
    val suggestedInfaqAmount: Double = estimatedValue * suggestedInfaqRate,
    val isPaidInfaq: Boolean = false,
    val paidJournalEntryId: String? = null,
    val dateMillis: Long = System.currentTimeMillis(),
    val hijriDateString: String = "",
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)
```

---

## 4. Rencana Kerja Bertahap (Execution Steps)

1. **Model & Lapisan Basis Data (Room SQLite)**:
   - Buat `RezekiNonTunaiEntity` dan enum `RezekiCategory` di `com.example.core.rezeki`.
   - Buat `RezekiNonTunaiDao` dengan operasi query reaktif `Flow`, insert, delete, dan update status infaq.
   - Daftarkan entitas dan DAO ke `AmanahDatabase` (versi Room dinaikkan ke versi berikutnya dengan migrasi otomatis).
2. **Logika Bisnis di ViewModel**:
   - Tambahkan state dan koleksi data `rezekiNonTunaiList` di `AmanahLedgerViewModel`.
   - Implementasikan fungsi `recordRezekiNonTunai()` dan `tunaikanInfaqSyukur()`.
3. **Penyempurnaan Formulir Transaksi (`AddTransactionScreen`)**:
   - Tambahkan tab pilihan tipe: *Rezeki Non-Tunai*.
   - Bangun formulir input spesifik rezeki non-tunai (nama pemberian, nama pemberi, estimasi nilai, kategori, dan pilihan rate infaq syukur).
   - Tambahkan modal/bottom sheet penawaran instan pengeluaran infaq syukur dari kantong kas.
4. **Widget Ringkasan di Beranda (`DashboardScreen`)**:
   - Tambahkan kartu ringkasan rezeki non-tunai di tab beranda utama.
   - Tampilkan akumulasi nilai manfaat bulan ini, status infaq syukur, dan riwayat terkini.
5. **Verifikasi & Pengujian**:
   - Jalankan `compile_applet` dan `gradle :app:testDebugUnitTest` untuk memastikan kestabilan kompilasi dan fungsi aplikasi 100% bebas error.
