package io.github.saksa84.chinamapquiz.game

import io.github.saksa84.chinamapquiz.data.GeoCatalogLoader
import io.github.saksa84.chinamapquiz.data.LoadResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AnswerCheckerTest {

    private fun asset(path: String) = File("src/main/assets/$path").readText()

    /** Vraies données : les tests vérifient aussi que les alias livrés sont bien acceptés. */
    private val checker = AnswerChecker(
        (GeoCatalogLoader.load(asset("geo/provinces.json"), asset("geo/regions.json"), asset("geo/islands.json")) as LoadResult.Success).catalog,
    )

    // --- Normalisation ---

    @Test
    fun normalize_ignoresCaseSpacesAndPunctuation() {
        assertEquals("sichuan", AnswerNormalizer.normalize("  SiChuan "))
        assertEquals("neimongol", AnswerNormalizer.normalize("Nei Mongol"))
        assertEquals("xian", AnswerNormalizer.normalize("Xi'an"))
        assertEquals("xian", AnswerNormalizer.normalize("Xi’an"))
        assertEquals("hongkong", AnswerNormalizer.normalize("Hong-Kong"))
    }

    @Test
    fun normalize_removesTonesAndAccents() {
        assertEquals("sichuan", AnswerNormalizer.normalize("Sìchuān"))
        assertEquals("guangdong", AnswerNormalizer.normalize("Guǎngdōng"))
        assertEquals("lu", AnswerNormalizer.normalize("Lǚ"))
        assertEquals("ilesparacel", AnswerNormalizer.normalize("Îles Paracel"))
    }

    @Test
    fun normalize_ofOnlyPunctuation_isEmpty() {
        assertEquals("", AnswerNormalizer.normalize(" -' "))
    }

    // --- Validation sur les vraies données ---

    @Test
    fun mainNameIsAccepted_inAnyCaseOrWithTones() {
        assertTrue(checker.isCorrect("CN-SC", "sichuan"))
        assertTrue(checker.isCorrect("CN-SC", "Sìchuān"))
        assertTrue(checker.isCorrect("CN-NM", "neimongol"))
    }

    @Test
    fun declaredAliasesAreAccepted() {
        assertTrue(checker.isCorrect("CN-XZ", "Tibet"))
        assertTrue(checker.isCorrect("CN-NM", "Inner Mongolia"))
        assertTrue(checker.isCorrect("CN-HK", "Xianggang"))
        assertTrue(checker.isCorrect("CN-HK", "hongkong"))
        assertTrue(checker.isCorrect("CN-MO", "Macau"))
        assertTrue(checker.isCorrect("IS-PARACEL", "îles Paracel"))
        assertTrue(checker.isCorrect("CN-BJ", "Pékin"))
    }

    @Test
    fun typosAreNotAccepted() {
        assertFalse(checker.isCorrect("CN-SC", "Sichaun"))
        assertFalse(checker.isCorrect("CN-SC", "Sichua"))
    }

    @Test
    fun shanxiAndShaanxiAreNeverConfused() {
        assertTrue(checker.isCorrect("CN-SX", "Shanxi"))
        assertFalse(checker.isCorrect("CN-SX", "Shaanxi"))
        assertTrue(checker.isCorrect("CN-SN", "Shaanxi"))
        assertFalse(checker.isCorrect("CN-SN", "Shanxi"))
    }

    @Test
    fun anotherPlaceNameIsWrong() {
        assertFalse(checker.isCorrect("CN-SC", "Yunnan"))
    }

    @Test
    fun emptyOrUnknownIsWrong() {
        assertFalse(checker.isCorrect("CN-SC", ""))
        assertFalse(checker.isCorrect("CN-SC", "  "))
        assertFalse(checker.isCorrect("CN-XX", "Sichuan"))
    }

    @Test
    fun capitalsAreNotAcceptedAsProvinceNames() {
        assertFalse(checker.isCorrect("CN-SC", "Chengdu"))
    }
}
