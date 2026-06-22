package com.passbolt.mobile.android.core.resources.usecase

import com.passbolt.mobile.android.common.usecase.AsyncUseCase
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.CompleteAuthenticatedOutput
import com.passbolt.mobile.android.core.mvp.authentication.IncompleteAuthenticatedOutput
import com.passbolt.mobile.android.core.networking.toDomainResult
import com.passbolt.mobile.android.core.resourcetypes.usecase.db.ResourceTypeIdToSlugMappingProvider
import com.passbolt.mobile.android.dto.response.Pagination
import com.passbolt.mobile.android.mappers.PermissionsModelMapper
import com.passbolt.mobile.android.mappers.ResourceModelMapper
import com.passbolt.mobile.android.mappers.TagsModelMapper
import com.passbolt.mobile.android.passboltapi.resource.ResourceRepository
import com.passbolt.mobile.android.ui.ResourceModelWithAttributes

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
class GetResourcesPaginatedUseCase(
    private val resourceRepository: ResourceRepository,
    private val resourceModelMapper: ResourceModelMapper,
    private val tagModelMapper: TagsModelMapper,
    private val permissionsModelMapper: PermissionsModelMapper,
    private val resourceTypeIdToSlugMappingProvider: ResourceTypeIdToSlugMappingProvider,
) : AsyncUseCase<GetResourcesPaginatedUseCase.Input, GetResourcesPaginatedUseCase.Output> {
    override suspend fun execute(input: Input): Output {
        val slugMapping = resourceTypeIdToSlugMappingProvider.provideMappingForSelectedAccount()
        return when (val result = resourceRepository.getResourcesPaginated(input.limit, input.page).toDomainResult()) {
            is DomainResult.Incomplete -> Output.Failure(result)
            is DomainResult.Finished ->
                Output.Success(
                    pagination = result.value.header.pagination,
                    result.value.body.map {
                        val slug = requireNotNull(slugMapping[it.resourceTypeId])
                        ResourceModelWithAttributes(
                            resourceModelMapper.map(it, slug = slug),
                            it.tags?.map { tag -> tagModelMapper.map(tag) }.orEmpty(),
                            it.permissions?.map { permission -> permissionsModelMapper.map(permission) }.orEmpty(),
                            it.favorite?.id?.toString(),
                        )
                    },
                )
        }
    }

    data class Input(
        val page: Int,
        val limit: Int,
    )

    sealed class Output : AuthenticatedUseCaseOutput {
        data class Success(
            val pagination: Pagination,
            val resources: List<ResourceModelWithAttributes>,
        ) : Output(),
            CompleteAuthenticatedOutput

        data class Failure(
            override val incomplete: DomainResult.Incomplete,
        ) : Output(),
            IncompleteAuthenticatedOutput
    }
}
