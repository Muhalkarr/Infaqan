# Peninjauan Multi-Kantong Rekening, Penghapusan Duplikasi Harta, & Fitur Saldo Awal Kantong

Memperbaiki pemetaan akun buku besar pada fitur Multi-Kantong Rekening agar tidak ada lagi duplikasi saldo harta antara kantong bertipe Lainnya/Khusus dengan Uang Tunai (Cash), menggabungkan kantong perbankan ke kelompok buku besar bersama sesuai preferensi pengguna, menyediakan opsi pemilihan dan pembuatan akun buku besar tertaut secara mandiri, serta **menambahkan fitur untuk menentukan dan mengedit nominal saldo awal saat menambah/mengedit kantong baru**.

### User Review & Critical Decisions

> [!IMPORTANT]
> Berdasarkan konfirmasi dan permintaan revisi pengguna:
> - **Fitur Menentukan & Mengedit Nominal Saldo Awal (Added/Revised)**: Pada formulir tambah atau edit kantong (`WalletFormDialog`), ditambahkan kolom input nominal saldo awal (lengkap dengan format mata uang Rupiah dinamis dan chip nominal cepat). Saat kantong baru disimpan dengan saldo awal > 0, sistem otomatis mencatat jurnal pembukuan seimbang (Debit: Akun Aset Kantong, Kredit: `acc_equity` / Modal Awal Harta Bersih).
> - **Pemilihan Akun Buku Besar Mandiri (Confirmed)**: Pengguna dapat memilih akun buku besar aset yang ditautkan ke kantong atau membuat akun aset baru langsung dari formulir kantong.
> - **Kelompok Rekening Perbankan (Confirmed)**: Kantong bertipe Bank Syariah, Bank Konvensional, dan Tabungan Khusus disatukan dalam kelompok buku besar perbankan (`acc_bank`), namun pengguna tetap dapat mengarahkan ke akun buku besar lain bila dikehendaki.
> - **Isolasi Penuh Kantong Lainnya/Khusus (Confirmed)**: Kantong bertipe Lainnya/Khusus (`WalletType.OTHER`) tidak lagi ditautkan ke `acc_cash`, melainkan dialokasikan ke akun aset terisolasi `acc_other_asset` (Kode 106: "Aset & Kantong Khusus Lainnya"), sehingga saldo kas fisik tidak terduplikasi.

---

### 1. Overview & Core Concept

- **Problem Statement**:
  1. *Duplikasi Saldo*: Kantong bertipe **Lainnya / Khusus** (`WalletType.OTHER`) sebelumnya ditautkan ke `acc_cash`, menyebabkan nilai saldo uang tunai fisik terduplikasi.
  2. *Ketiadaan Pengisian Saldo Awal Langsung*: Saat pengguna membuat kantong baru (misalnya membuka rekening bank baru atau memasukkan saldo e-wallet yang baru dibuat), pengguna tidak memiliki cara langsung untuk memasukkan saldo awal kantong tersebut dari dialog kantong, melainkan harus berpindah ke formulir transaksi manual.
- **Key Value**:
  1. *Bebas Duplikasi*: Nilai kas fisik dan kantong lainnya terisolasi 100%.
  2. *Pengalaman Onboarding Kantong Mulus*: Pengguna dapat langsung memasukkan saldo yang sudah ada di kantong barunya (misal: Rp 500.000) dan saldo tersebut seketika tampil di kartu kantong.
  3. *Kepatuhan Pembukuan Berpasangan (Double-Entry)*: Saldo awal dicatat sah secara akuntansi syariah ke akun ekuitas awal (`acc_equity`), menjaga persamaan dasar akuntansi `Aktiva = Pasiva (Kewajiban + Ekuitas)`.

---

### 2. User Experience & Visual Design

#### Key User Flows

```
[Formulir Tambah / Edit Kantong]
  ├── Nama Kantong, Lembaga/Bank, Nomor Rekening
  ├── Pilih Tipe Kantong (Tunai, Bank Syariah, Bank Konven, E-Wallet, Emas, Tabungan Khusus, Lainnya)
  │     └── Pemetaan otomatis default aman:
  │           ├── Tunai -> acc_cash
  │           ├── Bank Syariah / Konven / Tabungan Khusus -> acc_bank (Kelompok Perbankan)
  │           ├── E-Wallet -> acc_ewallet
  │           ├── Emas -> acc_gold
  │           └── Lainnya / Khusus -> acc_other_asset [TERISOLASI]
  ├── Dropdown "Tautkan Akun Buku Besar (Ledger Account)":
  │     ├── Menampilkan daftar akun aset yang ada beserta preview saldo
  │     └── Opsi "+ Buat Akun Aset Baru" (Nama Akun & Kode)
  ├── [BARU] Input "Saldo Awal / Nominal Saldo Kantong":
  │     ├── Text field nominal dengan awalan "Rp" dan pemformatan ribuan otomatis
  │     ├── Quick chips penambah nominal: [+100 rb] [+500 rb] [+1 jt] [+5 jt] [Reset 0]
  │     └── Keterangan edukatif: "Saldo awal akan dicatat otomatis ke pembukuan modal awal (Ekuitas)"
  └── Simpan:
        ├── Menyimpan data entitas kantong
        └── Jika nominal saldo awal > 0: Menghasilkan jurnal pembukuan saldo awal otomatis
```

#### Visual Identity & UI Components
- **Input Field Saldo Awal**: OutlinedTextField bernuansa Emerald dengan ikon `AccountBalanceWallet`, format ribuan rapi, dan tombol clear.
- **Row Chip Nominal Cepat**: Chip responsif dengan elevasi lembut untuk input instan.
- **Badge Akun Tertaut**: Indikator kode dan nama akun pada setiap kartu kantong di `MultiWalletScreen` agar pengguna langsung memahami ke mana kantong tersebut terhubung.

