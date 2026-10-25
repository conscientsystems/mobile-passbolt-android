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

package com.passbolt.mobile.android.groupdetails.groupmembers

import androidx.lifecycle.viewModelScope
import com.passbolt.mobile.android.core.compose.SideEffectViewModel
import com.passbolt.mobile.android.core.mvp.coroutinecontext.CoroutineLaunchContext
import com.passbolt.mobile.android.domain.accounts.usecase.GetSelectedAccountDataUseCase
import com.passbolt.mobile.android.domain.groups.usecase.GetGroupWithUsersUseCase
import com.passbolt.mobile.android.domain.groups.usecase.GroupsInteractor
import com.passbolt.mobile.android.domain.permissionsconfirmation.usecase.GetPermissionsSnapshotUseCase
import com.passbolt.mobile.android.domain.resources.share.GroupMembershipInteractor
import com.passbolt.mobile.android.domain.users.mapper.toUserModel
import com.passbolt.mobile.android.domain.users.usecase.GetLocalUsersUseCase
import com.passbolt.mobile.android.feature.authentication.session.runAuthenticatedOperation
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersIntent.AddMember
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersIntent.ConfirmRemoveMember
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersIntent.DismissAddMember
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersIntent.DismissRemoveMember
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersIntent.GoBack
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersIntent.GoToMemberDetails
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersIntent.Initialize
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersIntent.OpenAddMember
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersIntent.RequestRemoveMember
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersSideEffect.NavigateToMemberDetails
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersSideEffect.NavigateUp
import com.passbolt.mobile.android.groupdetails.groupmembers.GroupMembersSideEffect.ShowMessage
import com.passbolt.mobile.android.ui.PermissionModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import timber.log.Timber

internal class GroupMembersViewModel(
    private val getGroupWithUsersUseCase: GetGroupWithUsersUseCase,
    private val getPermissionsSnapshotUseCase: GetPermissionsSnapshotUseCase,
    private val coroutineLaunchContext: CoroutineLaunchContext,
    private val groupMembershipInteractor: GroupMembershipInteractor,
    private val groupsInteractor: GroupsInteractor,
    private val getLocalUsersUseCase: GetLocalUsersUseCase,
    private val getSelectedAccountDataUseCase: GetSelectedAccountDataUseCase,
) : SideEffectViewModel<GroupMembersState, GroupMembersSideEffect>(GroupMembersState()) {
    private var fromSnapshot = false
    private var groupId: String? = null

    fun onIntent(intent: GroupMembersIntent) {
        when (intent) {
            GoBack -> emitSideEffect(NavigateUp)
            is Initialize -> {
                fromSnapshot = intent.fromSnapshot
                groupId = intent.groupId
                if (intent.fromSnapshot) {
                    loadSnapshotGroupMembers(intent.groupId)
                } else {
                    loadGroupMembers(intent.groupId)
                }
            }
            is GoToMemberDetails -> emitSideEffect(NavigateToMemberDetails(intent.userId, fromSnapshot))
            OpenAddMember -> openAddMember()
            is AddMember -> changeMembership { id -> groupMembershipInteractor.addMember(id, intent.userId, isManager = false) }
            DismissAddMember -> updateViewState { copy(addableUsers = null) }
            is RequestRemoveMember -> updateViewState { copy(memberToRemove = intent.member) }
            ConfirmRemoveMember -> {
                val member = viewState.value.memberToRemove ?: return
                changeMembership { id -> groupMembershipInteractor.removeMember(id, member.id) }
            }
            DismissRemoveMember -> updateViewState { copy(memberToRemove = null) }
        }
    }

    private fun openAddMember() {
        viewModelScope.launch(coroutineLaunchContext.io) {
            val memberIds =
                viewState.value.members
                    .map { it.id }
                    .toSet()
            val users =
                getLocalUsersUseCase
                    .execute(GetLocalUsersUseCase.Input())
                    .users
                    .filter { it.id !in memberIds && !it.disabled }
            updateViewState { copy(addableUsers = users) }
        }
    }

    private fun changeMembership(change: suspend (groupId: String) -> GroupMembershipInteractor.Output) {
        val id = groupId ?: return
        val isAdding = viewState.value.memberToRemove == null
        updateViewState { copy(addableUsers = null, memberToRemove = null, isWorking = true) }
        viewModelScope.launch(coroutineLaunchContext.io) {
            val result = runAuthenticatedOperation { change(id) }
            if (result is GroupMembershipInteractor.Output.Success) {
                runAuthenticatedOperation { groupsInteractor.fetchAndSaveGroups() }
                emitSideEffect(ShowMessage(if (isAdding) MembershipMessage.MEMBER_ADDED else MembershipMessage.MEMBER_REMOVED))
            } else {
                Timber.e("Group membership change failed: $result")
                emitSideEffect(ShowMessage(MembershipMessage.FAILED))
            }
            updateViewState { copy(isWorking = false) }
            loadGroupMembers(id)
        }
    }

    private fun loadManagerFlag(groupId: String) {
        viewModelScope.launch(coroutineLaunchContext.io) {
            // managing is an extra: any failure here just leaves the screen read-only
            val isManager =
                try {
                    val myServerId = getSelectedAccountDataUseCase.execute(Unit).serverId
                    val output = runAuthenticatedOperation { groupMembershipInteractor.getMembership(groupId) }
                    output is GroupMembershipInteractor.Output.Membership &&
                        output.membership.members.any { it.userId == myServerId && it.isManager }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    Timber.e(exception, "Could not check the group manager role")
                    false
                }
            updateViewState { copy(canManage = isManager) }
        }
    }

    private fun loadSnapshotGroupMembers(groupId: String) {
        viewModelScope.launch(coroutineLaunchContext.io) {
            val snapshot = getPermissionsSnapshotUseCase.execute(Unit).snapshot
            when {
                // the snapshot is not available (i.e. after process death) - close instead of showing unconfirmed
                snapshot == null -> emitSideEffect(NavigateUp)
                groupId in snapshot.groupsMembers -> {
                    val groupName =
                        snapshot.permissions
                            .filterIsInstance<PermissionModel.GroupPermissionModel>()
                            .firstOrNull { it.group.groupId == groupId }
                            ?.group
                            ?.groupName
                            .orEmpty()
                    val members =
                        snapshot.groupsMembers
                            .getValue(groupId)
                            .mapNotNull { memberId -> snapshot.users[memberId] }
                            .map { it.toUserModel() }
                    updateViewState { copy(groupName = groupName, members = members) }
                }
                // not part of the snapshot - a group added from the local search during confirmation
                else -> loadGroupMembers(groupId)
            }
        }
    }

    private fun loadGroupMembers(groupId: String) {
        if (!fromSnapshot) {
            loadManagerFlag(groupId)
        }
        viewModelScope.launch(coroutineLaunchContext.io) {
            val result = getGroupWithUsersUseCase.execute(GetGroupWithUsersUseCase.Input(groupId))
            updateViewState {
                copy(
                    groupName = result.groupWithUsers.group.groupName,
                    members = result.groupWithUsers.users,
                )
            }
        }
    }
}
