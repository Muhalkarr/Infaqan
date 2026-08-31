package com.example.core.faraidh

import kotlin.math.roundToInt

enum class DeceasedGender(val title: String) {
    PRIA("Laki-laki (Suami / Ayah)"),
    WANITA("Perempuan (Istri / Ibu)")
}

data class HeirInput(
    val deceasedGender: DeceasedGender = DeceasedGender.PRIA,
    val hasSpouse: Boolean = true, // Istri jika pewaris pria, Suami jika pewaris wanita
    val wifeCount: Int = 1, // Jika pewaris pria dan beristri > 1
    val sonCount: Int = 1,
    val daughterCount: Int = 1,
    val hasFather: Boolean = true,
    val hasMother: Boolean = true,
    val fullBrotherCount: Int = 0,
    val fullSisterCount: Int = 0,
    val funeralExpenses: Double = 0.0,
    val deceasedDebts: Double = 0.0,
    val wasiatBequest: Double = 0.0
)

data class FaraidhShare(
    val heirGroup: String,
    val heirCount: Int,
    val portionFractionText: String,
    val portionPercentage: Double,
    val totalNominal: Double,
    val perPersonNominal: Double,
    val dalilSyariah: String
)

data class FaraidhResult(
    val grossEstate: Double,
    val funeralCost: Double,
    val debtsPaid: Double,
    val wasiatPaid: Double,
    val netDistributableEstate: Double,
    val shares: List<FaraidhShare>,
    val remainingUndistributed: Double = 0.0,
    val explanationNotes: List<String> = emptyList()
)

object FaraidhEngine {

