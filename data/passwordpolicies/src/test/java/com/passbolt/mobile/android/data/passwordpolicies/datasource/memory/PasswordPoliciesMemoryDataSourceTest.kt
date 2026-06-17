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

package com.passbolt.mobile.android.data.passwordpolicies.datasource.memory

import com.google.common.truth.Truth.assertThat
import com.passbolt.mobile.android.core.accounts.usecase.selectedaccount.GetSelectedAccountUseCase
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.domain.passwordpolicies.model.PasswordPolicies
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PasswordPoliciesMemoryDataSourceTest : KoinTest {
    @get:Rule
    val koinTestRule =
        KoinTestRule.create {
            printLogger(Level.ERROR)
            modules(
                listOf(
                    module {
                        single { mock<GetSelectedAccountUseCase>() }
                        factoryOf(::PasswordPoliciesMemoryDataSource)
                    },
                ),
            )
        }

    private lateinit var getSelectedAccountUseCase: GetSelectedAccountUseCase
    private lateinit var dataSource: PasswordPoliciesMemoryDataSource
    private val defaultPolicies = PasswordPolicies.defaults()
    private val policiesWithDisabledExternalCheck = defaultPolicies.copy(isExternalDictionaryCheckEnabled = false)

    @Before
    fun setUp() {
        getSelectedAccountUseCase = get()
        dataSource = get()
    }

    private fun mockSelectedAccount(userId: String) {
        whenever(getSelectedAccountUseCase.execute(Unit))
            .thenReturn(GetSelectedAccountUseCase.Output(userId))
    }

    @Test
    fun `get on an empty cache is a miss`() =
        runTest {
            mockSelectedAccount("userA")

            assertThat(dataSource.getPasswordPolicies()).isEqualTo(DomainResult.Failure.NotCached)
        }

    @Test
    fun `cached value is returned for the same account`() =
        runTest {
            mockSelectedAccount("userA")
            dataSource.setPasswordPolicies(defaultPolicies)

            assertThat(dataSource.getPasswordPolicies()).isEqualTo(DomainResult.Success(defaultPolicies))
        }

    @Test
    fun `cached value of one account is not served to another account`() =
        runTest {
            mockSelectedAccount("userA")
            dataSource.setPasswordPolicies(defaultPolicies)

            mockSelectedAccount("userB")

            assertThat(dataSource.getPasswordPolicies()).isEqualTo(DomainResult.Failure.NotCached)
        }

    @Test
    fun `each account retains its own cached value`() =
        runTest {
            mockSelectedAccount("userA")
            dataSource.setPasswordPolicies(defaultPolicies)
            mockSelectedAccount("userB")
            dataSource.setPasswordPolicies(policiesWithDisabledExternalCheck)

            mockSelectedAccount("userA")
            assertThat(dataSource.getPasswordPolicies()).isEqualTo(DomainResult.Success(defaultPolicies))
            mockSelectedAccount("userB")
            assertThat(dataSource.getPasswordPolicies()).isEqualTo(DomainResult.Success(policiesWithDisabledExternalCheck))
        }
}
