package com.passbolt.mobile.android.core.ui.text

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.passbolt.mobile.android.core.compose.PassboltTheme
import com.passbolt.mobile.android.core.ui.textinputfield.StatefulInput

@PreviewTest
@Preview(showBackground = true)
@Composable
fun TextInputFilledScreenshot() {
    PassboltTheme {
        TextInput(
            title = "Name",
            hint = "Enter resource name",
            isRequired = true,
            text = "Production Database",
            state = StatefulInput.State.Default,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun TextInputEmptyScreenshot() {
    PassboltTheme {
        TextInput(
            title = "Description",
            hint = "Add a description",
            isRequired = false,
            text = "",
            state = StatefulInput.State.Default,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun TextInputErrorScreenshot() {
    PassboltTheme {
        TextInput(
            title = "Name",
            hint = "Enter resource name",
            isRequired = true,
            text = "",
            state = StatefulInput.State.Error("This field is required"),
            modifier = Modifier.padding(16.dp),
        )
    }
}
