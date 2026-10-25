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

import com.google.common.truth.Truth.assertThat
import com.passbolt.mobile.android.mappers.SharePermissionsModelMapper.Companion.TEMPORARY_NEW_PERMISSION_ID
import com.passbolt.mobile.android.ui.GroupModel
import com.passbolt.mobile.android.ui.PermissionModelUi
import com.passbolt.mobile.android.ui.PermissionModelUi.GroupPermissionModel
import com.passbolt.mobile.android.ui.PermissionModelUi.UserPermissionModel
import com.passbolt.mobile.android.ui.ResourcePermission
import com.passbolt.mobile.android.ui.ResourcePermission.OWNER
import com.passbolt.mobile.android.ui.ResourcePermission.READ
import com.passbolt.mobile.android.ui.ResourcePermission.UPDATE
import com.passbolt.mobile.android.ui.UserWithAvatar
import org.junit.Test

class PermissionChangesCalculatorTest {
    @Test
    fun `adding a group to the folder adds it to an item that does not have it`() {
        val folderBefore = listOf(user("tor", OWNER, "f1"))
        val folderAfter = folderBefore + group("troels-og-tor", OWNER, TEMPORARY_NEW_PERMISSION_ID)
        val item = listOf(user("tor", OWNER, "r1"))

        val changes = PermissionChangesCalculator.calculateChanges(folderBefore, folderAfter)
        val result = PermissionChangesCalculator.applyChanges(item, changes)

        assertThat(result).containsExactly(user("tor", OWNER, "r1"), group("troels-og-tor", OWNER, TEMPORARY_NEW_PERMISSION_ID))
    }

    @Test
    fun `an item that already has the added recipient is left alone`() {
        val folderBefore = listOf(user("tor", OWNER, "f1"))
        val folderAfter = folderBefore + user("troels", READ, TEMPORARY_NEW_PERMISSION_ID)
        val item = listOf(user("tor", OWNER, "r1"), user("troels", UPDATE, "r2"))

        val result =
            PermissionChangesCalculator.applyChanges(
                item,
                PermissionChangesCalculator.calculateChanges(folderBefore, folderAfter),
            )

        assertThat(result).isNull()
    }

    @Test
    fun `removing a recipient from the folder removes it only where it has the same access`() {
        val folderBefore = listOf(user("tor", OWNER, "f1"), user("troels", READ, "f2"))
        val folderAfter = listOf(user("tor", OWNER, "f1"))
        val changes = PermissionChangesCalculator.calculateChanges(folderBefore, folderAfter)

        val sameAccess = listOf(user("tor", OWNER, "r1"), user("troels", READ, "r2"))
        val ownAccess = listOf(user("tor", OWNER, "r1"), user("troels", UPDATE, "r2"))

        assertThat(PermissionChangesCalculator.applyChanges(sameAccess, changes)).containsExactly(user("tor", OWNER, "r1"))
        assertThat(PermissionChangesCalculator.applyChanges(ownAccess, changes)).isNull()
    }

    @Test
    fun `an access change is applied only where the item still had the folder's original access`() {
        val folderBefore = listOf(user("tor", OWNER, "f1"), user("troels", READ, "f2"))
        val folderAfter = listOf(user("tor", OWNER, "f1"), user("troels", UPDATE, "f2"))
        val changes = PermissionChangesCalculator.calculateChanges(folderBefore, folderAfter)

        val inherited = listOf(user("tor", OWNER, "r1"), user("troels", READ, "r2"))
        val customised = listOf(user("tor", OWNER, "r1"), user("troels", OWNER, "r2"))

        assertThat(PermissionChangesCalculator.applyChanges(inherited, changes))
            .containsExactly(user("tor", OWNER, "r1"), user("troels", UPDATE, "r2"))
        assertThat(PermissionChangesCalculator.applyChanges(customised, changes)).isNull()
    }

    @Test
    fun `a change that would leave an item without an owner is not applied`() {
        val folderBefore = listOf(user("tor", OWNER, "f1"), group("troels-og-tor", OWNER, "f2"))
        val folderAfter = listOf(user("tor", OWNER, "f1"))
        val item = listOf(group("troels-og-tor", OWNER, "r1"))

        val result =
            PermissionChangesCalculator.applyChanges(
                item,
                PermissionChangesCalculator.calculateChanges(folderBefore, folderAfter),
            )

        assertThat(result).isNull()
    }

    @Test
    fun `no difference means no changes`() {
        val permissions = listOf(user("tor", OWNER, "f1"), group("troels-og-tor", READ, "f2"))

        assertThat(PermissionChangesCalculator.calculateChanges(permissions, permissions)).isEmpty()
    }

    private fun user(
        id: String,
        type: ResourcePermission,
        permissionId: String,
    ): PermissionModelUi =
        UserPermissionModel(
            permission = type,
            permissionId = permissionId,
            user = UserWithAvatar(id, "first-$id", "last-$id", "$id@example.com", false, null),
        )

    private fun group(
        id: String,
        type: ResourcePermission,
        permissionId: String,
    ): PermissionModelUi = GroupPermissionModel(permission = type, permissionId = permissionId, group = GroupModel(id, "group $id"))
}
