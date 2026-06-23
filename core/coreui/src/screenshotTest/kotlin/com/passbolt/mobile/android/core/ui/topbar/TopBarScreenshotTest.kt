package com.passbolt.mobile.android.core.ui.topbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.passbolt.mobile.android.core.compose.PassboltTheme

@PreviewTest
@Preview(showBackground = true)
@Composable
fun TitleAppBarScreenshot() {
    PassboltTheme {
        TitleAppBar(title = "Settings")
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun TitleAppBarWithBackScreenshot() {
    PassboltTheme {
        TitleAppBar(
            title = "App settings",
            navigationIcon = { BackNavigationIcon(onBackClick = {}) },
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun TitleAppBarLongTitleScreenshot() {
    PassboltTheme {
        TitleAppBar(
            title = "A very long title that should be truncated with ellipsis",
            navigationIcon = { BackNavigationIcon(onBackClick = {}) },
        )
    }
}
