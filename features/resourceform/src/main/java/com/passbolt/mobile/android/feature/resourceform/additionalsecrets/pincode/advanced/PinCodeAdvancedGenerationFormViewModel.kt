/**
 * Passbolt - Open source password manager for teams
 * Copyright (c) 2026 Passbolt SA
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

package com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.advanced

import com.passbolt.mobile.android.core.compose.SideEffectViewModel
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.advanced.PinCodeAdvancedGenerationFormIntent.GoBack
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.advanced.PinCodeAdvancedGenerationFormIntent.LengthChanged
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.advanced.PinCodeAdvancedGenerationFormIntent.SavePreferences
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.advanced.PinCodeAdvancedGenerationFormSideEffect.ApplyAndGoBack
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.advanced.PinCodeAdvancedGenerationFormSideEffect.NavigateBack
import com.passbolt.mobile.android.ui.PinCodeUiModel
import com.passbolt.mobile.android.ui.PinCodeUiModel.Companion.MAX_LENGTH
import com.passbolt.mobile.android.ui.PinCodeUiModel.Companion.MIN_LENGTH
import com.passbolt.mobile.android.ui.ResourceFormMode

internal class PinCodeAdvancedGenerationFormViewModel(
    mode: ResourceFormMode,
    private val pinCodeUiModel: PinCodeUiModel,
) : SideEffectViewModel<PinCodeAdvancedGenerationFormState, PinCodeAdvancedGenerationFormSideEffect>(
        initialState =
            PinCodeAdvancedGenerationFormState(
                resourceFormMode = mode,
                length = pinCodeUiModel.length.coerceIn(MIN_LENGTH, MAX_LENGTH),
            ),
    ) {
    fun onIntent(intent: PinCodeAdvancedGenerationFormIntent) {
        when (intent) {
            is LengthChanged ->
                updateViewState {
                    copy(length = intent.length.coerceIn(MIN_LENGTH, MAX_LENGTH))
                }
            SavePreferences ->
                emitSideEffect(
                    ApplyAndGoBack(
                        pinCodeUiModel.copy(length = viewState.value.length),
                    ),
                )
            GoBack -> emitSideEffect(NavigateBack)
        }
    }
}
