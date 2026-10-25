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

import com.passbolt.mobile.android.domain.folders.usecase.MoveResourceUseCase
import com.passbolt.mobile.android.domain.permissionsconfirmation.mapper.toEditModePermissions
import com.passbolt.mobile.android.domain.resources.actions.ResourceShareActionsInteractor
import com.passbolt.mobile.android.domain.resources.actions.ShareActionResult
import com.passbolt.mobile.android.domain.resources.share.PermissionChangesCalculator.aroOf
import com.passbolt.mobile.android.domain.resources.usecase.CreatePermissionsSnapshotInteractor
import com.passbolt.mobile.android.domain.resources.usecase.db.GetLocalResourceUseCase
import com.passbolt.mobile.android.domain.users.usecase.GetLocalCurrentUserUseCase
import com.passbolt.mobile.android.feature.authentication.session.runAuthenticatedOperation
import com.passbolt.mobile.android.mappers.SharePermissionsModelMapper.Companion.TEMPORARY_NEW_PERMISSION_ID
import com.passbolt.mobile.android.mappers.UsersModelMapper
import com.passbolt.mobile.android.ui.PermissionModelUi
import com.passbolt.mobile.android.ui.PermissionModelUi.GroupPermissionModel
import com.passbolt.mobile.android.ui.PermissionModelUi.UserPermissionModel
import com.passbolt.mobile.android.ui.ResourcePermission
import timber.log.Timber

/**
 * Moves a resource to another folder (or the root) and, when the user owns it, makes its
 * permissions follow the move like the browser extension does
 * (CalculatePermissionsChangesForMoveService): the permissions it had only because of its old
 * folder are dropped, the destination folder's permissions are added, and a move to the root
 * keeps the user as owner. Moving an entry into a shared folder therefore shares it with that
 * folder's users and groups; moving it out again revokes that access.
 */
