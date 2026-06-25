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

package com.passbolt.mobile.android.data.folders.datasource.local

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.passbolt.mobile.android.core.accounts.usecase.SelectedAccountUseCase
import com.passbolt.mobile.android.data.folders.mapper.toDomain
import com.passbolt.mobile.android.data.folders.mapper.toEntity
import com.passbolt.mobile.android.database.DatabaseProvider
import com.passbolt.mobile.android.database.QuerySanitizer
import com.passbolt.mobile.android.domain.folders.datasource.FoldersLocalDataSource
import com.passbolt.mobile.android.domain.folders.model.FolderModel
import com.passbolt.mobile.android.domain.folders.model.FolderModelWithAttributes
import com.passbolt.mobile.android.domain.folders.model.FolderUpdateState
import com.passbolt.mobile.android.domain.folders.model.FolderWithCountAndPath
import com.passbolt.mobile.android.domain.folders.model.ParentPermissionItemId
import com.passbolt.mobile.android.domain.folders.model.ResourcesAndFolders
import com.passbolt.mobile.android.domain.folders.model.ResourcesAndFoldersPaged
import com.passbolt.mobile.android.entity.folder.FolderAndUsersCrossRef
import com.passbolt.mobile.android.entity.group.FolderAndGroupsCrossRef
import com.passbolt.mobile.android.mappers.PermissionsModelMapper
import com.passbolt.mobile.android.mappers.ResourceModelMapper
import com.passbolt.mobile.android.mappers.SharePermissionsModelMapper
import com.passbolt.mobile.android.ui.Folder
import com.passbolt.mobile.android.ui.PermissionModel
import com.passbolt.mobile.android.ui.PermissionModelUi
import com.passbolt.mobile.android.ui.ResourceModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.passbolt.mobile.android.domain.folders.model.FolderUpdateState.UPDATED as DOMAIN_UPDATED

