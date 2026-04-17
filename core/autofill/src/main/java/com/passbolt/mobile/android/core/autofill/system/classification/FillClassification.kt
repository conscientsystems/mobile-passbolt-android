package com.passbolt.mobile.android.core.autofill.system.classification

import com.passbolt.mobile.android.core.navigation.AutofillType
import com.passbolt.mobile.android.ui.ParsedStructure

data class FillClassification(
    val type: AutofillType,
    val anchorFields: List<ParsedStructure>,
)
