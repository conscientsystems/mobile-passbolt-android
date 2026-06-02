package com.passbolt.mobile.android.core.ui.menu

import PassboltTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.passbolt.mobile.android.core.ui.R

@PreviewTest
@Preview(showBackground = true)
@Composable
fun SettingsItemStandardScreenshot() {
    PassboltTheme {
        OpenableSettingsItem(
            iconPainter = painterResource(R.drawable.ic_app_settings),
            title = "App settings",
            onClick = {},
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun SettingsItemWithWarningScreenshot() {
    PassboltTheme {
        OpenableSettingsItem(
            iconPainter = painterResource(R.drawable.ic_app_settings),
            title = "App settings",
            onClick = {},
            hasWarningBadge = true,
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun SettingsItemDisabledScreenshot() {
    PassboltTheme {
        OpenableSettingsItem(
            iconPainter = painterResource(R.drawable.ic_app_settings),
            title = "App settings",
            onClick = {},
            isEnabled = false,
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun SettingsItemListScreenshot() {
    PassboltTheme {
        Column {
            OpenableSettingsItem(
                iconPainter = painterResource(R.drawable.ic_app_settings),
                title = "App settings",
                onClick = {},
                hasWarningBadge = true,
            )
            OpenableSettingsItem(
                iconPainter = painterResource(R.drawable.ic_manage_accounts),
                title = "Accounts",
                onClick = {},
            )
            OpenableSettingsItem(
                iconPainter = painterResource(R.drawable.ic_terms),
                title = "Terms and licenses",
                onClick = {},
            )
            OpenableSettingsItem(
                iconPainter = painterResource(R.drawable.ic_sign_out),
                title = "Sign out",
                onClick = {},
                opensInternally = false,
            )
        }
    }
}
