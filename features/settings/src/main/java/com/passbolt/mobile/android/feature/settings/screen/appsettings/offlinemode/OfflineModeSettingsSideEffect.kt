package com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode

internal sealed interface OfflineModeSettingsSideEffect {
    data object NavigateUp : OfflineModeSettingsSideEffect

    data object StartDataRefresh : OfflineModeSettingsSideEffect
}
