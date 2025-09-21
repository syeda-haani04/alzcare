package com.risc.alzcare.ui.questionnaire

data class Question(
    val id: String,
    val text: String,
    val answerType: AnswerType,
    val options: List<Pair<String, String>>? = null,
    val valueRange: ClosedFloatingPointRange<Float>? = null
)

enum class AnswerType {
    TEXT,
    NUMBER_INTEGER,
    NUMBER_DECIMAL,
    SINGLE_CHOICE
}

fun getYesNoOptions(): List<Pair<String, String>> {
    return listOf("Yes" to "1", "No" to "0")
}
   