package com.passbolt.mobile.android.domain.secrets.offline

import java.time.ZonedDateTime
import timber.log.Timber

/**
 * Process-wide flag: the current session was established locally (passphrase verified
 * against the private key) because the server was unreachable, and no JWT exists.
 *
 * While an offline session is active
 *  - secrets are served from the local encrypted cache only,
 *  - the authenticated-operation runner does not force a server sign-in,
 *  - the UI is read-only (no create / edit / share).
 *
 * The state is left when a server session is established again (full sign-in or a
 * successful session refresh) or on sign-out.
 */
class OfflineSessionState {
    @Volatile
    var isOfflineSession: Boolean = false
        private set

    @Volatile
    var offlineSince: ZonedDateTime? = null
        private set

    fun enterOfflineSession() {
        Timber.d("[Offline] Entering offline session")
        isOfflineSession = true
        offlineSince = ZonedDateTime.now()
    }

    fun exitOfflineSession() {
        if (isOfflineSession) {
            Timber.d("[Offline] Leaving offline session")
        }
        isOfflineSession = false
        offlineSince = null
    }
}
