package io.github.saksa84.chinamapquiz.game

import io.github.saksa84.chinamapquiz.data.Category

/**
 * Ce qu'on apprend : premier choix du menu. Chaque catégorie a ses lieux, ses niveaux et sa progression.
 * @param configKey clé de ses niveaux dans `assets/game/levels.json` (« groups »), null pour les régions
 */
enum class GeoCategory(val categories: Set<Category>, val configKey: String?) {
    /** Provinces, régions autonomes, municipalités, régions spéciales, îles disputées. */
    REGIONS(setOf(Category.PROVINCIAL, Category.ISLAND), null),
    CITIES(setOf(Category.CITY), "cities"),
    RIVERS(setOf(Category.RIVER), "rivers"),
    MOUNTAINS(setOf(Category.MOUNTAIN), "mountains"),
    LAKES(setOf(Category.LAKE), "lakes"),
    ;

    /**
     * Clé de progression d'un mode. Les régions gardent la clé d'origine (« FIND ») pour ne pas
     * perdre la progression enregistrée avant l'arrivée des autres catégories.
     */
    fun progressKey(mode: String): String = if (this == REGIONS) mode else "${name}_$mode"
}
