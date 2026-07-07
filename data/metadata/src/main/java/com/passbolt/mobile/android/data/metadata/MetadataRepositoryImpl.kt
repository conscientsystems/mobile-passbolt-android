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

package com.passbolt.mobile.android.data.metadata

import com.passbolt.mobile.android.domain.metadata.MetadataRepository
import com.passbolt.mobile.android.domain.metadata.datasource.MetadataKeysLocalDataSource
import com.passbolt.mobile.android.domain.metadata.datasource.MetadataSettingsLocalDataSource
import com.passbolt.mobile.android.domain.metadata.model.MetadataKeyPurpose
import com.passbolt.mobile.android.domain.metadata.model.MetadataKeysSettings
import com.passbolt.mobile.android.domain.metadata.model.MetadataTypesSettings
import com.passbolt.mobile.android.domain.metadata.model.ParsedMetadataKey
import com.passbolt.mobile.android.domain.metadata.model.TrustedMetadataKey

internal class MetadataRepositoryImpl(
    private val settingsLocalDataSource: MetadataSettingsLocalDataSource,
    private val keysLocalDataSource: MetadataKeysLocalDataSource,
) : MetadataRepository {
    override suspend fun getMetadataKeysSettings(): MetadataKeysSettings = settingsLocalDataSource.getMetadataKeysSettings()

    override suspend fun saveMetadataKeysSettings(metadataKeysSettings: MetadataKeysSettings) =
        settingsLocalDataSource.saveMetadataKeysSettings(metadataKeysSettings)

    override suspend fun getMetadataTypesSettings(): MetadataTypesSettings = settingsLocalDataSource.getMetadataTypesSettings()

    override suspend fun saveMetadataTypesSettings(metadataTypesSettings: MetadataTypesSettings) =
        settingsLocalDataSource.saveMetadataTypesSettings(metadataTypesSettings)

    override suspend fun getTrustedMetadataKey(): TrustedMetadataKey? = settingsLocalDataSource.getTrustedMetadataKey()

    override suspend fun saveTrustedMetadataKey(trustedMetadataKey: TrustedMetadataKey) =
        settingsLocalDataSource.saveTrustedMetadataKey(trustedMetadataKey)

    override suspend fun deleteTrustedMetadataKey() = settingsLocalDataSource.deleteTrustedMetadataKey()

    override suspend fun getLocalMetadataKeys(purpose: MetadataKeyPurpose): List<ParsedMetadataKey> =
        keysLocalDataSource.getMetadataKeys(purpose)

    override suspend fun getLocalMetadataKey(metadataKeyId: String): ParsedMetadataKey = keysLocalDataSource.getMetadataKey(metadataKeyId)

    override suspend fun rebuildMetadataKeysTables(metadataKeys: List<ParsedMetadataKey>) =
        keysLocalDataSource.rebuildMetadataKeys(metadataKeys)
}
