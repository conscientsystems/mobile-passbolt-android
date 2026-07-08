package com.passbolt.mobile.android.core.resourcetypes

import android.database.SQLException
import com.passbolt.mobile.android.core.accounts.usecase.selectedaccount.GetSelectedAccountUseCase
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.architecture.result.DomainResult.Incomplete.Error.Reason.UNKNOWN
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.CompleteAuthenticatedOutput
import com.passbolt.mobile.android.core.mvp.authentication.IncompleteAuthenticatedOutput
import com.passbolt.mobile.android.core.resourcetypes.usecase.db.ResourceTypeIdToSlugMappingProvider
import com.passbolt.mobile.android.domain.resourcetypes.RefreshResourceTypesRepository
import timber.log.Timber

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
class ResourceTypesInteractor(
    private val refreshResourceTypesRepository: RefreshResourceTypesRepository,
    private val resourceTypeIdToSlugMappingProvider: ResourceTypeIdToSlugMappingProvider,
    private val getSelectedAccountUseCase: GetSelectedAccountUseCase,
) {
    suspend fun fetchAndSaveResourceTypes(): Output =
        try {
            val userId = requireNotNull(getSelectedAccountUseCase.execute(Unit).selectedAccount)
            when (val result = refreshResourceTypesRepository.refreshResourceTypes(userId)) {
                is DomainResult.Incomplete -> Output.Failure(result)
                is DomainResult.Finished -> {
                    resourceTypeIdToSlugMappingProvider.invalidateSelectedUserMapping()
                    Output.Success
                }
            }
        } catch (exception: SQLException) {
            Timber.e(exception, "There was an error during resource types db insert")
            Output.Failure(DomainResult.Incomplete.Error(UNKNOWN, exception.message))
        }

    sealed class Output : AuthenticatedUseCaseOutput {
        data object Success : Output(), CompleteAuthenticatedOutput

        data class Failure(
            override val incomplete: DomainResult.Incomplete,
        ) : Output(),
            IncompleteAuthenticatedOutput
    }
}
