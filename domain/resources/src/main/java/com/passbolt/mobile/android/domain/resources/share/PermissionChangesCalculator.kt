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

import com.passbolt.mobile.android.mappers.SharePermissionsModelMapper.Companion.TEMPORARY_NEW_PERMISSION_ID
import com.passbolt.mobile.android.ui.PermissionModelUi
import com.passbolt.mobile.android.ui.PermissionModelUi.GroupPermissionModel
import com.passbolt.mobile.android.ui.PermissionModelUi.UserPermissionModel
import com.passbolt.mobile.android.ui.ResourcePermission

/**
 * Propagation of a folder's permission changes to its content, the same rules as the browser
 * extension (PermissionChangesCollection.calculateChanges / reuseChanges):
 * - a recipient added to the folder is added to an item that has no permission for it yet,
 * - a recipient removed from the folder is removed from an item where it has the same access,
 * - a changed access is changed on an item where it still had the folder's original access.
 * Anything the item owner set differently on the item itself is left alone.
 */
object PermissionChangesCalculator {
    sealed class Change {
        abstract val aro: Aro

        data class Create(
            override val aro: Aro,
            val permission: PermissionModelUi,
        ) : Change()

        data class Update(
            override val aro: Aro,
            val from: ResourcePermission,
            val to: ResourcePermission,
        ) : Change()

        data class Delete(
            override val aro: Aro,
            val type: ResourcePermission,
        ) : Change()
    }

    data class Aro(
        val isGroup: Boolean,
        val id: String,
    )

    fun aroOf(permission: PermissionModelUi): Aro =
        when (permission) {
            is GroupPermissionModel -> Aro(isGroup = true, id = permission.group.groupId)
            is UserPermissionModel -> Aro(isGroup = false, id = permission.user.userId)
        }

    fun calculateChanges(
        original: List<PermissionModelUi>,
        expected: List<PermissionModelUi>,
    ): List<Change> {
        val originalByAro = original.associateBy { aroOf(it) }
        val expectedByAro = expected.associateBy { aroOf(it) }
        val createdOrUpdated =
            expected.mapNotNull { permission ->
                val aro = aroOf(permission)
                val before = originalByAro[aro]
                when {
                    before == null -> Change.Create(aro, permission)
                    before.permission != permission.permission -> Change.Update(aro, before.permission, permission.permission)
                    else -> null
                }
            }
        val deleted =
            original
                .filter { aroOf(it) !in expectedByAro }
                .map { Change.Delete(aroOf(it), it.permission) }
        return createdOrUpdated + deleted
    }

    /**
     * @return the item's new permission list, or null when the changes do not touch the item
     * (or would leave it without an owner).
     */
    fun applyChanges(
        itemPermissions: List<PermissionModelUi>,
        changes: List<Change>,
    ): List<PermissionModelUi>? {
        val result = itemPermissions.toMutableList()
        var changed = false
        changes.forEach { change ->
            val index = result.indexOfFirst { aroOf(it) == change.aro }
            val existing = result.getOrNull(index)
            when (change) {
                is Change.Create ->
                    if (existing == null) {
                        result.add(change.permission.withId(TEMPORARY_NEW_PERMISSION_ID))
                        changed = true
                    }
                is Change.Delete ->
                    if (existing != null && existing.permission == change.type) {
                        result.removeAt(index)
                        changed = true
                    }
                is Change.Update ->
                    if (existing != null && existing.permission == change.from) {
                        result[index] = existing.withType(change.to)
                        changed = true
                    }
            }
        }
        return result.takeIf { changed && it.any { permission -> permission.permission == ResourcePermission.OWNER } }
    }

    private fun PermissionModelUi.withId(id: String): PermissionModelUi =
        when (this) {
            is GroupPermissionModel -> copy(permissionId = id)
            is UserPermissionModel -> copy(permissionId = id)
        }

    private fun PermissionModelUi.withType(type: ResourcePermission): PermissionModelUi =
        when (this) {
            is GroupPermissionModel -> copy(permission = type)
            is UserPermissionModel -> copy(permission = type)
        }
}
