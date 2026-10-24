package io.github.saksa84.chinamapquiz.data

import io.github.saksa84.chinamapquiz.game.AnswerChecker
import io.github.saksa84.chinamapquiz.game.GeoCategory
import io.github.saksa84.chinamapquiz.game.LevelsConfig
import io.github.saksa84.chinamapquiz.game.QuizLevels
import io.github.saksa84.chinamapquiz.map.HitTester
import io.github.saksa84.chinamapquiz.map.MapGeometry
import io.github.saksa84.chinamapquiz.map.ShapeKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Villes, cours d'eau et montagnes : vraies données livrées, chargées comme dans l'application. */
class RealFeaturesTest {

    private fun asset(path: String) = File("src/main/assets/$path").readText()

    private val catalog = (GeoCatalogLoader.load(*AssetGeoRepository.CATALOG_FILES.map { asset("geo/$it") }.toTypedArray()) as LoadResult.Success).catalog
    private val geometry = MapGeometry.parse(asset("geo/map.geometry.json")) +
        MapGeometry.parse(asset("geo/features.geometry.json"), overlay = true)
    private val levels = QuizLevels.buildAll(LevelsConfig.parse(asset("game/levels.json")), catalog.entities.map { it.id }.filter { geometry[it] != null }.toSet())

    @Test
    fun everyFeatureHasGeometryOfTheRightKind() {
        val expected = mapOf(Category.CITY to ShapeKind.POINT, Category.RIVER to ShapeKind.LINE)
        for (entity in catalog.inCategory(Category.CITY) + catalog.inCategory(Category.RIVER) + catalog.inCategory(Category.MOUNTAIN)) {
            val shape = geometry[entity.id]
            assertTrue("${entity.id} sans géométrie", shape != null)
            val kind = expected[entity.category] ?: if (entity.type == FeatureType.PEAK) ShapeKind.POINT else ShapeKind.POLYGON
            assertEquals(entity.id, kind, shape!!.kind)
            assertTrue(entity.id, shape.overlay)
        }
    }

    @Test
    fun atLeast30CitiesWithPopulationAndAProvince() {
        val mainland = catalog.inCategory(Category.CITY).filter { it.population != null }
        assertTrue("Au moins 30 villes (décision CLAUDE.md)", mainland.size >= 30)
        mainland.forEach { assertTrue("${it.id} : plus de 3 millions", it.population!! >= 3_000_000) }
        catalog.inCategory(Category.CITY).forEach { assertEquals(it.id, Category.PROVINCIAL, catalog[it.parentId!!]!!.category) }
    }

    @Test
    fun citiesFallInTheirProvinceOnTheMap() {
        val provinces = HitTester(geometry.shapes.filter { !it.overlay })
        catalog.inCategory(Category.CITY).forEach { city ->
            val point = geometry[city.id]!!.label
            // Tolérance de 5 km : une ville côtière peut tomber juste en mer après simplification du littoral.
            assertEquals(city.id, city.parentId, provinces.hit(point, tolerance = 50f))
        }
    }

    @Test
    fun tributariesFlowIntoAnotherRiver() {
        catalog.ofType(FeatureType.TRIBUTARY).forEach { assertEquals(it.id, Category.RIVER, catalog[it.parentId!!]!!.category) }
        assertEquals("RV-MINJIANG", catalog["RV-DADUHE"]!!.parentId)
    }

    @Test
    fun peaksHaveAnElevation_andEverestIsTheHighest() {
        val peaks = catalog.ofType(FeatureType.PEAK)
        peaks.forEach { assertTrue("${it.id} sans altitude", (it.elevation ?: 0) > 0) }
        assertEquals("PK-QOMOLANGMA", peaks.maxByOrNull { it.elevation!! }!!.id)
    }

    @Test
    fun levelsOfEachCategoryHaveTheExpectedSizes() {
        val sizes = levels.mapValues { (_, l) -> l.associate { it.id to it.targetIds.size } }
        assertEquals(mapOf("cities-top15" to 15, "cities-mainland" to 38, "cities-complete" to 40), sizes[GeoCategory.CITIES])
        assertEquals(mapOf("rivers-major" to 12, "rivers-tributaries" to 15, "rivers-all" to 27), sizes[GeoCategory.RIVERS])
        assertEquals(mapOf("mountains-ranges" to 17, "mountains-peaks" to 20, "mountains-all" to 37), sizes[GeoCategory.MOUNTAINS])
        assertEquals(5, sizes[GeoCategory.REGIONS]!!.size)
    }

    @Test
    fun levelsOnlyContainPlacesOfTheirCategory() {
        levels.forEach { (category, categoryLevels) ->
            categoryLevels.flatMap { it.targetIds }.forEach { id ->
                assertTrue("$id hors de $category", catalog[id]!!.category in category.categories)
            }
        }
    }

    @Test
    fun realCoordinatesHitTheRightFeature() {
        val tester = HitTester(geometry.shapes)
        fun at(lon: Double, lat: Double, accept: (String) -> Boolean) =
            tester.hit(geometry.project(lon, lat), tolerance = 30f) { accept(it.id) }
        // Wuhan est sur le Yangtsé ; à Lanzhou passe le fleuve Jaune.
        assertEquals("RV-CHANGJIANG", at(114.30, 30.58) { it.startsWith("RV-") && it != "RV-HANJIANG" })
        assertEquals("RV-HUANGHE", at(103.83, 36.06) { it.startsWith("RV-") })
        // Un sommet dans sa chaîne : le point est prioritaire sur la zone (coordonnées Natural Earth de l'Everest).
        assertEquals("PK-QOMOLANGMA", at(86.88, 27.98) { it.startsWith("PK-") || it.startsWith("MT-") })
        // À 50 km de là, c'est la chaîne.
        assertEquals("MT-HIMALAYA", at(86.4, 28.1) { it.startsWith("PK-") || it.startsWith("MT-") })
        assertEquals("MT-QINLING", at(108.5, 33.9) { it.startsWith("MT-") })
        assertNull(at(130.0, 20.0) { it.startsWith("RV-") })
    }

    @Test
    fun typedNamesAreAcceptedForFeatures() {
        val checker = AnswerChecker(catalog)
        assertTrue(checker.isCorrect("RV-CHANGJIANG", "Yangtze"))
        assertTrue(checker.isCorrect("RV-HUANGHE", "fleuve jaune"))
        assertTrue(checker.isCorrect("PK-QOMOLANGMA", "Everest"))
        assertTrue(checker.isCorrect("PK-K2", "K2"))
        assertTrue(checker.isCorrect("CT-XIAN", "Xian"))
        assertTrue(checker.isCorrect("CT-GUANGZHOU", "Canton"))
        assertTrue(checker.isCorrect("MT-DAXINGANLING", "Grand Khingan"))
    }
}
