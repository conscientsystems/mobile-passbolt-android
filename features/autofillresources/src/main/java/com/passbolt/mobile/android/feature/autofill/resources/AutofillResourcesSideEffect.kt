package com.passbolt.mobile.android.feature.autofill.resources

import com.passbolt.mobile.android.feature.autofill.resources.datasetstrategy.AutofillPayload

sealed interface AutofillResourcesSideEffect {
    data object NavigateToAuth : AutofillResourcesSideEffect

    data object NavigateToSetup : AutofillResourcesSideEffect

    data class ShowToast(
        val type: ToastType,
    ) : AutofillResourcesSideEffect

    data class AutofillReturn(
        val payload: AutofillPayload,
    ) : AutofillResourcesSideEffect
}

enum class ToastType {
    DECRYPTION_FAILURE,
    FETCH_FAILURE,
    INVALID_TOTP_PARAMETERS,
}
