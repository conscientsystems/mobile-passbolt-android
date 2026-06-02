package com.passbolt.mobile.android.core.ui.button

import PassboltTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.passbolt.mobile.android.core.ui.R

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PrimaryButtonEnabledScreenshot() {
    PassboltTheme {
        PrimaryButton(
            text = "Sign in",
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun PrimaryButtonDisabledScreenshot() {
    PassboltTheme {
        PrimaryButton(
            text = "Sign in",
            onClick = {},
            isEnabled = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun SecondaryButtonScreenshot() {
    PassboltTheme {
        SecondaryButton(
            onClick = {},
            text = "Sign out",
            icon = painterResource(id = R.drawable.ic_sign_out),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewTest
@Preview(
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun PrimaryButtonDarkThemeScreenshot() {
    PassboltTheme(darkTheme = true) {
        PrimaryButton(
            text = "Sign in",
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
