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

package com.passbolt.mobile.android.data.resourcetypes

import com.google.common.truth.Truth.assertThat
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.architecture.result.DomainResult.Incomplete.Error.Reason.UNKNOWN
import com.passbolt.mobile.android.domain.resourcetypes.ResourceTypesDataSource
import com.passbolt.mobile.android.domain.resourcetypes.model.ResourceType
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
import java.util.UUID

class RefreshResourceTypesRepositoryImplTest : KoinTest {
    private val localQualifier = named("localResourceTypesDataSource")
    private val remoteQualifier = named("remoteResourceTypesDataSource")

    @get:Rule
    val koinTestRule =
        KoinTestRule.create {
            printLogger(Level.ERROR)
            modules(
                listOf(
                    module {
                        single<ResourceTypesDataSource>(localQualifier) { mock<ResourceTypesDataSource>() }
                        single<ResourceTypesDataSource>(remoteQualifier) { mock<ResourceTypesDataSource>() }
                        factory {
                            RefreshResourceTypesRepositoryImpl(
                                localDataSource = get(localQualifier),
                                remoteDataSource = get(remoteQualifier),
                            )
                        }
                    },
                ),
            )
        }

    private lateinit var local: ResourceTypesDataSource
    private lateinit var remote: ResourceTypesDataSource
    private lateinit var repository: RefreshResourceTypesRepositoryImpl

    private val resourceTypes =
        listOf(
            ResourceType(
                id = UUID.fromString("669f8c64-242a-59fb-92fc-81e660052e60"),
                slug = "password-and-description",
                name = "Password and description",
                deleted = null,
            ),
        )

    @Before
    fun setUp() {
        local = get(localQualifier)
        remote = get(remoteQualifier)
        repository = get()
    }

    @Test
    fun `refreshResourceTypes with remote success returns success and writes to local`() =
        runTest {
            remote.stub { onBlocking { getResourceTypes() }.thenReturn(DomainResult.Finished(resourceTypes)) }

            val result = repository.refreshResourceTypes()

            assertThat(result).isEqualTo(DomainResult.Finished(resourceTypes))
            verify(local).setResourceTypes(resourceTypes)
        }

    @Test
    fun `refreshResourceTypes with remote failure returns failure and does not write to local`() =
        runTest {
            val failure = DomainResult.Incomplete.Error(UNKNOWN, "boom")
            remote.stub { onBlocking { getResourceTypes() }.thenReturn(failure) }

            val result = repository.refreshResourceTypes()

            assertThat(result).isEqualTo(failure)
            verify(local, never()).setResourceTypes(resourceTypes)
        }
}
