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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import io.github.saksa84.chinamapquiz.map.MapPoint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import io.github.saksa84.chinamapquiz.map.ShapeKind
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

/**
 * Apparence d'un lieu sur la carte, décidée par l'écran appelant (mode Learn, Find…).
 * @param fill couleur de remplissage (surface, point) ou du trait (cours d'eau)
 * @param smallMarker quand le lieu est trop petit pour être vu, le dessiner comme un point grossi
 * @param emphasized lieu sélectionné ou montré en réponse : s'il est minuscule, un réticule l'entoure
 * @param tappable le lieu peut être touché (faux pour le fond de carte d'un quiz sur les fleuves, par exemple)
 * @param lineWidthDp épaisseur du trait d'un cours d'eau, constante à l'écran
 * @param largePoint lieu ponctuel (ville, sommet) dessiné en gros point ; faux pour un petit lac, dessiné
 *   comme les surfaces minuscules pour ne pas paraître plus grand que les grands lacs vus de loin
 */
data class ShapeStyle(
    val fill: Color,
    val dashedBorder: Boolean = false,
    val smallMarker: Boolean = true,
    val emphasized: Boolean = false,
    val tappable: Boolean = true,
    val lineWidthDp: Float = 2f,
    val largePoint: Boolean = true,
)

/**
 * Nom à écrire sur la carte.
 * @param fitWidth largeur disponible (unités de carte) : le nom n'est écrit que s'il y tient
 * @param aboveDp décalage vers le haut à l'écran (nom d'une ville écrit au-dessus de son point)
 */
data class MapLabel(val text: String, val at: MapPoint, val fitWidth: Float, val aboveDp: Float = 0f)

/**
 * Carte interactive : dessin des contours, zoom à deux doigts, déplacement, toucher.
 *
 * Ordre de dessin : provinces, zones de montagne (coupées à la frontière), cours d'eau, puis
 * villes et sommets sous forme de points.
 *
 * @param styleOf apparence de chaque lieu selon son identifiant ; null : lieu masqué et non touchable
 * @param selectedId lieu à mettre en évidence (contour épais)
 * @param recenterKey changer cette valeur remet la carte en vue d'ensemble
 * @param onShapeTap appelé avec l'identifiant touché, ou null si on touche la mer
 */
