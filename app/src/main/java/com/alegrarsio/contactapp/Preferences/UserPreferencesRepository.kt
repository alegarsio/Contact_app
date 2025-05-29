package com.alegrarsio.contactapp.Preferences // Atau paket pilihan Anda

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alegrarsio.contactapp.Themes.AppTheme // Pastikan impor ini benar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(context: Context) {

    private val appContext = context.applicationContext

    private object PreferencesKeys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val IS_GRID_VIEW = booleanPreferencesKey("is_grid_view")
    }

    val appTheme: Flow<AppTheme> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeName = preferences[PreferencesKeys.APP_THEME] ?: AppTheme.DEFAULT.name
            try {
                AppTheme.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                AppTheme.DEFAULT
            }
        }

    suspend fun saveThemePreference(theme: AppTheme) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_THEME] = theme.name
        }
    }

    val isGridView: Flow<Boolean> = appContext.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_GRID_VIEW] ?: false // Default ke false (list view)
        }

    suspend fun saveGridViewPreference(isGridView: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_GRID_VIEW] = isGridView
        }
    }
}
    