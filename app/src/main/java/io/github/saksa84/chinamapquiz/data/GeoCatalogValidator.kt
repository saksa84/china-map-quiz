package io.github.saksa84.chinamapquiz.data

/** Problème détecté dans les données. Chaque cas est un type distinct pour être vérifiable dans les tests. */
sealed interface ValidationError {
    data class UnsupportedSchema(val version: Int) : ValidationError
    data class DuplicateId(val id: String) : ValidationError

    /** Deux sources portent le même identifiant mais des informations différentes. */
    data class ConflictingSource(val id: String) : ValidationError
    data class UnknownSource(val entityId: String, val sourceId: String) : ValidationError
    data class MissingParent(val entityId: String) : ValidationError
    data class UnknownParent(val entityId: String, val parentId: String) : ValidationError

    /** Le parent existe mais n'est pas d'une catégorie autorisée (province sous une province, fleuve sous une ville…). */
    data class InvalidParent(val entityId: String, val parentId: String) : ValidationError
    data class InvalidName(val entityId: String, val name: String) : ValidationError

    /** Seules les grandes régions ont des membres. */
    data class UnexpectedMembers(val entityId: String) : ValidationError
    data class EmptyRegion(val regionId: String) : ValidationError
    data class UnknownMember(val regionId: String, val memberId: String) : ValidationError

    /** Un membre de région doit être une unité provinciale. */
    data class InvalidMember(val regionId: String, val memberId: String) : ValidationError

    /** Une sous-région (Huazhong) ne peut contenir que des membres de sa région parente (Zhongnan). */
    data class MemberOutsideParentRegion(val regionId: String, val memberId: String) : ValidationError

    /** Dès qu'il y a des grandes régions, chaque unité provinciale doit appartenir à exactement une. */
    data class RegionCoverage(val provincialId: String, val regionCount: Int) : ValidationError

    /** Un pays revendicateur doit être un code ISO 3166-1 alpha-2 (ex. "VN"). */
    data class InvalidClaimant(val entityId: String, val code: String) : ValidationError
}

object GeoCatalogValidator {
    const val SUPPORTED_SCHEMA_VERSION = 1

    /**
     * Pinyin sans tons : lettres ASCII, avec espaces, apostrophes (Xi'an), traits d'union et
     * chiffres après la première lettre (K2). Rejette les caractères chinois, les tons (ā, ǎ…)
     * et les chaînes vides.
     */
    private val LATIN_NAME = Regex("^[A-Za-z][A-Za-z0-9 '\\-]*$")
    private val COUNTRY_CODE = Regex("^[A-Z]{2}$")

    fun validate(files: List<GeoCatalogFile>): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        for (file in files) {
            if (file.schemaVersion != SUPPORTED_SCHEMA_VERSION) {
                errors += ValidationError.UnsupportedSchema(file.schemaVersion)
            }
        }

        // Une même source peut être déclarée dans plusieurs fichiers, à condition d'être identique.
        val sources = mutableMapOf<String, DataSource>()
        for (source in files.flatMap { it.sources }) {
            val previous = sources.putIfAbsent(source.id, source)
            if (previous != null && previous != source) errors += ValidationError.ConflictingSource(source.id)
        }

        val entities = files.flatMap { it.entities }
        val seenIds = mutableSetOf<String>()
        for (entity in entities) {
            if (!seenIds.add(entity.id)) errors += ValidationError.DuplicateId(entity.id)
        }

        // Première occurrence de chaque identifiant : suffit pour vérifier les références.
        val byId = entities.associateBy { it.id }

        for (entity in entities) {
            if (entity.sourceId !in sources) {
                errors += ValidationError.UnknownSource(entity.id, entity.sourceId)
            }
            for (name in listOf(entity.name) + entity.aliases) {
                if (!LATIN_NAME.matches(name.trim())) errors += ValidationError.InvalidName(entity.id, name)
            }
            for (code in entity.claimedBy) {
                if (!COUNTRY_CODE.matches(code)) errors += ValidationError.InvalidClaimant(entity.id, code)
            }
            errors += checkParent(entity, byId)
            errors += checkMembers(entity, byId)
        }

        errors += checkRegionCoverage(entities, byId)
        return errors
    }

    private fun checkParent(entity: GeoEntity, byId: Map<String, GeoEntity>): List<ValidationError> {
        val parentId = entity.parentId
            ?: return if (entity.type.parentRequired) listOf(ValidationError.MissingParent(entity.id)) else emptyList()
        val parent = byId[parentId] ?: return listOf(ValidationError.UnknownParent(entity.id, parentId))
        return if (parent.category in entity.type.parentCategories && parent.id != entity.id) {
            emptyList()
        } else {
            listOf(ValidationError.InvalidParent(entity.id, parentId))
        }
    }

    private fun checkMembers(entity: GeoEntity, byId: Map<String, GeoEntity>): List<ValidationError> {
        if (entity.type != FeatureType.REGION) {
            return if (entity.memberIds.isEmpty()) emptyList() else listOf(ValidationError.UnexpectedMembers(entity.id))
        }
        if (entity.memberIds.isEmpty()) return listOf(ValidationError.EmptyRegion(entity.id))

        val parentRegion = entity.parentId?.let { byId[it] }?.takeIf { it.type == FeatureType.REGION }
        return entity.memberIds.mapNotNull { memberId ->
            val member = byId[memberId]
            when {
                member == null -> ValidationError.UnknownMember(entity.id, memberId)
                member.category != Category.PROVINCIAL -> ValidationError.InvalidMember(entity.id, memberId)
                parentRegion != null && memberId !in parentRegion.memberIds ->
                    ValidationError.MemberOutsideParentRegion(entity.id, memberId)
                else -> null
            }
        }
    }

    private fun checkRegionCoverage(entities: List<GeoEntity>, byId: Map<String, GeoEntity>): List<ValidationError> {
        val topRegions = entities.filter { region ->
            region.type == FeatureType.REGION && region.parentId?.let { byId[it] }?.type != FeatureType.REGION
        }
        if (topRegions.isEmpty()) return emptyList()

        return entities
            .filter { it.category == Category.PROVINCIAL }
            .mapNotNull { provincial ->
                val count = topRegions.count { provincial.id in it.memberIds }
                if (count == 1) null else ValidationError.RegionCoverage(provincial.id, count)
            }
    }
}
