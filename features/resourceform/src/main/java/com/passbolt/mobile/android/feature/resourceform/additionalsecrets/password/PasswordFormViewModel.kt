package com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password

import com.passbolt.mobile.android.core.compose.SideEffectViewModel
import com.passbolt.mobile.android.core.passwordgenerator.SecretGenerator
import com.passbolt.mobile.android.core.passwordgenerator.SecretGenerator.SecretGenerationResult.FailedToGenerateLowEntropy
import com.passbolt.mobile.android.core.passwordgenerator.SecretGenerator.SecretGenerationResult.Success
import com.passbolt.mobile.android.core.passwordgenerator.entropy.EntropyCalculator
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.AdvancedSecretGenerationResult
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.ApplyChanges
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.DismissUnableToGeneratePassword
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.GeneratePassword
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.GoBack
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.MainUriTextChanged
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.OpenAdvancedSecretGeneration
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.PasswordTextChanged
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormIntent.UsernameTextChanged
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormSideEffect.ApplyAndGoBack
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormSideEffect.NavigateBack
import com.passbolt.mobile.android.feature.resourceform.additionalsecrets.password.PasswordFormSideEffect.NavigateToAdvancedSecretGeneration
import com.passbolt.mobile.android.feature.resourceform.main.GeneratorSettings
import com.passbolt.mobile.android.feature.resourceform.main.GetOrLoadGeneratorSettingsUseCase
import com.passbolt.mobile.android.feature.resourceform.main.GetOrLoadGeneratorSettingsUseCase.Input
import com.passbolt.mobile.android.feature.resourceform.navigation.AdvancedSecretGenerationFormResult
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
    private val getOrLoadGeneratorSettingsUseCase: GetOrLoadGeneratorSettingsUseCase,
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
            OpenAdvancedSecretGeneration -> openAdvancedSecretGeneration()
            is AdvancedSecretGenerationResult -> advancedSecretGenerationResult(intent.result)
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
            val (type, passwordSettings, passphraseSettings) = getOrLoadGeneratorSettings()
            val result =
                when (type) {
                    PASSWORD -> secretGenerator.generatePassword(passwordSettings)
                    PASSPHRASE -> secretGenerator.generatePassphrase(passphraseSettings)
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

    private fun openAdvancedSecretGeneration() {
        launch {
            val (type, passwordSettings, passphraseSettings) = getOrLoadGeneratorSettings()
            emitSideEffect(
                NavigateToAdvancedSecretGeneration(
                    selectedTab = type,
                    passwordSettings = passwordSettings,
                    passphraseSettings = passphraseSettings,
                ),
            )
        }
    }

    private fun advancedSecretGenerationResult(result: AdvancedSecretGenerationFormResult) {
        updateViewState {
            copy(
                generatorType = result.selectedTab,
                passwordGeneratorSettings = result.passwordSettings,
                passphraseGeneratorSettings = result.passphraseSettings,
            )
        }
        launch {
            val entropy = entropyCalculator.getSecretEntropy(result.generatedSecret)
            updateViewState {
                copy(
                    password = result.generatedSecret,
                    entropy = entropy,
                    passwordStrength = entropyViewMapper.map(Entropy.parse(entropy)),
                )
            }
        }
    }

    private suspend fun getOrLoadGeneratorSettings(): GeneratorSettings {
        val state = viewState.value
        val (settings, wasLoaded) =
            getOrLoadGeneratorSettingsUseCase.execute(
                Input(
                    type = state.generatorType,
                    passwordSettings = state.passwordGeneratorSettings,
                    passphraseSettings = state.passphraseGeneratorSettings,
                ),
            )
        if (wasLoaded) {
            updateViewState {
                copy(
                    generatorType = settings.type,
                    passwordGeneratorSettings = settings.passwordSettings,
                    passphraseGeneratorSettings = settings.passphraseSettings,
                )
            }
        }
        return settings
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
