package com.risc.alzcare.ui.patientdata.state


/**
 * Defines the different kinds of inputs that can be displayed and interacted with in the patient data form.
 * This helps determine ui rendering (eg, TextField, Switch) and behaviour (eg, keyboard type).
 */

enum class AppInputType {
    TEXT,
    NUMBER_INTEGER,
    NUMBER_DECIMAL,
    BOOLEAN_SWITCH,
    DROPDOWN
}

