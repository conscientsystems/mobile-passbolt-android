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

package com.passbolt.mobile.android.core.users.user

import com.passbolt.mobile.android.common.usecase.AsyncUseCase
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.architecture.result.displayMessage
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated.Reason.Mfa
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated.Reason.Session
import com.passbolt.mobile.android.domain.users.UsersRepository
import com.passbolt.mobile.android.domain.users.mapper.toUserModel
import com.passbolt.mobile.android.ui.UserUiModel

class FetchCurrentUserUseCase(
    private val usersRepository: UsersRepository,
) : AsyncUseCase<Unit, FetchCurrentUserUseCase.Output> {
    override suspend fun execute(input: Unit): Output =
        when (val result = usersRepository.getMyProfile()) {
            is DomainResult.Success -> Output.Success(result.value.toUserModel())
            is DomainResult.Failure -> Output.Failure(result)
        }

    sealed class Output : AuthenticatedUseCaseOutput {
        override val authenticationState: AuthenticationState
            get() =
                when (val output = this) {
                    is Failure ->
                        when (val failure = output.failure) {
                            is DomainResult.Failure.Unauthorized ->
                                AuthenticationState.Unauthenticated(Session)
                            is DomainResult.Failure.MfaRequired ->
                                AuthenticationState.Unauthenticated(Mfa(failure.providers))
                            else -> AuthenticationState.Authenticated
                        }
                    else -> AuthenticationState.Authenticated
                }

        data class Success(
            val userUiModel: UserUiModel,
        ) : Output()

        data class Failure(
            val failure: DomainResult.Failure,
        ) : Output() {
            val message: String?
                get() = failure.displayMessage()
        }
    }
}
