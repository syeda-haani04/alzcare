package com.risc.alzcare.ui.patientdata.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Represents the state and configuration for a single input field in a form.
 *
 * @property id A unique identifier for this field (eg, "Age", "PatientName").
 * @property label The human readable label to display for this field (eg, "Patient Name").
 * @property value The current string value entered by user.
 * @property appInputType The type of input this field represents (eg, TEXT, NUMBER_INTEGER, BOOLEAN_SWITCH), which helps determine how it's rendered and validated.
 * @property trueValueString The string representation of the 'true' state, mostly for BOOLEAN_SWITCH types.
 * @property falseValueString The string representation of the 'false' state, mostly for BOOLEAN_SWITCH types.
 * @property isEnabled Whether the input field is currently enabled.
 * @property errorMessage An optional error message to display for this specific field if validation fails.
 * @property isMandatory Whether this field is mandatory.
 */
@Parcelize
@Serializable
data class InputFieldModel(
    val id: String,
    val label: String,
    var value: String = "",
    val appInputType: AppInputType = AppInputType.TEXT,
    val trueValueString: String = "1",
    val falseValueString: String = "0",
    val isEnabled: Boolean = true,
    var errorMessage: String? = null,
    val isMandatory: Boolean = false
) : Parcelable

