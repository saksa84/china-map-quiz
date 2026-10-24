package io.github.saksa84.chinamapquiz.map

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Vraie carte livrée (`map.geometry.json`) et vraies coordonnées GPS : vérifie à la fois
 * la projection Kotlin (identique à celle du script Python) et la détection du toucher.
 */
class RealMapTest {

    private val geometry = MapGeometry.parse(File("src/main/assets/geo/map.geometry.json").readText())
    private val tester = HitTester(geometry.shapes)

    /** 1 unité de carte = 100 m ; tolérance d'environ 2 km, comme un doigt au zoom moyen. */
    private fun at(lon: Double, lat: Double, toleranceKm: Double = 0.0): String? =
        tester.hit(geometry.project(lon, lat), (toleranceKm / geometry.unitKm).toFloat())

    @Test
    fun citiesFallInTheirProvince() {
        assertEquals("CN-BJ", at(116.397, 39.909)) // Beijing, place Tian'anmen
        assertEquals("CN-SH", at(121.473, 31.230)) // Shanghai, place du Peuple
        assertEquals("CN-SC", at(104.066, 30.572)) // Chengdu
        assertEquals("CN-XZ", at(91.117, 29.653)) // Lhasa
        assertEquals("CN-XJ", at(87.617, 43.825)) // Urumqi
        assertEquals("CN-HL", at(126.535, 45.802)) // Harbin
        assertEquals("CN-GD", at(113.264, 23.129)) // Guangzhou
        assertEquals("CN-CQ", at(106.551, 29.563)) // Chongqing
        assertEquals("CN-NM", at(111.749, 40.842)) // Hohhot
        assertEquals("CN-HI", at(110.199, 20.044)) // Haikou
        assertEquals("CN-TW", at(121.565, 25.033)) // Taipei
    }

    @Test
    fun smallUnitsAreReachable() {
        assertEquals("CN-HK", at(114.172, 22.319)) // Kowloon
        assertEquals("CN-MO", at(113.543, 22.193, toleranceKm = 2.0)) // Macao, péninsule
        assertEquals("IS-PARACEL", at(112.336, 16.834, toleranceKm = 2.0)) // île Woody (Yongxing)
    }

    @Test
    fun neighbouringMunicipalitiesAreDistinct() {
        assertEquals("CN-TJ", at(117.200, 39.084)) // Tianjin
        assertEquals("CN-HE", at(114.514, 38.042)) // Shijiazhuang
    }

    @Test
    fun seaAndForeignCountriesAreNothing() {
        assertNull(at(130.0, 20.0)) // mer des Philippines
        assertNull(at(124.0, 35.0)) // mer Jaune
        assertNull(at(102.0, 47.0)) // Mongolie
        assertNull(at(77.209, 28.614)) // New Delhi
    }

    @Test
    fun allShapesLieInsideTheDeclaredBounds() {
        geometry.shapes.forEach { shape ->
            val b = shape.bounds
            assertTrue(shape.id, b.minX >= geometry.bounds.minX && b.maxX <= geometry.bounds.maxX)
            assertTrue(shape.id, b.minY >= geometry.bounds.minY && b.maxY <= geometry.bounds.maxY)
        }
        assertEquals(35, geometry.shapes.size)
    }

    @Test
    fun labelPointsLieInsideTheirShape() {
        // Y compris pour les formes tordues (Gansu, Nei Mongol) où le centre du cadre tombe dehors.
        geometry.shapes.forEach { shape ->
            assertTrue("Étiquette hors de ${shape.id}", shape.contains(shape.label.x, shape.label.y))
        }
    }

    @Test
    fun northIsUp() {
        val harbin = geometry.project(126.5, 45.8)
        val haikou = geometry.project(110.2, 20.0)
        assertTrue("y vers le bas : le nord a un y plus petit", harbin.y < haikou.y)
    }
}
