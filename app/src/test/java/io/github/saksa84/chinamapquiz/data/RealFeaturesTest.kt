package io.github.saksa84.chinamapquiz.data

import io.github.saksa84.chinamapquiz.game.AnswerChecker
import io.github.saksa84.chinamapquiz.game.AnswerNormalizer
import io.github.saksa84.chinamapquiz.game.GeoCategory
import io.github.saksa84.chinamapquiz.game.LevelsConfig
import io.github.saksa84.chinamapquiz.game.QuizLevels
import io.github.saksa84.chinamapquiz.map.HitTester
import io.github.saksa84.chinamapquiz.map.MapGeometry
import io.github.saksa84.chinamapquiz.map.MapShape
import io.github.saksa84.chinamapquiz.map.ShapeKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Villes, cours d'eau, montagnes et lacs : vraies données livrées, chargées comme dans l'application. */
class RealFeaturesTest {

    private companion object {
        const val MAX_GAP_KM = 30.0
    }

    private fun asset(path: String) = File("src/main/assets/$path").readText()

    private val catalog = (GeoCatalogLoader.load(*AssetGeoRepository.CATALOG_FILES.map { asset("geo/$it") }.toTypedArray()) as LoadResult.Success).catalog
    private val geometry = MapGeometry.parse(asset("geo/map.geometry.json")) +
        MapGeometry.parse(asset("geo/features.geometry.json"), overlay = true)
    private val levels = QuizLevels.buildAll(LevelsConfig.parse(asset("game/levels.json")), catalog.entities.map { it.id }.filter { geometry[it] != null }.toSet())