@Composable
fun ChinaMap(
    geometry: MapGeometry,
    styleOf: (String) -> ShapeStyle?,
    selectedId: String?,
    onShapeTap: (String?) -> Unit,
    modifier: Modifier = Modifier,
    recenterKey: Int = 0,
    labels: List<MapLabel> = emptyList(),
    seaColor: Color = Color(0xFFD6E9F8),
    borderColor: Color = Color(0xFF5A6470),
    selectedBorderColor: Color = Color(0xFF1B1B1B),
) {
    // Les contours Compose sont construits une seule fois : ils sont ensuite seulement redimensionnés.
    val paths = remember(geometry) { geometry.shapes.filter { it.kind != ShapeKind.POINT }.associate { it.id to it.toPath() } }
    // Dessin par couches : fond des provinces, zones de montagne, cours d'eau (les points viennent à la fin).
    val baseShapes = remember(geometry) { geometry.shapes.filter { !it.overlay } }
    val overlayAreas = remember(geometry) { geometry.shapes.filter { it.overlay && it.kind == ShapeKind.POLYGON } }
    val overlayLines = remember(geometry) { geometry.shapes.filter { it.kind == ShapeKind.LINE } }
    // Silhouette de la Chine : les zones de montagne qui débordent sur les pays voisins y sont coupées.
    val chinaOutline = remember(geometry) {
        Path().apply { baseShapes.forEach { addPath(paths.getValue(it.id)) } }.also { it.fillType = PathFillType.NonZero }
    }
    val hitTester = remember(geometry) { HitTester(geometry.shapes) }
    val chinaTester = remember(geometry) { HitTester(baseShapes) }

    var viewSize by remember { mutableStateOf(IntSize.Zero) }
    var viewport by remember { mutableStateOf<MapViewport?>(null) }
    var lastRecenterKey by remember { mutableStateOf(recenterKey) }

    // Vue d'ensemble au premier affichage et sur demande. Un changement de taille (clavier,
    // rotation, panneau) recadre la carte si l'on était en vue d'ensemble, mais n'annule pas
    // un zoom choisi par l'utilisateur.
    LaunchedEffect(viewSize, recenterKey) {
        if (viewSize == IntSize.Zero) return@LaunchedEffect
        val width = viewSize.width.toFloat()
        val height = viewSize.height.toFloat()
        val current = viewport
        val atOverview = current != null && current.scale <= current.minScale * 1.001f
        viewport = if (current == null || atOverview || recenterKey != lastRecenterKey) {
            MapViewport.fit(geometry.bounds, width, height)
        } else {
            current.constrainedTo(geometry.bounds, width, height)
        }
        lastRecenterKey = recenterKey
    }

    val touchTolerancePx = with(LocalDensity.current) { 16.dp.toPx() }
    val borderPx = with(LocalDensity.current) { 0.8.dp.toPx() }
    val selectedBorderPx = with(LocalDensity.current) { 2.5.dp.toPx() }
    // Lieux dont la surface à l'écran est inférieure à un carré de ce côté (Macao, Hong Kong,
    // Paracel en vue d'ensemble) : dessinés comme un point grossi. La surface compte, pas
    // l'étendue : les Paracel s'étalent sur 160 km mais ne font que quelques km².
    val tinyThresholdPx = with(LocalDensity.current) { 6.dp.toPx() }
    val dotRadiusPx = with(LocalDensity.current) { 3.dp.toPx() }
    val pointRadiusPx = with(LocalDensity.current) { 4.5.dp.toPx() } // villes et sommets
    val dotBorderPx = with(LocalDensity.current) { 1.dp.toPx() }
    val reticleRadiusPx = with(LocalDensity.current) { 46.dp.toPx() }
    val reticleStrokePx = with(LocalDensity.current) { 1.5.dp.toPx() }

    // Les noms sont mesurés une fois ; on ne décide qu'au dessin lesquels ont la place d'apparaître.
    val textMeasurer = rememberTextMeasurer()
    val measuredLabels = remember(labels) {
        val style = TextStyle(
            color = Color(0xFF263033),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            shadow = Shadow(Color.White, blurRadius = 6f),
        )
        // Les plus grands lieux d'abord : en cas de chevauchement, ce sont eux qui gardent leur nom.
        labels.sortedByDescending { it.fitWidth }.map { it to textMeasurer.measure(it.text, style) }
    }
    // Les gestionnaires de gestes sont créés une fois : ils lisent toujours la dernière valeur via ces références.
    val currentViewport by rememberUpdatedState(viewport)
    val currentOnTap by rememberUpdatedState(onShapeTap)
    val currentStyleOf by rememberUpdatedState(styleOf)

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
                    val tolerance = touchTolerancePx / current.scale
                    val hit = hitTester.hit(point, tolerance) { currentStyleOf(it.id)?.tappable == true }
                    // Une zone de montagne n'est visible qu'en Chine : un toucher sur sa partie coupée ne compte pas.
                    val shape = hit?.let { geometry[it] }
                    val outside = shape != null && shape.overlay && shape.kind == ShapeKind.POLYGON &&
                        chinaTester.hit(point, tolerance) == null
                    currentOnTap(if (outside) null else hit)
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
            for (shape in baseShapes) {
                val style = styleOf(shape.id) ?: continue
                val path = paths.getValue(shape.id)
                drawPath(path, style.fill)
                drawPath(path, borderColor, style = if (style.dashedBorder) dashed else border)
            }
            // Zones de montagne et cours d'eau : coupés à la frontière chinoise.
            clipPath(chinaOutline) {
                for (shape in overlayAreas) {
                    val style = styleOf(shape.id) ?: continue
                    val path = paths.getValue(shape.id)
                    drawPath(path, style.fill)
                    drawPath(path, style.fill.copy(alpha = 1f), style = border)
                }
                for (shape in overlayLines) {
                    val style = styleOf(shape.id) ?: continue
                    val width = style.lineWidthDp * density / vp.scale
                    drawPath(paths.getValue(shape.id), style.fill, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
            // Le lieu sélectionné est redessiné en dernier pour que son contour passe au-dessus des voisins.
            selectedId?.let { id -> geometry[id]?.let { it to paths[id] } }?.let { (shape, path) ->
                if (path == null) return@let
                if (shape.kind == ShapeKind.LINE) {
                    val width = ((styleOf(shape.id)?.lineWidthDp ?: 2f) * density + selectedBorderPx) / vp.scale
                    drawPath(path, selectedBorderColor, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round))
                    styleOf(shape.id)?.let { drawPath(path, it.fill, style = Stroke(it.lineWidthDp * density / vp.scale, cap = StrokeCap.Round)) }
                } else {
                    drawPath(path, selectedBorderColor, style = Stroke(width = selectedBorderPx / vp.scale))
                }
            }
        }

        // Noms : seulement ceux qui tiennent dans leur lieu à ce zoom, sans chevaucher un nom déjà écrit.
        // Plus on zoome, plus il y a de place : les noms des petites provinces apparaissent.
        val placed = mutableListOf<Rect>()
        for ((label, layout) in measuredLabels) {
            val width = layout.size.width.toFloat()
            val height = layout.size.height.toFloat()
            if (width > label.fitWidth * vp.scale) continue
            val x = label.at.x * vp.scale + vp.offsetX - width / 2
            val y = label.at.y * vp.scale + vp.offsetY - height / 2 - label.aboveDp * density
            val box = Rect(x - 2, y, x + width + 2, y + height)
            if (placed.any { it.overlaps(box) }) continue
            placed += box
            drawText(layout, topLeft = Offset(x, y))
        }

        // Lieux minuscules à ce zoom (villes, sommets, Macao…), dessinés en coordonnées écran pour garder une taille fixe.
        val tiny = geometry.shapes.mapNotNull { shape ->
            if (shape.kind == ShapeKind.LINE) return@mapNotNull null
            val style = styleOf(shape.id) ?: return@mapNotNull null
            if (!style.smallMarker || shape.area * vp.scale * vp.scale >= tinyThresholdPx * tinyThresholdPx) return@mapNotNull null
            val b = shape.bounds
            Triple(shape.id, style, Offset((b.minX + b.maxX) / 2 * vp.scale + vp.offsetX, (b.minY + b.maxY) / 2 * vp.scale + vp.offsetY))
        }
        // D'abord les réticules (sélection, réponse), puis les points par-dessus.
        for ((_, style, center) in tiny) {
            if (style.emphasized) drawReticle(center, reticleRadiusPx, reticleStrokePx)
        }
        for ((id, style, center) in tiny) {
            val radius = if (geometry[id]?.kind == ShapeKind.POINT && style.largePoint) pointRadiusPx else dotRadiusPx
            drawCircle(style.fill, radius = radius, center = center)
            val (ringColor, ringWidth) = if (id == selectedId) selectedBorderColor to dotBorderPx * 2 else borderColor to dotBorderPx
            drawCircle(ringColor, radius = radius, center = center, style = Stroke(ringWidth))
        }
    }
}

