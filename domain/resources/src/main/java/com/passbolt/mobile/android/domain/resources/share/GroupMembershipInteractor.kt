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

package com.passbolt.mobile.android.domain.resources.share

import com.passbolt.mobile.android.core.architecture.result.DomainResult
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticatedUseCaseOutput
import com.passbolt.mobile.android.core.mvp.authentication.AuthenticationState
import com.passbolt.mobile.android.core.passphrasememorycache.PassphraseMemoryCache
import com.passbolt.mobile.android.core.passphrasememorycache.usePassphraseCopy
import com.passbolt.mobile.android.domain.accounts.usecase.GetSelectedAccountUseCase
import com.passbolt.mobile.android.domain.groups.GroupsRepository
import com.passbolt.mobile.android.domain.groups.model.GroupMemberChange
import com.passbolt.mobile.android.domain.groups.model.GroupMembership
import com.passbolt.mobile.android.domain.groups.model.GroupSecret
import com.passbolt.mobile.android.domain.privatekey.PrivateKeyRepository
import com.passbolt.mobile.android.domain.users.usecase.GetLocalUserUseCase
import com.passbolt.mobile.android.gopenpgp.OpenPgp
import com.passbolt.mobile.android.gopenpgp.exception.OpenPgpResult
import timber.log.Timber

/**
 * Adds or removes group members, the way the browser extension does it (GroupUpdateService):
 * a dry run tells which secrets a new member needs, the current user's copies of them are
 * decrypted and encrypted again for the new member, then the update is sent with those
 * secrets. Removing a member needs no secrets - the server drops the member's copies.
 * Only a group manager can do this; the server enforces it.
 */
class GroupMembershipInteractor(
    private val groupsRepository: GroupsRepository,
    private val getSelectedAccountUseCase: GetSelectedAccountUseCase,
    private val getLocalUserUseCase: GetLocalUserUseCase,
    private val privateKeyRepository: PrivateKeyRepository,
    private val passphraseMemoryCache: PassphraseMemoryCache,
    private val openPgp: OpenPgp,
) {
    suspend fun getMembership(groupId: String): Output =
        when (val result = groupsRepository.getGroupMembership(groupId)) {
            is DomainResult.Finished -> Output.Membership(result.value)
            is DomainResult.Incomplete -> Output.Failure(result)
        }

    suspend fun removeMember(
        groupId: String,
        userId: String,
    ): Output {
        val membership =
            when (val result = groupsRepository.getGroupMembership(groupId)) {
                is DomainResult.Finished -> result.value
                is DomainResult.Incomplete -> return Output.Failure(result)
            }
        val entry = membership.members.firstOrNull { it.userId == userId } ?: return Output.Success
        return when (
            val result = groupsRepository.updateGroup(membership, listOf(GroupMemberChange.Remove(entry.membershipId)), emptyList())
        ) {
            is DomainResult.Finished -> Output.Success
            is DomainResult.Incomplete -> Output.Failure(result)
        }
    }

    suspend fun addMember(
        groupId: String,
        userId: String,
        isManager: Boolean,
    ): Output {
        val membership =
            when (val result = groupsRepository.getGroupMembership(groupId)) {
                is DomainResult.Finished -> result.value
                is DomainResult.Incomplete -> return Output.Failure(result)
            }
        if (membership.members.any { it.userId == userId }) {
            return Output.Success
        }
        val changes = listOf(GroupMemberChange.Add(userId, isManager))
        val dryRun =
            when (val result = groupsRepository.updateGroupDryRun(membership, changes)) {
                is DomainResult.Finished -> result.value
                is DomainResult.Incomplete -> return Output.Failure(result)
            }
        Timber.d("Group update dry run: ${dryRun.neededSecrets.size} secret(s) to encrypt for the new member")
        val secrets =
            if (dryRun.neededSecrets.isEmpty()) {
                emptyList()
            } else {
                when (val encrypted = encryptNeededSecrets(membership, dryRun.secrets, dryRun.neededSecrets)) {
                    is Encrypted.Secrets -> encrypted.secrets
                    is Encrypted.Error -> return encrypted.output
                }
            }
        return when (val result = groupsRepository.updateGroup(membership, changes, secrets)) {
            is DomainResult.Finished -> Output.Success
            is DomainResult.Incomplete -> Output.Failure(result)
        }
    }

    private suspend fun encryptNeededSecrets(
        membership: GroupMembership,
        mySecrets: List<GroupSecret>,
        needed: List<Pair<String, String>>,
    ): Encrypted {
        val currentUserId = requireNotNull(getSelectedAccountUseCase.execute(Unit).selectedAccount)
        val privateKey =
            privateKeyRepository.getPrivateKey(currentUserId)?.armoredKey
                ?: return Encrypted.Error(Output.CryptoFailure("Unable to restore private key"))
        val publicKeys =
            needed.map { it.second }.distinct().associateWith { userId ->
                getLocalUserUseCase.execute(GetLocalUserUseCase.Input(userId)).user.gpgKey.armoredKey
            }
        val mySecretByResource = mySecrets.associateBy { it.resourceId }
        Timber.d("Re-encrypting ${needed.size} secret(s) for group ${membership.groupId}")
        return passphraseMemoryCache.usePassphraseCopy(
            onPassphraseNotPresent = { Encrypted.Error(Output.PassphraseRequired) },
        ) { passphrase ->
            val result = mutableListOf<GroupSecret>()
            for ((resourceId, userId) in needed) {
                val mine =
                    mySecretByResource[resourceId]
                        ?: return@usePassphraseCopy Encrypted.Error(Output.CryptoFailure("No readable copy of a needed secret"))
                val plain =
                    when (val decrypted = openPgp.decryptMessageArmored(privateKey, passphrase, mine.data)) {
                        is OpenPgpResult.Result -> decrypted.result
                        is OpenPgpResult.Error ->
                            return@usePassphraseCopy Encrypted.Error(Output.CryptoFailure(decrypted.error.message))
                    }
                val publicKey = publicKeys.getValue(userId)
                when (val encrypted = openPgp.encryptSignMessageArmored(publicKey, privateKey, passphrase, plain)) {
                    is OpenPgpResult.Result -> result.add(GroupSecret(resourceId, userId, encrypted.result))
                    is OpenPgpResult.Error ->
                        return@usePassphraseCopy Encrypted.Error(Output.CryptoFailure(encrypted.error.message))
                }
            }
            Encrypted.Secrets(result)
        }
    }

    private sealed class Encrypted {
        data class Secrets(
            val secrets: List<GroupSecret>,
        ) : Encrypted()

        data class Error(
            val output: Output,
        ) : Encrypted()
    }

    sealed class Output : AuthenticatedUseCaseOutput {
        override val authenticationState: AuthenticationState
            get() =
                when (this) {
                    is Failure if this.incomplete is DomainResult.Incomplete.Unauthorized ->
                        AuthenticationState.Unauthenticated(AuthenticationState.Unauthenticated.Reason.Session)
                    is PassphraseRequired ->
                        AuthenticationState.Unauthenticated(AuthenticationState.Unauthenticated.Reason.Passphrase)
                    else -> AuthenticationState.Authenticated
                }

        data class Membership(
            val membership: GroupMembership,
        ) : Output()

        data object Success : Output()

        data object PassphraseRequired : Output()

        data class CryptoFailure(
            val message: String?,
        ) : Output()

        data class Failure(
            val incomplete: DomainResult.Incomplete,
        ) : Output()
    }
}
