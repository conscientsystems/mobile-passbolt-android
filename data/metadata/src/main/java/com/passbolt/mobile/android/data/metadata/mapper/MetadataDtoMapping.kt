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

package com.passbolt.mobile.android.data.metadata.mapper

import com.passbolt.mobile.android.domain.metadata.model.MetadataKey
import com.passbolt.mobile.android.domain.metadata.model.MetadataKeysSettings
import com.passbolt.mobile.android.domain.metadata.model.MetadataPrivateKey
import com.passbolt.mobile.android.domain.metadata.model.MetadataSessionKeysBundle
import com.passbolt.mobile.android.domain.metadata.model.MetadataType
import com.passbolt.mobile.android.domain.metadata.model.MetadataTypesSettings
import com.passbolt.mobile.android.dto.response.MetadataKeysResponseDto
import com.passbolt.mobile.android.dto.response.MetadataKeysSettingsResponseDto
import com.passbolt.mobile.android.dto.response.MetadataPrivateKeyDto
import com.passbolt.mobile.android.dto.response.MetadataSessionKeyResponseDto
import com.passbolt.mobile.android.dto.response.MetadataTypeDto
import com.passbolt.mobile.android.dto.response.MetadataTypesSettingsResponseDto
import java.time.ZonedDateTime

internal fun MetadataKeysResponseDto.toDomain(): MetadataKey =
    MetadataKey(
        id = id,
        fingerprint = fingerprint,
        armoredKey = armoredKey,
        modified = ZonedDateTime.parse(modified),
        expired = expired?.let { ZonedDateTime.parse(it) },
        deleted = deleted?.let { ZonedDateTime.parse(it) },
        metadataPrivateKeys = metadataPrivateKeys.map { it.toDomain() },
    )

internal fun MetadataPrivateKeyDto.toDomain(): MetadataPrivateKey =
    MetadataPrivateKey(
        id = id,
        metadataKeyId = metadataKeyId,
        userId = userId,
        pgpMessage = encryptedKeyData,
        created = created,
        createdBy = createdBy,
        modified = modified,
        modifiedBy = modifiedBy,
    )

internal fun MetadataKeysSettingsResponseDto.toDomain(): MetadataKeysSettings =
    MetadataKeysSettings(
        allowUsageOfPersonalKeys = allowUsageOfPersonalKeys,
        zeroKnowledgeKeyShare = zeroKnowledgeKeyShare,
    )

internal fun MetadataTypesSettingsResponseDto.toDomain(): MetadataTypesSettings =
    MetadataTypesSettings(
        defaultMetadataType = defaultMetadataType.toDomain(),
        defaultFolderType = defaultFolderType.toDomain(),
        defaultTagType = defaultTagType.toDomain(),
        allowCreationOfV5Resources = allowCreationOfV5Resources,
        allowCreationOfV5Folders = allowCreationOfV5Folders,
        allowCreationOfV5Tags = allowCreationOfV5Tags,
        allowCreationOfV4Resources = allowCreationOfV4Resources,
        allowCreationOfV4Folders = allowCreationOfV4Folders,
        allowCreationOfV4Tags = allowCreationOfV4Tags,
        allowV4V5Upgrade = allowV4V5Upgrade,
        allowV5V4Downgrade = allowV5V4Downgrade,
    )

internal fun MetadataTypeDto.toDomain(): MetadataType =
    when (this) {
        MetadataTypeDto.V4 -> MetadataType.V4
        MetadataTypeDto.V5 -> MetadataType.V5
    }

internal fun MetadataSessionKeyResponseDto.toDomain(): MetadataSessionKeysBundle =
    MetadataSessionKeysBundle(
        id = id,
        userId = userId,
        data = data,
        created = ZonedDateTime.parse(created),
        modified = ZonedDateTime.parse(modified),
    )
