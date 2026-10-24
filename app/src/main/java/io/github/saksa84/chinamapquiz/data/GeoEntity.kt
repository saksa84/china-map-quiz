package io.github.saksa84.chinamapquiz.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Grande famille de lieux. Chaque catégorie peut être activée séparément selon la version
 * de contenu (voir CLAUDE.md, section 4) et correspond à un fichier de données.
 */
enum class Category {
    COUNTRY,
    REGION,
    PROVINCIAL,
    PREFECTURAL,
    CITY,
    RIVER,
    MOUNTAIN,
    LAKE,
    PARK,
    ISLAND,
}

/** Forme de la géométrie à dessiner et à toucher sur la carte. */
enum class GeometryKind {
    POLYGON,

    /** Fleuves, lignes de crête. */
    LINE,

    /** Villes, sommets. */
    POINT,

    /** Pas de tracé propre : union des membres (grandes régions). */
    COMPOSITE,
}

/**
 * Type précis d'un lieu. Il fixe sa catégorie, sa forme, et les parents autorisés
 * (une municipalité n'est pas une province, un affluent n'est pas un grand fleuve).
 */
@Serializable
enum class FeatureType(
    val category: Category,
    val geometry: GeometryKind,
    /** Catégories autorisées pour le parent. Vide : le lieu n'a jamais de parent. */
    val parentCategories: Set<Category> = emptySet(),
    val parentRequired: Boolean = parentCategories.isNotEmpty(),
) {
    @SerialName("country") COUNTRY(Category.COUNTRY, GeometryKind.POLYGON),

    /** Grande région (Huabei…) ou sous-région (Huazhong, Huanan sous Zhongnan). */
    @SerialName("region") REGION(Category.REGION, GeometryKind.COMPOSITE, setOf(Category.COUNTRY, Category.REGION)),

    @SerialName("province") PROVINCE(Category.PROVINCIAL, GeometryKind.POLYGON, setOf(Category.COUNTRY)),
    @SerialName("autonomous_region") AUTONOMOUS_REGION(Category.PROVINCIAL, GeometryKind.POLYGON, setOf(Category.COUNTRY)),
    @SerialName("municipality") MUNICIPALITY(Category.PROVINCIAL, GeometryKind.POLYGON, setOf(Category.COUNTRY)),
    @SerialName("special_administrative_region")
    SPECIAL_ADMINISTRATIVE_REGION(Category.PROVINCIAL, GeometryKind.POLYGON, setOf(Category.COUNTRY)),

    @SerialName("prefecture_level_city") PREFECTURE_LEVEL_CITY(Category.PREFECTURAL, GeometryKind.POLYGON, setOf(Category.PROVINCIAL)),
    @SerialName("prefecture") PREFECTURE(Category.PREFECTURAL, GeometryKind.POLYGON, setOf(Category.PROVINCIAL)),
    @SerialName("autonomous_prefecture") AUTONOMOUS_PREFECTURE(Category.PREFECTURAL, GeometryKind.POLYGON, setOf(Category.PROVINCIAL)),
    @SerialName("league") LEAGUE(Category.PREFECTURAL, GeometryKind.POLYGON, setOf(Category.PROVINCIAL)),

    @SerialName("city") CITY(Category.CITY, GeometryKind.POINT, setOf(Category.PROVINCIAL, Category.PREFECTURAL)),

    @SerialName("major_river") MAJOR_RIVER(Category.RIVER, GeometryKind.LINE),

    /** Parent : le cours d'eau dans lequel il se jette. */
    @SerialName("tributary") TRIBUTARY(Category.RIVER, GeometryKind.LINE, setOf(Category.RIVER)),

    /** Zone de la chaîne (touchable), coupée à la frontière au dessin. */
    @SerialName("mountain_range") MOUNTAIN_RANGE(Category.MOUNTAIN, GeometryKind.POLYGON),

    /** Parent facultatif : la chaîne à laquelle il appartient. */
    @SerialName("peak") PEAK(Category.MOUNTAIN, GeometryKind.POINT, setOf(Category.MOUNTAIN), parentRequired = false),

    @SerialName("lake") LAKE(Category.LAKE, GeometryKind.POLYGON),
    @SerialName("park") PARK(Category.PARK, GeometryKind.POLYGON),

    /** Archipel hors des unités provinciales, sans parent pour ne pas trancher une souveraineté disputée. */
    @SerialName("island_group") ISLAND_GROUP(Category.ISLAND, GeometryKind.POLYGON),
}

