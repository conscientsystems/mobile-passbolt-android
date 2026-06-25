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

package com.passbolt.mobile.android.data.folders

import androidx.paging.PagingData
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.domain.folders.FoldersRepository
import com.passbolt.mobile.android.domain.folders.datasource.FoldersLocalDataSource
import com.passbolt.mobile.android.domain.folders.datasource.FoldersRemoteDataSource
import com.passbolt.mobile.android.domain.folders.model.FolderModel
import com.passbolt.mobile.android.domain.folders.model.FolderModelWithAttributes
import com.passbolt.mobile.android.domain.folders.model.FolderUpdateState
import com.passbolt.mobile.android.domain.folders.model.FolderWithCountAndPath
import com.passbolt.mobile.android.domain.folders.model.FoldersPage
import com.passbolt.mobile.android.domain.folders.model.ParentPermissionItemId
import com.passbolt.mobile.android.domain.folders.model.ResourcesAndFolders
import com.passbolt.mobile.android.domain.folders.model.ResourcesAndFoldersPaged
import com.passbolt.mobile.android.ui.Folder
import com.passbolt.mobile.android.ui.PermissionModelUi
import com.passbolt.mobile.android.ui.ResourceModel
import kotlinx.coroutines.flow.Flow

internal class FoldersRepositoryImpl(
    private val localDataSource: FoldersLocalDataSource,
    private val remoteDataSource: FoldersRemoteDataSource,
) : FoldersRepository {
    override suspend fun getFoldersPage(
        limit: Int,
        page: Int,
    ): DomainResult<FoldersPage> = remoteDataSource.getFoldersPage(limit, page)

    override suspend fun createFolder(
        name: String,
        parentFolderId: String?,
    ): DomainResult<FolderModelWithAttributes> = remoteDataSource.createFolder(name, parentFolderId)

    override suspend fun addFolder(folder: FolderModel) = localDataSource.addFolder(folder)

    override suspend fun upsertFolders(folders: List<FolderModel>) = localDataSource.upsertFolders(folders)

    override suspend fun setAllFoldersUpdateState(updateState: FolderUpdateState) = localDataSource.setAllFoldersUpdateState(updateState)

    override suspend fun removeFoldersWithUpdateState(updateState: FolderUpdateState) =
        localDataSource.removeFoldersWithUpdateState(updateState)

    override suspend fun updateFoldersIsShared(currentUserServerId: String) = localDataSource.updateFoldersIsShared(currentUserServerId)

    override suspend fun addFolderPermissions(foldersWithAttributes: List<FolderModelWithAttributes>) =
        localDataSource.addFolderPermissions(foldersWithAttributes)

    override suspend fun clearFolderPermissions() = localDataSource.clearFolderPermissions()

    override suspend fun getFolderDetails(folderId: String): FolderModel = localDataSource.getFolderDetails(folderId)

    override suspend fun getFolderLocation(folderId: String): List<FolderModel> = localDataSource.getFolderLocation(folderId)

    override suspend fun getFolderPermissions(folderId: String): List<PermissionModelUi> = localDataSource.getFolderPermissions(folderId)

    override suspend fun getParentFolderPermissionsToApplyToNewItem(
        parentFolderId: String,
        itemId: ParentPermissionItemId,
        currentUserServerId: String,
    ): List<PermissionModelUi> = localDataSource.getParentFolderPermissionsToApplyToNewItem(parentFolderId, itemId, currentUserServerId)

    override suspend fun getResourcesAndFolders(
        folderId: String?,
        slugs: Set<String>,
    ): ResourcesAndFolders = localDataSource.getResourcesAndFolders(folderId, slugs)

    override fun getResourcesAndFoldersPaged(
        folderId: String?,
        slugs: Set<String>,
        searchQuery: String?,
        pageSize: Int,
        enablePlaceholders: Boolean,
    ): ResourcesAndFoldersPaged = localDataSource.getResourcesAndFoldersPaged(folderId, slugs, searchQuery, pageSize, enablePlaceholders)

    override suspend fun getSubFoldersForFolder(
        folder: Folder,
        searchQuery: String?,
    ): List<FolderWithCountAndPath> = localDataSource.getSubFoldersForFolder(folder, searchQuery)

    override fun getSubFoldersForFolderPaged(
        folder: Folder,
        searchQuery: String?,
        pageSize: Int,
    ): Flow<PagingData<FolderWithCountAndPath>> = localDataSource.getSubFoldersForFolderPaged(folder, searchQuery, pageSize)

    override suspend fun getSubFolderResourcesFiltered(
        containingFolders: List<String>,
        containingQuery: String,
        slugs: Set<String>,
    ): List<ResourceModel> = localDataSource.getSubFolderResourcesFiltered(containingFolders, containingQuery, slugs)

    override fun getSubFolderResourcesFilteredPaged(
        containingFolders: List<String>,
        containingQuery: String,
        slugs: Set<String>,
        pageSize: Int,
    ): Flow<PagingData<ResourceModel>> =
        localDataSource.getSubFolderResourcesFilteredPaged(containingFolders, containingQuery, slugs, pageSize)
}
