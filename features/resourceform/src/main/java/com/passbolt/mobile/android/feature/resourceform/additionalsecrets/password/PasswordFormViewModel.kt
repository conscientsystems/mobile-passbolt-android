package com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password

import com.passbolt.mobile.android.core.compose.SideEffectViewModel
import com.passbolt.mobile.android.core.passwordgenerator.SecretGenerator
import com.passbolt.mobile.android.core.passwordgenerator.SecretGenerator.SecretGenerationResult.FailedToGenerateLowEntropy
import com.passbolt.mobile.android.core.passwordgenerator.SecretGenerator.SecretGenerationResult.Success
import com.passbolt.mobile.android.core.passwordgenerator.entropy.EntropyCalculator
import com.passbolt.mobile.android.domain.passwordpolicies.usecase.GetPasswordPoliciesUseCase
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.ApplyChanges
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.DismissUnableToGeneratePassword
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.GeneratePassword
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.GoBack
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.MainUriTextChanged
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.PasswordTextChanged
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.UsernameTextChanged
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormSideEffect.ApplyAndGoBack
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormSideEffect.NavigateBack
import com.passbolt.mobile.android.mappers.EntropyViewMapper
import com.passbolt.mobile.android.ui.Entropy
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeUiModel.PASSPHRASE
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeUiModel.PASSWORD
import com.passbolt.mobile.android.ui.PasswordUiModel
import com.passbolt.mobile.android.ui.ResourceFormMode

internal class PasswordFormViewModel(
    mode: ResourceFormMode,
    passwordModel: PasswordUiModel,
    private val entropyViewMapper: EntropyViewMapper,
    private val entropyCalculator: EntropyCalculator,
    private val getPasswordPoliciesUseCase: GetPasswordPoliciesUseCase,
    private val secretGenerator: SecretGenerator,
) : SideEffectViewModel<PasswordFormState, PasswordFormSideEffect>(PasswordFormState()) {
    init {
        updateViewState {
            copy(
                resourceFormMode = mode,
                password = passwordModel.password,
                mainUri = passwordModel.mainUri,
                username = passwordModel.username,
            )
        }
        launch {
            val entropy = entropyCalculator.getSecretEntropy(passwordModel.password)
            updateViewState {
                copy(
                    entropy = entropy,
                    passwordStrength = entropyViewMapper.map(Entropy.parse(entropy)),
                )
            }
        }
    }

    fun onIntent(intent: PasswordFormIntent) {
        when (intent) {
            is PasswordTextChanged -> passwordTextChanged(intent.password)
            is MainUriTextChanged -> updateViewState { copy(mainUri = intent.mainUri) }
            is UsernameTextChanged -> updateViewState { copy(username = intent.username) }
            GeneratePassword -> generatePassword()
            ApplyChanges -> applyChanges()
            GoBack -> emitSideEffect(NavigateBack)
            DismissUnableToGeneratePassword ->
                updateViewState { copy(isUnableToGeneratePasswordDialogVisible = false) }
        }
    }

    private fun passwordTextChanged(password: String) {
        updateViewState { copy(password = password) }
        launch {
            val entropy = entropyCalculator.getSecretEntropy(password)
            updateViewState {
                copy(
                    entropy = entropy,
                    passwordStrength = entropyViewMapper.map(Entropy.parse(entropy)),
                )
            }
        }
    }

    private fun generatePassword() {
        launch {
            val passwordPolicies = getPasswordPoliciesUseCase.execute(Unit)
            val result =
                when (passwordPolicies.defaultGenerator) {
                    PASSWORD -> secretGenerator.generatePassword(passwordPolicies.passwordGeneratorSettings)
                    PASSPHRASE -> secretGenerator.generatePassphrase(passwordPolicies.passphraseGeneratorSettings)
                }

            when (result) {
                is FailedToGenerateLowEntropy ->
                    updateViewState {
                        copy(
                            isUnableToGeneratePasswordDialogVisible = true,
                            minimumEntropyBits = result.minimumEntropyBits,
                        )
                    }
                is Success -> {
                    val passwordString =
                        buildString {
                            result.password.forEach { append(Character.toChars(it.value)) }
                        }
                    val strength = entropyViewMapper.map(Entropy.parse(result.entropy))
                    updateViewState {
                        copy(
                            password = passwordString,
                            entropy = result.entropy,
                            passwordStrength = strength,
                        )
                    }
                }
            }
        }
    }

    private fun applyChanges() {
        val state = viewState.value
        emitSideEffect(
            ApplyAndGoBack(
                PasswordUiModel(
                    password = state.password,
                    mainUri = state.mainUri,
                    username = state.username,
                ),
            ),
        )
    }
}
