package com.passbolt.mobile.android.feature.otp.screen

import com.passbolt.mobile.android.core.navigation.AppContext
import com.passbolt.mobile.android.feature.home.screen.ResourceHandlingStrategy
import com.passbolt.mobile.android.feature.home.screen.ShowSuggestedModel
import com.passbolt.mobile.android.ui.ResourceModel

class OtpResourceHandlingStrategy(
    private val onItemClick: (ResourceModel) -> Unit,
) : ResourceHandlingStrategy {
    override val appContext: AppContext = AppContext.APP

    override fun resourceItemClick(resourceModel: ResourceModel) {
        onItemClick(resourceModel)
    }

    override fun shouldShowResourceMoreMenu() = true

    override fun shouldShowCloseButton() = false

    override fun showSuggestedModel() = ShowSuggestedModel.DoNotShow

    override fun resourcePostCreateAction(resourceId: String) {
        // no-op
    }

    override fun shouldShowFolderMoreMenu() = true
}
