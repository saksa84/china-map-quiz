package io.github.saksa84.chinamapquiz.map

/**
 * Trouve le lieu touché. Un toucher exactement dans un contour l'emporte ; sinon on accepte
 * le contour le plus proche dans un rayon de tolérance, pour que Macao ou un îlot des
 * Paracel restent sélectionnables au doigt.
 */
class HitTester(private val shapes: List<MapShape>) {

    /** @param tolerance rayon de tolérance dans le repère de la carte (0 : toucher exact uniquement). */
    fun hit(point: MapPoint, tolerance: Float = 0f): String? {
        shapes.firstOrNull { it.contains(point.x, point.y) }?.let { return it.id }
        if (tolerance <= 0f) return null

        var bestId: String? = null
        var bestDistance = tolerance
        for (shape in shapes) {
            if (!shape.bounds.expandedBy(tolerance).contains(point.x, point.y)) continue
            val distance = shape.distanceToEdge(point.x, point.y)
            if (distance <= bestDistance) {
                bestDistance = distance
                bestId = shape.id
            }
        }
        return bestId
    }
}
