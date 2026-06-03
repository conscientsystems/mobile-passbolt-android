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

package com.passbolt.mobile.android.domain.passwordpolicies.usecase

import com.google.common.truth.Truth.assertThat
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.domain.passwordpolicies.PasswordPoliciesRepository
import com.passbolt.mobile.android.domain.passwordpolicies.mapper.toUiModel
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
import org.mockito.kotlin.stub

class GetPasswordPoliciesUseCaseTest : KoinTest {
    @get:Rule
    val koinTestRule =
        KoinTestRule.create {
            printLogger(Level.ERROR)
            modules(
                listOf(
                    module {
                        single { mock<PasswordPoliciesRepository>() }
                        factoryOf(::GetPasswordPoliciesUseCase)
                    },
                ),
            )
        }

    private lateinit var repository: PasswordPoliciesRepository
    private lateinit var useCase: GetPasswordPoliciesUseCase

    @Before
    fun setUp() {
        repository = get()
        useCase = get()
    }

    @Test
    fun `success returns repository value mapped to ui model`() =
        runTest {
            val policies = PasswordPolicies.defaults()
            repository.stub {
                onBlocking { getPasswordPolicies() }.thenReturn(DomainResult.Success(policies))
            }

            val result = useCase.execute(Unit)

            assertThat(result).isEqualTo(policies.toUiModel())
        }

    @Test
    fun `failure falls back to defaults mapped to ui model`() =
        runTest {
            repository.stub {
                onBlocking { getPasswordPolicies() }.thenReturn(DomainResult.Failure.Unknown(RuntimeException()))
            }

            val result = useCase.execute(Unit)

            assertThat(result).isEqualTo(PasswordPolicies.defaults().toUiModel())
        }

    @Test
    fun `notfound failure also falls back to defaults`() =
        runTest {
            repository.stub {
                onBlocking { getPasswordPolicies() }.thenReturn(DomainResult.Failure.NotFound)
            }

            val result = useCase.execute(Unit)

            assertThat(result).isEqualTo(PasswordPolicies.defaults().toUiModel())
        }
}
