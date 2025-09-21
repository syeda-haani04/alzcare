package com.risc.alzcare.ui.questionnaire

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    viewModel: QuestionnaireViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val questions = uiState.questions
    val pagerState = rememberPagerState(pageCount = { questions.size })
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            viewModel.onPageChanged(pagerState.currentPage)
        }
    }

    LaunchedEffect(uiState.submissionStatus) {
        uiState.submissionStatus?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            viewModel.clearSubmissionStatus()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Alzheimer's Risk Questionnaire") })
        },
        bottomBar = {
            QuestionnaireNavigation(
                currentPage = uiState.currentPage,
                isLastPage = uiState.isLastPage,
                isLoading = uiState.isLoading,
                onPrevious = {
                    if (pagerState.currentPage > 0) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                onNext = {
                    if (pagerState.currentPage < questions.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                onSubmit = {
                    viewModel.submitQuestionnaire()
                }
            )
        }
    ) { paddingValues ->
        if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No questions available.")
            }
            return@Scaffold
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            userScrollEnabled = false
        ) { pageIndex ->
            val question = questions[pageIndex]
            QuestionPage(
                question = question,
                answerCode = uiState.answers[question.id] ?: "",
                onAnswerCodeChanged = { answerCode ->
                    viewModel.recordAnswer(question.id, answerCode)
                }
            )
        }
    }
}

@Composable
fun QuestionPage(
    question: Question,
    answerCode: String,
    onAnswerCodeChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = question.text,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        )

        when (question.answerType) {
            AnswerType.TEXT -> {
                OutlinedTextField(
                    value = answerCode,
                    onValueChange = onAnswerCodeChanged,
                    label = { Text("Your answer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            AnswerType.NUMBER_INTEGER -> {
                if (question.valueRange != null) {
                    var sliderPosition by remember {
                        mutableFloatStateOf(answerCode.toFloatOrNull() ?: question.valueRange.start)
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Selected: ${sliderPosition.roundToInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Slider(
                            value = sliderPosition,
                            onValueChange = {
                                sliderPosition = it
                            },
                            onValueChangeFinished = {
                                onAnswerCodeChanged(sliderPosition.roundToInt().toString())
                            },
                            valueRange = question.valueRange,
                            steps = (question.valueRange.endInclusive.toInt() - question.valueRange.start.toInt() - 1).coerceAtLeast(0),
                            modifier = Modifier.padding(horizontal = 16.dp)
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
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
            AnswerType.NUMBER_DECIMAL -> {
                OutlinedTextField(
                    value = answerCode,
                    onValueChange = onAnswerCodeChanged,
                    label = { Text("Your answer (e.g., 24.5)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
            AnswerType.SINGLE_CHOICE -> {
                question.options?.let { options ->
                    Column(Modifier.selectableGroup()) {
                        options.forEach { (optionText, optionCode) ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (answerCode == optionCode),
                                        onClick = { onAnswerCodeChanged(optionCode) },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (answerCode == optionCode),
                                    onClick = null
                                )
                                Text(
                                    text = optionText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionnaireNavigation(
    currentPage: Int,
    isLastPage: Boolean,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentPage > 0) {
            Button(onClick = onPrevious, enabled = !isLoading) {
                Text("Previous")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Text(
            text = "Question ${currentPage + 1}",
            modifier = Modifier.weight(1f).wrapContentWidth(Alignment.CenterHorizontally)
        )


        if (isLastPage) {
            Button(onClick = onSubmit, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Submit")
                }
            }
        } else {
            Button(onClick = onNext, enabled = !isLoading) {
                Text("Next")
            }
        }
    }
}
