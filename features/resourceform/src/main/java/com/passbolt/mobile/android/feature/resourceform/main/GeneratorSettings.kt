package com.passbolt.mobile.android.feature.resourceform.main

import com.passbolt.mobile.android.ui.PassphraseGeneratorSettingsModel
import com.passbolt.mobile.android.ui.PasswordGeneratorSettingsModel
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeModel

internal data class GeneratorSettings(
    val type: PasswordGeneratorTypeModel,
    val passwordSettings: PasswordGeneratorSettingsModel,
    val passphraseSettings: PassphraseGeneratorSettingsModel,
)
