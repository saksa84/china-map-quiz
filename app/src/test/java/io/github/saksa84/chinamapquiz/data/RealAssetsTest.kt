package io.github.saksa84.chinamapquiz.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Vérifie les vrais fichiers de données livrés dans l'application (app/src/main/assets/geo).
 * Les tests unitaires s'exécutent depuis le dossier du module `app`, d'où le chemin relatif.
 */
class RealAssetsTest {

    private fun asset(name: String): String = File("src/main/assets/geo/$name").readText()

    private val catalog: GeoCatalog by lazy {
        when (val result = GeoCatalogLoader.load(asset("provinces.json"), asset("regions.json"), asset("islands.json"))) {
            is LoadResult.Success -> result.catalog
            else -> throw AssertionError("Les données livrées doivent être valides : $result")
        }
    }

    @Test
    fun has34ProvincialUnitsWithExpectedTypes() {
        val provincial = catalog.inCategory(Category.PROVINCIAL)
        assertEquals(34, provincial.size)
        val countByType = provincial.groupingBy { it.type }.eachCount()
        assertEquals(23, countByType[FeatureType.PROVINCE]) // 22 + Taïwan, comme ISO 3166-2:CN
        assertEquals(5, countByType[FeatureType.AUTONOMOUS_REGION])
        assertEquals(4, countByType[FeatureType.MUNICIPALITY])
        assertEquals(2, countByType[FeatureType.SPECIAL_ADMINISTRATIVE_REGION])
    }

    @Test
    fun provincialIdsFollowIso31662() {
        val iso = Regex("^CN-[A-Z]{2}$")
        catalog.inCategory(Category.PROVINCIAL).forEach { assertTrue(it.id, iso.matches(it.id)) }
    }

    @Test
    fun provincesAndAutonomousRegionsHaveACapital() {
        catalog.entities
            .filter { it.type == FeatureType.PROVINCE || it.type == FeatureType.AUTONOMOUS_REGION }
            .forEach { assertTrue("${it.id} sans capitale", !it.capital.isNullOrBlank()) }
    }

    @Test
    fun everyEntityHasHanzi() {
        catalog.entities.forEach { assertTrue("${it.id} sans hanzi", !it.hanzi.isNullOrBlank()) }
    }

    @Test
    fun regionsMatchTheDecidedLayout() {
        assertEquals(8, catalog.inCategory(Category.REGION).size)
        val top = catalog.children("CN").filter { it.type == FeatureType.REGION }.map { it.name }.toSet()
        assertEquals(setOf("Dongbei", "Huabei", "Huadong", "Zhongnan", "Xibei", "Xinan"), top)
        assertEquals(setOf("Huazhong", "Huanan"), catalog.children("RG-ZHONGNAN").map { it.name }.toSet())

        assertEquals("RG-HUADONG", catalog.topRegionOf("CN-TW")?.id)
        assertEquals("RG-ZHONGNAN", catalog.topRegionOf("CN-HK")?.id)
        assertEquals("RG-HUABEI", catalog.topRegionOf("CN-NM")?.id)
        assertTrue("CN-MO" in catalog["RG-HUANAN"]!!.memberIds)
    }

    @Test
    fun hongKongAndMacaoUseTheirFrenchNameWithPinyinAsAlias() {
        assertEquals("Hong Kong", catalog["CN-HK"]!!.name)
        assertTrue("Xianggang" in catalog["CN-HK"]!!.aliases)
        assertEquals("Macao", catalog["CN-MO"]!!.name)
        assertTrue("Aomen" in catalog["CN-MO"]!!.aliases)
    }

    @Test
    fun paracelIslandsAreDisputedAndNotAttachedToAnyCountry() {
        val paracel = catalog["IS-PARACEL"]!!
        assertEquals(FeatureType.ISLAND_GROUP, paracel.type)
        assertEquals(listOf("CN", "TW", "VN"), paracel.claimedBy)
        assertEquals(null, paracel.parentId)
        assertTrue("Les îles ne comptent pas parmi les 34 unités", paracel.category != Category.PROVINCIAL)
    }

    @Test
    fun onlyDisputedTerritoriesHaveClaimants() {
        assertEquals(listOf("IS-PARACEL"), catalog.entities.filter { it.claimedBy.isNotEmpty() }.map { it.id })
    }

    @Test
    fun geometryCoversExactlyTheMapEntities() {
        val geometry = Json.parseToJsonElement(asset("map.geometry.json")).jsonObject
        val ids = geometry["features"]!!.jsonArray.map { it.jsonObject["id"]!!.jsonPrimitive.content }

        assertEquals("Identifiant en double dans la géométrie", ids.size, ids.toSet().size)
        val expected = (catalog.inCategory(Category.PROVINCIAL) + catalog.inCategory(Category.ISLAND)).map { it.id }
        assertEquals(expected.toSet(), ids.toSet())
        geometry["features"]!!.jsonArray.forEach { feature ->
            val polygons = feature.jsonObject["polygons"]!!.jsonArray
            assertTrue("Polygone vide : ${feature.jsonObject["id"]}", polygons.isNotEmpty())
        }
    }
}
