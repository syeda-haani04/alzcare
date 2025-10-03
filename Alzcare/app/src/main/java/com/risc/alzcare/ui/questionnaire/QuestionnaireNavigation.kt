package com.risc.alzcare.ui.questionnaire

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import android.util.Log

@Composable
fun QuestionnaireNavigation(
    currentPage: Int,
    isLastPage: Boolean,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonHeight = 72.dp

    Log.d("AnimDebug", "Recomposing Nav - currentPage: $currentPage")
    val targetNextButtonWeight = if (currentPage > 0) 3f else 1f
    Log.d("AnimDebug", "targetNextButtonWeight: $targetNextButtonWeight")

    val animatedNextButtonWeight by animateFloatAsState(
        targetValue = targetNextButtonWeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "NextButtonWeightAnimation",
        finishedListener = { finalValue ->
            Log.d("AnimDebug", "Animation FINISHED. Final weight: $finalValue")
        }
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .height(buttonHeight),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(visible = currentPage > 0) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RectangleShape,
                border = BorderStroke(
                    2.dp,
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    //containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.25F)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    tint = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f),
                    contentDescription = "Previous"
                )
            }
        }

        val buttonText = if (isLastPage) "Submit" else "Next"
        Button(
            onClick = if (isLastPage) onSubmit else onNext,
            enabled = !isLoading,
            modifier = Modifier
                .weight(animatedNextButtonWeight)
                .fillMaxHeight(),
            shape = RectangleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            if (isLoading && isLastPage) {
                CircularProgressIndicator(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            } else {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
