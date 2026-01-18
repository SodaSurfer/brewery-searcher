package com.brewery.searcher.core.datastore

import androidx.datastore.core.okio.OkioSerializer
import com.brewery.searcher.core.datastore.model.DarkThemeConfig
import com.brewery.searcher.core.datastore.model.UserSettings
import okio.BufferedSink
import okio.BufferedSource
import com.brewery.searcher.core.datastore.UserSettings as ProtoUserSettings

object UserSettingsSerializer : OkioSerializer<ProtoUserSettings> {
    override val defaultValue: ProtoUserSettings = ProtoUserSettings(
        dark_theme_config = DarkThemeConfigProto.DARK_THEME_CONFIG_FOLLOW_SYSTEM
    )

    override suspend fun readFrom(source: BufferedSource): ProtoUserSettings {
        return ProtoUserSettings.ADAPTER.decode(source.readByteArray())
    }

    override suspend fun writeTo(t: ProtoUserSettings, sink: BufferedSink) {
        ProtoUserSettings.ADAPTER.encode(sink, t)
    }
}

fun ProtoUserSettings.toUserSettings(): UserSettings {
    return UserSettings(
        darkThemeConfig = when (dark_theme_config) {
            DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT -> DarkThemeConfig.LIGHT
            DarkThemeConfigProto.DARK_THEME_CONFIG_DARK -> DarkThemeConfig.DARK
            else -> DarkThemeConfig.FOLLOW_SYSTEM
        },
        locationDoNotAsk = location_permission_do_not_ask,
    )
}
