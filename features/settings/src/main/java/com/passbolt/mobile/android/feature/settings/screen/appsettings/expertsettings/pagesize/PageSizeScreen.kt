/**
 * Passbolt - Open source password manager for teams
 * Copyright (c) 2021 Passbolt SA
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

package com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.passbolt.mobile.android.core.compose.SideEffectDispatcher
import com.passbolt.mobile.android.core.navigation.compose.AppNavigator
import com.passbolt.mobile.android.core.ui.topbar.BackNavigationIcon
import com.passbolt.mobile.android.core.ui.topbar.TitleAppBar
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize.PageSizeIntent.GoBack
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize.PageSizeIntent.PageSizeChanged
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize.PageSizeSideEffect.NavigateBack
import com.passbolt.mobile.android.testtags.composetags.PageSize
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt
import com.passbolt.mobile.android.core.localization.R as LocalizationR

@Composable
internal fun PageSizeScreen(
    modifier: Modifier = Modifier,
    navigator: AppNavigator = koinInject(),
    viewModel: PageSizeViewModel = koinViewModel(),
) {
    val state = viewModel.viewState.collectAsStateWithLifecycle()

    SideEffectDispatcher(viewModel.sideEffect) {
        when (it) {
            NavigateBack -> navigator.navigateBack()
        }
    }

    PageSizeScreen(
        modifier = modifier,
        state = state.value,
        allowedPageSizes = ALLOWED_PAGE_SIZES,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun PageSizeScreen(
    state: PageSizeState,
    allowedPageSizes: List<Int>,
    onIntent: (PageSizeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TitleAppBar(
                title = stringResource(LocalizationR.string.settings_app_settings_expert_settings_fetch_page_size),
                navigationIcon = { BackNavigationIcon(onBackClick = { onIntent(GoBack) }) },
            )
        },
        content = { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Text(
                    text = stringResource(LocalizationR.string.settings_page_size_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )

                PageSizeSlider(
                    selectedIndex = state.selectedIndex,
                    allowedPageSizes = allowedPageSizes,
                    onSliderIndexChange = { onIntent(PageSizeChanged(it)) },
                )
            }
        },
    )
}

@Composable
private fun PageSizeSlider(
    selectedIndex: Int,
    allowedPageSizes: List<Int>,
    onSliderIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val numberFormat = remember(locale) { NumberFormat.getNumberInstance(locale) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = numberFormat.format(allowedPageSizes[selectedIndex]),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .testTag(PageSize.HEADLINE),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = selectedIndex.toFloat(),
            onValueChange = { onSliderIndexChange(it.roundToInt()) },
            valueRange = 0f..allowedPageSizes.lastIndex.toFloat(),
            steps = allowedPageSizes.size - 2,
            modifier = Modifier.testTag(PageSize.SLIDER),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = numberFormat.format(allowedPageSizes.first()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = numberFormat.format(allowedPageSizes.last()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PageSizeScreenPreview() {
    PageSizeScreen(
        state = PageSizeState(),
        allowedPageSizes = ALLOWED_PAGE_SIZES,
        onIntent = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun PageSizeScreenSmallValuePreview() {
    PageSizeScreen(
        state = PageSizeState(selectedIndex = 2),
        allowedPageSizes = ALLOWED_PAGE_SIZES,
        onIntent = {},
    )
}
