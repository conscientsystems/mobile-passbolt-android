package com.passbolt.mobile.android.core.commonfolders.usecase

import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.architecture.result.displayMessage
import com.passbolt.mobile.android.core.commonfolders.usecase.db.GetLocalFolderPermissionsUseCase
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState
import com.passbolt.mobile.android.core.mvp.authentication.UnauthenticatedReason
import com.passbolt.mobile.android.mappers.SharePermissionsModelMapper
import com.passbolt.mobile.android.ui.PermissionModelUi

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
class FolderShareInteractor(
    private val shareFolderUseCase: ShareFolderUseCase,
    private val sharePermissionsModelMapper: SharePermissionsModelMapper,
    private val getLocalFolderPermissionsUseCase: GetLocalFolderPermissionsUseCase,
) {
    // TODO FolderShareInteractor belongs in :share-domain, refactor after GetLocalFolderPermissionsUseCase is moved
    suspend fun shareFolder(
        folderId: String,
        permissions: List<PermissionModelUi>,
    ): Output {
        val existingPermissions =
            getLocalFolderPermissionsUseCase
                .execute(GetLocalFolderPermissionsUseCase.Input(folderId))
                .permissions

        val sharePermissions =
            sharePermissionsModelMapper.mapForShare(
                SharePermissionsModelMapper.ShareItem.Folder(folderId),
                permissions,
                existingPermissions,
            )

        return when (val output = shareFolderUseCase.execute(ShareFolderUseCase.Input(folderId, sharePermissions))) {
            is ShareFolderUseCase.Output.Failure -> Output.ShareFailure(output.incomplete)
            is ShareFolderUseCase.Output.Success -> Output.Success
        }
    }

    sealed class Output : AuthenticatedUseCaseOutput {
        override val authenticationState: AuthenticationState
            get() =
                when (this) {
                    is ShareFailure if this.incomplete is DomainResult.Incomplete.Unauthorized ->
                        AuthenticationState.Unauthenticated(AuthenticationState.Unauthenticated.Reason.Session)
                    is Unauthorized -> AuthenticationState.Unauthenticated(this.reason)
                    else -> AuthenticationState.Authenticated
                }

        data class ShareFailure(
            val incomplete: DomainResult.Incomplete,
        ) : Output() {
            val message: String?
                get() = incomplete.displayMessage()
        }

        class Unauthorized(
            val reason: UnauthenticatedReason,
        ) : Output()

        data object Success : Output()
    }
}
