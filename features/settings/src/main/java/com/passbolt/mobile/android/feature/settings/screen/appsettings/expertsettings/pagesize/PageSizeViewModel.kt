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

package com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize

import com.passbolt.mobile.android.core.compose.SideEffectViewModel
import com.passbolt.mobile.android.core.preferences.usecase.GetGlobalPreferencesUseCase
import com.passbolt.mobile.android.core.preferences.usecase.UpdateGlobalPreferencesUseCase
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize.PageSizeIntent.GoBack
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize.PageSizeIntent.PageSizeChanged
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize.PageSizeSideEffect.NavigateBack

internal class PageSizeViewModel(
    private val getGlobalPreferencesUseCase: GetGlobalPreferencesUseCase,
    private val updateGlobalPreferencesUseCase: UpdateGlobalPreferencesUseCase,
) : SideEffectViewModel<PageSizeState, PageSizeSideEffect>(PageSizeState()) {
    init {
        loadInitialValues()
    }

    fun onIntent(intent: PageSizeIntent) {
        when (intent) {
            GoBack -> emitSideEffect(NavigateBack)
            is PageSizeChanged -> pageSizeChanged(intent.sliderIndex)
        }
    }

    private fun loadInitialValues() {
        val currentPageSize = getGlobalPreferencesUseCase.execute(Unit).apiFetchPageSize
        val index = ALLOWED_PAGE_SIZES.indexOf(currentPageSize).coerceIn(0, ALLOWED_PAGE_SIZES.lastIndex)
        updateViewState { copy(selectedIndex = index) }
    }

    private fun pageSizeChanged(sliderIndex: Int) {
        val index = sliderIndex.coerceIn(0, ALLOWED_PAGE_SIZES.lastIndex)
        val pageSize = ALLOWED_PAGE_SIZES[index]
        updateGlobalPreferencesUseCase.execute(
            UpdateGlobalPreferencesUseCase.Input(apiFetchPageSize = pageSize),
        )
        updateViewState { copy(selectedIndex = index) }
    }
}
