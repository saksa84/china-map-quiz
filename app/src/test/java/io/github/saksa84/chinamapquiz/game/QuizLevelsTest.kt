package io.github.saksa84.chinamapquiz.game

import io.github.saksa84.chinamapquiz.data.Category
import io.github.saksa84.chinamapquiz.data.GeoCatalogLoader
import io.github.saksa84.chinamapquiz.data.LoadResult
import io.github.saksa84.chinamapquiz.map.MapGeometry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Vérifie la vraie configuration `assets/game/levels.json` contre les vraies données. */
class QuizLevelsTest {

    private fun asset(path: String) = File("src/main/assets/$path").readText()

    private val config = LevelsConfig.parse(asset("game/levels.json"))
    private val catalog = (GeoCatalogLoader.load(
        asset("geo/provinces.json"), asset("geo/regions.json"), asset("geo/islands.json"),
    ) as LoadResult.Success).catalog
    private val geometry = MapGeometry.parse(asset("geo/map.geometry.json"))

    /** Lieux affichables sur le fond de carte : unités provinciales et îles. */
    private val mapIds = (catalog.inCategory(Category.PROVINCIAL) + catalog.inCategory(Category.ISLAND)).map { it.id }.toSet()
    private val playable = mapIds.filter { geometry[it] != null }.toSet()
    private val levels = QuizLevels.build(config, playable)

    @Test
    fun zonesCoverEveryMapPlaceExactlyOnce() {
        val members = config.zones.flatMap { it.memberIds }
        assertEquals("Doublon entre zones", members.size, members.toSet().size)
        assertEquals(mapIds, members.toSet())
    }

    @Test
    fun specialUnitsAreTheFourDecidedOnes() {
        assertEquals(setOf("CN-HK", "CN-MO", "CN-TW", "IS-PARACEL"), config.specialIds.toSet())
    }

    @Test
    fun levelsHaveTheExpectedSizes() {
        val sizes = levels.associate { it.id to it.targetIds.size }
        assertEquals(
            mapOf(
                "north-mainland" to 15,
                "south-mainland" to 16,
                "south-complete" to 20,
                "all-mainland" to 31,
                "all-complete" to 35,
            ),
            sizes,
        )
    }

    @Test
    fun northAndSouthAreBalanced() {
        val north = levels.first { it.id == "north-mainland" }.targetIds.size
        val south = levels.first { it.id == "south-mainland" }.targetIds.size
        assertTrue("Écart Nord/Sud trop grand : $north / $south", kotlin.math.abs(north - south) <= 1)
    }

    @Test
    fun everyQuestionTargetsAPlaceWithGeometryAndData() {
        levels.flatMap { it.targetIds }.forEach { id ->
            assertTrue("$id sans données", catalog[id] != null)
            assertTrue("$id sans géométrie", geometry[id] != null)
        }
    }

    @Test
    fun placesMissingFromTheMapAreNeverAsked() {
        val withoutSichuan = QuizLevels.build(config, playable - "CN-SC")
        assertTrue(withoutSichuan.none { "CN-SC" in it.targetIds })
    }
}
