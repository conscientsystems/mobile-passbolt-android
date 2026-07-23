package com.passbolt.mobile.android.data.accounts

import android.content.Context
import com.passbolt.mobile.android.data.accounts.datasource.local.AccountDataLocalDataSourceImpl
import com.passbolt.mobile.android.data.accounts.datasource.local.AccountsLocalDataSourceImpl
import com.passbolt.mobile.android.data.accounts.datasource.local.Constants
import com.passbolt.mobile.android.data.accounts.datasource.local.SelectedAccountLocalDataSourceImpl
import com.passbolt.mobile.android.domain.accounts.AccountDataRepository
import com.passbolt.mobile.android.domain.accounts.AccountsRepository
import com.passbolt.mobile.android.domain.accounts.SelectedAccountRepository
import com.passbolt.mobile.android.domain.accounts.datasource.AccountDataLocalDataSource
import com.passbolt.mobile.android.domain.accounts.datasource.AccountsLocalDataSource
import com.passbolt.mobile.android.domain.accounts.datasource.SelectedAccountLocalDataSource
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

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
val accountsDataModule =
    module {
        single<AccountsLocalDataSource> {
            AccountsLocalDataSourceImpl(
                sharedPreferences =
                    androidApplication().getSharedPreferences(Constants.ACCOUNTS_PREFERENCES_NAME, Context.MODE_PRIVATE),
            )
        }
        singleOf(::AccountsRepositoryImpl) bind AccountsRepository::class
        singleOf(::AccountDataLocalDataSourceImpl) bind AccountDataLocalDataSource::class
        singleOf(::AccountDataRepositoryImpl) bind AccountDataRepository::class
        singleOf(::SelectedAccountLocalDataSourceImpl) bind SelectedAccountLocalDataSource::class
        singleOf(::SelectedAccountRepositoryImpl) bind SelectedAccountRepository::class
    }
