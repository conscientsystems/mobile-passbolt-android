package com.passbolt.mobile.android.feature.authentication.auth.usecase

import com.passbolt.mobile.android.common.usecase.AsyncUseCase
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated.Reason.Mfa
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated.Reason.Session
import com.passbolt.mobile.android.domain.mfa.MfaRepository
import com.passbolt.mobile.android.domain.mfa.model.YubikeyVerification

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
class VerifyYubikeyUseCase(
    private val mfaRepository: MfaRepository,
) : AsyncUseCase<VerifyYubikeyUseCase.Input, VerifyYubikeyUseCase.Output> {
    override suspend fun execute(input: Input): Output =
        when (val result = mfaRepository.verifyYubikeyOtp(input.totp, input.remember, input.jwtHeader)) {
            is DomainResult.Success ->
                when (val verification = result.value) {
                    is YubikeyVerification.Succeeded -> Output.Success(verification.mfaHeader)
                    is YubikeyVerification.Unauthorized -> Output.Unauthorized
                    is YubikeyVerification.NotFromCurrentUser -> Output.YubikeyNotFromCurrentUser
                    is YubikeyVerification.OtherFailure -> Output.NetworkFailure(verification.errorCode)
                }
            is DomainResult.Failure -> Output.Failure(result)
        }

    data class Input(
        val totp: String,
        val jwtHeader: String?,
        val remember: Boolean,
    )

    sealed class Output : AuthenticatedUseCaseOutput {
        override val authenticationState: AuthenticationState
            get() =
                when (val failure = (this as? Failure)?.failure) {
                    is DomainResult.Failure.Unauthorized -> AuthenticationState.Unauthenticated(Session)
                    is DomainResult.Failure.MfaRequired -> AuthenticationState.Unauthenticated(Mfa(failure.providers))
                    else -> AuthenticationState.Authenticated
                }

        data class Success(
            val mfaHeader: String?,
        ) : Output()

        data class NetworkFailure(
            val errorCode: Int,
        ) : Output()

        data object Unauthorized : Output()

        data object YubikeyNotFromCurrentUser : Output()

        data class Failure(
            val failure: DomainResult.Failure,
        ) : Output()
    }
}
