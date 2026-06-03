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

package com.passbolt.mobile.android.data.passwordpolicies

import com.google.common.truth.Truth.assertThat
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.domain.passwordpolicies.PasswordPoliciesDataSource
import com.passbolt.mobile.android.domain.passwordpolicies.model.PasswordPolicies
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.get
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

class PasswordPoliciesRepositoryImplTest : KoinTest {
    private val memoryQualifier = named("memoryPasswordPoliciesDataSource")
    private val remoteQualifier = named("remotePasswordPoliciesDataSource")

    @get:Rule
    val koinTestRule =
        KoinTestRule.create {
            printLogger(Level.ERROR)
            modules(
                listOf(
                    module {
                        single<PasswordPoliciesDataSource>(memoryQualifier) { mock<PasswordPoliciesDataSource>() }
                        single<PasswordPoliciesDataSource>(remoteQualifier) { mock<PasswordPoliciesDataSource>() }
                        factory {
                            PasswordPoliciesRepositoryImpl(
                                memoryDataSource = get(memoryQualifier),
                                remoteDataSource = get(remoteQualifier),
                            )
                        }
                    },
                ),
            )
        }

    private lateinit var memory: PasswordPoliciesDataSource
    private lateinit var remote: PasswordPoliciesDataSource
    private lateinit var repository: PasswordPoliciesRepositoryImpl
    private val policies = PasswordPolicies.defaults()

    @Before
    fun setUp() {
        memory = get(memoryQualifier)
        remote = get(remoteQualifier)
        repository = get()
    }

    @Test
    fun `memory hit returns memory value and never calls remote`() =
        runTest {
            memory.stub { onBlocking { getPasswordPolicies() }.thenReturn(DomainResult.Success(policies)) }

            val result = repository.getPasswordPolicies()

            assertThat(result).isEqualTo(DomainResult.Success(policies))
            verify(remote, never()).getPasswordPolicies()
        }

    @Test
    fun `memory miss with remote success returns success and writes to memory`() =
        runTest {
            memory.stub { onBlocking { getPasswordPolicies() }.thenReturn(DomainResult.Failure.NotFound) }
            remote.stub { onBlocking { getPasswordPolicies() }.thenReturn(DomainResult.Success(policies)) }

            val result = repository.getPasswordPolicies()

            assertThat(result).isEqualTo(DomainResult.Success(policies))
            verify(memory).setPasswordPolicies(policies)
        }

    @Test
    fun `memory miss with remote failure returns failure and does not write to memory`() =
        runTest {
            val failure = DomainResult.Failure.Unknown(RuntimeException("boom"))
            memory.stub { onBlocking { getPasswordPolicies() }.thenReturn(DomainResult.Failure.NotFound) }
            remote.stub { onBlocking { getPasswordPolicies() }.thenReturn(failure) }

            val result = repository.getPasswordPolicies()

            assertThat(result).isEqualTo(failure)
            verify(memory, never()).setPasswordPolicies(policies)
        }

    @Test
    fun `setPasswordPolicies writes through to memory`() =
        runTest {
            repository.setPasswordPolicies(policies)

            verify(memory).setPasswordPolicies(policies)
        }
}
