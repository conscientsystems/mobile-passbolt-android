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

package com.passbolt.mobile.android.data.resourcetypes.datasource.local

import com.passbolt.mobile.android.core.accounts.usecase.SelectedAccountUseCase
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.data.resourcetypes.mapper.toDomain
import com.passbolt.mobile.android.data.resourcetypes.mapper.toEntity
import com.passbolt.mobile.android.database.DatabaseProvider
import com.passbolt.mobile.android.domain.resourcetypes.ResourceTypesDataSource
import com.passbolt.mobile.android.domain.resourcetypes.model.ResourceType
import java.util.UUID

internal class ResourceTypesLocalDataSource(
    private val databaseProvider: DatabaseProvider,
) : ResourceTypesDataSource,
    SelectedAccountUseCase {
    private val resourceTypesDao
        get() = databaseProvider.get(selectedAccountId).resourceTypesDao()

    override suspend fun getResourceTypes(): DomainResult<List<ResourceType>> =
        DomainResult.Finished(resourceTypesDao.getAll().map { it.toDomain() })

    override suspend fun getResourceTypeIdToSlugMapping(): DomainResult<Map<UUID, String>> =
        DomainResult.Finished(
            resourceTypesDao
                .getResourceTypesIdToSlugMapping()
                .associate { UUID.fromString(it.resourceTypeId) to it.slug },
        )

    override suspend fun setResourceTypes(resourceTypes: List<ResourceType>) {
        val entities = resourceTypes.map { it.toEntity() }
        // Types are static; rewriting them invalidates every query that JOINs ResourceType (the whole
        // home list) and jumps the scroll, so skip the write when nothing changed.
        if (resourceTypesDao.getAll().toSet() != entities.toSet()) {
            resourceTypesDao.upsertAll(entities)
        }
    }
}
