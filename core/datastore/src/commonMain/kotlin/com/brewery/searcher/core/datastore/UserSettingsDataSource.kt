package com.brewery.searcher.core.datastore

import androidx.datastore.core.DataStore
import com.brewery.searcher.core.datastore.model.DarkThemeConfig
import com.brewery.searcher.core.datastore.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.brewery.searcher.core.datastore.UserSettings as ProtoUserSettings

class UserSettingsDataSource(
    private val dataStore: DataStore<ProtoUserSettings>,
) {
    val userData: Flow<UserSettings> = dataStore.data
        .map { it.toUserSettings() }

    suspend fun setDarkThemeConfig(config: DarkThemeConfig) {
        dataStore.updateData { prefs ->
            prefs.copy(
                dark_theme_config = when (config) {
                    DarkThemeConfig.FOLLOW_SYSTEM -> DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
                    DarkThemeConfig.LIGHT -> DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT
                    DarkThemeConfig.DARK -> DarkThemeConfigProto.DARK_THEME_CONFIG_DARK
                }
            )
        }
    }
}
