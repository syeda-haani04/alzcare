package com.risc.alzcare.ui.patientdata.state // Correct package

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
/**
 * Represents the overall ui state for the Patient Data Entry screen.
 *
 * @property inputFields The list of input field models that define the form.
 * @property isLoading True if data is currently being submitted to the server, false otherwise.
 * @property submissionError An optional error message to display if submission fails.
 * @property patientId The ID of the patient if this form is for an existing patient (optional).
 */
@Parcelize
@Serializable
data class PatientDataScreenState(
    val inputFields: List<InputFieldModel> = emptyList(),
    val isLoading: Boolean = false,
    val submissionError: String? = null,
    val patientId: String? = null
) : Parcelable

