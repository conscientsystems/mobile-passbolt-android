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

package com.passbolt.mobile.android.data.preferences.datasource.local

import com.passbolt.mobile.android.core.accounts.usecase.SelectedAccountUseCase
import com.passbolt.mobile.android.data.preferences.AccountPreferencesFileName
import com.passbolt.mobile.android.data.preferences.KEY_CHROME_NATIVE_AUTOFILL_DIALOG_SHOWN
import com.passbolt.mobile.android.data.preferences.KEY_LAST_USED_HOME_VIEW
import com.passbolt.mobile.android.data.preferences.KEY_USER_SET_HOME_VIEW
import com.passbolt.mobile.android.domain.preferences.AccountFlagsUpdate
import com.passbolt.mobile.android.domain.preferences.AccountPreferencesLocalDataSource
import com.passbolt.mobile.android.domain.preferences.HomeDisplayViewPreferencesUpdate
import com.passbolt.mobile.android.encryptedstorage.EncryptedSharedPreferencesFactory
import com.passbolt.mobile.android.ui.AccountFlagsUiModel
import com.passbolt.mobile.android.ui.DefaultFilterUiModel
import com.passbolt.mobile.android.ui.HomeDisplayViewPreferencesUiModel
import com.passbolt.mobile.android.ui.HomeDisplayViewUiModel

internal class AccountPreferencesLocalDataSourceImpl(
    private val encryptedSharedPreferencesFactory: EncryptedSharedPreferencesFactory,
) : AccountPreferencesLocalDataSource,
    SelectedAccountUseCase {
    private val sharedPreferences
        get() =
            encryptedSharedPreferencesFactory.get("${AccountPreferencesFileName(selectedAccountId).name}.xml")

    override fun getHomeDisplayViewPreferences(): HomeDisplayViewPreferencesUiModel {
        with(sharedPreferences) {
            val lastUsedHomeViewOrdinal = getInt(KEY_LAST_USED_HOME_VIEW, DEFAULT_LAST_USED_FILTER_ORDINAL)
            val lastUsedHomeView = HomeDisplayViewUiModel.entries[lastUsedHomeViewOrdinal]

            val userSetHomeViewOrdinal = getInt(KEY_USER_SET_HOME_VIEW, -1)
            val userSetHomeView =
                if (userSetHomeViewOrdinal != -1) {
                    DefaultFilterUiModel.entries[userSetHomeViewOrdinal]
                } else {
                    DefaultFilterUiModel.LAST_USED
                }

            return HomeDisplayViewPreferencesUiModel(
                lastUsedHomeView = lastUsedHomeView,
                userSetHomeView = userSetHomeView,
            )
        }
    }

    override fun updateHomeDisplayViewPreferences(update: HomeDisplayViewPreferencesUpdate) {
        with(sharedPreferences.edit()) {
            update.lastUsedHomeView?.let { putInt(KEY_LAST_USED_HOME_VIEW, it.ordinal) }
            update.userSetHomeView?.let { putInt(KEY_USER_SET_HOME_VIEW, it.ordinal) }
            apply()
        }
    }

    override fun getAccountFlags(): AccountFlagsUiModel {
        with(sharedPreferences) {
            return AccountFlagsUiModel(
                wasChromeNativeAutofillDialogShown = getBoolean(KEY_CHROME_NATIVE_AUTOFILL_DIALOG_SHOWN, false),
            )
        }
    }

    override fun updateAccountFlags(update: AccountFlagsUpdate) {
        with(sharedPreferences.edit()) {
            update.wasChromeNativeAutofillDialogShown?.let { putBoolean(KEY_CHROME_NATIVE_AUTOFILL_DIALOG_SHOWN, it) }
            apply()
        }
    }

    private companion object {
        private val DEFAULT_LAST_USED_FILTER_ORDINAL = HomeDisplayViewUiModel.ALL_ITEMS.ordinal
    }
}
