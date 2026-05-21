/**
 * Passbolt - Open source password manager for teams
 * Copyright (c) 2026 Passbolt SA
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License (AGPL) as published by the Free Software Foundation version 3.
 *
 * The name "Passbolt" is a registered trademark of Passbolt SA, and Passbolt SA hereby declines to grant a trademark
 * license to "Passbolt" pursuant to the GNU Affero General Public License version 3 Section 7(e), without a separate
 * agreement with Passbolt SA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see GNU Affero General Public License v3 (http://www.gnu.org/licenses/agpl-3.0.html).
 *
 * @copyright Copyright (c) Passbolt SA (https://www.passbolt.com)
 * @license https://opensource.org/licenses/AGPL-3.0 AGPL License
 * @link https://www.passbolt.com Passbolt (tm)
 * @since v1.0
 */

package com.passbolt.mobile.android.feature.resourceform.additionalsecrets.secret.advanced.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeModel
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeModel.PASSPHRASE
import com.passbolt.mobile.android.ui.PasswordGeneratorTypeModel.PASSWORD
import com.passbolt.mobile.android.core.localization.R as LocalizationR
import com.passbolt.mobile.android.core.ui.R as CoreUiR

@Composable
internal fun SecretPreviewSection(
    selectedTab: PasswordGeneratorTypeModel,
    preview: String,
    minimumEntropyBits: Int?,
    modifier: Modifier = Modifier,
) {
    val title =
        when (selectedTab) {
            PASSWORD ->
                stringResource(LocalizationR.string.resource_form_advanced_password_generation_password_preview)
            PASSPHRASE ->
                stringResource(LocalizationR.string.resource_form_advanced_password_generation_passphrase_preview)
        }
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = preview,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors =
                MaterialTheme.colorScheme.surfaceVariant.let {
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = it,
                        unfocusedContainerColor = it,
                    )
                },
        )
        if (minimumEntropyBits != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text =
                    stringResource(LocalizationR.string.dialog_unable_to_generate_password_message, minimumEntropyBits),
                color = colorResource(CoreUiR.color.red),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
