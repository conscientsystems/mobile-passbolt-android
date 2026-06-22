package com.passbolt.mobile.android.metadata.usecase

import com.passbolt.mobile.android.common.usecase.AsyncUseCase
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.CompleteAuthenticatedOutput
import com.passbolt.mobile.android.core.mvp.authentication.IncompleteAuthenticatedOutput
import com.passbolt.mobile.android.core.networking.NetworkResult
import com.passbolt.mobile.android.core.networking.toDomainResult
import com.passbolt.mobile.android.dto.request.EncryptedDataAndModifiedRequest
import com.passbolt.mobile.android.passboltapi.metadata.MetadataRepository
import java.net.HttpURLConnection.HTTP_CONFLICT
import java.time.ZonedDateTime

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
class UpdateMetadataSessionKeysUseCase(
    private val metadataRepository: MetadataRepository,
) : AsyncUseCase<UpdateMetadataSessionKeysUseCase.Input, UpdateMetadataSessionKeysUseCase.Output> {
    override suspend fun execute(input: Input): Output {
        val response =
            metadataRepository.updateMetadataSessionKeys(
                input.metadataBundleId,
                EncryptedDataAndModifiedRequest(
                    data = input.encryptedData,
                    modified = input.modifiedDate,
                ),
            )
        // 409 means that the bundle has been updated in the meantime by other client
        // TODO once MetadataRepository returns DomainResult, model this 409 as a Conflict result in the data source
        if (response is NetworkResult.Failure && response.errorCode == HTTP_CONFLICT) {
            return Output.Conflict
        }
        return when (val result = response.toDomainResult()) {
            is DomainResult.Finished -> Output.Success
            is DomainResult.Incomplete -> Output.Failure(result)
        }
    }

    data class Input(
        val metadataBundleId: String,
        val encryptedData: String,
        val modifiedDate: ZonedDateTime,
    )

    sealed class Output : AuthenticatedUseCaseOutput {
        data object Success :
            Output(),
            CompleteAuthenticatedOutput

        data object Conflict :
            Output(),
            CompleteAuthenticatedOutput

        data class Failure(
            override val incomplete: DomainResult.Incomplete,
        ) : Output(),
            IncompleteAuthenticatedOutput
    }
}
