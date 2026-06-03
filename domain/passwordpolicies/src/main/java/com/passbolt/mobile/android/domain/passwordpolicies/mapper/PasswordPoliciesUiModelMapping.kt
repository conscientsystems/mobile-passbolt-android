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

package com.passbolt.mobile.android.domain.passwordpolicies.mapper

import com.passbolt.mobile.android.domain.passwordpolicies.model.CaseType
import com.passbolt.mobile.android.domain.passwordpolicies.model.PassphraseGeneratorSettings
import com.passbolt.mobile.android.domain.passwordpolicies.model.PasswordGeneratorSettings
import com.passbolt.mobile.android.domain.passwordpolicies.model.PasswordGeneratorType
import com.passbolt.mobile.android.domain.passwordpolicies.model.PasswordPolicies
import com.passbolt.mobile.android.ui.CaseTypeUiModel
import com.passbolt.mobile.android.ui.PassphraseGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeUiModel
import com.passbolt.mobile.android.ui.PasswordPoliciesUiModel

fun PasswordPolicies.toUiModel(): PasswordPoliciesUiModel =
    PasswordPoliciesUiModel(
        defaultGenerator = defaultGenerator.toUiModel(),
        passwordGeneratorSettings = passwordGeneratorSettings.toUiModel(),
        passphraseGeneratorSettings = passphraseGeneratorSettings.toUiModel(),
        isExternalDictionaryCheckEnabled = isExternalDictionaryCheckEnabled,
    )

fun PasswordGeneratorSettings.toUiModel(): PasswordGeneratorSettingsUiModel =
    PasswordGeneratorSettingsUiModel(
        length = length,
        maskUpper = maskUpper,
        maskLower = maskLower,
        maskDigit = maskDigit,
        maskParenthesis = maskParenthesis,
        maskEmoji = maskEmoji,
        maskChar1 = maskChar1,
        maskChar2 = maskChar2,
        maskChar3 = maskChar3,
        maskChar4 = maskChar4,
        maskChar5 = maskChar5,
        excludeLookAlikeChars = excludeLookAlikeChars,
    )

fun PassphraseGeneratorSettings.toUiModel(): PassphraseGeneratorSettingsUiModel =
    PassphraseGeneratorSettingsUiModel(
        words = words,
        wordSeparator = wordSeparator,
        wordCase = wordCase.toUiModel(),
    )

fun PasswordGeneratorType.toUiModel(): PasswordGeneratorTypeUiModel =
    when (this) {
        PasswordGeneratorType.PASSWORD -> PasswordGeneratorTypeUiModel.PASSWORD
        PasswordGeneratorType.PASSPHRASE -> PasswordGeneratorTypeUiModel.PASSPHRASE
    }

fun CaseType.toUiModel(): CaseTypeUiModel =
    when (this) {
        CaseType.LOWERCASE -> CaseTypeUiModel.LOWERCASE
        CaseType.UPPERCASE -> CaseTypeUiModel.UPPERCASE
        CaseType.CAMELCASE -> CaseTypeUiModel.CAMELCASE
    }
