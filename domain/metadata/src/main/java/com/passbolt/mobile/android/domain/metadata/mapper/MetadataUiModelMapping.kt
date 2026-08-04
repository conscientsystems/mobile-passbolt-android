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

package com.passbolt.mobile.android.domain.metadata.mapper

import com.passbolt.mobile.android.domain.metadata.model.MetadataKey
import com.passbolt.mobile.android.domain.metadata.model.MetadataKeysSettings
import com.passbolt.mobile.android.domain.metadata.model.MetadataPrivateKey
import com.passbolt.mobile.android.domain.metadata.model.MetadataSessionKeysBundle
import com.passbolt.mobile.android.domain.metadata.model.MetadataType
import com.passbolt.mobile.android.domain.metadata.model.MetadataTypesSettings
import com.passbolt.mobile.android.domain.metadata.model.ParsedMetadataKey
import com.passbolt.mobile.android.domain.metadata.model.ParsedMetadataPrivateKey
import com.passbolt.mobile.android.ui.MetadataKeyModel
import com.passbolt.mobile.android.ui.MetadataKeysSettingsModel
import com.passbolt.mobile.android.ui.MetadataPrivateKeyModel
import com.passbolt.mobile.android.ui.MetadataSessionKeysBundleModel
import com.passbolt.mobile.android.ui.MetadataTypeModel
import com.passbolt.mobile.android.ui.MetadataTypesSettingsModel
import com.passbolt.mobile.android.ui.ParsedMetadataKeyModel
import com.passbolt.mobile.android.ui.ParsedMetadataPrivateKeyModel

fun MetadataKeysSettings.toUiModel(): MetadataKeysSettingsModel =
    MetadataKeysSettingsModel(
        allowUsageOfPersonalKeys = allowUsageOfPersonalKeys,
        zeroKnowledgeKeyShare = zeroKnowledgeKeyShare,
    )

fun MetadataTypesSettings.toUiModel(): MetadataTypesSettingsModel =
    MetadataTypesSettingsModel(
        defaultMetadataType = defaultMetadataType.toUiModel(),
        defaultFolderType = defaultFolderType.toUiModel(),
        defaultTagType = defaultTagType.toUiModel(),
        allowCreationOfV5Resources = allowCreationOfV5Resources,
        allowCreationOfV5Folders = allowCreationOfV5Folders,
        allowCreationOfV5Tags = allowCreationOfV5Tags,
        allowCreationOfV4Resources = allowCreationOfV4Resources,
        allowCreationOfV4Folders = allowCreationOfV4Folders,
        allowCreationOfV4Tags = allowCreationOfV4Tags,
        allowV4V5Upgrade = allowV4V5Upgrade,
        allowV5V4Downgrade = allowV5V4Downgrade,
    )

fun MetadataType.toUiModel(): MetadataTypeModel =
    when (this) {
        MetadataType.V4 -> MetadataTypeModel.V4
        MetadataType.V5 -> MetadataTypeModel.V5
    }

fun MetadataKey.toUiModel(): MetadataKeyModel =
    MetadataKeyModel(
        id = id,
        fingerprint = fingerprint,
        armoredKey = armoredKey,
        modified = modified,
        expired = expired,
        deleted = deleted,
        metadataPrivateKeys = metadataPrivateKeys.map { it.toUiModel() },
    )

fun MetadataPrivateKey.toUiModel(): MetadataPrivateKeyModel =
    MetadataPrivateKeyModel(
        id = id,
        metadataKeyId = metadataKeyId,
        userId = userId,
        pgpMessage = pgpMessage,
        created = created,
        createdBy = createdBy,
        modified = modified,
        modifiedBy = modifiedBy,
    )

fun ParsedMetadataKey.toUiModel(): ParsedMetadataKeyModel =
    ParsedMetadataKeyModel(
        id = id,
        armoredKey = armoredKey,
        fingerprint = fingerprint,
        modified = modified,
        expired = expired,
        deleted = deleted,
        metadataPrivateKeys = metadataPrivateKeys.map { it.toUiModel() },
    )

fun ParsedMetadataPrivateKey.toUiModel(): ParsedMetadataPrivateKeyModel =
    ParsedMetadataPrivateKeyModel(
        id = id,
        userId = userId,
        keyData = keyData,
        passphrase = passphrase,
        created = created,
        createdBy = createdBy,
        modified = modified,
        modifiedBy = modifiedBy,
        fingerprint = fingerprint,
        domain = domain,
        pgpMessage = pgpMessage,
    )

fun MetadataSessionKeysBundle.toUiModel(): MetadataSessionKeysBundleModel =
    MetadataSessionKeysBundleModel(
        id = id,
        userId = userId,
        data = data,
        created = created,
        modified = modified,
    )

fun MetadataKeysSettingsModel.toDomain(): MetadataKeysSettings =
    MetadataKeysSettings(
        allowUsageOfPersonalKeys = allowUsageOfPersonalKeys,
        zeroKnowledgeKeyShare = zeroKnowledgeKeyShare,
    )

fun MetadataTypesSettingsModel.toDomain(): MetadataTypesSettings =
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

fun MetadataTypeModel.toDomain(): MetadataType =
    when (this) {
        MetadataTypeModel.V4 -> MetadataType.V4
        MetadataTypeModel.V5 -> MetadataType.V5
    }

fun ParsedMetadataKeyModel.toDomain(): ParsedMetadataKey =
    ParsedMetadataKey(
        id = id,
        armoredKey = armoredKey,
        fingerprint = fingerprint,
        modified = modified,
        expired = expired,
        deleted = deleted,
        metadataPrivateKeys = metadataPrivateKeys.map { it.toDomain() },
    )

fun ParsedMetadataPrivateKeyModel.toDomain(): ParsedMetadataPrivateKey =
    ParsedMetadataPrivateKey(
        id = id,
        userId = userId,
        keyData = keyData,
        passphrase = passphrase,
        created = created,
        createdBy = createdBy,
        modified = modified,
        modifiedBy = modifiedBy,
        fingerprint = fingerprint,
        domain = domain,
        pgpMessage = pgpMessage,
    )