internal class FoldersLocalDataSourceImpl(
    private val databaseProvider: DatabaseProvider,
    private val querySanitizer: QuerySanitizer,
    private val permissionsModelMapper: PermissionsModelMapper,
    private val resourceModelMapper: ResourceModelMapper,
) : FoldersLocalDataSource,
    SelectedAccountUseCase {
    override suspend fun addFolder(folder: FolderModel) {
        databaseProvider
            .get(selectedAccountId)
            .foldersDao()
            .insert(folder.toEntity(DOMAIN_UPDATED.toEntity(), permissionsModelMapper))
    }

    override suspend fun upsertFolders(folders: List<FolderModel>) {
        val entities = folders.map { it.toEntity(DOMAIN_UPDATED.toEntity(), permissionsModelMapper) }
        databaseProvider
            .get(selectedAccountId)
            .foldersDao()
            .upsertAll(entities)
    }

    override suspend fun setAllFoldersUpdateState(updateState: FolderUpdateState) {
        databaseProvider
            .get(selectedAccountId)
            .foldersDao()
            .setAllUpdateState(updateState.toEntity())
    }

    override suspend fun removeFoldersWithUpdateState(updateState: FolderUpdateState) {
        databaseProvider
            .get(selectedAccountId)
            .foldersDao()
            .removeWithUpdateState(updateState.toEntity())
    }

    override suspend fun updateFoldersIsShared(currentUserServerId: String) {
        databaseProvider
            .get(selectedAccountId)
            .foldersDao()
            .updateIsShared(currentUserServerId)
    }

    override suspend fun addFolderPermissions(foldersWithAttributes: List<FolderModelWithAttributes>) {
        val db = databaseProvider.get(selectedAccountId)
        val foldersAndGroupsCrossRefDao = db.folderAndGroupsCrossRefDao()
        val foldersAndUsersCrossRefDao = db.folderAndUsersCrossRefDao()

        val folderGroupsPermissions =
            foldersWithAttributes.map {
                it.folderModel.folderId to it.folderPermissions.filterIsInstance<PermissionModel.GroupPermissionModel>()
            }
        val folderUsersPermissions =
            foldersWithAttributes.map {
                it.folderModel.folderId to it.folderPermissions.filterIsInstance<PermissionModel.UserPermissionModel>()
            }

        val folderAndGroupCrossRefs =
            folderGroupsPermissions.flatMap { (folderId, groupPermissions) ->
                groupPermissions.map { groupPermission ->
                    FolderAndGroupsCrossRef(
                        folderId,
                        groupPermission.group.groupId,
                        permissionsModelMapper.map(groupPermission.permission),
                        groupPermission.permissionId,
                    )
                }
            }

        val folderAndUsersCrossRefs =
            folderUsersPermissions.flatMap { (folderId, userPermissions) ->
                userPermissions.map { userPermission ->
                    FolderAndUsersCrossRef(
                        folderId,
                        userPermission.userId,
                        permissionsModelMapper.map(userPermission.permission),
                        userPermission.permissionId,
                    )
                }
            }

        foldersAndGroupsCrossRefDao.insertAll(folderAndGroupCrossRefs)
        foldersAndUsersCrossRefDao.insertAll(folderAndUsersCrossRefs)
    }

    override suspend fun clearFolderPermissions() {
        databaseProvider
            .get(selectedAccountId)
            .apply {
                folderAndGroupsCrossRefDao().deleteAll()
                folderAndUsersCrossRefDao().deleteAll()
            }
    }

    override suspend fun getFolderDetails(folderId: String): FolderModel =
        databaseProvider
            .get(selectedAccountId)
            .foldersDao()
            .get(folderId)
            .toDomain(permissionsModelMapper)

    override suspend fun getFolderLocation(folderId: String): List<FolderModel> =
        databaseProvider
            .get(selectedAccountId)
            .foldersDao()
            .getFolderLocation(folderId)
            .map { it.toDomain(permissionsModelMapper) }

    override suspend fun getFolderPermissions(folderId: String): List<PermissionModelUi> {
        val foldersDao = databaseProvider.get(selectedAccountId).foldersDao()
        val groupsPermissions = foldersDao.getFolderGroupsPermissions(folderId)
        val usersPermissions = foldersDao.getFolderUsersPermissions(folderId)
        return permissionsModelMapper.map(groupsPermissions, usersPermissions)
    }

    override suspend fun getParentFolderPermissionsToApplyToNewItem(
        parentFolderId: String,
        itemId: ParentPermissionItemId,
        currentUserServerId: String,
    ): List<PermissionModelUi> {
        val db = databaseProvider.get(selectedAccountId)
        val foldersDao = db.foldersDao()
        val resourcesDao = db.resourcesDao()

        val groupsPermissions =
            foldersDao
                .getFolderGroupsPermissions(parentFolderId)
                .map { it.copy(permissionId = SharePermissionsModelMapper.TEMPORARY_NEW_PERMISSION_ID) }
        val usersPermissions =
            foldersDao
                .getFolderUsersPermissions(parentFolderId)
                .map {
                    if (it.userId != currentUserServerId) {
                        // new permissions from parent folder to inherit
                        it.copy(permissionId = SharePermissionsModelMapper.TEMPORARY_NEW_PERMISSION_ID)
                    } else {
                        // special case: permission for current user
                        //
                        // use permission values from parent folder to apply but overwrite the permission id
                        val currentUserPermissions =
                            when (itemId) {
                                is ParentPermissionItemId.FolderId -> foldersDao.getFolderUsersPermissions(itemId.folderId)
                                is ParentPermissionItemId.ResourceId -> resourcesDao.getResourceUsersPermissions(itemId.resourceId)
                            }

                        require(currentUserPermissions.size == 1) {
                            "On newly created item there should be exactly one permission" +
                                " - only for the current user (the one who just created the item)"
                        }
                        it.copy(permissionId = currentUserPermissions[0].permissionId)
                    }
                }

        return permissionsModelMapper.map(groupsPermissions, usersPermissions)
    }

    override suspend fun getResourcesAndFolders(
        folderId: String?,
        slugs: Set<String>,
    ): ResourcesAndFolders {
        val db = databaseProvider.get(selectedAccountId)
        val resourcesInFolder = db.resourcesDao().getResourcesForFolderWithId(folderId, slugs)
        val foldersInFolder = db.foldersDao().getFolderDirectChildFolders(folderId)
        return ResourcesAndFolders(
            folders = foldersInFolder.map { it.toDomain(permissionsModelMapper) },
            resources = resourcesInFolder.map { resourceModelMapper.map(it) },
        )
    }

    override fun getResourcesAndFoldersPaged(
        folderId: String?,
        slugs: Set<String>,
        searchQuery: String?,
        pageSize: Int,
        enablePlaceholders: Boolean,
    ): ResourcesAndFoldersPaged {
        val ftsQuery = querySanitizer.sanitize(searchQuery)
        return ResourcesAndFoldersPaged(
            folders =
                Pager(
                    config = PagingConfig(pageSize = pageSize, enablePlaceholders = enablePlaceholders),
                    pagingSourceFactory = {
                        databaseProvider
                            .get(selectedAccountId)
                            .paginatedFoldersDao()
                            .getFolderDirectChildFolders(folderId, ftsQuery)
                    },
                ).flow.map { pagingData -> pagingData.map { it.toDomain(permissionsModelMapper) } },
            resources =
                Pager(
                    config = PagingConfig(pageSize = pageSize, enablePlaceholders = enablePlaceholders),
                    pagingSourceFactory = {
                        databaseProvider
                            .get(selectedAccountId)
                            .paginatedResourcesDao()
                            .getResourcesForFolderWithId(folderId, slugs, ftsQuery)
                    },
                ).flow.map { pagingData -> pagingData.map { resourceModelMapper.map(it) } },
        )
    }

    override suspend fun getSubFoldersForFolder(
        folder: Folder,
        searchQuery: String?,
    ): List<FolderWithCountAndPath> {
        val foldersDao = databaseProvider.get(selectedAccountId).foldersDao()
        val ftsQuery = querySanitizer.sanitize(searchQuery)
        val folders =
            when (folder) {
                is Folder.Child -> foldersDao.getFolderAllChildFoldersRecursively(folder.folderId, ftsQuery)
                is Folder.Root -> foldersDao.getAllFolders(ftsQuery)
            }
        return folders.map { it.toDomain(permissionsModelMapper) }
    }

    override fun getSubFoldersForFolderPaged(
        folder: Folder,
        searchQuery: String?,
        pageSize: Int,
    ): Flow<PagingData<FolderWithCountAndPath>> =
        Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = {
                val foldersDao = databaseProvider.get(selectedAccountId).paginatedFoldersDao()
                val ftsQuery = querySanitizer.sanitize(searchQuery)
                when (folder) {
                    is Folder.Child -> foldersDao.getFolderAllChildFoldersRecursively(folder.folderId, ftsQuery)
                    is Folder.Root -> foldersDao.getAllFolders(ftsQuery)
                }
            },
        ).flow.map { pagingData -> pagingData.map { it.toDomain(permissionsModelMapper) } }

    override suspend fun getSubFolderResourcesFiltered(
        containingFolders: List<String>,
        containingQuery: String,
        slugs: Set<String>,
    ): List<ResourceModel> {
        val resources =
            databaseProvider
                .get(selectedAccountId)
                .resourcesDao()
                .getFilteredForChildFolders(containingFolders, slugs, querySanitizer.sanitize(containingQuery))
        return resources.map { resourceModelMapper.map(it) }
    }

    override fun getSubFolderResourcesFilteredPaged(
        containingFolders: List<String>,
        containingQuery: String,
        slugs: Set<String>,
        pageSize: Int,
    ): Flow<PagingData<ResourceModel>> =
        Pager(
            config = PagingConfig(pageSize = pageSize, enablePlaceholders = false),
            pagingSourceFactory = {
                databaseProvider
                    .get(selectedAccountId)
                    .paginatedResourcesDao()
                    .getFilteredForChildFolders(containingFolders, slugs, querySanitizer.sanitize(containingQuery))
            },
        ).flow.map { pagingData -> pagingData.map { resourceModelMapper.map(it) } }
}
