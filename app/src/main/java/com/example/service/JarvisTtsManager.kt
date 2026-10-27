package com.example.service

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.VoiceLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class JarvisTtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val pendingSpeechQueue = mutableListOf<PendingUtterance>()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechAmplitude = MutableStateFlow(0f)
    val speechAmplitude: StateFlow<Float> = _speechAmplitude.asStateFlow()

    private var currentLanguage: VoiceLanguage = VoiceLanguage.ENGLISH
    private var isMuted = false

    private data class PendingUtterance(
        val text: String,
        val isHindi: Boolean,
        val onDone: () -> Unit
    )

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupVoiceParams()
            setupProgressListener()

            // Process any pending utterances queued before initialization
            synchronized(pendingSpeechQueue) {
                pendingSpeechQueue.forEach { item ->
                    speakInternal(item.text, item.isHindi, item.onDone)
                }
                pendingSpeechQueue.clear()
            }
        } else {
            Log.e("JarvisTts", "TTS initialization failed with code: $status")
        }
    }

    private fun setupVoiceParams() {
        tts?.let { engine ->
            // Futuristic, poised Jarvis style: slightly lower pitch, measured cadence
            engine.setPitch(0.90f)
            engine.setSpeechRate(0.98f)

            val locale = if (currentLanguage == VoiceLanguage.HINDI) {
                Locale("hi", "IN")
            } else {
                Locale("en", "GB").takeIf { engine.isLanguageAvailable(it) >= TextToSpeech.LANG_AVAILABLE }
                    ?: Locale("en", "US")
            }

            val langResult = engine.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w("JarvisTts", "Preferred voice language $locale not fully supported, falling back to default")
                engine.setLanguage(Locale.getDefault())
            }
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                _speechAmplitude.value = 0.85f
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                _speechAmplitude.value = 0f
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                _speechAmplitude.value = 0f
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                _speechAmplitude.value = 0f
            }
        })
    }

    fun setLanguage(language: VoiceLanguage) {
        currentLanguage = language
        if (isInitialized) {
            setupVoiceParams()
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        if (muted) {
            stop()
        }
    }

    fun isMuted(): Boolean = isMuted

    fun speak(text: String, isHindi: Boolean = false, onDone: () -> Unit = {}) {
        if (isMuted) {
            onDone()
            return
        }

        if (!isInitialized) {
            synchronized(pendingSpeechQueue) {
                pendingSpeechQueue.add(PendingUtterance(text, isHindi, onDone))
            }
            return
        }

        speakInternal(text, isHindi, onDone)
    }

    private fun speakInternal(text: String, isHindi: Boolean, onDone: () -> Unit) {
        tts?.let { engine ->
            val locale = if (isHindi || currentLanguage == VoiceLanguage.HINDI) {
                Locale("hi", "IN")
            } else {
                Locale("en", "GB").takeIf { engine.isLanguageAvailable(it) >= TextToSpeech.LANG_AVAILABLE }
                    ?: Locale("en", "US")
            }
            engine.language = locale

            val utteranceId = "JARVIS_${System.currentTimeMillis()}"
            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
            }
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _speechAmplitude.value = 0f
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
