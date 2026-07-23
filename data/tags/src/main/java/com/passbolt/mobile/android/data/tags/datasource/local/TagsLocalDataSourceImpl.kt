package com.passbolt.mobile.android.data.tags.datasource.local

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.passbolt.mobile.android.database.DatabaseProvider
import com.passbolt.mobile.android.database.QuerySanitizer
import com.passbolt.mobile.android.domain.tags.datasource.TagsLocalDataSource
import com.passbolt.mobile.android.entity.resource.ResourceAndTagsCrossRef
import com.passbolt.mobile.android.mappers.TagsModelMapper
import com.passbolt.mobile.android.ui.ResourceUiModelWithAttributes
import com.passbolt.mobile.android.ui.TagWithCount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
internal class TagsLocalDataSourceImpl(
    private val databaseProvider: DatabaseProvider,
    private val tagModelMapper: TagsModelMapper,
    private val querySanitizer: QuerySanitizer,
) : TagsLocalDataSource {
    override suspend fun addTags(
        resourcesWithTags: List<ResourceUiModelWithAttributes>,
        userId: String,
    ) {
        val tagsDao =
            databaseProvider
                .get(userId)
                .tagsDao()

        val tagsAndResourcesCrossRefDao =
            databaseProvider
                .get(userId)
                .resourcesAndTagsCrossRefDao()

        resourcesWithTags.apply {
            val tags = flatMap { it.resourceTags }
            val resourceAndTagCrossRefs =
                map { it.resourceModel.resourceId to it.resourceTags.map { tag -> tag.id } }
                    .flatMap { (resourceId, resourceTagsIds) ->
                        resourceTagsIds.map { tagId ->
                            ResourceAndTagsCrossRef(tagId, resourceId)
                        }
                    }
            tagsDao.insertAll(tagModelMapper.map(tags))
            tagsAndResourcesCrossRefDao.insertAll(resourceAndTagCrossRefs)
        }
    }

    override suspend fun getTagsWithCount(
        searchQuery: String?,
        userId: String,
    ): List<TagWithCount> =
        databaseProvider
            .get(userId)
            .tagsDao()
            .getAllWithTaggedItemsCount(querySanitizer.sanitize(searchQuery))
            .map { tagModelMapper.map(it) }

    override fun getTagsWithCountPaginated(
        searchQuery: String?,
        pageSize: Int,
        userId: String,
    ): Flow<PagingData<TagWithCount>> =
        Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = {
                databaseProvider
                    .get(userId)
                    .paginatedTagsDao()
                    .getAllWithTaggedItemsCount(querySanitizer.sanitize(searchQuery))
            },
        ).flow.map { pagingData ->
            pagingData.map {
                tagModelMapper.map(it)
            }
        }

    override suspend fun removeTags(userId: String) {
        val tagsDao =
            databaseProvider
                .get(userId)
                .tagsDao()

        val resourcesAndTagsCrossRefDao =
            databaseProvider
                .get(userId)
                .resourcesAndTagsCrossRefDao()

        tagsDao.deleteAll()
        resourcesAndTagsCrossRefDao.deleteAll()
    }
}
