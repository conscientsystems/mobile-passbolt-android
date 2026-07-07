package com.passbolt.mobile.android.data.auth.datasource.remote

import com.passbolt.mobile.android.common.CookieExtractor
import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.architecture.result.DomainResult.Incomplete.Error.Reason.UNKNOWN
import com.passbolt.mobile.android.core.architecture.result.map
import com.passbolt.mobile.android.core.networking.NetworkResult
import com.passbolt.mobile.android.core.networking.ResponseHandler
import com.passbolt.mobile.android.core.networking.callWithHandler
import com.passbolt.mobile.android.core.networking.callWithLibraryResponseHandler
import com.passbolt.mobile.android.core.networking.toDomainResult
import com.passbolt.mobile.android.data.auth.datasource.remote.api.AuthApi
import com.passbolt.mobile.android.data.auth.mapper.toDomain
import com.passbolt.mobile.android.domain.auth.datasource.AuthRemoteDataSource
import com.passbolt.mobile.android.domain.auth.model.RefreshedSession
import com.passbolt.mobile.android.domain.auth.model.ServerPgpKey
import com.passbolt.mobile.android.domain.auth.model.ServerRsaKey
import com.passbolt.mobile.android.domain.auth.model.SignInFailureType
import com.passbolt.mobile.android.domain.auth.model.SignInResult
import com.passbolt.mobile.android.dto.request.RefreshSessionRequest
import com.passbolt.mobile.android.dto.request.SignInRequestDto
import com.passbolt.mobile.android.dto.request.SignOutRequestDto
import java.net.HttpURLConnection

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
internal class AuthRemoteDataSourceImpl(
    private val authApi: AuthApi,
    private val responseHandler: ResponseHandler,
    private val cookieExtractor: CookieExtractor,
) : AuthRemoteDataSource {
    override suspend fun fetchServerPublicPgpKey(): DomainResult<ServerPgpKey> =
        callWithHandler(responseHandler) { authApi.getServerPublicPgpKey() }
            .toDomainResult()
            .map { it.toDomain() }

    override suspend fun fetchServerPublicRsaKey(): DomainResult<ServerRsaKey> =
        callWithHandler(responseHandler) { authApi.getServerPublicRsaKey() }
            .toDomainResult()
            .map { it.toDomain() }

    override suspend fun signIn(
        userId: String,
        challenge: String,
        mfaToken: String?,
    ): SignInResult =
        when (
            val result =
                callWithLibraryResponseHandler(responseHandler) {
                    authApi.signIn(SignInRequestDto(userId, challenge), mfaToken)
                }
        ) {
            is NetworkResult.Failure.NetworkError ->
                SignInResult.Failure(result.headerMessage, SignInFailureType.OTHER)
            is NetworkResult.Failure.ServerError ->
                SignInResult.Failure(result.headerMessage, failureType(result.errorCode))
            is NetworkResult.Success ->
                result.value
                    .body()
                    ?.body
                    ?.challenge
                    ?.let { SignInResult.Success(it, cookieExtractor.get(result.value, CookieExtractor.MFA_COOKIE)) }
                    ?: SignInResult.Failure("", SignInFailureType.OTHER)
        }

    override suspend fun signOut(refreshToken: String): DomainResult<Unit> =
        callWithHandler(responseHandler) { authApi.signOut(SignOutRequestDto(refreshToken)) }
            .toDomainResult()
            .map { }

    override suspend fun refreshSession(
        refreshToken: String,
        serverUserId: String,
    ): DomainResult<RefreshedSession> =
        when (
            val result =
                callWithLibraryResponseHandler(responseHandler) {
                    authApi.refreshSession(RefreshSessionRequest(refreshToken, serverUserId))
                }.toDomainResult()
        ) {
            is DomainResult.Finished -> {
                val response = result.value
                val newAccessToken = response.body()?.body?.accessToken
                val newRefreshToken = cookieExtractor.getCookieValue(response, CookieExtractor.REFRESH_TOKEN_COOKIE)
                val mfaToken = cookieExtractor.get(response, CookieExtractor.MFA_COOKIE)
                if (newAccessToken != null && newRefreshToken != null) {
                    DomainResult.Finished(RefreshedSession(newAccessToken, newRefreshToken, mfaToken))
                } else {
                    DomainResult.Incomplete.Error(UNKNOWN, null)
                }
            }
            is DomainResult.Incomplete -> result
        }

    private fun failureType(errorCode: Int?) =
        if (errorCode == HttpURLConnection.HTTP_NOT_FOUND) {
            SignInFailureType.ACCOUNT_DOES_NOT_EXIST
        } else {
            SignInFailureType.OTHER
        }
}
