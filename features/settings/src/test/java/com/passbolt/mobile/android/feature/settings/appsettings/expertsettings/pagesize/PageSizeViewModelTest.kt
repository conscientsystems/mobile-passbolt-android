package com.passbolt.mobile.android.feature.settings.appsettings.expertsettings.pagesize

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
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.passbolt.mobile.android.domain.preferences.GlobalPreferencesRepository
import com.passbolt.mobile.android.domain.preferences.GlobalPreferencesUpdate
import com.passbolt.mobile.android.domain.preferences.PreferencesDefaults
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize.PageSizeIntent.GoBack
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize.PageSizeIntent.PageSizeChanged
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize.PageSizeSideEffect.NavigateBack
import com.passbolt.mobile.android.feature.settings.screen.appsettings.expertsettings.pagesize.PageSizeViewModel
import com.passbolt.mobile.android.ui.GlobalPreferencesUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class PageSizeViewModelTest : KoinTest {
    @get:Rule
    val koinTestRule =
        KoinTestRule.create {
            printLogger(Level.ERROR)
            modules(
                listOf(
                    module {
                        single { mock<GlobalPreferencesRepository>() }
                        factory { PageSizeViewModel(get()) }
                    },
                ),
            )
        }

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: PageSizeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun stubPreferences() {
        val globalPreferencesRepository: GlobalPreferencesRepository = get()
        whenever(globalPreferencesRepository.getGlobalPreferences()) doReturn
            GlobalPreferencesUiModel(
                areDebugLogsEnabled = false,
                debugLogFileCreationDateTime = null,
                isHideRootDialogEnabled = true,
                isAuthRequiredOnEveryEntry = true,
                debugLogLastAppVersion = null,
                apiFetchPageSize = PreferencesDefaults.API_FETCH_PAGE_SIZE,
                accessibilityPoliciesConsentGiven = true,
            )
    }

    @Test
    fun `initial state should show correct index when stored value matches`() =
        runTest {
            stubPreferences()
            viewModel = get()

            val state = viewModel.viewState.value

            assertThat(state.selectedIndex).isEqualTo(5)
        }

    @Test
    fun `initial state should not re-save when stored value matches an allowed size`() =
        runTest {
            stubPreferences()
            viewModel = get()

            val globalPreferencesRepository: GlobalPreferencesRepository = get()
            verify(globalPreferencesRepository, never()).updateGlobalPreferences(any())
        }

    @Test
    fun `changing page size should save immediately and update state`() =
        runTest {
            stubPreferences()
            viewModel = get()

            viewModel.onIntent(PageSizeChanged(sliderIndex = 6))

            val state = viewModel.viewState.value
            assertThat(state.selectedIndex).isEqualTo(6)

            val globalPreferencesRepository: GlobalPreferencesRepository = get()
            argumentCaptor<GlobalPreferencesUpdate> {
                verify(globalPreferencesRepository).updateGlobalPreferences(capture())
                assertThat(firstValue.apiFetchPageSize).isEqualTo(5000)
            }
        }

    @Test
    fun `go back intent should emit navigate back side effect`() =
        runTest {
            stubPreferences()
            viewModel = get()

            viewModel.sideEffect.test {
                viewModel.onIntent(GoBack)
                assertThat(awaitItem()).isEqualTo(NavigateBack)
            }
        }
}