---

### 3. Key Product Decisions & Trade-Offs

- **Keputusan 1: Penjurnalan Saldo Awal via Ekuitas (`acc_equity`)**
  - *Pendekatan*: Saldo awal kantong yang diinput dicatat sebagai jurnal `INFLOW` dengan Debit akun aset kantong dan Kredit `acc_equity` ("Modal Awal / Harta Bersih").
  - *Alasan*: Menjamin neraca tetap seimbang tanpa memicu perhitungan zakat/infaq sebagai kasab aktif baru (karena ini merupakan modal awal, bukan pemasukan gaji berjalan).
- **Keputusan 2: Penyesuaian Saldo pada Mode Edit Kantong**
  - *Pendekatan*: Saat mengedit kantong yang sudah memiliki saldo, form menampilkan saldo saat ini dan memungkinkan pengguna memperbaruinya dengan mencatat penyesuaian selisih ke `acc_equity`.
  - *Alasan*: Memberikan kendali penuh kepada pengguna bila terdapat rekonsiliasi saldo riil tanpa kerumitan teknis.
- **Keputusan 3: Isolasi Penuh Bawaan `acc_other_asset` (Kode 106)**
  - *Pendekatan*: Akun bawaan `acc_other_asset` disediakan secara permanen di daftar akun standar dan basis data.
  - *Alasan*: Mengeliminasi akar masalah duplikasi saldo pada kantong bertipe Lainnya/Khusus.

---

### 4. Technical Architecture & Data Strategy

#### Architecture & Component Diagram

```
┌───────────────────────────────────────────────────────────────┐
│                    MultiWalletScreen                          │
│  ┌───────────────────────┐       ┌─────────────────────────┐  │
│  │ WalletCard (List)     │       │ WalletFormDialog        │  │
│  │ - Saldo Terkini       │       │ - Pilih Tipe Kantong    │  │
│  │ - Badge Akun Tertaut  │       │ - Dropdown Akun Aset    │  │
│  └───────────┬───────────┘       │ - [BARU] Input Saldo    │  │
│              │                   │ - + Buat Akun Baru      │  │
│              │                   └────────────┬────────────┘  │
└──────────────┼────────────────────────────────┼───────────────┘
               │                                │
               ▼                                ▼
┌───────────────────────────────────────────────────────────────┐
│                   AmanahLedgerViewModel                       │
│  - getWalletBalance(walletId): Double                         │
│  - addWalletWithInitialBalance(wallet, initialBalance)        │
│  - adjustWalletBalance(walletId, targetBalance)               │
│  - addNewAssetAccount(name, code, desc)                       │
│  - migrateLegacyOtherWallets()                                │
└──────────────────────────────┬────────────────────────────────┘
                               │
                               ▼
┌───────────────────────────────────────────────────────────────┐
│              Room Database & Persistence                      │
│  - AccountEntity (Menyimpan acc_other_asset & custom)         │
│  - WalletAccountEntity (Menyimpan linkedAccountId)            │
│  - JournalEntryEntity (Mencatat jurnal saldo awal ke equity)  │
└───────────────────────────────────────────────────────────────┘
```

#### Detailed Changes Planned
1. **`AmanahLedgerViewModel.kt`**:
   - Daftarkan akun standar `Account("acc_other_asset", "106", "Aset & Kantong Khusus Lainnya", AccountCategory.ASSET, ...)` pada `defaultAccounts`.
   - Tambahkan fungsi `saveWalletWithInitialBalance(wallet: WalletAccount, initialBalance: Double, isEdit: Boolean)`:
     - Jika kantong baru dan `initialBalance > 0`: tambahkan jurnal `INFLOW` saldo awal debit ke `wallet.linkedAccountId` dan kredit ke `acc_equity`.
     - Jika edit kantong dan saldo disesuaikan: hitung selisih nominal dan catat jurnal penyesuaian ke `acc_equity`.
   - Tambahkan fungsi `addNewAssetAccount(name: String, code: String, desc: String): String` untuk membuat akun aset custom bila pengguna memilih opsi membuat akun baru.
   - Tambahkan migrasi otomatis di startup untuk memeriksa kantong `WalletType.OTHER` lama yang tertaut ke `acc_cash` dan mengarahkannya ke `acc_other_asset`.
2. **`MultiWalletScreen.kt`**:
   - Di `WalletFormDialog`, tambahkan field input nominal `initialBalance` dengan parser angka bersih dan chip penambah nominal cepat (+100rb, +500rb, +1jt, +5jt).
   - Saat tipe kantong diubah, tautkan `WalletType.OTHER` ke `"acc_other_asset"`, bukan `"acc_cash"`.
   - Tambahkan pemilih akun buku besar tertaut (`ExposedDropdownMenu`) yang menampilkan seluruh akun aset beserta saldo real-time-nya, serta tombol opsi `+ Buat Akun Aset Baru`.
   - Kirim `initialBalance` ke ViewModel saat tombol Simpan ditekan.
3. **`AddTransactionScreen.kt`**:
   - Pastikan akun aset `acc_other_asset` dan akun custom baru dapat dipilih dalam dropdown akun tujuan/sumber dana.
4. **Verifikasi & Kompilasi**:
   - Jalankan `compile_applet` dan unit test untuk memverifikasi keakuratan pembukuan, ketiadaan duplikasi nilai harta, dan kelancaran alur pembuatan kantong.
