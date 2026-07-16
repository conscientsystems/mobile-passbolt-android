package com.passbolt.mobile.android.feature.home.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.passbolt.mobile.android.core.navigation.compose.HomeNavigation
import com.passbolt.mobile.android.domain.preferences.AccountPreferencesRepository
import com.passbolt.mobile.android.mappers.HomeDisplayViewMapper
import org.koin.compose.koinInject

@Composable
fun HomeTabContent() {
    val accountPreferencesRepository: AccountPreferencesRepository = koinInject()
    val homeDisplayMapper: HomeDisplayViewMapper = koinInject()
    val initialHomeDisplay =
        remember {
            val prefs = accountPreferencesRepository.getHomeDisplayViewPreferences()
            homeDisplayMapper.map(prefs.userSetHomeView, prefs.lastUsedHomeView)
        }
    HomeNavigation(initialHomeDisplay)
}
