package com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password

import com.passbolt.mobile.android.ui.PassphraseGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeUiModel
import com.passbolt.mobile.android.ui.PasswordUiModel

internal sealed interface PasswordFormSideEffect {
    data object NavigateBack : PasswordFormSideEffect

    data class ApplyAndGoBack(
        val model: PasswordUiModel,
    ) : PasswordFormSideEffect

    data class NavigateToAdvancedSecretGeneration(
        val selectedTab: PasswordGeneratorTypeUiModel,
        val passwordSettings: PasswordGeneratorSettingsUiModel,
        val passphraseSettings: PassphraseGeneratorSettingsUiModel,
    ) : PasswordFormSideEffect
}
