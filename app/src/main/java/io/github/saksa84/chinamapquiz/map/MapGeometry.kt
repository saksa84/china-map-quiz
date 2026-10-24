package io.github.saksa84.chinamapquiz.map

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** Point dans le repère de la carte : unités de [MapGeometry.unitKm] km, y vers le bas. */
data class MapPoint(val x: Float, val y: Float)

data class MapBounds(val minX: Float, val minY: Float, val maxX: Float, val maxY: Float) {
    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY

    fun contains(x: Float, y: Float): Boolean = x in minX..maxX && y in minY..maxY

    fun expandedBy(margin: Float) = MapBounds(minX - margin, minY - margin, maxX + margin, maxY + margin)
}

/**
 * Contour d'un lieu : anneaux aplatis [x0, y0, x1, y1, …]. Les anneaux extérieurs, les trous
 * et les îles sont mélangés : la règle pair-impair suffit à savoir si un point est dedans.
 */
class MapShape(val id: String, val rings: List<FloatArray>) {
    val bounds: MapBounds = run {
        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for (ring in rings) {
            for (i in ring.indices step 2) {
                minX = min(minX, ring[i]); maxX = max(maxX, ring[i])
                minY = min(minY, ring[i + 1]); maxY = max(maxY, ring[i + 1])
            }
        }
        MapBounds(minX, minY, maxX, maxY)
    }

    /** Surface approximative (somme des anneaux, trous compris), en unités de carte au carré. */
    val area: Float = rings.sumOf { ring ->
        var twice = 0.0
        val n = ring.size / 2
        for (i in 0 until n) {
            val j = (i + 1) % n
            twice += ring[2 * i].toDouble() * ring[2 * j + 1] - ring[2 * j].toDouble() * ring[2 * i + 1]
        }
        kotlin.math.abs(twice) / 2
    }.toFloat()

    /** Test pair-impair : un rayon horizontal partant du point coupe un nombre impair d'arêtes. */
    fun contains(x: Float, y: Float): Boolean {
        if (!bounds.contains(x, y)) return false
        var inside = false
        for (ring in rings) {
            val n = ring.size / 2
            var j = n - 1
            for (i in 0 until n) {
                val xi = ring[2 * i]; val yi = ring[2 * i + 1]
                val xj = ring[2 * j]; val yj = ring[2 * j + 1]
                if ((yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi) inside = !inside
                j = i
            }
        }
        return inside
    }

    /** Distance du point au contour le plus proche (0 sur le bord). */
    fun distanceToEdge(x: Float, y: Float): Float {
        var best = Float.MAX_VALUE
        for (ring in rings) {
            val n = ring.size / 2
            var j = n - 1
            for (i in 0 until n) {
                best = min(best, distanceToSegment(x, y, ring[2 * j], ring[2 * j + 1], ring[2 * i], ring[2 * i + 1]))
                j = i
            }
        }
        return best
    }

    private fun distanceToSegment(px: Float, py: Float, ax: Float, ay: Float, bx: Float, by: Float): Float {
        val dx = bx - ax
        val dy = by - ay
        val lengthSq = dx * dx + dy * dy
        val t = if (lengthSq == 0f) 0f else (((px - ax) * dx + (py - ay) * dy) / lengthSq).coerceIn(0f, 1f)
        return hypot(px - (ax + t * dx), py - (ay + t * dy))
    }
}

/** Fond de carte prêt à dessiner, issu de `assets/geo/map.geometry.json` (généré par tools/build_map_geometry.py). */
class MapGeometry(
    val shapes: List<MapShape>,
    val bounds: MapBounds,
    val unitKm: Double,
    private val projection: AlbersProjection,
) {
    private val byId = shapes.associateBy { it.id }

    operator fun get(id: String): MapShape? = byId[id]

    /** Longitude/latitude -> repère de la carte, avec la même projection que le script de génération. */
    fun project(lon: Double, lat: Double): MapPoint {
        val (xKm, yKm) = projection.project(lon, lat)
        return MapPoint((xKm / unitKm).toFloat(), (-yKm / unitKm).toFloat())
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun parse(text: String): MapGeometry {
            val file = json.decodeFromString<MapGeometryFile>(text)
            require(file.schemaVersion == 1) { "Version de géométrie non prise en charge : ${file.schemaVersion}" }
            require(file.projection.name == "albers") { "Projection inconnue : ${file.projection.name}" }
            require(file.yAxis == "down") { "Orientation de l'axe y inattendue : ${file.yAxis}" }

            val shapes = file.features.map { feature ->
                MapShape(feature.id, feature.polygons.flatten().map { ring -> ring.toFloatArray() })
            }
            val (minX, minY, maxX, maxY) = file.bounds
            val p = file.projection
            return MapGeometry(shapes, MapBounds(minX, minY, maxX, maxY), file.unitKm, AlbersProjection(p.lon0, p.lat0, p.lat1, p.lat2))
        }
    }
}

@Serializable
private data class ProjectionParams(val name: String, val lon0: Double, val lat0: Double, val lat1: Double, val lat2: Double)

@Serializable
private data class FeatureGeometry(val id: String, val polygons: List<List<List<Float>>>)

@Serializable
private data class MapGeometryFile(
    val schemaVersion: Int,
    val sourceId: String,
    val projection: ProjectionParams,
    val unitKm: Double,
    val yAxis: String,
    val bounds: List<Float>,
    val features: List<FeatureGeometry>,
)
