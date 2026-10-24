package io.github.saksa84.chinamapquiz.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.saksa84.chinamapquiz.map.HitTester
import io.github.saksa84.chinamapquiz.map.MapGeometry
import io.github.saksa84.chinamapquiz.map.MapShape
import io.github.saksa84.chinamapquiz.map.MapViewport

/** Apparence d'un lieu sur la carte, décidée par l'écran appelant (mode Learn, Find…). */
data class ShapeStyle(val fill: Color, val dashedBorder: Boolean = false)

/**
 * Carte interactive : dessin des contours, zoom à deux doigts, déplacement, toucher.
 *
 * @param styleOf couleur de chaque lieu selon son identifiant
 * @param selectedId lieu à mettre en évidence (contour épais)
 * @param recenterKey changer cette valeur remet la carte en vue d'ensemble
 * @param onShapeTap appelé avec l'identifiant touché, ou null si on touche la mer
 */
@Composable
fun ChinaMap(
    geometry: MapGeometry,
    styleOf: (String) -> ShapeStyle,
    selectedId: String?,
    onShapeTap: (String?) -> Unit,
    modifier: Modifier = Modifier,
    recenterKey: Int = 0,
    seaColor: Color = Color(0xFFD6E9F8),
    borderColor: Color = Color(0xFF5A6470),
    selectedBorderColor: Color = Color(0xFF1B1B1B),
) {
    // Les contours Compose sont construits une seule fois : ils sont ensuite seulement redimensionnés.
    val paths = remember(geometry) { geometry.shapes.associate { it.id to it.toPath() } }
    val hitTester = remember(geometry) { HitTester(geometry.shapes) }

    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var viewport by remember { mutableStateOf<MapViewport?>(null) }
    var lastRecenterKey by remember { mutableStateOf(recenterKey) }

    // Vue d'ensemble au premier affichage et sur demande ; un simple changement de taille
    // (rotation, panneau du bas) ne doit pas annuler le zoom de l'utilisateur.
    LaunchedEffect(viewSize, recenterKey) {
        if (viewSize == IntSize.Zero) return@LaunchedEffect
        val width = viewSize.width.toFloat()
        val height = viewSize.height.toFloat()
        val current = viewport
        viewport = if (current == null || recenterKey != lastRecenterKey) {
            MapViewport.fit(geometry.bounds, width, height)
        } else {
            current.constrainedTo(geometry.bounds, width, height)
        }
        lastRecenterKey = recenterKey
    }

    val touchTolerancePx = with(LocalDensity.current) { 16.dp.toPx() }
    val borderPx = with(LocalDensity.current) { 0.8.dp.toPx() }
    val selectedBorderPx = with(LocalDensity.current) { 2.5.dp.toPx() }
    // Les gestionnaires de gestes sont créés une fois : ils lisent toujours la dernière valeur via ces références.
    val currentViewport by rememberUpdatedState(viewport)
    val currentOnTap by rememberUpdatedState(onShapeTap)

    Canvas(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { viewSize = it }
            .pointerInput(geometry) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val current = currentViewport ?: return@detectTransformGestures
                    viewport = current
                        .zoomedAt(centroid.x, centroid.y, zoom)
                        .pannedBy(pan.x, pan.y)
                        .constrainedTo(geometry.bounds, size.width.toFloat(), size.height.toFloat())
                }
            }
            .pointerInput(geometry) {
                detectTapGestures { tap ->
                    val current = currentViewport ?: return@detectTapGestures
                    val point = current.toMap(tap.x, tap.y)
                    currentOnTap(hitTester.hit(point, tolerance = touchTolerancePx / current.scale))
                }
            },
    ) {
        drawRect(seaColor)
        val vp = viewport ?: return@Canvas

        withTransform({
            translate(vp.offsetX, vp.offsetY)
            scale(vp.scale, vp.scale, pivot = Offset.Zero)
        }) {
            // Les épaisseurs sont divisées par le zoom pour rester constantes à l'écran.
            val border = Stroke(width = borderPx / vp.scale)
            val dashed = Stroke(
                width = borderPx / vp.scale,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f / vp.scale, 4f / vp.scale)),
            )
            for ((id, path) in paths) {
                val style = styleOf(id)
                drawPath(path, style.fill)
                drawPath(path, borderColor, style = if (style.dashedBorder) dashed else border)
            }
            // Le lieu sélectionné est redessiné en dernier pour que son contour passe au-dessus des voisins.
            selectedId?.let { paths[it] }?.let { path ->
                drawPath(path, selectedBorderColor, style = Stroke(width = selectedBorderPx / vp.scale))
            }
        }
    }
}

private fun MapShape.toPath(): Path = Path().apply {
    fillType = PathFillType.EvenOdd
    for (ring in rings) {
        moveTo(ring[0], ring[1])
        for (i in 2 until ring.size step 2) lineTo(ring[i], ring[i + 1])
        close()
    }
}
