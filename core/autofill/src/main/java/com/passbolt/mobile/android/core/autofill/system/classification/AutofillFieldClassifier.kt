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

package com.passbolt.mobile.android.core.autofill.system.classification

import com.passbolt.mobile.android.core.autofill.system.AutofillField
import com.passbolt.mobile.android.core.autofill.system.FillableInputsFinder
import com.passbolt.mobile.android.core.navigation.AutofillType
import com.passbolt.mobile.android.ui.ParsedStructure

class AutofillFieldClassifier(
    private val fillableInputsFinder: FillableInputsFinder,
) {
    fun classifyFill(structures: Set<ParsedStructure>): FillClassification? {
        val usernameView = findAutofillableView(AutofillField.USERNAME, structures)
        val passwordView = findAutofillableView(AutofillField.PASSWORD, structures)
        val totpView = findAutofillableView(AutofillField.TOTP, structures)

        val credentialsViews = listOfNotNull(usernameView, passwordView)
        val allViews = credentialsViews + listOfNotNull(totpView)

        val hasCredentials = credentialsViews.size == 2
        val hasTotp = totpView != null
        val credentialsShareDomain = credentialsViews.map { it.domain }.toSet().size == 1
        val allViewsShareDomain = allViews.map { it.domain }.toSet().size == 1

        val canFillCredentials = hasCredentials && credentialsShareDomain
        val canAddTotpWithCredentials = canFillCredentials && hasTotp && allViewsShareDomain

        return when {
            canAddTotpWithCredentials ->
                FillClassification(AutofillType.CREDENTIALS_AND_TOTP, credentialsViews + totpView)
            canFillCredentials ->
                FillClassification(AutofillType.CREDENTIALS, credentialsViews)
            hasTotp ->
                FillClassification(AutofillType.TOTP, listOf(totpView))
            else -> null
        }
    }

    private fun findAutofillableView(
        field: AutofillField,
        autofillStructure: Set<ParsedStructure>,
    ) = fillableInputsFinder.findStructureForAutofillFields(field, autofillStructure)
}
