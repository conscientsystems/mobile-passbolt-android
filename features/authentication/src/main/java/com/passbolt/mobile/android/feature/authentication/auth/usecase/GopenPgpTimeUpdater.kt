package com.passbolt.mobile.android.feature.authentication.auth.usecase

import androidx.annotation.VisibleForTesting
import com.passbolt.mobile.android.gopenpgp.OpenPgp
import timber.log.Timber
import kotlin.math.abs

class GopenPgpTimeUpdater(
    private val openPgp: OpenPgp,
) {
    fun updateTimeIfNeeded(
        serverTimeSeconds: Long,
        deviceTimeAtFetchSeconds: Long,
        getTimeRequestDurationSeconds: Long,
    ): Result {
        // The server timestamp is already stale by the time the phone reads it - the response still had
        // to travel back, roughly half the round-trip. Adding that half (instead of subtracting the whole
        // request duration) stops a slow connection from looking like a wrong device clock.
        val timeDeltaSeconds =
            serverTimeSeconds - deviceTimeAtFetchSeconds + getTimeRequestDurationSeconds / 2

        return if (abs(timeDeltaSeconds) <= TIME_DELTA_FOR_LOCAL_SYNC_SECS) {
            Timber.d("Local time sync needed. Adjusted: $timeDeltaSeconds")
            openPgp.setTimeOffsetSeconds(timeDeltaSeconds)
            Result.TIME_SYNCED
        } else {
            Timber.d("Time delta to big for sync: $timeDeltaSeconds. Showing error.")
            Result.TIME_DELTA_TOO_BIG_FOR_SYNC
        }
    }

    enum class Result {
        TIME_SYNCED,
        TIME_DELTA_TOO_BIG_FOR_SYNC,
    }

    companion object {
        @VisibleForTesting
        const val TIME_DELTA_FOR_LOCAL_SYNC_SECS = 10
    }
}
