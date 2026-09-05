package com.example.core.database

import com.example.core.accounting.JournalEntry
import com.example.core.accounting.JournalLine
import com.example.core.budget.BudgetAllocation
import com.example.core.calendar.HijriCalendarEngine
import com.example.core.ibadah.IbadahGoal
import com.example.core.ibadah.IbadahGoalType
import com.example.core.infaq.AsnafCategory
import com.example.core.infaq.InfaqDistributionRecord
import com.example.core.infaq.SedekahSubuhState
import com.example.core.infaq.SedekahSubuhStreakEngine
import com.example.core.qardh.QardhInstallment
import com.example.core.qardh.QardhRecord
import com.example.core.qardh.QardhStatus
import com.example.core.qardh.QardhType
import com.example.core.receipt.ReceiptAttachment
import com.example.core.receipt.ReceiptType
import com.example.core.scheduler.RecurringFrequency
import com.example.core.scheduler.RecurringTransaction
import com.example.core.scheduler.RecurringType
import com.example.core.wallet.WalletAccount
import com.example.core.wallet.WalletType
import com.example.core.zakat.ZakatFitrahFamilyCalculation
import com.example.core.zakat.ZakatPerniagaanCalculation
import com.example.core.zakat.ZakatProfesiCalculation
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AmanahDummyDataGenerator {

    private const val ONE_DAY_MILLIS = 86400000L

    fun getSampleWallets(): List<WalletAccount> = listOf(
        WalletAccount(
            id = "w_cash",
            name = "Kas Utama / Dompet Tunai",
            type = WalletType.CASH,
            institutionName = "Uang Tunai Fisik",
            accountNumber = "KAS-FISIK",
            linkedAccountId = "acc_cash",
            colorHex = 0xFF10B981, // Emerald
            isDefault = true,
            notes = "Uang kas fisik di dompet untuk kebutuhan harian"
        ),
        WalletAccount(
            id = "w_bsi",
            name = "Rekening Tabungan BSI Wadiah",
            type = WalletType.BANK_SYARIAH,
            institutionName = "Bank Syariah Indonesia",
            accountNumber = "7144-8899-22",
            linkedAccountId = "acc_bank",
            colorHex = 0xFF0D9488, // Teal
            isDefault = false,
            notes = "Rekening utama gaji dan perputaran operasional tanpa bunga"
        ),
        WalletAccount(
            id = "w_emas",
            name = "Tabungan Emas Batangan (25 gr)",
            type = WalletType.GOLD_ASSET,
            institutionName = "Antam LM Syariah",
            accountNumber = "ANTAM-9921",
            linkedAccountId = "acc_gold",
            colorHex = 0xFFF59E0B, // Amber Gold
            isDefault = false,
            notes = "Logam mulia batangan untuk perlindungan aset dan haul zakat mal"
        ),
        WalletAccount(
            id = "w_haji",
            name = "Tabungan Porsi Haji & Umroh",
            type = WalletType.SPECIAL_SAVINGS,
            institutionName = "BSI Haji Indonesia",
            accountNumber = "7988-1122-33",
            linkedAccountId = "acc_bank",
            colorHex = 0xFF3B82F6, // Blue
            isDefault = false,
            notes = "Dana khusus persiapan ibadah ke Baitullah"
        ),
        WalletAccount(
            id = "w_qris",
            name = "Dompet Digital LinkAja Syariah",
            type = WalletType.E_WALLET,
            institutionName = "LinkAja Syariah",
            accountNumber = "0812-8877-6655",
            linkedAccountId = "acc_bank",
            colorHex = 0xFFEC4899, // Pink
            isDefault = false,
            notes = "QRIS syariah untuk sedekah subuh dan transaksi cepat"
        )
    )

    fun getSampleBudgets(): List<BudgetAllocation> = listOf(
        BudgetAllocation(
            id = "b_living",
            accountId = "acc_living",
            categoryName = "Biaya Hidup & Pangan",
            monthlyLimit = 4000000.0,
            iconKey = "shopping",
            alertThresholdPercent = 0.8
        ),
        BudgetAllocation(
            id = "b_education",
            accountId = "acc_education",
            categoryName = "Pendidikan & Majelis Dakwah",
            monthlyLimit = 1200000.0,
            iconKey = "school",
            alertThresholdPercent = 0.8
        ),
        BudgetAllocation(
            id = "b_health",
            accountId = "acc_health",
            categoryName = "Kesehatan & Pengobatan",
            monthlyLimit = 800000.0,
            iconKey = "medical",
            alertThresholdPercent = 0.8
        ),
        BudgetAllocation(
            id = "b_transport",
            accountId = "acc_transport",
            categoryName = "Transportasi & Bensin",
            monthlyLimit = 1600000.0,
            iconKey = "commute",
            alertThresholdPercent = 0.8
        ),
        BudgetAllocation(
            id = "b_utility",
            accountId = "acc_utility",
            categoryName = "Tagihan Listrik, Air & Pulsa",
            monthlyLimit = 1400000.0,
            iconKey = "bolt",
            alertThresholdPercent = 0.8
        ),
        BudgetAllocation(
            id = "b_infaq",
            accountId = "acc_disbursed",
            categoryName = "Infaq, Sedekah & Tabungan Ibadah",
            monthlyLimit = 1200000.0,
            iconKey = "volunteer_activism",
            alertThresholdPercent = 0.8
        ),
        BudgetAllocation(
            id = "b_other",
            accountId = "acc_other_exp",
            categoryName = "Kebutuhan Pelengkap & Rekreasi Halal",
            monthlyLimit = 1000000.0,
            iconKey = "category",
            alertThresholdPercent = 0.8
        )
    )

    fun getSampleIbadahGoals(): List<IbadahGoal> = listOf(
        IbadahGoal(
            id = "g_qurban",
            type = IbadahGoalType.QURBAN_KAMBING,
            title = "Qurban 1 Ekor Kambing Idul Adha",
            targetAmount = 3500000.0,
            currentAccumulated = 2800000.0,
            targetMonthsRemaining = 2,
            targetHijriYearMonth = "10 Dzulhijjah 1448 H",
            notes = "Niat qurban sunnah muakkadah untuk keluarga mukmin",
            linkedWalletId = "w_bsi",
            isCompleted = false
        ),
        IbadahGoal(
            id = "g_umrah",
            type = IbadahGoalType.PAKET_UMRAH_MUKMIN,
            title = "Tabungan Umrah Berkah Bersama Pasangan",
            targetAmount = 32000000.0,
            currentAccumulated = 18500000.0,
            targetMonthsRemaining = 8,
            targetHijriYearMonth = "Rajab 1448 H",
            notes = "Paket ibadah umrah 9 hari bersama biro perjalanan sunnah terpercaya",
            linkedWalletId = "w_haji",
            isCompleted = false
        ),
        IbadahGoal(
            id = "g_wakaf",
            type = IbadahGoalType.WAKAF_PRODUKTIF,
            title = "Wakaf Pembebasan Lahan Pesantren Tahfidz",
            targetAmount = 5000000.0,
            currentAccumulated = 3000000.0,
            targetMonthsRemaining = 4,
            targetHijriYearMonth = "Ramadhan 1448 H",
            notes = "Wakaf jariyah atas nama kedua orang tua",
            linkedWalletId = "w_bsi",
            isCompleted = false
        ),
        IbadahGoal(
            id = "g_aqiqah",
            type = IbadahGoalType.CUSTOM_IBADAH,
            title = "Tabungan Aqiqah & Tasyakuran Anak",
            targetAmount = 4000000.0,
            currentAccumulated = 4000000.0,
            targetMonthsRemaining = 0,
            targetHijriYearMonth = "Safar 1448 H",
            notes = "Alhamdulillah dana aqiqah telah tercapai penuh dan siap ditunaikan",
            linkedWalletId = "w_cash",
            isCompleted = true
        )
    )

    fun getSampleQardhRecords(): List<QardhRecord> {
        val now = System.currentTimeMillis()
        val qardhPiutangId = "q_piutang_sample"
        val qardhHutangId = "q_hutang_sample"

        val piutangInstallments = listOf(
            QardhInstallment(
                id = "qi_sample_1",
                qardhId = qardhPiutangId,
                amount = 1000000.0,
                dateMillis = now - 20 * ONE_DAY_MILLIS,
                fromWalletId = "w_bsi",
                note = "Angsuran ke-1 transfer via BSI Mobile",
                receipt = ReceiptAttachment(
                    title = "Bukti Transfer Cicilan 1",
                    receiptType = ReceiptType.BANK_TRANSFER,
                    merchantName = "BSI Mobile",
                    referenceNumber = "TRF-BSI-99102",
                    amount = 1000000.0,
                    isDigitalVerified = true
                )
            ),
            QardhInstallment(
                id = "qi_sample_2",
                qardhId = qardhPiutangId,
                amount = 1000000.0,
                dateMillis = now - 5 * ONE_DAY_MILLIS,
                fromWalletId = "w_bsi",
                note = "Angsuran ke-2 via transfer",
                receipt = ReceiptAttachment(
                    title = "Bukti Transfer Cicilan 2",
                    receiptType = ReceiptType.BANK_TRANSFER,
                    merchantName = "BSI Mobile",
                    referenceNumber = "TRF-BSI-99540",
                    amount = 1000000.0,
                    isDigitalVerified = true
                )
            )
        )

        val hutangInstallments = listOf(
            QardhInstallment(
                id = "qi_sample_3",
                qardhId = qardhHutangId,
                amount = 2500000.0,
                dateMillis = now - 10 * ONE_DAY_MILLIS,
                fromWalletId = "w_bsi",
                note = "Pengembalian tahap 1 via transfer BSI",
                receipt = ReceiptAttachment(
                    title = "Kuitansi Pengembalian Pinjaman",
                    receiptType = ReceiptType.OFFICIAL_INVOICE,
                    merchantName = "Keluarga Besar",
                    referenceNumber = "KWT-RIDWAN-01",
                    amount = 2500000.0,
                    isDigitalVerified = true
                )
            )
        )

        return listOf(
            QardhRecord(
                id = qardhPiutangId,
                type = QardhType.PIUTANG_SAYA,
                counterpartyName = "Ahmad Fauzi (Rekan Usaha)",
                contactInfo = "0813-8899-7711",
                totalAmount = 3000000.0,
                remainingAmount = 1000000.0,
                startDateMillis = now - 35 * ONE_DAY_MILLIS,
                dueDateMillis = now + 25 * ONE_DAY_MILLIS,
                notes = "Pinjaman kebajikan qardh hasan tanpa bunga untuk tambahan modal warung berkah",
                witnessName = "Ust. Syarif Hidayat (Saksi Akad)",
                witnessContact = "0812-7766-5544",
                agreementTerms = "Pengembalian bertahap 3 kali angsuran tanpa tambahan biaya sepeser pun",
                installments = piutangInstallments,
                status = QardhStatus.SEBAGIAN_LUNAS,
                walletId = "w_bsi"
            ),
            QardhRecord(
                id = qardhHutangId,
                type = QardhType.HUTANG_SAYA,
                counterpartyName = "Paman Ridwan",
                contactInfo = "0815-4433-2211",
                totalAmount = 5000000.0,
                remainingAmount = 2500000.0,
                startDateMillis = now - 25 * ONE_DAY_MILLIS,
                dueDateMillis = now + 35 * ONE_DAY_MILLIS,
                notes = "Talangan amanah tanpa riba untuk renovasi atap rumah keluarga",
                witnessName = "H. Mansur (Kerabat)",
                witnessContact = "0811-3322-1100",
                agreementTerms = "Dikembalikan utuh tanpa bunga dalam tempo 4 bulan",
                installments = hutangInstallments,
                status = QardhStatus.SEBAGIAN_LUNAS,
                walletId = "w_bsi"
            )
        )
    }

    fun getSampleRecurringTransactions(): List<RecurringTransaction> = listOf(
        RecurringTransaction(
            id = "rec_gaji",
            title = "Ujrah / Gaji Pokok Bulanan",
            type = RecurringType.INCOME,
            amount = 15000000.0,
            categoryAccountId = "acc_salary",
            assetAccountId = "acc_bank",
            frequency = RecurringFrequency.MONTHLY,
            dayOfMonthOrWeek = 25,
            customInfaqRate = 0.05,
            isActive = true,
            autoExecute = true,
            note = "Penghasilan utama nafkah halal keluarga dengan alokasi otomatis infaq 5%"
        ),
        RecurringTransaction(
            id = "rec_spp",
            title = "SPP Sekolah Tahfidz Qur'an",
            type = RecurringType.EXPENSE,
            amount = 450000.0,
            categoryAccountId = "acc_education",
            assetAccountId = "acc_bank",
            frequency = RecurringFrequency.MONTHLY,
            dayOfMonthOrWeek = 5,
            isActive = true,
            autoExecute = true,
            note = "Biaya pendidikan syariah & hafalan Al-Qur'an anak"
        ),
        RecurringTransaction(
            id = "rec_yatim",
            title = "Santunan Bulanan Yatim & Dhuafa",
            type = RecurringType.EXPENSE,
            amount = 250000.0,
            categoryAccountId = "acc_disbursed",
            assetAccountId = "acc_cash",
            frequency = RecurringFrequency.MONTHLY,
            dayOfMonthOrWeek = 1,
            isActive = true,
            autoExecute = true,
            note = "Infaq rutin awal bulan untuk anak asuh binaan"
        ),
        RecurringTransaction(
            id = "rec_wifi",
            title = "Tagihan Internet & Listrik Rumah",
            type = RecurringType.EXPENSE,
            amount = 350000.0,
            categoryAccountId = "acc_utility",
            assetAccountId = "acc_bank",
            frequency = RecurringFrequency.MONTHLY,
            dayOfMonthOrWeek = 10,
            enableRoundUp = true,
            roundUpStep = 5000.0,
            isActive = true,
            autoExecute = true,
            note = "Kebutuhan operasional listrik dan komunikasi dakwah keluarga"
        )
    )

    fun getSampleSedekahSubuhState(): SedekahSubuhState {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val historyMap = mutableMapOf<String, Double>()
        val now = System.currentTimeMillis()

        // 14 hari berturut-turut hingga hari ini
        for (i in 0 until 14) {
            val date = Date(now - i * ONE_DAY_MILLIS)
            val key = sdf.format(date)
            historyMap[key] = 20000.0
        }

        return SedekahSubuhStreakEngine.calculateStreak(historyMap)
    }

    fun getSampleJournalEntries(): List<JournalEntry> {
        val now = System.currentTimeMillis()

        fun makeEntry(
            id: String,
            daysAgo: Int,
            desc: String,
            type: String,
            lines: List<JournalLine>,
            receipt: ReceiptAttachment? = null
        ): JournalEntry {
            val date = Date(now - daysAgo * ONE_DAY_MILLIS)
            val hijri = HijriCalendarEngine.fromGregorian(date)
            return JournalEntry(
                id = id,
                gregorianDate = date,
                hijriYear = hijri.year,
                hijriMonth = hijri.month,
                hijriDay = hijri.day,
                description = desc,
                transactionType = type,
                lines = lines,
                receiptAttachment = receipt
            )
        }

        return listOf(
            // 1. Saldo Awal Harta
            makeEntry(
                id = "j_sample_01",
                daysAgo = 25,
                desc = "Saldo Pembukuan Kas Awal & Simpanan Emas Mukmin",
                type = "INFLOW",
                lines = listOf(
                    JournalLine("acc_bank", 82500000.0, 0.0),
                    JournalLine("acc_cash", 2500000.0, 0.0),
                    JournalLine("acc_gold", 33750000.0, 0.0),
                    JournalLine("acc_equity", 0.0, 118750000.0)
                )
            ),
            // 2. Gaji Bulanan (Kasab)
            makeEntry(
                id = "j_sample_02",
                daysAgo = 14,
                desc = "Penerimaan Ujrah / Gaji Pokok Bulanan (Auto Infaq 5%)",
                type = "INFLOW",
                lines = listOf(
                    JournalLine("acc_bank", 15000000.0, 0.0),
                    JournalLine("acc_salary", 0.0, 15000000.0),
                    JournalLine("acc_disbursed", 750000.0, 0.0),
                    JournalLine("acc_vault", 0.0, 750000.0)
                ),
                receipt = ReceiptAttachment(
                    title = "Slip Gaji Perusahaan Syariah",
                    receiptType = ReceiptType.OFFICIAL_INVOICE,
                    merchantName = "PT Berkah Amanah Solusi",
                    referenceNumber = "SLIP-2024-02",
                    amount = 15000000.0,
                    isDigitalVerified = true
                )
            ),
            // 3. Laba Usaha Dagang Halal (Kasab)
            makeEntry(
                id = "j_sample_03",
                daysAgo = 11,
                desc = "Bagi Hasil Penjualan Busana Muslim & Madu Herbal (Infaq 5%)",
                type = "INFLOW",
                lines = listOf(
                    JournalLine("acc_bank", 4500000.0, 0.0),
                    JournalLine("acc_trade", 0.0, 4500000.0),
                    JournalLine("acc_disbursed", 225000.0, 0.0),
                    JournalLine("acc_vault", 0.0, 225000.0)
                )
            ),
            // 4. Belanja Kebutuhan Pokok Sembako (Expense - Dharuriyyat)
            makeEntry(
                id = "j_sample_04",
                daysAgo = 9,
                desc = "Belanja Bahan Pangan & Sayur Organik Keluarga (Supermarket Halal)",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_living", 1850000.0, 0.0),
                    JournalLine("acc_bank", 0.0, 1850000.0)
                ),
                receipt = ReceiptAttachment(
                    title = "Struk Belanja Supermarket Halal",
                    receiptType = ReceiptType.STORE_RECEIPT,
                    merchantName = "Superindo Halal Mart",
                    referenceNumber = "POS-99218",
                    amount = 1850000.0,
                    isDigitalVerified = true
                )
            ),
            // 4b. Belanja Bulanan Beras & Lauk Dapur (Expense - Dharuriyyat)
            makeEntry(
                id = "j_sample_04b",
                daysAgo = 8,
                desc = "Belanja Bulanan Beras Ramos, Minyak & Lauk Dapur Halal",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_living", 1400000.0, 0.0),
                    JournalLine("acc_bank", 0.0, 1400000.0)
                )
            ),
            // 5. Pembayaran Listrik PLN & PDAM (Expense - Hajiyyat)
            makeEntry(
                id = "j_sample_05",
                daysAgo = 8,
                desc = "Pembayaran Token Listrik PLN & Tagihan PDAM Rumah",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_utility", 850000.0, 0.0),
                    JournalLine("acc_bank", 0.0, 850000.0)
                ),
                receipt = ReceiptAttachment(
                    title = "Struk Pembayaran PLN & PDAM",
                    receiptType = ReceiptType.DIGITAL_PAYMENT,
                    merchantName = "PLN Mobile / BSI",
                    referenceNumber = "PLN-881920",
                    amount = 850000.0,
                    isDigitalVerified = true
                )
            ),
            // 6. Transportasi & BBM (Expense - Hajiyyat)
            makeEntry(
                id = "j_sample_06",
                daysAgo = 7,
                desc = "Isi BBM Pertamax & Servis Berkala Kendaraan Harian",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_transport", 950000.0, 0.0),
                    JournalLine("acc_cash", 0.0, 950000.0)
                )
            ),
            // 6b. Kuota Internet & Paket Komunikasi (Expense - Hajiyyat)
            makeEntry(
                id = "j_sample_06b",
                daysAgo = 6,
                desc = "Langganan Paket Internet WiFi Rumah & Komunikasi Dakwah",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_utility", 450000.0, 0.0),
                    JournalLine("acc_bank", 0.0, 450000.0)
                )
            ),
            // 6c. Iuran Lingkungan & Operasional (Expense - Hajiyyat)
            makeEntry(
                id = "j_sample_06c",
                daysAgo = 6,
                desc = "Iuran Kebersihan RT, Keamanan & Pengelolaan Lingkungan",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_transport", 300000.0, 0.0),
                    JournalLine("acc_cash", 0.0, 300000.0)
                )
            ),
            // 7. Hadiah & Hibah Sukarela (Non-Kasab)
            makeEntry(
                id = "j_sample_07",
                daysAgo = 5,
                desc = "Pemberian Hibah Syukuran dari Keluarga (Infaq 10%)",
                type = "INFLOW",
                lines = listOf(
                    JournalLine("acc_cash", 2000000.0, 0.0),
                    JournalLine("acc_gift", 0.0, 2000000.0),
                    JournalLine("acc_disbursed", 200000.0, 0.0),
                    JournalLine("acc_vault", 0.0, 200000.0)
                )
            ),
            // 8. SPP Sekolah Tahfidz Qur'an & Kitab Kajian (Expense - Dharuriyyat)
            makeEntry(
                id = "j_sample_08",
                daysAgo = 4,
                desc = "SPP Sekolah Tahfidz Qur'an Anak & Pembelian Kitab Kajian",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_education", 650000.0, 0.0),
                    JournalLine("acc_cash", 0.0, 650000.0)
                )
            ),
            // 9. Kesehatan & Medis Keluarga (Expense - Dharuriyyat)
            makeEntry(
                id = "j_sample_09",
                daysAgo = 3,
                desc = "Beli Vitamin Habbatus Sauda, Madu & Pemeriksaan Medis Keluarga",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_health", 450000.0, 0.0),
                    JournalLine("acc_bank", 0.0, 450000.0)
                )
            ),
            // 10. Penyaluran Infaq Vault 1 (Infaq & Tabungan Syariah)
            makeEntry(
                id = "j_sample_10",
                daysAgo = 2,
                desc = "Penyaluran Infaq: Keluarga Ibu Maryam (Fakir & Miskin)",
                type = "INFAQ_PAYOUT",
                lines = listOf(
                    JournalLine("acc_vault", 500000.0, 0.0),
                    JournalLine("acc_bank", 0.0, 500000.0)
                )
            ),
            // 11. Penyaluran Infaq Vault 2 (Infaq & Tabungan Syariah)
            makeEntry(
                id = "j_sample_11",
                daysAgo = 1,
                desc = "Penyaluran Infaq: BAZNAS Program Dakwah Pelosok (Fisabilillah)",
                type = "INFAQ_PAYOUT",
                lines = listOf(
                    JournalLine("acc_vault", 300000.0, 0.0),
                    JournalLine("acc_cash", 0.0, 300000.0)
                )
            ),
            // 12. Sedekah Subuh Rutin & Santunan Dhuafa (Infaq & Tabungan Syariah)
            makeEntry(
                id = "j_sample_12",
                daysAgo = 1,
                desc = "Sedekah Subuh Istiqomah & Santunan Dhuafa Sekitar",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_disbursed", 100000.0, 0.0),
                    JournalLine("acc_cash", 0.0, 100000.0)
                )
            ),
            // 13. Silaturahmi & Rekreasi Halal (Expense - Tahsiniyyat)
            makeEntry(
                id = "j_sample_13",
                daysAgo = 1,
                desc = "Jamuan Silaturahmi Keluarga & Kuliner Halal Akhir Pekan",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_other_exp", 450000.0, 0.0),
                    JournalLine("acc_bank", 0.0, 450000.0)
                )
            ),
            // 14. Pakaian Muslim & Kebutuhan Halal (Expense - Tahsiniyyat)
            makeEntry(
                id = "j_sample_14",
                daysAgo = 0,
                desc = "Pembelian Busana Muslim Syar'i & Perlengkapan Sholat Keluarga",
                type = "EXPENSE",
                lines = listOf(
                    JournalLine("acc_other_exp", 250000.0, 0.0),
                    JournalLine("acc_cash", 0.0, 250000.0)
                )
            )
        )
    }

    fun getSampleInfaqDistributions(): List<InfaqDistributionRecord> {
        val now = System.currentTimeMillis()
        val date1 = Date(now - 2 * ONE_DAY_MILLIS)
        val date2 = Date(now - 1 * ONE_DAY_MILLIS)
        val hijri1 = HijriCalendarEngine.fromGregorian(date1)
        val hijri2 = HijriCalendarEngine.fromGregorian(date2)

        return listOf(
            InfaqDistributionRecord(
                id = "dist_sample_001",
                amount = 500000.0,
                recipientName = "Keluarga Ibu Maryam",
                asnafCategory = AsnafCategory.FAKIR_MISKIN,
                distributionDate = date1,
                hijriDateString = "${hijri1.day} ${hijri1.monthName} ${hijri1.year} H",
                sourceAccountId = "acc_bank",
                programName = "Santunan Pangan Sembako Dhuafa",
                receiptNumber = "INV-VAULT-20240220-001",
                notes = "Pemberian paket sembako beras, minyak, dan uang santunan pendidikan untuk 2 anak yatim",
                isVerified = true
            ),
            InfaqDistributionRecord(
                id = "dist_sample_002",
                amount = 300000.0,
                recipientName = "BAZNAS Pusat",
                asnafCategory = AsnafCategory.FISABILILLAH,
                distributionDate = date2,
                hijriDateString = "${hijri2.day} ${hijri2.monthName} ${hijri2.year} H",
                sourceAccountId = "acc_cash",
                programName = "Da'i Pedalaman Nusantara",
                receiptNumber = "INV-VAULT-20240222-002",
                notes = "Dukungan operasional dakwah dan penyaluran mushaf Al-Qur'an ke daerah pelosok",
                isVerified = true
            )
        )
    }

    fun getSampleZakatProfesi(): ZakatProfesiCalculation = ZakatProfesiCalculation(
        monthlyGrossSalary = 15000000.0,
        monthlyAllowanceAndOther = 2000000.0,
        monthlyEssentialExpenses = 5500000.0,
        monthlyDebtInstallment = 1000000.0,
        isNetMethod = true,
        ricePricePerKg = 15000.0
    )

    fun getSampleZakatPerniagaan(): ZakatPerniagaanCalculation = ZakatPerniagaanCalculation(
        currentCashInBusiness = 25000000.0,
        inventoryValue = 95000000.0,
        receivableCollectible = 15000000.0,
        shortTermPayable = 12000000.0,
        goldPricePerGram = 1350000.0,
        isLunarCalendar = true
    )

    fun getSampleZakatFitrah(): ZakatFitrahFamilyCalculation = ZakatFitrahFamilyCalculation(
        familyMembersCount = 4,
        ricePricePerKg = 16000.0
    )
}
