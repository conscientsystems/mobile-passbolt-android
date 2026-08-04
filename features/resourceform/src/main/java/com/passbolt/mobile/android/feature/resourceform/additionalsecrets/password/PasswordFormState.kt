package com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password

import com.passbolt.mobile.android.ui.PassphraseGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeUiModel
import com.passbolt.mobile.android.ui.PasswordStrength
import com.passbolt.mobile.android.ui.ResourceFormMode

internal data class PasswordFormState(
    val resourceFormMode: ResourceFormMode? = null,
    val password: String = "",
    val passwordStrength: PasswordStrength = PasswordStrength.Empty,
    val entropy: Double = 0.0,
    val mainUri: String = "",
    val username: String = "",
    val isUnableToGeneratePasswordDialogVisible: Boolean = false,
    val minimumEntropyBits: Int = 0,
    val generatorType: PasswordGeneratorTypeUiModel? = null,
    val passwordGeneratorSettings: PasswordGeneratorSettingsUiModel? = null,
    val passphraseGeneratorSettings: PassphraseGeneratorSettingsUiModel? = null,
)