    fun calculate(grossEstate: Double, input: HeirInput): FaraidhResult {
        val funeral = input.funeralExpenses.coerceAtLeast(0.0)
        val debts = input.deceasedDebts.coerceAtLeast(0.0)
        val afterObligations = (grossEstate - funeral - debts).coerceAtLeast(0.0)

        // Wasiat maksimal 1/3 dari harta setelah hutang & jenazah
        val maxWasiat = afterObligations / 3.0
        val wasiat = input.wasiatBequest.coerceIn(0.0, maxWasiat)

        val netDistributable = (afterObligations - wasiat).coerceAtLeast(0.0)

        if (netDistributable <= 0.0) {
            return FaraidhResult(
                grossEstate = grossEstate,
                funeralCost = funeral,
                debtsPaid = debts,
                wasiatPaid = wasiat,
                netDistributableEstate = 0.0,
                shares = emptyList(),
                explanationNotes = listOf("Harta waris telah habis untuk pemenuhan hak jenazah, pelunasan hutang, atau wasiat.")
            )
        }

        val notes = mutableListOf<String>()
        val hasChildren = (input.sonCount + input.daughterCount) > 0
        val hasSons = input.sonCount > 0
        val sharesList = mutableListOf<FaraidhShare>()

        var allocatedFraction = 0.0

        // 1. Suami / Istri
        if (input.hasSpouse) {
            if (input.deceasedGender == DeceasedGender.WANITA) {
                // Suami
                val fraction = if (hasChildren) 0.25 else 0.50
                val fracText = if (hasChildren) "1/4" else "1/2"
                val dalil = if (hasChildren) "QS. An-Nisa: 12 (Suami mendapat 1/4 karena ada anak/keturunan)" else "QS. An-Nisa: 12 (Suami mendapat 1/2 karena tidak ada anak)"
                val nominal = netDistributable * fraction
                allocatedFraction += fraction
                sharesList.add(
                    FaraidhShare(
                        heirGroup = "Suami",
                        heirCount = 1,
                        portionFractionText = fracText,
                        portionPercentage = fraction * 100,
                        totalNominal = nominal,
                        perPersonNominal = nominal,
                        dalilSyariah = dalil
                    )
                )
            } else {
                // Istri
                val fraction = if (hasChildren) 0.125 else 0.25
                val fracText = if (hasChildren) "1/8" else "1/4"
                val dalil = if (hasChildren) "QS. An-Nisa: 12 (Istri mendapat 1/8 karena ada anak/keturunan)" else "QS. An-Nisa: 12 (Istri mendapat 1/4 karena tidak ada anak)"
                val nominal = netDistributable * fraction
                val count = input.wifeCount.coerceAtLeast(1)
                allocatedFraction += fraction
                sharesList.add(
                    FaraidhShare(
                        heirGroup = if (count > 1) "Istri ($count Orang)" else "Istri",
                        heirCount = count,
                        portionFractionText = fracText,
                        portionPercentage = fraction * 100,
                        totalNominal = nominal,
                        perPersonNominal = nominal / count,
                        dalilSyariah = dalil
                    )
                )
            }
        }

        // 2. Ibu
        if (input.hasMother) {
            val siblingsCount = input.fullBrotherCount + input.fullSisterCount
            val fraction = if (hasChildren || siblingsCount >= 2) (1.0 / 6.0) else (1.0 / 3.0)
            val fracText = if (hasChildren || siblingsCount >= 2) "1/6" else "1/3"
            val dalil = if (hasChildren || siblingsCount >= 2) "QS. An-Nisa: 11 (Ibu mendapat 1/6 karena ada anak atau 2+ saudara)" else "QS. An-Nisa: 11 (Ibu mendapat 1/3 karena tidak ada anak & minim saudara)"
            val nominal = netDistributable * fraction
            allocatedFraction += fraction
            sharesList.add(
                FaraidhShare(
                    heirGroup = "Ibu Kandung",
                    heirCount = 1,
                    portionFractionText = fracText,
                    portionPercentage = fraction * 100,
                    totalNominal = nominal,
                    perPersonNominal = nominal,
                    dalilSyariah = dalil
                )
            )
        }

        // 3. Ayah (Ashabul Furudh jika ada anak laki-laki)
        var fatherIsAshabah = false
        if (input.hasFather) {
            if (hasSons) {
                val fraction = 1.0 / 6.0
                val nominal = netDistributable * fraction
                allocatedFraction += fraction
                sharesList.add(
                    FaraidhShare(
                        heirGroup = "Ayah Kandung",
                        heirCount = 1,
                        portionFractionText = "1/6",
                        portionPercentage = fraction * 100,
                        totalNominal = nominal,
                        perPersonNominal = nominal,
                        dalilSyariah = "QS. An-Nisa: 11 (Ayah mendapat 1/6 sebagai fardh karena ada anak laki-laki)"
                    )
                )
            } else if (input.daughterCount > 0) {
                // Ayah dapat 1/6 fardh + Ashabah sisa jika ada anak perempuan saja
                val fraction = 1.0 / 6.0
                allocatedFraction += fraction
                fatherIsAshabah = true
            } else {
                // Tidak ada anak sama sekali, Ayah adalah Ashabah Bi Nafsih penuh
                fatherIsAshabah = true
            }
        }

        // 4. Anak-anak (Furudh atau Ashabah)
        if (hasSons) {
            // Anak Laki-laki menarik Anak Perempuan menjadi Ashabah Bil Ghair (2 : 1)
            val remainingFraction = (1.0 - allocatedFraction).coerceAtLeast(0.0)
            val totalParts = (input.sonCount * 2) + input.daughterCount
            val sonFraction = remainingFraction * (2.0 * input.sonCount / totalParts)
            val daughterFraction = if (input.daughterCount > 0) remainingFraction * (1.0 * input.daughterCount / totalParts) else 0.0

            val sonNominal = netDistributable * sonFraction
            sharesList.add(
                FaraidhShare(
                    heirGroup = "Anak Laki-Laki (${input.sonCount} Orang)",
                    heirCount = input.sonCount,
                    portionFractionText = "Ashabah (Sisa 2 Bagian)",
                    portionPercentage = sonFraction * 100,
                    totalNominal = sonNominal,
                    perPersonNominal = sonNominal / input.sonCount,
                    dalilSyariah = "QS. An-Nisa: 11 (Bagian seorang anak laki-laki sama dengan 2 bagian anak perempuan)"
                )
            )

            if (input.daughterCount > 0) {
                val daughterNominal = netDistributable * daughterFraction
                sharesList.add(
                    FaraidhShare(
                        heirGroup = "Anak Perempuan (${input.daughterCount} Orang)",
                        heirCount = input.daughterCount,
                        portionFractionText = "Ashabah Bil Ghair (1 Bagian)",
                        portionPercentage = daughterFraction * 100,
                        totalNominal = daughterNominal,
                        perPersonNominal = daughterNominal / input.daughterCount,
                        dalilSyariah = "QS. An-Nisa: 11 (Mewarisi sisa bersama anak laki-laki dengan rasio 1:2)"
                    )
                )
            }
            allocatedFraction = 1.0
        } else if (input.daughterCount > 0) {
            // Hanya ada anak perempuan (Ashabul Furudh)
            val fraction = if (input.daughterCount == 1) 0.50 else (2.0 / 3.0)
            val fracText = if (input.daughterCount == 1) "1/2" else "2/3"
            val dalil = if (input.daughterCount == 1) "QS. An-Nisa: 11 (Anak perempuan tunggal mendapat 1/2)" else "QS. An-Nisa: 11 (Dua atau lebih anak perempuan mendapat 2/3 dibagi rata)"
            val nominal = netDistributable * fraction
            allocatedFraction += fraction

            sharesList.add(
                FaraidhShare(
                    heirGroup = if (input.daughterCount == 1) "Anak Perempuan Tunggal" else "Anak Perempuan (${input.daughterCount} Orang)",
                    heirCount = input.daughterCount,
                    portionFractionText = fracText,
                    portionPercentage = fraction * 100,
                    totalNominal = nominal,
                    perPersonNominal = nominal / input.daughterCount,
                    dalilSyariah = dalil
                )
            )

            // Jika Ayah ada, Ayah mengambil sisa (1/6 fardh + Ashabah)
            if (input.hasFather && fatherIsAshabah) {
                val remainingFraction = (1.0 - allocatedFraction).coerceAtLeast(0.0)
                val totalFatherFraction = (1.0 / 6.0) + remainingFraction
                val totalFatherNominal = netDistributable * totalFatherFraction
                sharesList.add(
                    FaraidhShare(
                        heirGroup = "Ayah Kandung",
                        heirCount = 1,
                        portionFractionText = "1/6 + Ashabah Sisa",
                        portionPercentage = totalFatherFraction * 100,
                        totalNominal = totalFatherNominal,
                        perPersonNominal = totalFatherNominal,
                        dalilSyariah = "QS. An-Nisa: 11 & HR. Bukhari (Ayah mengambil 1/6 dan sisa harta setelah dzawil furudh)"
                    )
                )
                allocatedFraction = 1.0
            }
        } else {
            // Tidak ada anak sama sekali
            if (input.hasFather && fatherIsAshabah) {
                val remainingFraction = (1.0 - allocatedFraction).coerceAtLeast(0.0)
                val fatherNominal = netDistributable * remainingFraction
                sharesList.add(
                    FaraidhShare(
                        heirGroup = "Ayah Kandung",
                        heirCount = 1,
                        portionFractionText = "Ashabah Bi Nafsih (Sisa Penuh)",
                        portionPercentage = remainingFraction * 100,
                        totalNominal = fatherNominal,
                        perPersonNominal = fatherNominal,
                        dalilSyariah = "HR. Bukhari & Muslim: Berikan fardh kepada yang berhak, sisanya untuk kerabat laki-laki terdekat (Ayah)"
                    )
                )
                allocatedFraction = 1.0
            } else if (input.fullBrotherCount > 0 || input.fullSisterCount > 0) {
                // Saudara mewarisi jika tidak ada anak laki-laki & ayah
                val remainingFraction = (1.0 - allocatedFraction).coerceAtLeast(0.0)
                val totalParts = (input.fullBrotherCount * 2) + input.fullSisterCount
                if (totalParts > 0) {
                    if (input.fullBrotherCount > 0) {
                        val brFraction = remainingFraction * (2.0 * input.fullBrotherCount / totalParts)
                        val brNominal = netDistributable * brFraction
                        sharesList.add(
                            FaraidhShare(
                                heirGroup = "Saudara Laki-Laki Kandung (${input.fullBrotherCount} Orang)",
                                heirCount = input.fullBrotherCount,
                                portionFractionText = "Ashabah (2 Bagian)",
                                portionPercentage = brFraction * 100,
                                totalNominal = brNominal,
                                perPersonNominal = brNominal / input.fullBrotherCount,
                                dalilSyariah = "QS. An-Nisa: 176 (Kalalah - Saudara laki-laki mewarisi sisa)"
                            )
                        )
                    }
                    if (input.fullSisterCount > 0) {
                        val sisFraction = remainingFraction * (1.0 * input.fullSisterCount / totalParts)
                        val sisNominal = netDistributable * sisFraction
                        sharesList.add(
                            FaraidhShare(
                                heirGroup = "Saudara Perempuan Kandung (${input.fullSisterCount} Orang)",
                                heirCount = input.fullSisterCount,
                                portionFractionText = "Ashabah (1 Bagian)",
                                portionPercentage = sisFraction * 100,
                                totalNominal = sisNominal,
                                perPersonNominal = sisNominal / input.fullSisterCount,
                                dalilSyariah = "QS. An-Nisa: 176 (Saudara perempuan bersama saudara laki-laki dengan rasio 1:2)"
                            )
                        )
                    }
                }
                allocatedFraction = 1.0
            }
        }

        notes.add("Perhitungan faraidh disusun berdasarkan dalil syar'i Al-Qur'an (Surah An-Nisa: 11-12, 176) dan Sunnah Rasulullah SAW.")
        if (wasiat > 0) {
            notes.add("Wasiat pewaris senilai ${wasiat.toLong()} telah diprioritaskan sebelum pembagian tirkah (maksimal 1/3 dari harta bersih).")
        }

        return FaraidhResult(
            grossEstate = grossEstate,
            funeralCost = funeral,
            debtsPaid = debts,
            wasiatPaid = wasiat,
            netDistributableEstate = netDistributable,
            shares = sharesList,
            remainingUndistributed = (netDistributable * (1.0 - allocatedFraction)).coerceAtLeast(0.0),
            explanationNotes = notes
        )
    }
}
