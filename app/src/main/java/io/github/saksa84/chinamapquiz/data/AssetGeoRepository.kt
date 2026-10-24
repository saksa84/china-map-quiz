package io.github.saksa84.chinamapquiz.data

import android.content.res.AssetManager
import io.github.saksa84.chinamapquiz.map.MapGeometry

/** Tout ce qu'il faut pour afficher la carte : les lieux (noms, types…) et leurs contours. */
class MapData(val catalog: GeoCatalog, val geometry: MapGeometry)

/** Lit les fichiers de `assets/geo`. Seule partie du package `data` qui dépend d'Android. */
class AssetGeoRepository(private val assets: AssetManager) {

    /** Lecture et analyse des fichiers : à appeler hors du thread principal. */
    fun load(): MapData {
        val catalog = when (val result = GeoCatalogLoader.load(*CATALOG_FILES.map(::read).toTypedArray())) {
            is LoadResult.Success -> result.catalog
            is LoadResult.Malformed -> error("Données géographiques illisibles : ${result.message}")
            is LoadResult.Invalid -> error("Données géographiques incohérentes : ${result.errors}")
        }
        return MapData(catalog, MapGeometry.parse(read(GEOMETRY_FILE)))
    }

    private fun read(name: String): String = assets.open("geo/$name").bufferedReader().use { it.readText() }

    private companion object {
        val CATALOG_FILES = listOf("provinces.json", "regions.json", "islands.json")
        const val GEOMETRY_FILE = "map.geometry.json"
    }
}
