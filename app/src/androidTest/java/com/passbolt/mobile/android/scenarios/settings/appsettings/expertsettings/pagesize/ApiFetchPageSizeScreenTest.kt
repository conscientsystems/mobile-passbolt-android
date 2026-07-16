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

package com.passbolt.mobile.android.scenarios.settings.appsettings.expertsettings.pagesize

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import com.passbolt.mobile.android.core.idlingresource.ResourcesFullRefreshIdlingResource
import com.passbolt.mobile.android.core.idlingresource.SignInIdlingResource
import com.passbolt.mobile.android.core.idlingresource.SignOutIdlingResource
import com.passbolt.mobile.android.core.navigation.ActivityIntents
import com.passbolt.mobile.android.core.navigation.AppContext
import com.passbolt.mobile.android.domain.preferences.GlobalPreferencesRepository
import com.passbolt.mobile.android.domain.preferences.GlobalPreferencesUpdate
import com.passbolt.mobile.android.domain.preferences.PreferencesDefaults
import com.passbolt.mobile.android.feature.authentication.AuthenticationMainActivity
import com.passbolt.mobile.android.helpers.getString
import com.passbolt.mobile.android.helpers.signIn
import com.passbolt.mobile.android.instrumentationTestsModule
import com.passbolt.mobile.android.intents.ManagedAccountIntentCreator
import com.passbolt.mobile.android.rules.IdlingResourceRule
import com.passbolt.mobile.android.rules.lazyActivitySetupScenarioRule
import com.passbolt.mobile.android.scenarios.setup.autofill.autofillConfiguredModuleTests
import com.passbolt.mobile.android.scenarios.setup.configurebiometric.biometricSetupUnavailableModuleTests
import com.passbolt.mobile.android.testtags.composetags.BackNavigation
import com.passbolt.mobile.android.testtags.composetags.BottomNav
import com.passbolt.mobile.android.testtags.composetags.PageSize
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.inject
import org.koin.test.KoinTest
import com.passbolt.mobile.android.core.localization.R as LocalizationR

@RunWith(AndroidJUnit4::class)
@LargeTest
class ApiFetchPageSizeScreenTest : KoinTest {
    @get:Rule(order = 1)
    val startUpActivityRule =
        lazyActivitySetupScenarioRule<AuthenticationMainActivity>(
            koinOverrideModules =
                listOf(
                    instrumentationTestsModule,
                    biometricSetupUnavailableModuleTests,
                    autofillConfiguredModuleTests,
                ),
            intentSupplier = {
                ActivityIntents.authentication(
                    getInstrumentation().targetContext,
                    ActivityIntents.AuthConfig.Startup,
                    AppContext.APP,
                    managedAccountIntentCreator.getUserLocalId(),
                )
            },
        )

    @get:Rule(order = 0)
    val composeTestRule = createEmptyComposeRule()

    private val managedAccountIntentCreator: ManagedAccountIntentCreator by inject()
    private val globalPreferencesRepository: GlobalPreferencesRepository by inject()

    @get:Rule
    val idlingResourceRule =
        let {
            val signInIdlingResource: SignInIdlingResource by inject()
            val signOutIdlingResource: SignOutIdlingResource by inject()
            val resourcesFullRefreshIdlingResource: ResourcesFullRefreshIdlingResource by inject()
            IdlingResourceRule(arrayOf(signInIdlingResource, resourcesFullRefreshIdlingResource, signOutIdlingResource))
        }

    /**
     * **#MOBILE_USER_ON_SETTINGS_PAGE:**
     * Given    I am a mobile user with the application installed
     * And      I am logged in
     * And      I am on the Settings page
     * And      the persisted API fetch page size has been reset to the default,
     *          so previous test runs don't leak state through EncryptedSharedPreferences
     */
    @Before
    fun resetPrefsAndOpenSettings() {
        setPersistedPageSize(PreferencesDefaults.API_FETCH_PAGE_SIZE)
        composeTestRule.apply {
            signIn(managedAccountIntentCreator.getPassphrase())
            onNodeWithTag(BottomNav.SETTINGS_TAB).performClick()
        }
    }

    /**
     * **API fetch page size screen shows title, description and slider bounds**
     *
     * TestRail: this test is not placed in TestRail
     *
     * Given    I am #MOBILE_USER_ON_SETTINGS_PAGE
     * When     I open the "API fetch page size" screen
     * Then     the screen title is "API fetch page size"
     * And      a description explains the memory / network trade-off
     * And      the slider min label is "50"
     * And      the slider max label is "100,000"
     * And      the headline number above the slider matches the persisted default ("2,000")
     */
    @Test
    fun pageSizeScreenShowsTitleDescriptionAndSliderBounds() {
        openPageSizeScreen()
        composeTestRule.apply {
            onNodeWithText(getString(LocalizationR.string.settings_app_settings_expert_settings_fetch_page_size))
                .assertIsDisplayed()
            onNodeWithText(getString(LocalizationR.string.settings_page_size_description))
                .assertIsDisplayed()
            onNodeWithText("50").assertIsDisplayed()
            onNodeWithText("100,000").assertIsDisplayed()
            // "2,000" mirrors PreferencesDefaults.API_FETCH_PAGE_SIZE; update both together if the default changes.
            onNodeWithTag(PageSize.HEADLINE).assertTextEquals("2,000")
        }
    }

