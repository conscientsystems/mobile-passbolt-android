package com.passbolt.mobile.android.mappers

import com.passbolt.mobile.android.entity.user.User
import com.passbolt.mobile.android.entity.user.UserGpgKey
import com.passbolt.mobile.android.entity.user.UserProfile
import com.passbolt.mobile.android.entity.user.UserUpdateState
import com.passbolt.mobile.android.ui.GpgKeyUiModel
import com.passbolt.mobile.android.ui.UserProfileUiModel
import com.passbolt.mobile.android.ui.UserUiModel
import com.passbolt.mobile.android.ui.UserWithAvatar

class UsersModelMapper {
    fun map(input: UserUiModel) =
        User(
            id = input.id,
            userName = input.userName,
            profile =
                UserProfile(
                    firstName = input.profile.firstName,
                    lastName = input.profile.lastName,
                    avatarUrl = input.profile.avatarUrl,
                ),
            disabled = input.disabled,
            gpgKey =
                UserGpgKey(
                    id = input.gpgKey.id,
                    armoredKey = input.gpgKey.armoredKey,
                    bits = input.gpgKey.bits,
                    uid = input.gpgKey.uid,
                    keyId = input.gpgKey.keyId,
                    fingerprint = input.gpgKey.fingerprint,
                    type = input.gpgKey.type,
                    expires = input.gpgKey.keyExpirationDate,
                    created = input.gpgKey.keyCreationDate,
                ),
            updateState = UserUpdateState.UPDATED,
        )

    fun map(input: User) =
        UserUiModel(
            id = input.id,
            userName = input.userName,
            gpgKey =
                GpgKeyUiModel(
                    id = input.gpgKey.id,
                    armoredKey = input.gpgKey.armoredKey,
                    fingerprint = input.gpgKey.fingerprint,
                    bits = input.gpgKey.bits,
                    uid = input.gpgKey.uid,
                    keyId = input.gpgKey.keyId,
                    type = input.gpgKey.type,
                    keyExpirationDate = input.gpgKey.expires,
                    keyCreationDate = input.gpgKey.created,
                ),
            disabled = input.disabled,
            profile =
                UserProfileUiModel(
                    username = input.userName,
                    firstName = input.profile.firstName,
                    lastName = input.profile.lastName,
                    avatarUrl = input.profile.avatarUrl,
                ),
        )

    fun mapToUserWithAvatar(input: UserUiModel) =
        UserWithAvatar(
            userId = input.id,
            firstName = input.profile.firstName.orEmpty(),
            lastName = input.profile.lastName.orEmpty(),
            userName = input.userName,
            avatarUrl = input.profile.avatarUrl,
            isDisabled = input.disabled,
        )
}
