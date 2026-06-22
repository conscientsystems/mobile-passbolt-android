package com.passbolt.mobile.android.core.mvp.authentication

import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.architecture.result.DomainResult.Incomplete.Error.Reason.OFFLINE
import com.passbolt.mobile.android.core.architecture.result.DomainResult.Incomplete.Error.Reason.SERVER
import com.passbolt.mobile.android.core.architecture.result.displayMessage
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Authenticated
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated.Reason.Mfa
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated.Reason.Mfa.MfaProvider.TOTP
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated.Reason.Passphrase
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState.Unauthenticated.Reason.Session
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class IncompleteAuthenticatedOutputTest {
    @Test
    fun `Unauthorized maps to a session reason`() {
        val state = DomainResult.Incomplete.Unauthorized.toAuthenticationState()

        assertEquals(Session, assertIs<Unauthenticated>(state).reason)
    }

    @Test
    fun `MfaRequired maps to an mfa reason preserving providers`() {
        val providers = listOf(TOTP)

        val state = DomainResult.Incomplete.MfaRequired(providers).toAuthenticationState()

        assertEquals(Mfa(providers), assertIs<Unauthenticated>(state).reason)
    }

    @Test
    fun `NotCached maps to authenticated`() {
        assertEquals(Authenticated, DomainResult.Incomplete.NotCached.toAuthenticationState())
    }

    @Test
    fun `PassphraseNotInCache maps to a passphrase reason`() {
        val state = DomainResult.Incomplete.PassphraseNotInCache.toAuthenticationState()

        assertEquals(Passphrase, assertIs<Unauthenticated>(state).reason)
    }

    @Test
    fun `Error maps to authenticated regardless of reason`() {
        assertEquals(Authenticated, DomainResult.Incomplete.Error(SERVER, "boom").toAuthenticationState())
        assertEquals(Authenticated, DomainResult.Incomplete.Error(OFFLINE, null).toAuthenticationState())
    }

    @Test
    fun `IncompleteAuthenticatedOutput delegates authenticationState to its incomplete`() {
        val output =
            object : IncompleteAuthenticatedOutput {
                override val incomplete = DomainResult.Incomplete.Unauthorized
            }

        assertEquals(Session, assertIs<Unauthenticated>(output.authenticationState).reason)
    }

    @Test
    fun `displayMessage returns the Error message and null for every other incomplete`() {
        assertEquals("boom", DomainResult.Incomplete.Error(SERVER, "boom").displayMessage())
        assertEquals(null, DomainResult.Incomplete.Error(OFFLINE, null).displayMessage())
        assertEquals(null, DomainResult.Incomplete.Unauthorized.displayMessage())
        assertEquals(null, DomainResult.Incomplete.NotCached.displayMessage())
        assertEquals(null, DomainResult.Incomplete.PassphraseNotInCache.displayMessage())
        assertEquals(null, DomainResult.Incomplete.MfaRequired(emptyList()).displayMessage())
    }
}
