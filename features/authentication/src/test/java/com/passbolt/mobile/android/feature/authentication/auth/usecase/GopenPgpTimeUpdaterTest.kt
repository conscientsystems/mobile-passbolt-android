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

package com.passbolt.mobile.android.feature.authentication.auth.usecase

import com.google.common.truth.Truth.assertThat
import com.passbolt.mobile.android.gopenpgp.OpenPgp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.logger.Level
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.inject
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GopenPgpTimeUpdaterTest : KoinTest {
    private val mockOpenPgp = mock<OpenPgp>()
    private val gopenPgpTimeUpdater: GopenPgpTimeUpdater by inject()

    @get:Rule
    val koinTestRule =
        KoinTestRule.create {
            printLogger(Level.ERROR)
            modules(
                module {
                    factory { mockOpenPgp }
                    factoryOf(::GopenPgpTimeUpdater)
                },
            )
        }

    @Before
    fun setup() {
        reset(mockOpenPgp)
        whenever(mockOpenPgp.setTimeOffsetSeconds(any())).then { }
    }

    @Test
    fun `time should be synced if time delta is in range and device time is ahead`() {
        val serverTime = SERVER_TIME
        val deviceTimeAtFetch = serverTime + GopenPgpTimeUpdater.TIME_DELTA_FOR_LOCAL_SYNC_SECS - 1
        val requestDuration = 0L

        val result = gopenPgpTimeUpdater.updateTimeIfNeeded(serverTime, deviceTimeAtFetch, requestDuration)

        assertThat(result).isEqualTo(GopenPgpTimeUpdater.Result.TIME_SYNCED)
    }

    @Test
    fun `time should be synced if time delta is in range and device time is behind`() {
        val serverTime = SERVER_TIME
        val deviceTimeAtFetch = serverTime - GopenPgpTimeUpdater.TIME_DELTA_FOR_LOCAL_SYNC_SECS + 1
        val requestDuration = 0L

        val result = gopenPgpTimeUpdater.updateTimeIfNeeded(serverTime, deviceTimeAtFetch, requestDuration)

        assertThat(result).isEqualTo(GopenPgpTimeUpdater.Result.TIME_SYNCED)
    }

    @Test
    fun `time should not be synced if time delta is out of range and device time is ahead`() {
        val serverTime = SERVER_TIME
        val deviceTimeAtFetch = serverTime + GopenPgpTimeUpdater.TIME_DELTA_FOR_LOCAL_SYNC_SECS + 1
        val requestDuration = 0L

        val result = gopenPgpTimeUpdater.updateTimeIfNeeded(serverTime, deviceTimeAtFetch, requestDuration)

        assertThat(result).isEqualTo(GopenPgpTimeUpdater.Result.TIME_DELTA_TOO_BIG_FOR_SYNC)
    }

    @Test
    fun `time should not be synced if time delta is out of range and device time is behind`() {
        val serverTime = SERVER_TIME
        val deviceTimeAtFetch = serverTime - GopenPgpTimeUpdater.TIME_DELTA_FOR_LOCAL_SYNC_SECS - 1
        val requestDuration = 0L

        val result = gopenPgpTimeUpdater.updateTimeIfNeeded(serverTime, deviceTimeAtFetch, requestDuration)

        assertThat(result).isEqualTo(GopenPgpTimeUpdater.Result.TIME_DELTA_TOO_BIG_FOR_SYNC)
    }

    @Test
    fun `time should be synced on a slow connection when clocks agree`() {
        val requestDuration = 30L
        val serverTime = SERVER_TIME
        val deviceTimeAtFetch = serverTime + requestDuration / 2

        val result = gopenPgpTimeUpdater.updateTimeIfNeeded(serverTime, deviceTimeAtFetch, requestDuration)

        assertThat(result).isEqualTo(GopenPgpTimeUpdater.Result.TIME_SYNCED)
    }

    @Test
    fun `applies the fetch-time offset to gopenpgp when synced`() {
        val serverTime = SERVER_TIME
        val deviceTimeAtFetch = serverTime + 3
        val requestDuration = 0L

        gopenPgpTimeUpdater.updateTimeIfNeeded(serverTime, deviceTimeAtFetch, requestDuration)

        verify(mockOpenPgp).setTimeOffsetSeconds(-3)
    }

    private companion object {
        const val SERVER_TIME = 1_700_000_000L
    }
}
