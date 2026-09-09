package com.passbolt.mobile.android.core.fulldatarefresh

import com.passbolt.mobile.android.common.datarefresh.DataRefreshStatus.Idle.FinishedWithFailure
import com.passbolt.mobile.android.common.datarefresh.DataRefreshStatus.Idle.FinishedWithSuccess
import com.passbolt.mobile.android.common.datarefresh.DataRefreshStatus.InProgress
import com.passbolt.mobile.android.common.datarefresh.DataRefreshTrackingFlow
import com.passbolt.mobile.android.core.fulldatarefresh.HomeDataInteractor.Output.Failure
import com.passbolt.mobile.android.core.fulldatarefresh.HomeDataInteractor.Output.Success
import com.passbolt.mobile.android.core.mvp.coroutinecontext.CoroutineLaunchContext
import com.passbolt.mobile.android.feature.authentication.session.runAuthenticatedOperation
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.passbolt.mobile.android.domain.accounts.usecase.GetSelectedAccountUseCase
import com.passbolt.mobile.android.domain.secrets.offline.OfflineSessionState
import com.passbolt.mobile.android.feature.authentication.auth.usecase.RefreshSessionUseCase
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

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

class FullDataRefreshExecutor(
    private val homeDataInteractor: HomeDataInteractor,
    private val dataRefreshTrackingFlow: DataRefreshTrackingFlow,
    private val coroutineLaunchContext: CoroutineLaunchContext,
    private val refreshRecencyTracker: RefreshRecencyTracker,
    private val getSelectedAccountUseCase: GetSelectedAccountUseCase,
    private val offlineSessionState: OfflineSessionState,
    private val refreshSessionUseCase: RefreshSessionUseCase,
) {
    /**
     * @param force run even if a refresh for this account completed recently.
     * Automatic refreshes (app entry) pass false; user-initiated ones true.
     */
    suspend fun performFullDataRefresh(force: Boolean = false) {
        Timber.d("Full data refresh initiated (force=$force)")
        val accountId = getSelectedAccountUseCase.execute(Unit).selectedAccount
        if (!force && accountId != null && refreshRecencyTracker.isFresh(accountId)) {
            Timber.d("Full data refresh skipped - last successful refresh is recent")
            dataRefreshTrackingFlow.updateStatus(FinishedWithSuccess)
            return
        }
        if (offlineSessionState.isOfflineSession && !tryLeaveOfflineSession()) {
            Timber.d("Full data refresh skipped - offline session and the server is still unreachable")
            dataRefreshTrackingFlow.updateStatus(FinishedWithFailure)
            return
        }
        if (!dataRefreshTrackingFlow.isInProgress()) {
            dataRefreshTrackingFlow.updateStatus(InProgress(progress = 0f))
            val output =
                runAuthenticatedOperation {
                    withContext(coroutineLaunchContext.default) {
                        homeDataInteractor.refreshAllHomeScreenData { progress ->
                            dataRefreshTrackingFlow.updateStatus(InProgress(progress))
                        }
                    }
                }

            when (output) {
                is Success -> {
                    accountId?.let { refreshRecencyTracker.markSuccess(it) }
                    dataRefreshTrackingFlow.updateStatus(InProgress(progress = 1f))
                    delay(FULL_PROGRESS_DISPLAY_MILLIS.milliseconds)
                    dataRefreshTrackingFlow.updateStatus(FinishedWithSuccess)
                }
                is Failure -> dataRefreshTrackingFlow.updateStatus(FinishedWithFailure)
            }
        }
    }

    /**
     * During an offline session a refresh is only possible if the server is back: a
     * session refresh with the stored refresh token is the cheapest probe, and on success
     * it also restores the JWT, so the refresh below runs as a normal online one.
     */
    private suspend fun tryLeaveOfflineSession(): Boolean =
        when (refreshSessionUseCase.execute(Unit)) {
            is RefreshSessionUseCase.Output.Success -> {
                Timber.d("Server reachable again - leaving offline session")
                offlineSessionState.exitOfflineSession()
                true
            }
            is RefreshSessionUseCase.Output.Failure -> false
        }

    private companion object {
        private const val FULL_PROGRESS_DISPLAY_MILLIS = 300L
    }
}
