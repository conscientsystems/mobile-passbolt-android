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

package com.passbolt.mobile.android.data.inappreview.datasource.local

import com.google.common.truth.Truth.assertThat
import com.passbolt.mobile.android.domain.inappreview.model.InAppReviewShowMode
import org.junit.Test

/**
 * Guards the persisted ordinal contract: existing users have these integers stored on disk, so the
 * mapping between show mode and ordinal must stay stable across the module migration.
 */
class InAppReviewShowSerializerTest {
    private val serializer = InAppReviewShowSerializer()

    @Test
    fun `serialize maps show modes to their stored ordinals`() {
        assertThat(serializer.serialize(InAppReviewShowMode.FirstShow())).isEqualTo(0)
        assertThat(serializer.serialize(InAppReviewShowMode.ConsecutiveShow())).isEqualTo(1)
    }

    @Test
    fun `deserialize maps stored ordinals back to show modes`() {
        assertThat(serializer.deserialize(0)).isInstanceOf(InAppReviewShowMode.FirstShow::class.java)
        assertThat(serializer.deserialize(1)).isInstanceOf(InAppReviewShowMode.ConsecutiveShow::class.java)
    }
}
