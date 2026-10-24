package io.github.saksa84.chinamapquiz.map

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Projection conique équivalente d'Albers sur une sphère, en kilomètres, y vers le nord.
 * Mêmes formules que tools/build_map_geometry.py : les villes et sommets ajoutés plus tard
 * (points en longitude/latitude) tomberont exactement sur les contours générés.
 */
class AlbersProjection(lon0: Double, lat0: Double, lat1: Double, lat2: Double) {
    private val lon0Rad = Math.toRadians(lon0)
    private val n = (sin(Math.toRadians(lat1)) + sin(Math.toRadians(lat2))) / 2
    private val c = cos(Math.toRadians(lat1)).let { it * it } + 2 * n * sin(Math.toRadians(lat1))
    private val rho0 = EARTH_RADIUS_KM * sqrt(c - 2 * n * sin(Math.toRadians(lat0))) / n

    fun project(lon: Double, lat: Double): Pair<Double, Double> {
        val rho = EARTH_RADIUS_KM * sqrt(c - 2 * n * sin(Math.toRadians(lat))) / n
        val theta = n * (Math.toRadians(lon) - lon0Rad)
        return rho * sin(theta) to rho0 - rho * cos(theta)
    }

    private companion object {
        const val EARTH_RADIUS_KM = 6371.0088
    }
}
