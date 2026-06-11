package com.passbolt.mobile.android.permissions.userpermissionsdetails

import com.passbolt.mobile.android.ui.PermissionModelUi.UserPermissionModel
import com.passbolt.mobile.android.ui.UserUiModel

data class UserPermissionsState(
    val permission: UserPermissionModel,
    val user: UserUiModel? = null,
    val isEditMode: Boolean = false,
    val isDeleteConfirmationVisible: Boolean = false,
)
