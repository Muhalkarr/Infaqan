package com.example

import com.example.core.changelog.ChangelogRegistry
import com.example.core.feedback.FeedbackRepository
import com.example.core.feedback.FeedbackType
import com.example.core.feedback.UserFeedback
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun numpad_zero_zero_zero_key_matching_logic() {
        // Test that "000" is correctly distinguished from "0".."9"
        val key = "000"
        var matchedBranch = ""

        when (key) {
            "000" -> matchedBranch = "ZERO_ZERO_ZERO"
            in "0".."9" -> matchedBranch = "SINGLE_DIGIT"
            else -> matchedBranch = "OTHER"
        }

        assertEquals("ZERO_ZERO_ZERO", matchedBranch)
    }

    @Test
    fun numpad_zero_zero_zero_expression_logic() {
        var expression = "5"
        val lastChar = expression.lastOrNull()
        if (lastChar != null && lastChar.isDigit() && expression.length < 22) {
            expression += "000"
        }
        assertEquals("5000", expression)

        // If expression is "0", pressing 000 should keep it "0"
        var zeroExpression = "0"
        if (zeroExpression == "0" || zeroExpression.isEmpty()) {
            zeroExpression = "0"
        }
        assertEquals("0", zeroExpression)
    }

    @Test
    fun user_feedback_formatting_contains_muhalkarr() {
        val feedback = UserFeedback(
            type = FeedbackType.FEATURE_SUGGESTION,
            rating = 5,
            message = "Aplikasi sangat bermanfaat dan amanah!",
            contact = "08123456789"
        )
        val formatted = FeedbackRepository.formatMessageText(feedback)
        assertTrue(formatted.contains("Muhalkarr"))
        assertTrue(formatted.contains("★★★★★"))
        assertTrue(formatted.contains("Saran Fitur Baru"))
    }

    @Test
    fun changelog_registry_contains_versions_from_v0_to_v2_5() {
        val versions = ChangelogRegistry.versions
        assertTrue(versions.isNotEmpty())
        assertEquals("v2.5", versions.first().version)
        assertTrue(versions.first().isLatest)
        assertTrue(versions.any { it.version == "v0.1" })
        assertTrue(versions.first().highlights.any { it.contains("Muhalkarr") })
        assertTrue(versions.first().highlights.any { it.contains("Kamera") })
        assertTrue(versions.first().highlights.any { it.contains("000") })
    }

    @Test
    fun user_feedback_draft_and_revision_state() {
        val draftFeedback = UserFeedback(
            type = FeedbackType.CRITICISM_AND_SUGGESTION,
            rating = 4,
            message = "Draf masukan perbaikan tata kelola kas",
            status = com.example.core.feedback.FeedbackStatus.DRAFT,
            revisionCount = 0
        )
        assertEquals(com.example.core.feedback.FeedbackStatus.DRAFT, draftFeedback.status)
        assertEquals(0, draftFeedback.revisionCount)

        val updatedSent = draftFeedback.copy(
            status = com.example.core.feedback.FeedbackStatus.SENT,
            revisionCount = draftFeedback.revisionCount + 1,
            channelSent = "WhatsApp"
        )
        assertEquals(com.example.core.feedback.FeedbackStatus.SENT, updatedSent.status)
        assertEquals(1, updatedSent.revisionCount)
        assertEquals("WhatsApp", updatedSent.channelSent)
    }

    @Test
    fun rikaz_20_percent_khumus_calculation_accuracy() {
        val rikazValue = 100_000_000.0 // 100 Juta
        val zakatRate = 0.20 // 20%
        val zakatObligation = rikazValue * zakatRate
        val finderHalalPortion = rikazValue * (1.0 - zakatRate)

        assertEquals(20_000_000.0, zakatObligation, 0.001)
        assertEquals(80_000_000.0, finderHalalPortion, 0.001)
    }

    @Test
    fun custom_ruling_data_integrity() {
        val customRuling = com.example.core.grounding.CustomGroundedRuling(
            title = "Fatwa Hukum P2P Lending Syariah",
            category = "INVESTASI",
            authority = "DSN-MUI",
            referenceNumber = "Fatwa No. 117/DSN-MUI/II/2018",
            summary = "Layanan pembiayaan berbasis teknologi informasi wajib menggunakan akad syariah (wakalah bil ujrah atau mudharabah).",
            detailedRuling = "Tidak boleh mengandung unsur riba, gharar, maysir, dan kedhaliman.",
            dalilSource = "QS. Al-Maidah: 2",
            dalilArabic = "وَتَعَاوَنُوا عَلَى الْبِرِّ وَالتَّقْوَىٰ ۖ وَلَا تَعَاوَنُوا عَلَى الْإِثْمِ وَالْعُدْوَانِ",
            dalilTranslation = "Dan tolong-menolonglah kamu dalam kebajikan dan takwa...",
            calculationFormula = "N/A"
        )

        assertEquals("Fatwa Hukum P2P Lending Syariah", customRuling.title)
        assertEquals("INVESTASI", customRuling.category)
        assertTrue(customRuling.dalilArabic!!.contains("وَتَعَاوَنُوا"))
        assertNotNull(customRuling.id)
    }
}
