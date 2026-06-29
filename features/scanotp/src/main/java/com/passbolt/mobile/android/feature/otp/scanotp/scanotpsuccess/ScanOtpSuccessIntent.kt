package com.passbolt.mobile.android.feature.otp.scanotp.scanotpsuccess

import com.passbolt.mobile.android.ui.ResourceUiModel

sealed interface ScanOtpSuccessIntent {
    data object CreateStandaloneOtpClick : ScanOtpSuccessIntent

    data object LinkToResourceClick : ScanOtpSuccessIntent

    data class LinkedResourceReceived(
        val resource: ResourceUiModel,
    ) : ScanOtpSuccessIntent

    data object TrustNewMetadataKey : ScanOtpSuccessIntent

    data object TrustedMetadataKeyDeleted : ScanOtpSuccessIntent

    data object DismissNewMetadataTrustDialog : ScanOtpSuccessIntent

    data object DismissTrustedMetadataKeyDeletedDialog : ScanOtpSuccessIntent
}
