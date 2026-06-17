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

package com.passbolt.mobile.android.data.passwordexpiry

import com.passbolt.mobile.android.core.networking.RestService
import com.passbolt.mobile.android.data.passwordexpiry.datasource.memory.PasswordExpiryMemoryDataSource
import com.passbolt.mobile.android.data.passwordexpiry.datasource.remote.PasswordExpiryRemoteDataSource
import com.passbolt.mobile.android.data.passwordexpiry.datasource.remote.api.PasswordExpiryApi
import com.passbolt.mobile.android.domain.passwordexpiry.PasswordExpiryDataSource
import com.passbolt.mobile.android.domain.passwordexpiry.PasswordExpiryRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val DATA_SOURCE_MEMORY = named("memoryPasswordExpiryDataSource")
private val DATA_SOURCE_REMOTE = named("remotePasswordExpiryDataSource")

val passwordExpiryDataModule =
    module {
        single { get<RestService>().service(PasswordExpiryApi::class.java) }

        single<PasswordExpiryDataSource>(DATA_SOURCE_MEMORY) { PasswordExpiryMemoryDataSource(get()) }
        single<PasswordExpiryDataSource>(DATA_SOURCE_REMOTE) { PasswordExpiryRemoteDataSource(get(), get()) }

        single<PasswordExpiryRepository> {
            PasswordExpiryRepositoryImpl(
                memoryDataSource = get(DATA_SOURCE_MEMORY),
                remoteDataSource = get(DATA_SOURCE_REMOTE),
            )
        }
    }
