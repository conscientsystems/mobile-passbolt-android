package com.passbolt.mobile.android.feature.autofill.resources.datasetstrategy

import android.app.Activity
import android.content.Intent
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.passbolt.mobile.android.core.autofill.system.AssistStructureParser
import com.passbolt.mobile.android.core.autofill.system.AutofillField
import com.passbolt.mobile.android.core.autofill.system.FillableInputsFinder
import com.passbolt.mobile.android.feature.autofill.autofill.RemoteViewsFactory
import com.passbolt.mobile.android.ui.ParsedStructure

class ReturnAutofillDataset(
    private val autofillCallback: AutofillCallback,
    private val assistStructureParser: AssistStructureParser,
    private val fillableInputsFinder: FillableInputsFinder,
    private val remoteViewsFactory: RemoteViewsFactory,
) : ReturnAutofillDatasetStrategy {
    override fun returnDataset(payload: AutofillPayload) {
        val parsedStructures = assistStructureParser.parse(autofillCallback.getAutofillStructure())
        val fieldIds = resolveFieldIds(parsedStructures.structures)
        val responseBuilder = buildFillResponse(payload, fieldIds)

        val replyIntent =
            Intent().apply {
                putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, responseBuilder.build())
            }
        autofillCallback.setResultAndFinish(Activity.RESULT_OK, replyIntent)
    }

    private fun resolveFieldIds(structures: Set<ParsedStructure>) =
        FieldIds(
            usernameId = fillableInputsFinder.findStructureForAutofillFields(AutofillField.USERNAME, structures)?.id,
            passwordId = fillableInputsFinder.findStructureForAutofillFields(AutofillField.PASSWORD, structures)?.id,
            totpId = fillableInputsFinder.findStructureForAutofillFields(AutofillField.TOTP, structures)?.id,
        )

    private fun buildFillResponse(
        payload: AutofillPayload,
        ids: FieldIds,
    ): FillResponse.Builder {
        val label = remoteViewsFactory.getAutofillFillDropdown()
        val dataset =
            Dataset
                .Builder()
                .apply {
                    addValue(ids.usernameId, payload.username, label)
                    addValue(ids.passwordId, payload.password, label)
                    addValue(ids.totpId, payload.totpCode, label)
                }.build()
        return FillResponse.Builder().addDataset(dataset)
    }

    private fun Dataset.Builder.addValue(
        id: AutofillId?,
        valueText: String?,
        presentation: RemoteViews,
    ) {
        if (id != null && valueText != null) {
            setValue(id, AutofillValue.forText(valueText), presentation)
        }
    }

    private data class FieldIds(
        val usernameId: AutofillId?,
        val passwordId: AutofillId?,
        val totpId: AutofillId?,
    )
}
