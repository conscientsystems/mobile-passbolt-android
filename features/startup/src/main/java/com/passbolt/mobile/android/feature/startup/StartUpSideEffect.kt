package com.passbolt.mobile.android.feature.startup

import com.passbolt.mobile.android.ui.AccountSetupDataModel

sealed class StartUpSideEffect {
    data class NavigateToSetup(
        val accountSetupDataModel: AccountSetupDataModel?,
    ) : StartUpSideEffect()

    data object NavigateToSignIn : StartUpSideEffect()
}