/**
 * Réticule autour d'un lieu minuscule sélectionné : disque sombre translucide, repères de visée
 * aux quatre points cardinaux et croix pointillée, pour voir clairement ce qui est choisi.
 */
private fun DrawScope.drawReticle(center: Offset, radius: Float, stroke: Float) {
    drawCircle(
        Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.30f)), center, radius),
        radius = radius,
        center = center,
    )
    drawCircle(Color.White.copy(alpha = 0.35f), radius = radius, center = center, style = Stroke(stroke))
    val dashed = PathEffect.dashPathEffect(floatArrayOf(stroke * 2, stroke * 3))
    val tick = radius * 0.2f
    for (direction in listOf(Offset(1f, 0f), Offset(-1f, 0f), Offset(0f, 1f), Offset(0f, -1f))) {
        drawLine(Color.White.copy(alpha = 0.7f), center + direction * (radius - tick), center + direction * radius, stroke)
        drawLine(Color.White.copy(alpha = 0.25f), center + direction * (radius * 0.25f), center + direction * (radius - tick), stroke * 0.7f, pathEffect = dashed)
    }
}

private fun MapShape.toPath(): Path = Path().apply {
    fillType = PathFillType.EvenOdd
    for (ring in rings) {
        moveTo(ring[0], ring[1])
        for (i in 2 until ring.size step 2) lineTo(ring[i], ring[i + 1])
        if (kind == ShapeKind.POLYGON) close() // un cours d'eau reste ouvert
    }
}
