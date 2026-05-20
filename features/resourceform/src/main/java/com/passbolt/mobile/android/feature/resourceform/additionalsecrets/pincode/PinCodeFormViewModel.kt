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

package com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode

import com.passbolt.mobile.android.common.validation.StringMaxLength
import com.passbolt.mobile.android.common.validation.StringMinLength
import com.passbolt.mobile.android.common.validation.validation
import com.passbolt.mobile.android.core.compose.SideEffectViewModel
import com.passbolt.mobile.android.core.passwordgenerator.PinCodeGenerator
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.PinCodeFormIntent.AdvancedGenerationResult
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.PinCodeFormIntent.ApplyChanges
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.PinCodeFormIntent.Generate
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.PinCodeFormIntent.GoBack
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.PinCodeFormIntent.OpenAdvancedGeneration
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.PinCodeFormIntent.PinCodeChanged
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.PinCodeFormIntent.RemovePinCode
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.PinCodeFormSideEffect.ApplyAndGoBack
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.PinCodeFormSideEffect.NavigateBack
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.pincode.PinCodeFormSideEffect.NavigateToAdvancedGeneration
import com.passbolt.mobile.android.ui.PinCodeUiModel
import com.passbolt.mobile.android.ui.PinCodeUiModel.Companion.MAX_LENGTH
import com.passbolt.mobile.android.ui.PinCodeUiModel.Companion.MIN_LENGTH
import com.passbolt.mobile.android.ui.ResourceFormMode

internal class PinCodeFormViewModel(
    mode: ResourceFormMode,
    pinCodeUiModel: PinCodeUiModel,
    private val pinCodeGenerator: PinCodeGenerator,
) : SideEffectViewModel<PinCodeFormState, PinCodeFormSideEffect>(
        initialState =
            PinCodeFormState(
                resourceFormMode = mode,
                pinCode = pinCodeUiModel.pinCode,
                length = pinCodeUiModel.length,
            ),
    ) {
    fun onIntent(intent: PinCodeFormIntent) {
        when (intent) {
            is PinCodeChanged ->
                updateViewState {
                    copy(pinCode = intent.pinCode.filter(Char::isDigit).take(MAX_LENGTH), validationErrors = emptyList())
                }
            Generate -> generatePinCode(viewState.value.length)
            OpenAdvancedGeneration ->
                emitSideEffect(
                    NavigateToAdvancedGeneration(
                        PinCodeUiModel(pinCode = viewState.value.pinCode, length = viewState.value.length),
                    ),
                )
            is AdvancedGenerationResult -> generatePinCode(intent.pinCodeUiModel.length)
            ApplyChanges -> applyChanges()
            RemovePinCode -> emitSideEffect(ApplyAndGoBack(null))
            GoBack -> emitSideEffect(NavigateBack)
        }
    }

    private fun generatePinCode(length: Int) {
        val generated = pinCodeGenerator.generate(length)
        updateViewState { copy(pinCode = generated, length = length, validationErrors = emptyList()) }
    }

    private fun applyChanges() {
        updateViewState { copy(validationErrors = emptyList()) }
        val state = viewState.value
        validation {
            of(state.pinCode) {
                withRules(StringMinLength(MIN_LENGTH)) {
                    onInvalid {
                        updateViewState {
                            copy(validationErrors = validationErrors + PinCodeValidationError.TooShort(MIN_LENGTH))
                        }
                    }
                }
                withRules(StringMaxLength(MAX_LENGTH)) {
                    onInvalid {
                        updateViewState {
                            copy(validationErrors = validationErrors + PinCodeValidationError.TooLong(MAX_LENGTH))
                        }
                    }
                }
            }
            onValid {
                emitSideEffect(
                    ApplyAndGoBack(
                        PinCodeUiModel(pinCode = state.pinCode, length = state.length),
                    ),
                )
            }
        }
    }
}
