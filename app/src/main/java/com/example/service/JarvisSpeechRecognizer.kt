package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.model.VoiceLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisSpeechRecognizer(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isCurrentlyListening = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(
        language: VoiceLanguage,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isCurrentlyListening) {
            stopListening()
        }

        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isCurrentlyListening = true
                    _isListening.value = true
                    _audioRms.value = 0.1f
                }

                override fun onBeginningOfSpeech() {
                    _audioRms.value = 0.5f
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Normalize -2dB to 10dB to 0f..1f range for HUD waveforms
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
                    _audioRms.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    isCurrentlyListening = false
                    _audioRms.value = 0f
                }

                override fun onError(error: Int) {
                    isCurrentlyListening = false
                    _isListening.value = false
                    _audioRms.value = 0f
                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                        SpeechRecognizer.ERROR_CLIENT -> "Client error. Please try again."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                        SpeechRecognizer.ERROR_NETWORK -> "Network connection required."
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timed out."
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Say again, Sir."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy."
                        SpeechRecognizer.ERROR_SERVER -> "Recognition server error."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard."
                        else -> "Recognition issue: $error"
                    }
                    onError(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    isCurrentlyListening = false
                    _isListening.value = false
                    _audioRms.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        onResult(text)
                    } else {
                        onError("Could not understand, Sir.")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    partialMatches?.firstOrNull()?.let {
                        // Could report partials if desired
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

                val langTag = if (language == VoiceLanguage.HINDI) "hi-IN" else "en-IN"
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("JarvisSpeech", "Failed to start speech recognition", e)
            isCurrentlyListening = false
            _isListening.value = false
            _audioRms.value = 0f
            onError("Speech recognizer unavailable on this device: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            Log.e("JarvisSpeech", "Error stopping recognizer", e)
        } finally {
            isCurrentlyListening = false
            _isListening.value = false
            _audioRms.value = 0f
        }
    }

    fun destroy() {
        stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
