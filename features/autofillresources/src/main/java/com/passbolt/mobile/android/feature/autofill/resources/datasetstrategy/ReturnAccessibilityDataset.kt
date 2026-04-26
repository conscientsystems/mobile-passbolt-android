package com.passbolt.mobile.android.feature.autofill.resources.datasetstrategy

import com.passbolt.mobile.android.core.autofill.accessibility.AccessibilityCommunicator

class ReturnAccessibilityDataset(
    private val autofillCallback: AutofillCallback,
    private val accessibilityCommunicator: AccessibilityCommunicator,
) : ReturnAutofillDatasetStrategy {
    override fun returnDataset(payload: AutofillPayload) {
        accessibilityCommunicator.lastFill =
            AccessibilityCommunicator.LastFill(
                username = payload.username,
                password = payload.password,
                totpCode = payload.totpCode,
                uri = payload.uri,
            )
        autofillCallback.finishAutofill()
    }
}
