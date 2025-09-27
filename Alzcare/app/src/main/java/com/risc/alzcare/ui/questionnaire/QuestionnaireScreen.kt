package com.risc.alzcare.ui.questionnaire

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.risc.alzcare.network.model.PostResponse
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

class SimpleTtsManager(
    context: Context,
    private val onInit: (Boolean) -> Unit
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported or missing data")
                    isInitialized = false
                    onInit(false)
                } else {
                    isInitialized = true
                    onInit(true)
                }
            } else {
                Log.e("TTS", "TTS Initialization failed with status: $status")
                isInitialized = false
                onInit(false)
            }
        }
    }

    fun speak(text: String, utteranceId: String = "defaultUtteranceId") {
        if (isInitialized && tts != null) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            Log.e("TTS", "TTS not initialized or null, cannot speak.")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    viewModel: QuestionnaireViewModel = viewModel(),
    onNavigateToPredictionResult: (response: PostResponse) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val questions = uiState.questions
    val pagerState = rememberPagerState(pageCount = { questions.size })
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var ttsInitialized by remember { mutableStateOf(false) }
    val ttsManager = remember {
        SimpleTtsManager(context) { success ->
            ttsInitialized = success
            if (!success) {
                Log.e("QuestionnaireScreen", "TTS Initialization failed.")
            }
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, ttsManager) {
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                ttsManager.shutdown()
                Log.d("QuestionnaireScreen", "TTS Shutdown on Destroy")
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
                    AnswerType.TEXT -> { /* Always valid for now */ }
                    else -> { /* No STT for other types */ }
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

    LaunchedEffect(uiState.predictionNavTrigger) {
        uiState.predictionNavTrigger?.let { responseData ->
            onNavigateToPredictionResult(responseData)
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Alzheimer's Risk Questionnaire") },
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
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
                },
                onSpeakClicked = { textToSpeak ->
                    if (ttsInitialized) {
                        ttsManager.speak(textToSpeak, "question_${question.id}")
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Text-to-speech is not ready.")
                        }
                        Log.w("QuestionnaireScreen", "Speak called but TTS not initialized.")
                    }
                },
                onMicClicked = { questionId, answerType ->
                    currentQuestionIdForStt = questionId
                    currentAnswerTypeForStt = answerType
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            )
        }
    }
}

fun launchSpeechToTextIntent(
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your answer")
    }
    try {
        launcher.launch(intent)
    } catch (e: Exception) {
        Log.e("STT", "Speech recognizer not available or error: ${e.message}")
        // Consider showing a Snackbar here as well via a callback or passed SnackbarHostState
    }
}

@Composable
fun QuestionPage(
    question: Question,
    answerCode: String,
    onAnswerCodeChanged: (String) -> Unit,
    onSpeakClicked: (String) -> Unit,
    onMicClicked: (questionId: String, answerType: AnswerType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onSpeakClicked(question.text) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Read question aloud",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        when (question.answerType) {
            AnswerType.TEXT -> {
                OutlinedTextField(
                    value = answerCode,
                    onValueChange = onAnswerCodeChanged,
                    label = { Text("Your answer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { onMicClicked(question.id, question.answerType) }) {
                            Icon(Icons.Filled.Mic, "Speak your answer")
                        }
                    }
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
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { onMicClicked(question.id, question.answerType) }) {
                                Icon(Icons.Filled.Mic, "Speak your answer")
                            }
                        }
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
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { onMicClicked(question.id, question.answerType) }) {
                            Icon(Icons.Filled.Mic, "Speak your answer")
                        }
                    }
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
