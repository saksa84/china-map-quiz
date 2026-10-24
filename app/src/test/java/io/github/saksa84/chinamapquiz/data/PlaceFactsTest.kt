package io.github.saksa84.chinamapquiz.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Fiches « En savoir plus » : règles de validation, puis vraies données livrées. */
class PlaceFactsTest {

    private fun asset(path: String) = File("src/main/assets/$path").readText()

    private val catalog = (GeoCatalogLoader.load(*AssetGeoRepository.CATALOG_FILES.map { asset("geo/$it") }.toTypedArray()) as LoadResult.Success).catalog

    private fun file(vararg facts: String) = """{"schemaVersion":1,"origin":"test","facts":[${facts.joinToString(",")}]}"""

    private fun errorsOf(text: String): List<FactsError> =
        when (val result = PlaceFactsLoader.load(text, catalog)) {
            is FactsResult.Invalid -> result.errors
            else -> throw AssertionError("Erreurs attendues, obtenu : $result")
        }

    @Test
    fun validFacts_areIndexedById() {
        val result = PlaceFactsLoader.load(file("""{"id":"CN-SC","populationMillions":83.7,"populationYear":2020,"dish":"Mapo tofu"}"""), catalog)
        val facts = (result as FactsResult.Success).facts
        assertEquals("Mapo tofu", facts["CN-SC"]!!.dish)
        assertEquals(false, facts["CN-SC"]!!.reviewed)
    }

    @Test
    fun unknownOrDuplicateIds_areRejected() {
        val sichuan = """{"id":"CN-SC","dish":"Mapo tofu"}"""
        assertEquals(listOf(FactsError.DuplicateId("CN-SC")), errorsOf(file(sichuan, sichuan)))
        assertEquals(listOf(FactsError.UnknownId("CN-XX")), errorsOf(file("""{"id":"CN-XX","dish":"?"}""")))
    }

    @Test
    fun emptyOrTooLongFacts_areRejected() {
        assertEquals(listOf(FactsError.Empty("CN-SC")), errorsOf(file("""{"id":"CN-SC"}""")))
        val long = "a".repeat(PlaceFactsLoader.MAX_TEXT_LENGTH + 1)
        assertEquals(listOf(FactsError.TooLong("CN-SC", long)), errorsOf(file("""{"id":"CN-SC","history":"$long"}""")))
        val threeAnecdotes = """{"id":"CN-SC","anecdotes":["a","b","c"]}"""
        assertEquals(listOf(FactsError.TooLong("CN-SC", "3 anecdotes")), errorsOf(file(threeAnecdotes)))
    }

    @Test
    fun impossibleNumbers_areRejected() {
        listOf(
            """{"id":"CN-SC","populationMillions":-1,"populationYear":2020}""",
            """{"id":"CN-SC","populationMillions":83.7}""",
            """{"id":"CN-SC","populationYear":2020,"dish":"x"}""",
            """{"id":"CN-SC","areaKm2":0}""",
        ).forEach { assertEquals(it, listOf(FactsError.InvalidNumber("CN-SC")), errorsOf(file(it))) }
    }

    @Test
    fun realFacts_coverEveryProvincialUnit() {
        val facts = (PlaceFactsLoader.load(asset("geo/facts.json"), catalog) as FactsResult.Success).facts
        val provincial = catalog.inCategory(Category.PROVINCIAL).map { it.id }.toSet()
        assertEquals(provincial, provincial.intersect(facts.keys))
        provincial.forEach { id ->
            val f = facts[id]!!
            assertTrue("$id : population", f.populationMillions != null && f.areaKm2 != null)
            assertTrue("$id : langues, histoire, économie, plat", listOf(f.languages, f.history, f.economy, f.dish).all { it != null })
        }
        // Repères faciles à vérifier : la province la plus peuplée et la plus étendue.
        assertEquals("CN-GD", provincial.maxByOrNull { facts[it]!!.populationMillions!! })
        assertEquals("CN-XJ", provincial.maxByOrNull { facts[it]!!.areaKm2!! })
    }
}
