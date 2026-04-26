package com.passbolt.mobile.android.core.autofill.system.classification

import com.passbolt.mobile.android.core.navigation.AutofillType
import com.passbolt.mobile.android.ui.ParsedStructure

sealed class FillClassification {
    abstract val type: AutofillType
    abstract val allFields: List<ParsedStructure>

    val anchorDomain: String?
        get() = allFields.firstOrNull { it.domain != null }?.domain

    data class Credentials(
        val username: ParsedStructure,
        val password: ParsedStructure,
    ) : FillClassification() {
        override val type = AutofillType.CREDENTIALS
        override val allFields = listOf(username, password)
    }

    data class CredentialsAndTotp(
        val username: ParsedStructure,
        val password: ParsedStructure,
        val totp: ParsedStructure,
    ) : FillClassification() {
        override val type = AutofillType.CREDENTIALS_AND_TOTP
        override val allFields = listOf(username, password, totp)
    }

    data class Totp(
        val totp: ParsedStructure,
    ) : FillClassification() {
        override val type = AutofillType.TOTP
        override val allFields = listOf(totp)
    }
}
