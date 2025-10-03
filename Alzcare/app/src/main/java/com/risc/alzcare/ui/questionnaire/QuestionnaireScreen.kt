package com.risc.alzcare.ui.questionnaire

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.risc.alzcare.network.model.PostResponse
import com.risc.alzcare.ui.theme.CirclesBackground
import com.risc.alzcare.ui.theme.myLayerConfigs
import com.risc.alzcare.ui.utils.SimpleTtsManager
import com.risc.alzcare.ui.utils.launchSpeechToTextIntent
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    viewModel: QuestionnaireViewModel = viewModel(),
    onNavigateToPredictionResult: (response: PostResponse, offset:Float) -> Unit
) {
    Log.d("ScreenDebug", "Recomposing QuestionnaireScreen")
    val uiState by viewModel.uiState.collectAsState()
    val questions = uiState.questions
    val pagerState = rememberPagerState(pageCount = { questions.size })

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var ttsInitialized by remember { mutableStateOf(false) }
    val ttsManager = remember(context) {
        SimpleTtsManager(context) { success ->
            ttsInitialized = success
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, ttsManager) {
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                ttsManager.shutdown()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var currentQuestionIdForStt by remember { mutableStateOf<String?>(null) }
    var currentAnswerTypeForStt by remember { mutableStateOf<AnswerType?>(null) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                    results[0]
                }

            if (!spokenText.isNullOrEmpty() && currentQuestionIdForStt != null && currentAnswerTypeForStt != null) {
                var isValidInput = true
                val answerType = currentAnswerTypeForStt!!

                when (answerType) {
                    AnswerType.NUMBER_INTEGER -> {
                        val numValue = spokenText.toIntOrNull()
                        if (numValue == null) {
                            isValidInput = false
                        } else {
                            val question = questions.find { it.id == currentQuestionIdForStt }
                            if (question?.valueRange != null) {
                                if (numValue < question.valueRange.start || numValue > question.valueRange.endInclusive) {
                                    isValidInput = false
                                }
                            }
                        }
                    }
                    AnswerType.NUMBER_DECIMAL -> {
                        val numValue = spokenText.toFloatOrNull()
                        if (numValue == null) {
                            isValidInput = false
                        } else {
                            val question = questions.find { it.id == currentQuestionIdForStt }
                            if (question?.valueRange != null) {
                                if (numValue < question.valueRange.start || numValue > question.valueRange.endInclusive) {
                                    isValidInput = false
                                }
                            }
                        }
                    }
                    AnswerType.TEXT -> { }
                    else -> { }
                }

                if (isValidInput) {
                    viewModel.recordAnswer(currentQuestionIdForStt!!, spokenText)
                } else {
                    if (ttsInitialized) {
                        ttsManager.speak("Invalid input, please try again.", "stt_invalid_input")
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Invalid input for voice command.")
                        }
                    }
                }
            }
        }
        currentQuestionIdForStt = null
        currentAnswerTypeForStt = null
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            if (currentQuestionIdForStt != null && currentAnswerTypeForStt != null) {
                launchSpeechToTextIntent(speechRecognizerLauncher)
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission is needed for voice input.")
            }
            currentQuestionIdForStt = null
            currentAnswerTypeForStt = null
        }
    }

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            viewModel.onPageChanged(pagerState.currentPage)
        }
    }

    val questionBasedOffsetValue by remember {
        derivedStateOf {
            if (pagerState.pageCount > 1) {
                val progress = pagerState.currentPage.toFloat() / (pagerState.pageCount - 1).toFloat()
                progress - 0.5f
            } else {
                0f
            }
        }
    }

    LaunchedEffect(uiState.predictionNavTrigger) {
        uiState.predictionNavTrigger?.let { responseData ->
            onNavigateToPredictionResult(responseData, questionBasedOffsetValue)
            viewModel.predictionNavigated()
        }
    }

    LaunchedEffect(uiState.submissionError) {
        uiState.submissionError?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long
            )
            viewModel.clearSubmissionError()
        }
    }

    val onPreviousRemembered = remember(pagerState, scope) {
        {
            if (pagerState.currentPage > 0) {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                }
            }
        }
    }

    val onNextRemembered = remember(pagerState, scope, questions.size) {
        {
            if (pagerState.currentPage < questions.size - 1) {
                scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            }
        }
    }

    val onSubmitRemembered = remember(viewModel) {
        {
            viewModel.submitQuestionnaire()
        }
    }

    val currentPageForNav by remember {
        derivedStateOf { pagerState.currentPage }
    }
    val isLastPageForNav by remember {
        derivedStateOf { questions.isNotEmpty() && pagerState.currentPage == questions.size - 1 }
    }



    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent,

    ) {
        CirclesBackground(
            modifier = Modifier.fillMaxSize(),
                layerConfigs = myLayerConfigs,
                questionBasedOffset = questionBasedOffsetValue,
            ) {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    Log.d("ScreenDebug", "BottomBar recomposing. DerivedCP=${currentPageForNav}, DerivedLastPg=${isLastPageForNav}, Load=${uiState.isLoading}")
                    QuestionnaireNavigation(
                        currentPage = currentPageForNav,
                        isLastPage = isLastPageForNav,
                        isLoading = uiState.isLoading,
                        onPrevious = onPreviousRemembered,
                        onNext = onNextRemembered,
                        onSubmit = onSubmitRemembered
                    )
                }
            ) { paddingValues ->
                if (questions.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text("No questions available.")
                    }
                } else {
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
                            },
                            onSpeakClicked = { textToSpeak ->
                                if (ttsInitialized) {
                                    ttsManager.speak(textToSpeak, "question_${question.id}")
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Text-to-speech is not ready.")
                                    }
                                }
                            },
                            onMicClicked = { questionId, answerType ->
                                currentQuestionIdForStt = questionId
                                currentAnswerTypeForStt = answerType
                                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            },
                            currentQuestionNumber = pageIndex + 1,
                            totalQuestions = questions.size
                        )
                    }
                }
            }
        }
    }
}
