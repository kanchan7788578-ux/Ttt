package com.example.tv.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tv.model.TvGameId
import com.example.tv.model.TvHighScores
import com.example.tv.model.TvLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tvDataStore: DataStore<Preferences> by preferencesDataStore(name = "tv_arcade_store")

class TvRepository(private val context: Context) {

    private object Keys {
        val SPACE_BEST = intPreferencesKey("space_best")
        val SNAKE_BEST = intPreferencesKey("snake_best")
        val BRICK_BEST = intPreferencesKey("brick_best")
        val LANGUAGE = stringPreferencesKey("language")
    }

    val scoresFlow: Flow<TvHighScores> = context.tvDataStore.data.map { prefs ->
        TvHighScores(
            spaceStrikerBest = prefs[Keys.SPACE_BEST] ?: 0,
            neonSnakeBest = prefs[Keys.SNAKE_BEST] ?: 0,
            brickBreakerBest = prefs[Keys.BRICK_BEST] ?: 0,
            language = prefs[Keys.LANGUAGE]?.let { runCatching { TvLanguage.valueOf(it) }.getOrNull() }
                ?: TvLanguage.BENGALI
        )
    }

    suspend fun saveHighScoreIfBest(gameId: TvGameId, score: Int) {
        context.tvDataStore.edit { prefs ->
            val key = when (gameId) {
                TvGameId.SPACE_STRIKER -> Keys.SPACE_BEST
                TvGameId.NEON_SNAKE -> Keys.SNAKE_BEST
                TvGameId.BRICK_BREAKER -> Keys.BRICK_BEST
            }
            val current = prefs[key] ?: 0
            if (score > current) {
                prefs[key] = score
            }
        }
    }

    suspend fun setLanguage(language: TvLanguage) {
        context.tvDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.name
        }
    }
}
