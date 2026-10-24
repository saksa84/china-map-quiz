package io.github.saksa84.chinamapquiz.progress

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Un seul fichier de préférences pour toute l'application (règle DataStore : une instance par fichier). */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "china_map_quiz")

/** Réglages modifiables dans l'écran Paramètres. */
data class Settings(val showHanzi: Boolean = true)

/**
 * Sauvegarde locale (sur le téléphone, sans compte ni serveur) de la progression et des réglages.
 * La progression est stockée en JSON dans une seule préférence : simple, et facile à faire évoluer
 * grâce à son numéro de version.
 */
class ProgressStore(private val context: Context) {

    val progress: Flow<Progress> = context.dataStore.data.map { Progress.fromJson(it[PROGRESS]) }

    val settings: Flow<Settings> = context.dataStore.data.map { Settings(showHanzi = it[SHOW_HANZI] ?: true) }

    suspend fun save(progress: Progress) {
        context.dataStore.edit { it[PROGRESS] = progress.toJson() }
    }

    suspend fun reset() {
        context.dataStore.edit { it.remove(PROGRESS) }
    }

    suspend fun setShowHanzi(show: Boolean) {
        context.dataStore.edit { it[SHOW_HANZI] = show }
    }

    private companion object {
        val PROGRESS = stringPreferencesKey("progress_v1")
        val SHOW_HANZI = booleanPreferencesKey("show_hanzi")
    }
}
