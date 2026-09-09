package com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode

import androidx.lifecycle.viewModelScope
import com.passbolt.mobile.android.common.datarefresh.DataRefreshStatus
import com.passbolt.mobile.android.common.datarefresh.DataRefreshTrackingFlow
import com.passbolt.mobile.android.core.compose.SideEffectViewModel
import com.passbolt.mobile.android.core.mvp.coroutinecontext.CoroutineLaunchContext
import com.passbolt.mobile.android.domain.secrets.usecase.offline.ClearOfflineCacheUseCase
import com.passbolt.mobile.android.domain.secrets.usecase.offline.GetOfflineCacheStatusUseCase
import com.passbolt.mobile.android.domain.secrets.usecase.offline.SetOfflineModeUseCase
import com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode.OfflineModeSettingsIntent.CancelClear
import com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode.OfflineModeSettingsIntent.ClearClick
import com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode.OfflineModeSettingsIntent.ConfirmClear
import com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode.OfflineModeSettingsIntent.GoBack
import com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode.OfflineModeSettingsIntent.SelectMode
import com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode.OfflineModeSettingsIntent.SyncNow
import com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode.OfflineModeSettingsIntent.ToggleEnabled
import com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode.OfflineModeSettingsSideEffect.NavigateUp
import com.passbolt.mobile.android.feature.settings.screen.appsettings.offlinemode.OfflineModeSettingsSideEffect.StartDataRefresh
import com.passbolt.mobile.android.ui.OfflineModeSetting
import kotlinx.coroutines.launch

internal class OfflineModeSettingsViewModel(
    private val getOfflineCacheStatusUseCase: GetOfflineCacheStatusUseCase,
    private val setOfflineModeUseCase: SetOfflineModeUseCase,
    private val clearOfflineCacheUseCase: ClearOfflineCacheUseCase,
    private val dataRefreshTrackingFlow: DataRefreshTrackingFlow,
    private val coroutineLaunchContext: CoroutineLaunchContext,
) : SideEffectViewModel<OfflineModeSettingsState, OfflineModeSettingsSideEffect>(OfflineModeSettingsState()) {
    init {
        loadStatus()
        observeDataRefresh()
    }

    fun onIntent(intent: OfflineModeSettingsIntent) {
        when (intent) {
            GoBack -> emitSideEffect(NavigateUp)
            ToggleEnabled -> setMode(if (viewState.value.isEnabled) OfflineModeSetting.OFF else OfflineModeSetting.SELECTED_ENTRIES)
            is SelectMode -> setMode(intent.mode)
            SyncNow -> emitSideEffect(StartDataRefresh)
            ClearClick -> updateViewState { copy(showClearConfirmation = true) }
            CancelClear -> updateViewState { copy(showClearConfirmation = false) }
            ConfirmClear -> clearCache()
        }
    }

    private fun loadStatus() {
        viewModelScope.launch(coroutineLaunchContext.io) {
            val status = getOfflineCacheStatusUseCase.execute(Unit)
            updateViewState {
                copy(
                    mode = status.mode,
                    cachedCount = status.cachedCount,
                    markedCount = status.markedCount,
                    lastSyncEpochMillis = status.lastSyncEpochMillis,
                    isOfflineSession = status.isOfflineSession,
                )
            }
        }
    }

    private fun observeDataRefresh() {
        viewModelScope.launch(coroutineLaunchContext.io) {
            dataRefreshTrackingFlow.dataRefreshStatusFlow.collect {
                val refreshing = it is DataRefreshStatus.InProgress
                updateViewState { copy(isRefreshing = refreshing) }
                if (!refreshing) {
                    loadStatus()
                }
            }
        }
    }

    // a change of mode is followed by a refresh so the cache matches the new choice right away
    private fun setMode(mode: OfflineModeSetting) {
        viewModelScope.launch(coroutineLaunchContext.io) {
            setOfflineModeUseCase.execute(SetOfflineModeUseCase.Input(mode))
            updateViewState { copy(mode = mode) }
            loadStatus()
            if (mode.isEnabled && !viewState.value.isOfflineSession) {
                emitSideEffect(StartDataRefresh)
            }
        }
    }

    private fun clearCache() {
        viewModelScope.launch(coroutineLaunchContext.io) {
            clearOfflineCacheUseCase.execute(Unit)
            updateViewState { copy(showClearConfirmation = false) }
            loadStatus()
        }
    }
}
