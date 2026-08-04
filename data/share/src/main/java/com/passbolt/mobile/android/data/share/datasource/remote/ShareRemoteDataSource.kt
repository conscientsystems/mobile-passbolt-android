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

package com.passbolt.mobile.android.data.share.datasource.remote

import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.architecture.result.map
import com.passbolt.mobile.android.core.networking.ResponseHandler
import com.passbolt.mobile.android.core.networking.callWithHandler
import com.passbolt.mobile.android.core.networking.toDomainResult
import com.passbolt.mobile.android.data.share.datasource.remote.api.ShareApi
import com.passbolt.mobile.android.data.share.mapper.toDomain
import com.passbolt.mobile.android.data.share.mapper.toDto
import com.passbolt.mobile.android.domain.share.ShareDataSource
import com.passbolt.mobile.android.domain.share.model.EncryptedSecret
import com.passbolt.mobile.android.domain.share.model.ShareChanges
import com.passbolt.mobile.android.domain.share.model.SharePermission
import com.passbolt.mobile.android.dto.request.FolderShareRequest
import com.passbolt.mobile.android.dto.request.ResourceShareRequest
import com.passbolt.mobile.android.dto.request.SimulateShareRequest

internal class ShareRemoteDataSource(
    private val shareApi: ShareApi,
    private val responseHandler: ResponseHandler,
) : ShareDataSource {
    override suspend fun simulateShareResource(
        resourceId: String,
        permissions: List<SharePermission>,
    ): DomainResult<ShareChanges> =
        callWithHandler(responseHandler) {
            shareApi.simulateShareResource(resourceId, SimulateShareRequest(permissions.map { it.toDto() })).body
        }.toDomainResult().map { it.toDomain() }

    override suspend fun shareResource(
        resourceId: String,
        permissions: List<SharePermission>,
        secrets: List<EncryptedSecret>,
    ): DomainResult<Unit> =
        callWithHandler(responseHandler) {
            shareApi
                .shareResource(
                    resourceId,
                    ResourceShareRequest(permissions.map { it.toDto() }, secrets.map { it.toDto() }),
                ).body
        }.toDomainResult()

    override suspend fun shareFolder(
        folderId: String,
        permissions: List<SharePermission>,
    ): DomainResult<Unit> =
        callWithHandler(responseHandler) {
            shareApi.shareFolder(folderId, FolderShareRequest(permissions.map { it.toDto() })).body
        }.toDomainResult()
}