    /**
     * **Slider opens at the currently persisted value**
     *
     * TestRail: this test is not placed in TestRail
     *
     * Given    I am #MOBILE_USER_ON_SETTINGS_PAGE
     * And      the persisted API fetch page size is 500
     * When     I open the "API fetch page size" screen
     * Then     the headline number above the slider reads "500"
     */
    @Test
    fun sliderOpensAtTheCurrentlyPersistedValue() {
        setPersistedPageSize(persistedValue = 500)
        openPageSizeScreen()
        composeTestRule.onNodeWithTag(PageSize.HEADLINE).assertTextEquals("500")
    }

    /**
     * **Slider snaps to one of the allowed page sizes**
     *
     * TestRail: this test is not placed in TestRail
     *
     * Given    I am #MOBILE_USER_ON_SETTINGS_PAGE
     * And      I am on the "API fetch page size" screen
     * When     I move the slider to a step
     * Then     the headline number above the slider reads the value formatted for that step
     * And      the persisted API fetch page size becomes the raw integer value for that step
     *
     * Cases covered: step 0 → 50, step 3 → 500, step 6 → 5,000, step 10 → 100,000.
     */
    @Test
    fun sliderSnapsToOneOfTheAllowedPageSizes() {
        openPageSizeScreen()
        // Driving the Slider via the SetProgress semantics action is the
        // accessibility-equivalent of a precise drag — robust regardless of
        // screen size / density.
        listOf(
            SliderStep(step = 0, persisted = 50, displayed = "50"),
            SliderStep(step = 3, persisted = 500, displayed = "500"),
            SliderStep(step = 6, persisted = 5_000, displayed = "5,000"),
            SliderStep(step = 10, persisted = 100_000, displayed = "100,000"),
        ).forEach { (step, persisted, displayed) ->
            composeTestRule
                .onNodeWithTag(PageSize.SLIDER)
                .performSemanticsAction(SemanticsActions.SetProgress) { it(step.toFloat()) }

            composeTestRule.onNodeWithTag(PageSize.HEADLINE).assertTextEquals(displayed)

            val actuallyPersisted = globalPreferencesRepository.getGlobalPreferences().apiFetchPageSize
            check(actuallyPersisted == persisted) {
                "Expected persisted apiFetchPageSize=$persisted after slider step $step, was $actuallyPersisted"
            }
        }
    }

    /**
     * **Selected page size survives leaving and re-entering the screen**
     *
     * TestRail: this test is not placed in TestRail
     *
     * Given    I am #MOBILE_USER_ON_SETTINGS_PAGE
     * And      I am on the "API fetch page size" screen
     * And      I have moved the slider to step 4 (1,000)
     * When     I tap the back navigation icon
     * And      I re-open the "API fetch page size" screen
     * Then     the headline number above the slider reads "1,000"
     */
    @Test
    fun selectedPageSizeSurvivesLeavingAndReEnteringTheScreen() {
        openPageSizeScreen()
        composeTestRule.apply {
            onNodeWithTag(PageSize.SLIDER)
                .performSemanticsAction(SemanticsActions.SetProgress) { it(4f) }
            // Sanity-check the in-screen change took effect before we leave.
            onNodeWithTag(PageSize.HEADLINE).assertTextEquals("1,000")

            onNode(hasTestTag(BackNavigation.ICON), useUnmergedTree = true).performClick()
            onNodeWithText(getString(LocalizationR.string.settings_app_settings_expert_settings_fetch_page_size))
                .performClick()

            onNodeWithTag(PageSize.HEADLINE).assertTextEquals("1,000")
        }
    }

    private fun setPersistedPageSize(persistedValue: Int) {
        globalPreferencesRepository.updateGlobalPreferences(
            GlobalPreferencesUpdate(apiFetchPageSize = persistedValue),
        )
    }

    private fun openPageSizeScreen() {
        composeTestRule.apply {
            onNodeWithText(getString(LocalizationR.string.settings_app_settings)).performClick()
            onNodeWithText(getString(LocalizationR.string.settings_app_settings_expert_settings)).performClick()
            onNodeWithText(getString(LocalizationR.string.settings_app_settings_expert_settings_fetch_page_size))
                .performClick()
        }
    }

    private data class SliderStep(
        val step: Int,
        val persisted: Int,
        val displayed: String,
    )
}
