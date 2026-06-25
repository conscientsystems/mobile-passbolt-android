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

package com.passbolt.mobile.android.domain.folders

import androidx.paging.PagingData
import com.passbolt.mobile.android.core.architecture.result.DomainResult
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

/**
 * Single source of truth for folders: local reads return plain types straight from Room, while remote
 * operations return [DomainResult] so callers can react to authentication / connectivity failures.
 */
interface FoldersRepository {
    suspend fun getFoldersPage(
        limit: Int,
        page: Int,
    ): DomainResult<FoldersPage>

    suspend fun createFolder(
        name: String,
        parentFolderId: String?,
    ): DomainResult<FolderModelWithAttributes>

    suspend fun addFolder(folder: FolderModel)

    suspend fun upsertFolders(folders: List<FolderModel>)

    suspend fun setAllFoldersUpdateState(updateState: FolderUpdateState)

    suspend fun removeFoldersWithUpdateState(updateState: FolderUpdateState)

    suspend fun updateFoldersIsShared(currentUserServerId: String)

    suspend fun addFolderPermissions(foldersWithAttributes: List<FolderModelWithAttributes>)

    suspend fun clearFolderPermissions()

    suspend fun getFolderDetails(folderId: String): FolderModel

    suspend fun getFolderLocation(folderId: String): List<FolderModel>

    suspend fun getFolderPermissions(folderId: String): List<PermissionModelUi>

    suspend fun getParentFolderPermissionsToApplyToNewItem(
        parentFolderId: String,
        itemId: ParentPermissionItemId,
        currentUserServerId: String,
    ): List<PermissionModelUi>

    suspend fun getResourcesAndFolders(
        folderId: String?,
        slugs: Set<String>,
    ): ResourcesAndFolders

    fun getResourcesAndFoldersPaged(
        folderId: String?,
        slugs: Set<String>,
        searchQuery: String?,
        pageSize: Int,
        enablePlaceholders: Boolean,
    ): ResourcesAndFoldersPaged

    suspend fun getSubFoldersForFolder(
        folder: Folder,
        searchQuery: String?,
    ): List<FolderWithCountAndPath>

    fun getSubFoldersForFolderPaged(
        folder: Folder,
        searchQuery: String?,
        pageSize: Int,
    ): Flow<PagingData<FolderWithCountAndPath>>

    suspend fun getSubFolderResourcesFiltered(
        containingFolders: List<String>,
        containingQuery: String,
        slugs: Set<String>,
    ): List<ResourceModel>

    fun getSubFolderResourcesFilteredPaged(
        containingFolders: List<String>,
        containingQuery: String,
        slugs: Set<String>,
        pageSize: Int,
    ): Flow<PagingData<ResourceModel>>
}