/** Provenance d'un ensemble de données, référencée par [GeoEntity.sourceId]. */
@Serializable
data class DataSource(
    val id: String,
    val name: String,
    val license: String,
    val url: String? = null,
    val version: String? = null,
)

/**
 * Un lieu : pays, grande région, province, ville, fleuve, montagne, lac ou parc.
 *
 * La géométrie est stockée à part, dans un fichier indexé par [id] : on peut ainsi charger
 * les noms sans les tracés (mode Type) et remplacer la carte sans toucher aux noms.
 */
@Serializable
data class GeoEntity(
    /** Identifiant stable, indépendant du nom affiché. Code ISO 3166-2 au niveau provincial (ex. "CN-SC"). */
    val id: String,
    val type: FeatureType,
    /**
     * Nom principal affiché dans les quiz : pinyin sans tons par défaut, nom francophone
     * usuel pour les exceptions documentées dans CLAUDE.md (Hong Kong, Macao, îles Paracel).
     */
    val name: String,
    /**
     * Nom courant en français ou à l'international, quand il existe (« Everest », « Pékin », « Mékong ») :
     * affiché avec le pinyin, sous la forme « Everest / Zhumulangma Feng ». Accents autorisés.
     */
    val knownAs: String? = null,
    /** Autres graphies acceptées dans le mode Type (ex. "Tibet" pour Xizang, "Yangtze" pour Chang Jiang). */
    val aliases: List<String> = emptyList(),
    /** Nom en caractères chinois, facultatif, pour vérification et enrichissement. Lu par le bouton 🔊. */
    val hanzi: String? = null,
    /** Pinyin avec tons (« Sìchuān », « Zhūmùlǎngmǎ Fēng »), affiché pour aider à prononcer. */
    val pinyinTones: String? = null,
    /** Parent hiérarchique ; les catégories autorisées dépendent de [type]. */
    val parentId: String? = null,
    /** Pour une grande région uniquement : les unités provinciales qui la composent. */
    val memberIds: List<String> = emptyList(),
    /** Capitale ou siège administratif, en pinyin. */
    val capital: String? = null,
    /**
     * Territoire disputé : codes ISO 3166-1 alpha-2 des pays qui le revendiquent (ex. ["CN", "TW", "VN"]).
     * Vide pour un territoire non disputé. L'application affiche « revendiqué par … » sans trancher.
     */
    val claimedBy: List<String> = emptyList(),
    /** Villes : population urbaine (voir la source pour l'année et la définition). */
    val population: Long? = null,
    /** Sommets : altitude en mètres. */
    val elevation: Int? = null,
    /** Cours d'eau : longueur totale en km, de la source principale à l'embouchure (voir la source). */
    val lengthKm: Int? = null,
    /** Cours d'eau transfrontaliers : longueur du tronçon en Chine, en km. */
    val lengthInChinaKm: Int? = null,
    val sourceId: String,
) {
    val category: Category get() = type.category

    /** Nom complet des consignes et des fiches : « Everest / Zhumulangma Feng », ou le pinyin seul. */
    val displayName: String get() = knownAs?.let { "$it / $name" } ?: name

    /** Nom court pour la carte et les listes : le nom connu s'il existe. */
    val shortName: String get() = knownAs ?: name
}
