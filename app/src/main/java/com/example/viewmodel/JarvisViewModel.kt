package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AppLauncherItem
import com.example.model.CallStateInfo
import com.example.model.ChatMessage
import com.example.model.CommandAction
import com.example.model.JarvisStatus
import com.example.model.MessageSender
import com.example.model.SystemTelemetry
import com.example.model.VoiceLanguage
import com.example.service.CallAnnouncementManager
import com.example.service.CommandProcessor
import com.example.service.JarvisAppLauncher
import com.example.service.JarvisSpeechRecognizer
import com.example.service.JarvisTtsManager
import com.example.service.SystemTelemetryManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

data class JarvisUiState(
    val status: JarvisStatus = JarvisStatus.INITIALIZING,
    val selectedLanguage: VoiceLanguage = VoiceLanguage.ENGLISH,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isMuted: Boolean = false,
    val liveAudioWave: Float = 0f,
    val latestTranscript: String = "",
    val latestResponse: String = "JARVIS PROTOCOL INITIALIZED. STANDING BY.",
    val messages: List<ChatMessage> = emptyList(),
    val installedApps: List<AppLauncherItem> = emptyList(),
    val telemetry: SystemTelemetry = SystemTelemetry(),
    val callState: CallStateInfo = CallStateInfo(),
    val isPermissionDialogVisible: Boolean = false,
    val confirmationDialogTitle: String? = null,
    val confirmationDialogMessage: String? = null,
    val pendingConfirmationAction: (() -> Unit)? = null
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val ttsManager = JarvisTtsManager(application)
    private val speechRecognizer = JarvisSpeechRecognizer(application)
    private val appLauncher = JarvisAppLauncher(application)
    private val telemetryManager = SystemTelemetryManager(application)
    private val commandProcessor = CommandProcessor()

    private val _uiState = MutableStateFlow(JarvisUiState())
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    private var speechPulseJob: Job? = null

    init {
        loadInstalledApps()
        observeTelemetry()
        observeTtsSpeaking()
        observeSpeechRecognition()
        observeCallAnnouncements()
        performInitialGreeting()
    }

    private fun loadInstalledApps() {
        val apps = appLauncher.getAppsWithStatus()
        _uiState.value = _uiState.value.copy(installedApps = apps)
    }

    private fun observeTelemetry() {
        viewModelScope.launch {
            telemetryManager.telemetry.collect { tele ->
                _uiState.value = _uiState.value.copy(telemetry = tele)
            }
        }
    }

    private fun observeTtsSpeaking() {
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                val newStatus = if (speaking) {
                    JarvisStatus.SPEAKING
                } else if (_uiState.value.isListening) {
                    JarvisStatus.LISTENING
                } else {
                    JarvisStatus.ONLINE
                }

                _uiState.value = _uiState.value.copy(
                    isSpeaking = speaking,
                    status = newStatus
                )

                if (speaking) {
                    startTtsWavePulse()
                } else {
                    speechPulseJob?.cancel()
                    if (!_uiState.value.isListening) {
                        _uiState.value = _uiState.value.copy(liveAudioWave = 0f)
                    }
                }
            }
        }
    }

    private fun startTtsWavePulse() {
        speechPulseJob?.cancel()
        speechPulseJob = viewModelScope.launch {
            while (isActive && ttsManager.isSpeaking.value) {
                // Generate dynamic cybernetic audio pulses for Jarvis's speech visualizer
                val randomWave = (0.35f + Math.random().toFloat() * 0.65f)
                _uiState.value = _uiState.value.copy(liveAudioWave = randomWave)
                delay(70)
            }
        }
    }

    private fun observeSpeechRecognition() {
        viewModelScope.launch {
            speechRecognizer.isListening.collect { listening ->
                val newStatus = if (listening) {
                    JarvisStatus.LISTENING
                } else if (_uiState.value.isSpeaking) {
                    JarvisStatus.SPEAKING
                } else {
                    JarvisStatus.ONLINE
                }
                _uiState.value = _uiState.value.copy(
                    isListening = listening,
                    status = newStatus
                )
            }
        }

        viewModelScope.launch {
            speechRecognizer.audioRms.collect { rms ->
                if (_uiState.value.isListening) {
                    _uiState.value = _uiState.value.copy(liveAudioWave = rms)
                }
            }
        }
    }

    private fun observeCallAnnouncements() {
        CallAnnouncementManager.registerListener { callerName, number ->
            handleIncomingCall(callerName, number)
        }

        viewModelScope.launch {
            CallAnnouncementManager.callState.collect { callInfo ->
                _uiState.value = _uiState.value.copy(callState = callInfo)
            }
        }
    }

    private fun performInitialGreeting() {
        viewModelScope.launch {
            delay(600)
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val timeSalutation = when (hour) {
                in 4..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                else -> "Good evening"
            }
            val greeting = "$timeSalutation, Sir. Jarvis is online."

            _uiState.value = _uiState.value.copy(
                status = JarvisStatus.ONLINE,
                latestResponse = greeting,
                messages = listOf(
                    ChatMessage(
                        sender = MessageSender.JARVIS,
                        text = greeting,
                        actionSummary = "INITIALIZATION: Systems Nominal"
                    )
                )
            )

            ttsManager.speak(greeting, isHindi = false)
        }
    }

    fun toggleVoiceListening() {
        if (_uiState.value.isListening) {
            speechRecognizer.stopListening()
            _uiState.value = _uiState.value.copy(isListening = false, status = JarvisStatus.ONLINE)
        } else {
            ttsManager.stop()
            _uiState.value = _uiState.value.copy(
                isListening = true,
                status = JarvisStatus.LISTENING,
                latestTranscript = "Listening to your voice command, Sir..."
            )

            speechRecognizer.startListening(
                language = _uiState.value.selectedLanguage,
                onResult = { text ->
                    onCommandReceived(text)
                },
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        isListening = false,
                        status = JarvisStatus.ONLINE,
                        latestTranscript = error
                    )
                }
            )
        }
    }

    fun submitTextCommand(commandText: String) {
        if (commandText.isBlank()) return
        onCommandReceived(commandText)
    }

    private fun onCommandReceived(commandText: String) {
        val userMsg = ChatMessage(sender = MessageSender.USER, text = commandText)
        val updatedMessages = _uiState.value.messages + userMsg

        _uiState.value = _uiState.value.copy(
            latestTranscript = commandText,
            status = JarvisStatus.PROCESSING,
            messages = updatedMessages
        )

        val result = commandProcessor.processCommand(commandText, _uiState.value.telemetry)

        val jarvisMsg = ChatMessage(
            sender = MessageSender.JARVIS,
            text = result.displayText,
            actionSummary = result.actionSummary
        )

        _uiState.value = _uiState.value.copy(
            latestResponse = result.displayText,
            messages = updatedMessages + jarvisMsg,
            status = JarvisStatus.SPEAKING
        )

        // Speak response
        ttsManager.speak(result.spokenResponse, isHindi = result.isHindi)

        // Execute system/app action
        executeCommandAction(result.action)
    }

    private fun executeCommandAction(action: CommandAction) {
        when (action) {
            is CommandAction.LaunchApp -> {
                launchTargetApp(action.packageName, action.appName, action.fallbackUrl)
            }
            is CommandAction.ToggleTorch -> {
                val success = telemetryManager.setFlashlight(action.enable)
                if (!success) {
                    val errorText = "Sir, flashlight hardware is not available on this device."
                    ttsManager.speak(errorText, isHindi = false)
                }
            }
            is CommandAction.OpenSetting -> {
                telemetryManager.openSystemSetting(action.settingIntent)
            }
            is CommandAction.CheckBattery -> {
                // Battery announced by TTS
            }
            is CommandAction.AnnounceTime -> {
                // Time announced by TTS
            }
            is CommandAction.OpenCamera -> {
                appLauncher.launchCamera()
            }
            is CommandAction.OpenDialer -> {
                appLauncher.launchDialer()
            }
            CommandAction.None -> {
                // Pure conversation
            }
        }
    }

    fun launchTargetApp(packageName: String, appName: String, fallbackUrl: String? = null) {
        val result = appLauncher.launchAppByPackage(packageName, fallbackUrl)
        when (result) {
            is JarvisAppLauncher.AppLaunchResult.Success -> {
                // Application launched
            }
            is JarvisAppLauncher.AppLaunchResult.NotInstalled -> {
                val warnText = "Sir, $appName is not installed on this device."
                ttsManager.speak(warnText, isHindi = false)
                _uiState.value = _uiState.value.copy(
                    confirmationDialogTitle = "$appName Not Installed",
                    confirmationDialogMessage = "Would you like to install $appName from the Google Play Store or open the web version?",
                    pendingConfirmationAction = {
                        appLauncher.openPlayStore(packageName)
                    }
                )
            }
            is JarvisAppLauncher.AppLaunchResult.Error -> {
                ttsManager.speak("Sir, unable to open $appName.", isHindi = false)
            }
        }
    }

    private fun handleIncomingCall(callerName: String?, number: String) {
        _uiState.value = _uiState.value.copy(status = JarvisStatus.ALERT)

        val callAnnouncementHindi = if (!callerName.isNullOrBlank()) {
            "Sir, aapka phone aaya hai. $callerName call kar rahe hain."
        } else if (number.isNotBlank()) {
            "Sir, aapka phone aaya hai."
        } else {
            "Sir, aapka phone aaya hai."
        }

        // Prompt requirement: "Speak: 'Sir, aapka phone aaya hai.'"
        ttsManager.speak(callAnnouncementHindi, isHindi = true)
    }

    fun simulateIncomingCall(callerName: String = "Tony Stark", number: String = "+1 212-970-4133") {
        CallAnnouncementManager.simulateIncomingCall(callerName, number)
    }

    fun dismissCallAlert() {
        CallAnnouncementManager.dismissCallAlert()
        if (_uiState.value.status == JarvisStatus.ALERT) {
            _uiState.value = _uiState.value.copy(status = JarvisStatus.ONLINE)
        }
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.isMuted
        ttsManager.setMuted(newMuted)
        _uiState.value = _uiState.value.copy(isMuted = newMuted)
    }

    fun toggleLanguage() {
        val nextLang = if (_uiState.value.selectedLanguage == VoiceLanguage.ENGLISH) {
            VoiceLanguage.HINDI
        } else {
            VoiceLanguage.ENGLISH
        }
        ttsManager.setLanguage(nextLang)
        _uiState.value = _uiState.value.copy(selectedLanguage = nextLang)

        val confirmText = if (nextLang == VoiceLanguage.HINDI) {
            "Hindi bhasha select ho gayi hai, Sir."
        } else {
            "Voice protocol switched to English, Sir."
        }
        ttsManager.speak(confirmText, isHindi = nextLang == VoiceLanguage.HINDI)
    }

    fun toggleFlashlight() {
        telemetryManager.toggleFlashlight()
    }

    fun requestConfirmSensitiveAction(title: String, message: String, action: () -> Unit) {
        _uiState.value = _uiState.value.copy(
            confirmationDialogTitle = title,
            confirmationDialogMessage = message,
            pendingConfirmationAction = action
        )
    }

    fun confirmPendingAction() {
        _uiState.value.pendingConfirmationAction?.invoke()
        dismissConfirmationDialog()
    }

    fun dismissConfirmationDialog() {
        _uiState.value = _uiState.value.copy(
            confirmationDialogTitle = null,
            confirmationDialogMessage = null,
            pendingConfirmationAction = null
        )
    }

    fun setPermissionDialogVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(isPermissionDialogVisible = visible)
    }

    fun refreshAppList() {
        loadInstalledApps()
    }

    fun openSystemSetting(settingAction: String) {
        telemetryManager.openSystemSetting(settingAction)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
        speechRecognizer.destroy()
        telemetryManager.destroy()
        CallAnnouncementManager.unregisterListener()
    }
}
