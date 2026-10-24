package io.github.saksa84.chinamapquiz.data

import kotlinx.serialization.Serializable

/** Contenu brut d'un fichier de données, tel qu'il est lu dans le JSON. */
@Serializable
data class GeoCatalogFile(
    val schemaVersion: Int,
    val sources: List<DataSource>,
    val entities: List<GeoEntity>,
)

/**
 * Ensemble de lieux validé, issu d'un ou plusieurs fichiers, avec des accès rapides.
 * On ne le construit qu'à partir de données sans erreur (voir [GeoCatalogLoader]).
 */
class GeoCatalog(
    val sources: List<DataSource>,
    val entities: List<GeoEntity>,
) {
    private val byId: Map<String, GeoEntity> = entities.associateBy { it.id }
    private val byParent: Map<String?, List<GeoEntity>> = entities.groupBy { it.parentId }

    operator fun get(id: String): GeoEntity? = byId[id]

    fun children(parentId: String): List<GeoEntity> = byParent[parentId].orEmpty()

    fun inCategory(category: Category): List<GeoEntity> = entities.filter { it.category == category }

    fun ofType(type: FeatureType): List<GeoEntity> = entities.filter { it.type == type }

    /** Unités provinciales d'une grande région (pour la colorier ou poser une question). */
    fun membersOf(regionId: String): List<GeoEntity> = byId[regionId]?.memberIds.orEmpty().mapNotNull { byId[it] }

    /** Grande région de premier rang (Huabei, Zhongnan…) contenant une unité provinciale. */
    fun topRegionOf(provincialId: String): GeoEntity? =
        entities.firstOrNull { it.type == FeatureType.REGION && it.isTopRegion() && provincialId in it.memberIds }

    private fun GeoEntity.isTopRegion(): Boolean = parentId?.let { byId[it] }?.type != FeatureType.REGION
}
