package com.example.model

enum class JarvisStatus {
    INITIALIZING,
    ONLINE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ALERT
}

enum class VoiceLanguage(val code: String, val displayName: String) {
    ENGLISH("en-IN", "ENGLISH"),
    HINDI("hi-IN", "हिंदी (HINDI)")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionSummary: String? = null
)

enum class MessageSender {
    USER,
    JARVIS
}

data class SystemTelemetry(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val batteryHealth: String = "Good",
    val networkType: String = "Wi-Fi Connected",
    val isNetworkAvailable: Boolean = true,
    val totalRamMb: Long = 8192,
    val availableRamMb: Long = 4096,
    val isFlashlightOn: Boolean = false,
    val currentTime: String = "12:00:00",
    val currentDate: String = "OCT 27, 2026",
    val cpuStatus: String = "STABLE"
)

data class AppLauncherItem(
    val id: String,
    val name: String,
    val packageName: String,
    val category: String,
    val hindiCommand: String,
    val englishCommand: String,
    val fallbackWebUrl: String? = null,
    val isInstalled: Boolean = false
)

data class CallStateInfo(
    val isRinging: Boolean = false,
    val incomingNumber: String = "",
    val callerName: String? = null,
    val lastAnnouncedTime: Long = 0L
)

sealed class CommandAction {
    data class LaunchApp(val packageName: String, val appName: String, val fallbackUrl: String? = null) : CommandAction()
    data class OpenSetting(val actionName: String, val settingIntent: String) : CommandAction()
    data class ToggleTorch(val enable: Boolean) : CommandAction()
    data object CheckBattery : CommandAction()
    data object AnnounceTime : CommandAction()
    data object OpenDialer : CommandAction()
    data object OpenCamera : CommandAction()
    data object None : CommandAction()
}
