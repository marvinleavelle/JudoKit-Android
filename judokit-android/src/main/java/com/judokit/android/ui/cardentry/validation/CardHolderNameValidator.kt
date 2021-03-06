package com.judokit.android.ui.cardentry.validation

import com.judokit.android.ui.cardentry.model.FormFieldEvent
import com.judokit.android.ui.cardentry.model.FormFieldType

data class CardHolderNameValidator(override val fieldType: FormFieldType = FormFieldType.HOLDER_NAME) : Validator {
    override fun validate(input: String, formFieldEvent: FormFieldEvent): ValidationResult {
        return ValidationResult(input.length > 3)
    }
}
