package com.passbolt.mobile.android.feature.resourceform.navigation

import com.passbolt.mobile.android.ui.AdditionalUrisUiModel
import com.passbolt.mobile.android.ui.CustomFieldsUiModel
import com.passbolt.mobile.android.ui.PassphraseGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeUiModel
import com.passbolt.mobile.android.ui.PasswordUiModel
import com.passbolt.mobile.android.ui.PinCodeUiModel
import com.passbolt.mobile.android.ui.ResourceAppearanceModel
import com.passbolt.mobile.android.ui.TotpUiModel

data class PasswordFormResult(
    val model: PasswordUiModel,
)

data class TotpFormResult(
    val totpUiModel: TotpUiModel?,
)

data class TotpAdvancedSettingsFormResult(
    val totpModel: TotpUiModel,
)

data class NoteFormResult(
    val note: String?,
)

data class DescriptionFormResult(
    val metadataDescription: String,
)

data class AdditionalUrisFormResult(
    val model: AdditionalUrisUiModel,
)

data class AppearanceFormResult(
    val model: ResourceAppearanceModel,
)

data class CustomFieldsFormResult(
    val model: CustomFieldsUiModel,
)

data class PinCodeFormResult(
    val pinCodeUiModel: PinCodeUiModel?,
)

data class PinCodeAdvancedGenerationFormResult(
    val pinCodeUiModel: PinCodeUiModel,
)

data class AdvancedSecretGenerationFormResult(
    val passwordSettings: PasswordGeneratorSettingsUiModel,
    val passphraseSettings: PassphraseGeneratorSettingsUiModel,
    val selectedTab: PasswordGeneratorTypeUiModel,
    val generatedSecret: String,
)