    @Test
    fun everyFeatureHasGeometryOfTheRightKind() {
        val expected = mapOf(Category.CITY to ShapeKind.POINT, Category.RIVER to ShapeKind.LINE)
        val features = listOf(Category.CITY, Category.RIVER, Category.MOUNTAIN, Category.LAKE).flatMap { catalog.inCategory(it) }
        for (entity in features) {
            val shape = geometry[entity.id]
            assertTrue("${entity.id} sans géométrie", shape != null)
            val kind = expected[entity.category] ?: if (entity.type in setOf(FeatureType.PEAK, FeatureType.SMALL_LAKE)) ShapeKind.POINT else ShapeKind.POLYGON
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
    fun riversHaveALength_andTheYangtzeIsTheLongest() {
        val rivers = catalog.inCategory(Category.RIVER)
        rivers.forEach { assertTrue("${it.id} sans longueur", (it.lengthKm ?: 0) > 0) }
        assertEquals("RV-CHANGJIANG", rivers.maxByOrNull { it.lengthKm!! }!!.id)
        // Un affluent est plus court que le fleuve dans lequel il se jette (le Hai He n'est compté que pour son cours principal).
        catalog.ofType(FeatureType.TRIBUTARY).filter { it.parentId != "RV-HAIHE" }
            .forEach { assertTrue(it.id, it.lengthKm!! < catalog[it.parentId!!]!!.lengthKm!!) }
        listOf("RV-LANCANGJIANG", "RV-NUJIANG", "RV-YARLUNGZANGBO", "RV-EERQISIHE")
            .forEach { assertTrue("$it : longueur en Chine", catalog[it]!!.lengthInChinaKm != null) }
    }

    /**
     * Les tronçons d'un cours d'eau forment un seul tracé : en reliant les tronçons distants de moins
     * de [MAX_GAP_KM], on doit pouvoir passer de l'un à l'autre. Détecte un tronçon manquant au milieu
     * (comme le Qian Jiang dans le Zhu Jiang avant correction), pas une source ou une embouchure absentes.
     */
    @Test
    fun riverSectionsLeaveNoGap() {
        val maxGap = (MAX_GAP_KM / geometry.unitKm).toFloat()
        for (river in catalog.inCategory(Category.RIVER)) {
            val sections = geometry[river.id]!!.rings
            val reached = mutableSetOf(0)
            var grew = true
            while (grew) {
                grew = false
                for (i in sections.indices) {
                    if (i !in reached && reached.any { gap(sections[it], sections[i]) <= maxGap }) {
                        reached += i
                        grew = true
                    }
                }
            }
            assertEquals("${river.id} : tronçons isolés (écart > $MAX_GAP_KM km)", sections.size, reached.size)
        }
    }

    /** Plus courte distance entre une extrémité de l'un des tronçons et l'autre tronçon. */
    private fun gap(a: FloatArray, b: FloatArray): Float {
        fun endsTo(ends: FloatArray, line: FloatArray): Float {
            val shape = MapShape("gap", listOf(line), kind = ShapeKind.LINE)
            return minOf(
                shape.distanceToEdge(ends[0], ends[1]),
                shape.distanceToEdge(ends[ends.size - 2], ends[ends.size - 1]),
            )
        }
        return minOf(endsTo(a, b), endsTo(b, a))
    }

    /**
     * Le pinyin avec tons, sans ses accents, redonne le nom ou un alias : une faute de frappe dans
     * les tons se voit. Exceptions : Shaanxi (orthographe spéciale de Shǎnxī, pour le distinguer du
     * Shanxi) et Yarlung Zangbo (transcription du tibétain ; pinyin Yǎlǔ Zàngbù Jiāng).
     */
    @Test
    fun pinyinTonesMatchTheName() {
        val exceptions = setOf("CN-SN", "RV-YARLUNGZANGBO")
        for (entity in catalog.entities) {
            val tones = entity.pinyinTones
            assertTrue("${entity.id} sans pinyin avec tons", tones != null)
            if (entity.id in exceptions) continue
            val accepted = (listOf(entity.name) + entity.aliases).map(AnswerNormalizer::normalize)
            assertTrue("${entity.id} : $tones", AnswerNormalizer.normalize(tones!!) in accepted)
        }
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
        assertEquals(mapOf("lakes-west" to 9, "lakes-east" to 15, "lakes-all" to 24), sizes[GeoCategory.LAKES])
        assertEquals(5, sizes[GeoCategory.REGIONS]!!.size)
    }

    @Test
    fun lakesHaveAnAreaAndAProvince_andQinghaiIsTheLargestInChina() {
        val lakes = catalog.inCategory(Category.LAKE)
        assertTrue("Au moins 20 lacs", lakes.size >= 20)
        lakes.forEach {
            assertTrue("${it.id} : superficie", it.areaKm2 != null)
            assertEquals(it.id, Category.PROVINCIAL, catalog[it.parentId!!]!!.category)
        }
        // Le lac Khanka est plus étendu (4 190 km²) mais partagé avec la Russie.
        assertEquals("LK-QINGHAI", lakes.filter { it.id != "LK-XINGKAI" }.maxByOrNull { it.areaKm2!! }!!.id)
        assertEquals("Le plus haut : Mapam ou Namtso, au-dessus de 4 500 m", true, lakes.maxOf { it.elevation ?: 0 } > 4_500)
    }

    @Test
    fun lakesAreDrawnInTheirProvince() {
        val provinces = HitTester(geometry.shapes.filter { !it.overlay })
        // Le réservoir de Danjiangkou est à cheval sur le Hubei (barrage) et le Henan, où tombe le centre de son contour.
        catalog.inCategory(Category.LAKE).filter { it.id != "LK-DANJIANGKOU" }.forEach { lake ->
            // Point du lac, ou point d'étiquette de son contour (partie chinoise pour le lac Khanka).
            assertEquals(lake.id, lake.parentId, provinces.hit(geometry[lake.id]!!.label, tolerance = 150f))
        }
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
        // Lacs : le centre du lac Qinghai, l'île de Xi Hu à Hangzhou, et le côté chinois du lac Khanka.
        assertEquals("LK-QINGHAI", at(100.2, 36.9) { it.startsWith("LK-") })
        assertEquals("LK-XIHU", at(120.14, 30.24) { it.startsWith("LK-") })
        assertEquals("LK-XINGKAI", at(132.5, 45.3) { it.startsWith("LK-") })
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
        assertTrue(checker.isCorrect("LK-NAMCO", "Nam Co"))
        assertTrue(checker.isCorrect("LK-QIANDAO", "lac des mille iles"))
        assertEquals("Namtso / Namu Cuo", catalog["LK-NAMCO"]!!.displayName)
        // Noms connus (étape B de la v1.1) : acceptés avec ou sans accents.
        assertTrue(checker.isCorrect("RV-LANCANGJIANG", "Mékong"))
        assertTrue(checker.isCorrect("RV-LANCANGJIANG", "mekong"))
        assertTrue(checker.isCorrect("RV-CHANGJIANG", "Yangtse"))
        assertEquals("Everest / Zhumulangma Feng", catalog["PK-QOMOLANGMA"]!!.displayName)
        assertEquals("Canton / Guangzhou", catalog["CT-GUANGZHOU"]!!.displayName)
    }
}
