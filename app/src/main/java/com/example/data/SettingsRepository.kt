package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.AppSettings
import com.example.model.ControllerLayoutMode
import com.example.model.HapticStrength
import com.example.model.OrientationPreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "tv_pad_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val LAYOUT_MODE = stringPreferencesKey("layout_mode")
        val HAPTIC_STRENGTH = stringPreferencesKey("haptic_strength")
        val DEADZONE_PERCENT = intPreferencesKey("deadzone_percent")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val LANGUAGE = stringPreferencesKey("language")
        val ORIENTATION = stringPreferencesKey("orientation")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            layoutMode = prefs[Keys.LAYOUT_MODE]?.let { runCatching { ControllerLayoutMode.valueOf(it) }.getOrNull() }
                ?: ControllerLayoutMode.PRO_DUAL_STICK,
            hapticStrength = prefs[Keys.HAPTIC_STRENGTH]?.let { runCatching { HapticStrength.valueOf(it) }.getOrNull() }
                ?: HapticStrength.MEDIUM,
            deadzonePercent = (prefs[Keys.DEADZONE_PERCENT] ?: 10).coerceIn(0, 30),
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
            language = prefs[Keys.LANGUAGE]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.BENGALI,
            orientationPreference = prefs[Keys.ORIENTATION]?.let { runCatching { OrientationPreference.valueOf(it) }.getOrNull() }
                ?: OrientationPreference.AUTO
        )
    }

    suspend fun updateLayoutMode(mode: ControllerLayoutMode) {
        context.settingsDataStore.edit { it[Keys.LAYOUT_MODE] = mode.name }
    }

    suspend fun updateHapticStrength(strength: HapticStrength) {
        context.settingsDataStore.edit { it[Keys.HAPTIC_STRENGTH] = strength.name }
    }

    suspend fun updateDeadzonePercent(percent: Int) {
        context.settingsDataStore.edit { it[Keys.DEADZONE_PERCENT] = percent.coerceIn(0, 30) }
    }

    suspend fun updateKeepScreenOn(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun updateOrientationPreference(preference: OrientationPreference) {
        context.settingsDataStore.edit { it[Keys.ORIENTATION] = preference.name }
    }
}
