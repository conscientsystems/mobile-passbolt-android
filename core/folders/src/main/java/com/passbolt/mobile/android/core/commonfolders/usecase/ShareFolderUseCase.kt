package com.passbolt.mobile.android.core.commonfolders.usecase

import com.passbolt.mobile.android.common.usecase.AsyncUseCase
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.architecture.result.displayMessage
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated.Reason.Mfa
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated.Reason.Session
import com.passbolt.mobile.android.domain.share.ShareRepository
import com.passbolt.mobile.android.domain.share.model.SharePermission

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

class ShareFolderUseCase(
    private val shareRepository: ShareRepository,
) : AsyncUseCase<ShareFolderUseCase.Input, ShareFolderUseCase.Output> {
    override suspend fun execute(input: Input): Output =
        when (val result = shareRepository.shareFolder(input.folderId, input.folderPermissions)) {
            is DomainResult.Success -> Output.Success
            is DomainResult.Failure -> Output.Failure(result)
        }

    data class Input(
        val folderId: String,
        val folderPermissions: List<SharePermission>,
    )

    sealed class Output : AuthenticatedUseCaseOutput {
        override val authenticationState: AuthenticationState
            get() =
                when (val output = this) {
                    is Failure ->
                        when (val failure = output.failure) {
                            is DomainResult.Failure.Unauthorized -> AuthenticationState.Unauthenticated(Session)
                            is DomainResult.Failure.MfaRequired -> AuthenticationState.Unauthenticated(Mfa(failure.providers))
                            else -> AuthenticationState.Authenticated
                        }
                    else -> AuthenticationState.Authenticated
                }

        data object Success : Output()

        data class Failure(
            val failure: DomainResult.Failure,
        ) : Output() {
            val message: String?
                get() = failure.displayMessage()
        }
    }
}
