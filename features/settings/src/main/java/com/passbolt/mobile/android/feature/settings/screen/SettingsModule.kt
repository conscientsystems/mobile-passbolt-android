package com.passbolt.mobile.android.feature.settings.screen

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf

fun Module.settingsModule() {
    viewModelOf(::SettingsViewModel)
}
