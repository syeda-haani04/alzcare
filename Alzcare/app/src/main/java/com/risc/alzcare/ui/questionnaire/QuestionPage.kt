package com.risc.alzcare.ui.questionnaire

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun QuestionPage(
    question: Question,
    answerCode: String,
    onAnswerCodeChanged: (String) -> Unit,
    onSpeakClicked: (String) -> Unit,
    onMicClicked: (questionId: String, answerType: AnswerType) -> Unit,
    currentQuestionNumber: Int,
    totalQuestions: Int
) {
    val optionsAndQuestionWidthFraction = 0.95f

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        cursorColor = MaterialTheme.colorScheme.primary,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.38f),
        focusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Question $currentQuestionNumber of $totalQuestions",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth(optionsAndQuestionWidthFraction)
                    .padding(bottom = 8.dp)
            )

            IconButton(
                onClick = { onSpeakClicked(question.text) },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Read question aloud",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (question.answerType) {
                AnswerType.TEXT -> {
                    OutlinedTextField(
                        value = answerCode,
                        onValueChange = onAnswerCodeChanged,
                        label = { Text("Your answer") },
                        modifier = Modifier.fillMaxWidth(optionsAndQuestionWidthFraction),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { onMicClicked(question.id, question.answerType) }) {
                                Icon(Icons.Filled.Mic, "Speak your answer")
                            }
                        },
                        colors = textFieldColors
                    )
                }
                AnswerType.NUMBER_INTEGER -> {
                    if (question.valueRange != null) {
                        var sliderPosition by remember {
                            mutableFloatStateOf(answerCode.toFloatOrNull() ?: question.valueRange.start)
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(optionsAndQuestionWidthFraction),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Selected: ${sliderPosition.roundToInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Slider(
                                value = sliderPosition,
                                onValueChange = { sliderPosition = it },
                                onValueChangeFinished = {
                                    onAnswerCodeChanged(sliderPosition.roundToInt().toString())
                                },
                                valueRange = question.valueRange,
                                steps = (question.valueRange.endInclusive.toInt() - question.valueRange.start.toInt() - 1).coerceAtLeast(0),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )
                        }
                        LaunchedEffect(answerCode) {
                            sliderPosition = answerCode.toFloatOrNull() ?: question.valueRange.start
                        }
                    } else {
                        OutlinedTextField(
                            value = answerCode,
                            onValueChange = onAnswerCodeChanged,
                            label = { Text("Your answer (whole number)") },
                            modifier = Modifier.fillMaxWidth(optionsAndQuestionWidthFraction),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { onMicClicked(question.id, question.answerType) }) {
                                    Icon(Icons.Filled.Mic, "Speak your answer")
                                }
                            },
                            colors = textFieldColors
                        )
                    }
                }
                AnswerType.NUMBER_DECIMAL -> {
                    OutlinedTextField(
                        value = answerCode,
                        onValueChange = onAnswerCodeChanged,
                        label = { Text("Your answer (e.g., 24.5)") },
                        modifier = Modifier.fillMaxWidth(optionsAndQuestionWidthFraction),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { onMicClicked(question.id, question.answerType) }) {
                                Icon(Icons.Filled.Mic, "Speak your answer")
                            }
                        },
                        colors = textFieldColors
                    )
                }
                AnswerType.SINGLE_CHOICE -> {
                    question.options?.forEach { (optionText, optionCode) ->
                        val isSelected = (answerCode == optionCode)
                        val backgroundColor = if (isSelected) {
                            // In QuestionPage.kt, inside the SINGLE_CHOICE part:
                            Log.d("M3Default", "Default primaryContainer: ${MaterialTheme.colorScheme.primaryContainer}")
                            Log.d("M3Default", "Default onPrimaryContainer: ${MaterialTheme.colorScheme.onPrimaryContainer}")
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textColorForButton = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        val border = if (isSelected) {
                            null
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(optionsAndQuestionWidthFraction)
                                .padding(vertical = 8.dp)
                                .heightIn(min = 56.dp)
                                .selectable(
                                    selected = isSelected,
                                    onClick = { onAnswerCodeChanged(optionCode) },
                                    role = Role.RadioButton
                                ),
                            shape = RoundedCornerShape(12.dp),
                            color = backgroundColor,
                            border = border,
                            shadowElevation = if (isSelected) 4.dp else 1.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textColorForButton,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
