/**
 * Passbolt - Open source password manager for teams
 * Copyright (c) 2021 Passbolt SA
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License (AGPL) as published by the Free Software Foundation version 3.
 *
 * The name "Passbolt" is a registered trademark of Passbolt SA, and Passbolt SA hereby declines to grant a trademark
 * license to "Passbolt" pursuant to the GNU Affero General Public License version 3 Section 7(e), without a separate
 * agreement with Passbolt SA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see GNU Affero General Public License v3 (http://www.gnu.org/licenses/agpl-3.0.html).
 *
 * @copyright Copyright (c) Passbolt SA (https://www.passbolt.com)
 * @license https://opensource.org/licenses/AGPL-3.0 AGPL License
 * @link https://www.passbolt.com Passbolt (tm)
 * @since v1.0
 */

package com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings

import com.passbolt.mobile.android.core.compose.SideEffectViewModel
import com.passbolt.mobile.android.domain.preferences.GlobalPreferencesRepository
import com.passbolt.mobile.android.domain.preferences.GlobalPreferencesUpdate
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.ExpertSettingsIntent.GoBack
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.ExpertSettingsIntent.GoToPageSize
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.ExpertSettingsIntent.ToggleAuthRequiredOnEveryEntry
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.ExpertSettingsIntent.ToggleHideRootWarning
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.ExpertSettingsScreenSideEffect.NavigateToPageSize
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.ExpertSettingsScreenSideEffect.NavigateUp

internal class ExpertSettingsViewModel(
    private val globalPreferencesRepository: GlobalPreferencesRepository,
) : SideEffectViewModel<ExpertSettingsState, ExpertSettingsScreenSideEffect>(ExpertSettingsState()) {
    init {
        loadInitialValues()
    }

    fun onIntent(intent: ExpertSettingsIntent) {
        when (intent) {
            GoBack -> emitSideEffect(NavigateUp)
            ToggleAuthRequiredOnEveryEntry -> toggleAuthRequiredOnEveryEntry()
            ToggleHideRootWarning -> toggleHideRootWarning()
            GoToPageSize -> emitSideEffect(NavigateToPageSize)
        }
    }

    private fun loadInitialValues() {
        val globalPreferences = globalPreferencesRepository.getGlobalPreferences()
        updateViewState {
            copy(
                isAuthRequiredOnEveryEntryChecked = globalPreferences.isAuthRequiredOnEveryEntry,
                isHideRootWarningChecked = globalPreferences.isHideRootDialogEnabled,
            )
        }
    }

    private fun toggleAuthRequiredOnEveryEntry() {
        val isChecked = !viewState.value.isAuthRequiredOnEveryEntryChecked
        globalPreferencesRepository.updateGlobalPreferences(
            GlobalPreferencesUpdate(isAuthRequiredOnEveryEntry = isChecked),
        )
        updateViewState {
            copy(isAuthRequiredOnEveryEntryChecked = isChecked)
        }
    }

    private fun toggleHideRootWarning() {
        val isHideRootWarningChecked = !viewState.value.isHideRootWarningChecked

        globalPreferencesRepository.updateGlobalPreferences(
            GlobalPreferencesUpdate(isHideRootDialogEnabled = isHideRootWarningChecked),
        )
        updateViewState {
            copy(isHideRootWarningChecked = isHideRootWarningChecked)
        }
    }
}
