package com.passbolt.mobile.android.feature.accountdetails.screen

import android.content.Context
import com.passbolt.mobile.android.core.localization.R

internal fun getProfileFetchErrorMessage(
    context: Context,
    message: String?,
): String {
    val base = context.getString(R.string.auth_error_profile_fetch_failure)
    return if (!message.isNullOrBlank()) {
        "$base($message)"
    } else {
        base
    }
}
