package com.risc.alzcare.ui.utils // Or your chosen package for utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

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