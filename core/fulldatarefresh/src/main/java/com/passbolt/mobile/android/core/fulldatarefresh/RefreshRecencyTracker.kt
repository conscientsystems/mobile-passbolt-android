package com.passbolt.mobile.android.core.fulldatarefresh

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeSource

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
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see GNU Affero General Public License v3 (http://www.gnu.org/licenses/agpl-3.0.html).
 *
 * @copyright Copyright (c) Passbolt SA (https://www.passbolt.com)
 * @license https://opensource.org/licenses/AGPL-3.0 AGPL License
 * @link https://www.passbolt.com Passbolt (tm)
 * @since v1.0
 */

/**
 * Remembers when the last full data refresh completed successfully, per account.
 * Every foreground after the 60 s background grace period sends the user through
 * authentication and Main starts a full refresh - all endpoints, all pages,
 * every time. For a vault of ~1500 resources that is 7-9 s of work whose result
 * is almost always identical to what is already in the database. Automatic
 * refreshes are skipped while the last successful one is younger than
 * [DEFAULT_MAX_AGE]; explicit ones (pull to refresh, after a create/edit/share)
 * always run.
 *
 * In-memory on purpose: a process restart simply refreshes again.
 */
class RefreshRecencyTracker {
    private val lastSuccess = ConcurrentHashMap<String, TimeSource.Monotonic.ValueTimeMark>()

    fun markSuccess(accountId: String) {
        lastSuccess[accountId] = TimeSource.Monotonic.markNow()
    }

    fun isFresh(
        accountId: String,
        maxAge: Duration = DEFAULT_MAX_AGE,
    ): Boolean = lastSuccess[accountId]?.let { it.elapsedNow() < maxAge } ?: false

    fun invalidate(accountId: String) {
        lastSuccess.remove(accountId)
    }

    companion object {
        val DEFAULT_MAX_AGE = 5.minutes
    }
}
