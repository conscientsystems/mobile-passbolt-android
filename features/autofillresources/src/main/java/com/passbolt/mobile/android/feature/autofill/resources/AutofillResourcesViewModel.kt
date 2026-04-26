package com.passbolt.mobile.android.feature.autofill.resources

import androidx.lifecycle.viewModelScope
import com.passbolt.mobile.android.core.accounts.usecase.accounts.GetAccountsUseCase
import com.passbolt.mobile.android.core.compose.SideEffectViewModel
import com.passbolt.mobile.android.core.mvp.coroutinecontext.CoroutineLaunchContext
import com.passbolt.mobile.android.core.otpcore.TotpParametersProvider
import com.passbolt.mobile.android.core.otpcore.TotpParametersProvider.OtpParametersResult.InvalidTotpInput
import com.passbolt.mobile.android.core.otpcore.TotpParametersProvider.OtpParametersResult.OtpParameters
import com.passbolt.mobile.android.core.resources.actions.SecretPropertiesActionsInteractor
import com.passbolt.mobile.android.core.resources.actions.performSecretPropertyAction
import com.passbolt.mobile.android.core.resources.usecase.db.GetLocalResourceUseCase
import com.passbolt.mobile.android.core.secrets.usecase.decrypt.parser.SecretJsonModel
import com.passbolt.mobile.android.feature.autofill.resources.AutofillResourcesIntent.NewResourceCreated
import com.passbolt.mobile.android.feature.autofill.resources.AutofillResourcesIntent.SelectAutofillItem
import com.passbolt.mobile.android.feature.autofill.resources.AutofillResourcesIntent.UserAuthenticated
import com.passbolt.mobile.android.feature.autofill.resources.AutofillResourcesSideEffect.AutofillReturn
import com.passbolt.mobile.android.feature.autofill.resources.AutofillResourcesSideEffect.NavigateToAuth
import com.passbolt.mobile.android.feature.autofill.resources.AutofillResourcesSideEffect.NavigateToSetup
import com.passbolt.mobile.android.feature.autofill.resources.AutofillResourcesSideEffect.ShowToast
import com.passbolt.mobile.android.feature.autofill.resources.ToastType.DECRYPTION_FAILURE
import com.passbolt.mobile.android.feature.autofill.resources.ToastType.FETCH_FAILURE
import com.passbolt.mobile.android.feature.autofill.resources.ToastType.INVALID_TOTP_PARAMETERS
import com.passbolt.mobile.android.feature.autofill.resources.datasetstrategy.AutofillPayload
import com.passbolt.mobile.android.jsonmodel.delegates.TotpSecret
import com.passbolt.mobile.android.ui.ResourceModel
import com.passbolt.mobile.android.ui.contentType
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class AutofillResourcesViewModel(
    getAccountsUseCase: GetAccountsUseCase,
    private val uri: String?,
    private val getLocalResourceUseCase: GetLocalResourceUseCase,
    private val totpParametersProvider: TotpParametersProvider,
    private val coroutineLaunchContext: CoroutineLaunchContext,
) : SideEffectViewModel<AutofillResourcesState, AutofillResourcesSideEffect>(AutofillResourcesState()),
    KoinComponent {
    init {
        if (getAccountsUseCase.execute(Unit).users.isNotEmpty()) {
            emitSideEffect(NavigateToAuth)
        } else {
            emitSideEffect(NavigateToSetup)
        }
    }

    fun onIntent(intent: AutofillResourcesIntent) {
        when (intent) {
            is UserAuthenticated -> userAuthenticated()
            is SelectAutofillItem -> selectAutofillItem(intent.resourceModel)
            is NewResourceCreated -> newResourceCreated(intent.resourceId)
        }
    }

    private fun userAuthenticated() {
        updateViewState { copy(showHome = true) }
    }

    private fun selectAutofillItem(resource: ResourceModel) {
        updateViewState { copy(showProgress = true) }
        viewModelScope.launch(coroutineLaunchContext.io) {
            val payload = buildPayload(resource)
            if (payload != null) {
                emitSideEffect(AutofillReturn(payload))
            }
            updateViewState { copy(showProgress = false) }
        }
    }

    private suspend fun buildPayload(resource: ResourceModel): AutofillPayload? {
        val contentType = resource.contentType()
        val username = resource.metadataJsonModel.username
        val secret = fetchDecryptedSecret(resource)

        val password = secret?.getPassword(contentType)
        val totpCode = secret?.totp?.let { totpCode(it) }

        return if (username == null && password == null && totpCode == null) {
            null
        } else {
            AutofillPayload(
                username = username,
                password = password,
                totpCode = totpCode,
                uri = uri,
            )
        }
    }

    private suspend fun fetchDecryptedSecret(resource: ResourceModel): SecretJsonModel? {
        val interactor: SecretPropertiesActionsInteractor = get { parametersOf(resource) }
        var secret: SecretJsonModel? = null
        performSecretPropertyAction(
            action = { interactor.provideDecryptedSecret() },
            doOnFetchFailure = { emitSideEffect(ShowToast(FETCH_FAILURE)) },
            doOnDecryptionFailure = { emitSideEffect(ShowToast(DECRYPTION_FAILURE)) },
            doOnSuccess = { secret = it.result },
        )
        return secret
    }

    private fun totpCode(totp: TotpSecret): String? =
        when (
            val parameters =
                totpParametersProvider.provideOtpParameters(
                    secretKey = totp.key,
                    digits = totp.digits,
                    period = totp.period,
                    algorithm = totp.algorithm,
                )
        ) {
            is OtpParameters -> parameters.otpValue
            InvalidTotpInput -> {
                Timber.e("Invalid TOTP parameters")
                emitSideEffect(ShowToast(INVALID_TOTP_PARAMETERS))
                null
            }
        }

    private fun newResourceCreated(resourceId: String) {
        viewModelScope.launch(coroutineLaunchContext.io) {
            selectAutofillItem(
                getLocalResourceUseCase
                    .execute(
                        GetLocalResourceUseCase.Input(resourceId),
                    ).resource,
            )
        }
    }
}
