package com.risc.alzcare.ui.utils

import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import java.util.Locale

fun launchSpeechToTextIntent(
    launcher: ActivityResultLauncher<Intent>
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
    }
}
