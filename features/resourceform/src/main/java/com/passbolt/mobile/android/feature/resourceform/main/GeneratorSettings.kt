package com.passbolt.mobile.android.feature.resourceform.main

import com.passbolt.mobile.android.ui.PassphraseGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeUiModel

data class GeneratorSettings(
    val type: PasswordGeneratorTypeUiModel,
    val passwordSettings: PasswordGeneratorSettingsUiModel,
    val passphraseSettings: PassphraseGeneratorSettingsUiModel,
)
