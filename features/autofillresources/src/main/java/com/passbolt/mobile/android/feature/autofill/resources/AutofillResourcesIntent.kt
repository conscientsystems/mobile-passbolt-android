package com.passbolt.mobile.android.feature.autofill.resources

import com.passbolt.mobile.android.ui.ResourceUiModel

sealed interface AutofillResourcesIntent {
    data object UserAuthenticated : AutofillResourcesIntent

    data class SelectAutofillItem(
        val resourceModel: ResourceUiModel,
    ) : AutofillResourcesIntent

    data class NewResourceCreated(
        val resourceId: String,
    ) : AutofillResourcesIntent
}
