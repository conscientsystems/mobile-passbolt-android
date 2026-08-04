package com.passbolt.mobile.android.permissions.grouppermissionsdetails

import com.passbolt.mobile.android.ui.PermissionModelUi.GroupPermissionModel
import com.passbolt.mobile.android.ui.UserUiModel

data class GroupPermissionsState(
    val groupPermission: GroupPermissionModel,
    val users: List<UserUiModel> = emptyList(),
    val isEditMode: Boolean = false,
    val isDeleteConfirmationVisible: Boolean = false,
)
