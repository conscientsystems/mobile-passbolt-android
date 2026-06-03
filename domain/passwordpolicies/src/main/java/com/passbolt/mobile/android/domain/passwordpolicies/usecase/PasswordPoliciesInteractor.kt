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

package com.passbolt.mobile.android.domain.passwordpolicies.usecase

import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState
import com.passbolt.mobile.android.domain.passwordpolicies.PasswordPoliciesRepository
import com.passbolt.mobile.android.domain.passwordpolicies.mapper.toUiModel
import com.passbolt.mobile.android.domain.passwordpolicies.model.PasswordPolicies
import com.passbolt.mobile.android.domain.passwordpolicies.validation.PasswordPoliciesValidator
import com.passbolt.mobile.android.ui.PasswordPoliciesUiModel

class PasswordPoliciesInteractor(
    private val passwordPoliciesRepository: PasswordPoliciesRepository,
    private val passwordPoliciesValidator: PasswordPoliciesValidator,
) {
    suspend fun fetchAndSavePasswordPolicies(): Output =
        when (val result = passwordPoliciesRepository.getPasswordPolicies()) {
            is DomainResult.Failure -> Output.Failure.FetchFailure(result)
            is DomainResult.Success -> validatePasswordPolicies(result.value)
        }

    private fun validatePasswordPolicies(passwordPolicies: PasswordPolicies): Output =
        if (passwordPoliciesValidator.arePasswordPoliciesValid(passwordPolicies)) {
            Output.Success(passwordPolicies.toUiModel())
        } else {
            Output.Failure.ValidationFailure
        }

    sealed class Output : AuthenticatedUseCaseOutput {
        override val authenticationState: AuthenticationState
            get() {
                val failure = (this as? Failure.FetchFailure)?.failure
                return when (failure) {
                    is DomainResult.Failure.Unauthorized ->
                        AuthenticationState.Unauthenticated(
                            AuthenticationState.Unauthenticated.Reason.Session,
                        )
                    is DomainResult.Failure.MfaRequired ->
                        AuthenticationState.Unauthenticated(
                            AuthenticationState.Unauthenticated.Reason.Mfa(failure.providers),
                        )
                    else -> AuthenticationState.Authenticated
                }
            }

        data class Success(
            val passwordPolicies: PasswordPoliciesUiModel,
        ) : Output()

        sealed class Failure : Output() {
            data class FetchFailure(
                val failure: DomainResult.Failure,
            ) : Failure()

            data object ValidationFailure : Failure()
        }
    }
}
