package com.passbolt.mobile.android.domain.resources.actions

import com.passbolt.mobile.android.ui.ResourceUiModel

fun interface SecretPropertiesActionsInteractorFactory {
    fun create(resource: ResourceUiModel): SecretPropertiesActionsInteractor
}
