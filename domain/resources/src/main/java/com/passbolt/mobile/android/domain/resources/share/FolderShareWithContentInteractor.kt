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

package com.passbolt.mobile.android.domain.resources.share

import com.passbolt.mobile.android.domain.folders.usecase.GetLocalSubFoldersForFolderUseCase
import com.passbolt.mobile.android.domain.permissionsconfirmation.mapper.toEditModePermissions
import com.passbolt.mobile.android.domain.resources.actions.ResourceShareActionsInteractor
import com.passbolt.mobile.android.domain.resources.actions.ShareActionResult
import com.passbolt.mobile.android.domain.resources.share.PermissionChangesCalculator.Change
import com.passbolt.mobile.android.domain.resources.usecase.CreatePermissionsSnapshotInteractor
import com.passbolt.mobile.android.domain.resources.usecase.db.GetLocalResourcesUseCase
import com.passbolt.mobile.android.domain.share.usecase.ShareFolderUseCase
import com.passbolt.mobile.android.feature.authentication.session.runAuthenticatedOperation
import com.passbolt.mobile.android.mappers.SharePermissionsModelMapper
import com.passbolt.mobile.android.supportedresourceTypes.SupportedContentTypes
import com.passbolt.mobile.android.ui.Folder
import com.passbolt.mobile.android.ui.HomeDisplayViewModel
import com.passbolt.mobile.android.ui.PermissionModelUi
import com.passbolt.mobile.android.ui.ResourcePermission
import timber.log.Timber

/**
 * Shares a folder and applies the same permission changes to everything inside it that the
 * user owns (sub folders and resources, at any depth) - what the browser extension does when
 * a folder is shared. Resources go through [ResourceShareActionsInteractor], so secrets are
 * encrypted for new recipients and v5 metadata is moved to the shared key, exactly as for a
 * single resource share.
 */
class FolderShareWithContentInteractor(
    private val createPermissionsSnapshotInteractor: CreatePermissionsSnapshotInteractor,
    private val shareFolderUseCase: ShareFolderUseCase,
    private val sharePermissionsModelMapper: SharePermissionsModelMapper,
    private val getLocalSubFoldersForFolderUseCase: GetLocalSubFoldersForFolderUseCase,
    private val getLocalResourcesUseCase: GetLocalResourcesUseCase,
    private val resourceShareActionsInteractor: ResourceShareActionsInteractor,
) {
    suspend fun shareFolderWithContent(
        folderId: String,
        originalPermissions: List<PermissionModelUi>,
        confirmedPermissions: List<PermissionModelUi>,
        onProgress: (done: Int, total: Int) -> Unit = { _, _ -> },
    ): Output {
        val changes = PermissionChangesCalculator.calculateChanges(originalPermissions, confirmedPermissions)
        if (changes.isEmpty()) {
            return Output.Success(sharedItems = 0, skippedItems = 0)
        }
        Timber.d("Folder share: ${changes.size} permission change(s)")

        shareFolder(folderId, originalPermissions, confirmedPermissions)?.let { return it }

        val subFolderIds =
            getLocalSubFoldersForFolderUseCase
                .execute(GetLocalSubFoldersForFolderUseCase.Input(Folder.Child(folderId)))
                .folders
                .filter { it.permission == ResourcePermission.OWNER }
                .map { it.folderId }
        val resourceIds =
            (listOf(folderId) + subFolderIds).flatMap { id ->
                getLocalResourcesUseCase
                    .execute(
                        GetLocalResourcesUseCase.Input(
                            slugs = SupportedContentTypes.allSlugs,
                            homeDisplayView = HomeDisplayViewModel.Folders(Folder.Child(id)),
                        ),
                    ).resources
                    .filter { it.permission == ResourcePermission.OWNER }
                    .map { it.resourceId }
            }
        val total = subFolderIds.size + resourceIds.size
        Timber.d("Folder share: propagating to ${subFolderIds.size} folder(s) and ${resourceIds.size} resource(s)")

        var done = 0
        var shared = 0
        val failures = mutableListOf<String>()
        subFolderIds.forEach { subFolderId ->
            when (propagateToFolder(subFolderId, changes)) {
                PropagationResult.SHARED -> shared++
                PropagationResult.UNCHANGED -> Unit
                PropagationResult.FAILED -> failures.add(subFolderId)
            }
            onProgress(++done, total)
        }
        resourceIds.forEach { resourceId ->
            when (propagateToResource(resourceId, changes)) {
                PropagationResult.SHARED -> shared++
                PropagationResult.UNCHANGED -> Unit
                PropagationResult.FAILED -> failures.add(resourceId)
            }
            onProgress(++done, total)
        }
        return if (failures.isEmpty()) {
            Output.Success(sharedItems = shared, skippedItems = total - shared)
        } else {
            Output.PartialFailure(sharedItems = shared, failedItems = failures.size)
        }
    }

    private suspend fun shareFolder(
        folderId: String,
        existing: List<PermissionModelUi>,
        expected: List<PermissionModelUi>,
    ): Output? {
        val sharePermissions =
            sharePermissionsModelMapper.mapForShare(
                SharePermissionsModelMapper.ShareItem.Folder(folderId),
                expected,
                existing,
            )
        return when (
            val output =
                runAuthenticatedOperation {
                    shareFolderUseCase.execute(ShareFolderUseCase.Input(folderId, sharePermissions))
                }
        ) {
            is ShareFolderUseCase.Output.Success -> null
            is ShareFolderUseCase.Output.Failure -> {
                Timber.e("Folder share failed: ${output.message}")
                Output.FolderShareFailure(output.message)
            }
        }
    }

    private suspend fun propagateToFolder(
        folderId: String,
        changes: List<Change>,
    ): PropagationResult {
        val current =
            when (val snapshot = runAuthenticatedOperation { createPermissionsSnapshotInteractor.createForFolder(folderId) }) {
                is CreatePermissionsSnapshotInteractor.Output.Success -> snapshot.snapshot.toEditModePermissions()
                is CreatePermissionsSnapshotInteractor.Output.Failure -> return PropagationResult.FAILED
            }
        val updated = PermissionChangesCalculator.applyChanges(current, changes) ?: return PropagationResult.UNCHANGED
        return if (shareFolder(folderId, current, updated) == null) PropagationResult.SHARED else PropagationResult.FAILED
    }

    private suspend fun propagateToResource(
        resourceId: String,
        changes: List<Change>,
    ): PropagationResult {
        // the snapshot is also what the share's drift check compares against
        val current =
            when (val snapshot = runAuthenticatedOperation { createPermissionsSnapshotInteractor.createForResource(resourceId) }) {
                is CreatePermissionsSnapshotInteractor.Output.Success -> snapshot.snapshot.toEditModePermissions()
                is CreatePermissionsSnapshotInteractor.Output.Failure -> return PropagationResult.FAILED
            }
        val updated = PermissionChangesCalculator.applyChanges(current, changes) ?: return PropagationResult.UNCHANGED
        return when (val result = resourceShareActionsInteractor.shareWithConfirmedPermissions(resourceId, updated)) {
            ShareActionResult.Success -> PropagationResult.SHARED
            else -> {
                Timber.e("Folder share: resource share failed: $result")
                PropagationResult.FAILED
            }
        }
    }

    private enum class PropagationResult { SHARED, UNCHANGED, FAILED }

    sealed class Output {
        data class Success(
            val sharedItems: Int,
            val skippedItems: Int,
        ) : Output()

        data class PartialFailure(
            val sharedItems: Int,
            val failedItems: Int,
        ) : Output()

        data class FolderShareFailure(
            val message: String?,
        ) : Output()
    }
}