class MoveResourceInteractor(
    private val getLocalResourceUseCase: GetLocalResourceUseCase,
    private val createPermissionsSnapshotInteractor: CreatePermissionsSnapshotInteractor,
    private val moveResourceUseCase: MoveResourceUseCase,
    private val resourceShareActionsInteractor: ResourceShareActionsInteractor,
    private val getLocalCurrentUserUseCase: GetLocalCurrentUserUseCase,
    private val usersModelMapper: UsersModelMapper,
) {
    @Suppress("ReturnCount")
    suspend fun move(
        resourceId: String,
        destinationFolderId: String?,
    ): Output {
        val resource = getLocalResourceUseCase.execute(GetLocalResourceUseCase.Input(resourceId)).resource
        val sourceFolderId = resource.folderId
        if (sourceFolderId == destinationFolderId) {
            return Output.Success(permissionsChanged = false)
        }
        val isOwner = resource.permission == ResourcePermission.OWNER

        // permissions are read before the move: the source folder's are needed to know what was inherited
        val newPermissions =
            if (isOwner) {
                val sourcePermissions = sourceFolderId?.let { folderPermissions(it) ?: return Output.Failure }
                val destinationPermissions =
                    destinationFolderId?.let { folderPermissions(it) ?: return Output.Failure }
                // taken last: the share's drift check compares against this stored snapshot
                val resourcePermissions = resourcePermissions(resourceId) ?: return Output.Failure
                calculateNewPermissions(resourcePermissions, sourcePermissions, destinationPermissions)
                    .takeIf { it.isNotEqualTo(resourcePermissions) }
            } else {
                null
            }

        when (
            val moveOutput =
                runAuthenticatedOperation {
                    moveResourceUseCase.execute(MoveResourceUseCase.Input(resourceId, destinationFolderId))
                }
        ) {
            is MoveResourceUseCase.Output.Failure -> {
                Timber.e("Move failed: ${moveOutput.incomplete}")
                return Output.Failure
            }
            MoveResourceUseCase.Output.Success -> Timber.d("Resource moved")
        }

        if (newPermissions == null) {
            return Output.Success(permissionsChanged = false)
        }
        return when (val shareResult = resourceShareActionsInteractor.shareWithConfirmedPermissions(resourceId, newPermissions)) {
            ShareActionResult.Success -> Output.Success(permissionsChanged = true)
            else -> {
                Timber.e("Moved, but the permissions could not be updated: $shareResult")
                Output.MovedButPermissionsNotUpdated
            }
        }
    }

    private suspend fun calculateNewPermissions(
        resourcePermissions: List<PermissionModelUi>,
        sourceFolderPermissions: List<PermissionModelUi>?,
        destinationFolderPermissions: List<PermissionModelUi>?,
    ): List<PermissionModelUi> {
        // what the resource has beyond its old folder (at least the same access there = inherited)
        val remaining =
            resourcePermissions.filterNot { permission ->
                sourceFolderPermissions.orEmpty().any {
                    aroOf(it) == aroOf(permission) && it.permission >= permission.permission
                }
            }
        val result = remaining.toMutableList()
        destinationFolderPermissions.orEmpty().forEach { fromFolder -> result.addOrRaise(fromFolder, resourcePermissions) }
        if (destinationFolderPermissions == null) {
            val me = usersModelMapper.mapToUserWithAvatar(getLocalCurrentUserUseCase.execute(Unit).user)
            result.addOrRaise(
                UserPermissionModel(ResourcePermission.OWNER, TEMPORARY_NEW_PERMISSION_ID, me),
                resourcePermissions,
            )
        }
        return result
    }

    /** Adds the permission, or raises an existing one for the same user/group to it (never lowers). */
    private fun MutableList<PermissionModelUi>.addOrRaise(
        permission: PermissionModelUi,
        resourcePermissions: List<PermissionModelUi>,
    ) {
        val index = indexOfFirst { aroOf(it) == aroOf(permission) }
        if (index == -1) {
            // keep the server id when the resource had this user/group before (it was only dropped as inherited)
            val previousId = resourcePermissions.firstOrNull { aroOf(it) == aroOf(permission) }?.permissionId
            add(permission.with(permission.permission, previousId ?: TEMPORARY_NEW_PERMISSION_ID))
        } else if (permission.permission > this[index].permission) {
            this[index] = this[index].with(permission.permission, this[index].permissionId)
        }
    }

    private fun PermissionModelUi.with(
        type: ResourcePermission,
        id: String,
    ): PermissionModelUi =
        when (this) {
            is GroupPermissionModel -> copy(permission = type, permissionId = id)
            is UserPermissionModel -> copy(permission = type, permissionId = id)
        }

    private fun List<PermissionModelUi>.isNotEqualTo(other: List<PermissionModelUi>): Boolean =
        map { aroOf(it) to it.permission }.toSet() != other.map { aroOf(it) to it.permission }.toSet()

    private suspend fun folderPermissions(folderId: String): List<PermissionModelUi>? =
        when (val snapshot = runAuthenticatedOperation { createPermissionsSnapshotInteractor.createForFolder(folderId) }) {
            is CreatePermissionsSnapshotInteractor.Output.Success -> snapshot.snapshot.toEditModePermissions()
            is CreatePermissionsSnapshotInteractor.Output.Failure -> null
        }

    private suspend fun resourcePermissions(resourceId: String): List<PermissionModelUi>? =
        when (val snapshot = runAuthenticatedOperation { createPermissionsSnapshotInteractor.createForResource(resourceId) }) {
            is CreatePermissionsSnapshotInteractor.Output.Success -> snapshot.snapshot.toEditModePermissions()
            is CreatePermissionsSnapshotInteractor.Output.Failure -> null
        }

    sealed class Output {
        data class Success(
            val permissionsChanged: Boolean,
        ) : Output()

        data object MovedButPermissionsNotUpdated : Output()

        data object Failure : Output()
    }
}
