package com.passbolt.mobile.android.feature.resourceform.main

import com.passbolt.mobile.android.common.usecase.AsyncUseCase
import com.passbolt.mobile.android.domain.passwordpolicies.usecase.GetPasswordPoliciesUseCase
import com.passbolt.mobile.android.ui.PassphraseGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorSettingsUiModel
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeUiModel

class GetOrLoadGeneratorSettingsUseCase(
    private val getPasswordPoliciesUseCase: GetPasswordPoliciesUseCase,
) : AsyncUseCase<GetOrLoadGeneratorSettingsUseCase.Input, GetOrLoadGeneratorSettingsUseCase.Output> {
    override suspend fun execute(input: Input): Output =
        if (input.type != null && input.passwordSettings != null && input.passphraseSettings != null) {
            Output(
                settings = GeneratorSettings(input.type, input.passwordSettings, input.passphraseSettings),
                wasLoaded = false,
            )
        } else {
            val policies = getPasswordPoliciesUseCase.execute(Unit)
            Output(
                settings =
                    GeneratorSettings(
                        type = policies.defaultGenerator,
                        passwordSettings = policies.passwordGeneratorSettings,
                        passphraseSettings = policies.passphraseGeneratorSettings,
                    ),
                wasLoaded = true,
            )
        }

    data class Input(
        val type: PasswordGeneratorTypeUiModel?,
        val passwordSettings: PasswordGeneratorSettingsUiModel?,
        val passphraseSettings: PassphraseGeneratorSettingsUiModel?,
    )

    data class Output(
        val settings: GeneratorSettings,
        val wasLoaded: Boolean,
    )
}
