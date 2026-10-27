package com.example.service

import com.example.model.CommandAction
import com.example.model.SystemTelemetry
import java.util.Calendar

data class ProcessedCommandResult(
    val spokenResponse: String,
    val displayText: String,
    val isHindi: Boolean,
    val action: CommandAction,
    val actionSummary: String? = null
)

class CommandProcessor {

    fun processCommand(rawInput: String, telemetry: SystemTelemetry): ProcessedCommandResult {
        val cleanInput = rawInput.trim().lowercase()

        // Detect Hindi vs English tone
        val isHindi = isHindiQuery(cleanInput)

        // 1. YouTube
        if (matches(cleanInput, "youtube", "yt") && matchesAny(cleanInput, "kholo", "open", "chalao", "start", "lagao", "play")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, YouTube khol raha hoon." else "Opening YouTube now, Sir.",
                displayText = if (isHindi) "COMMAND ACKNOWLEDGED: Opening YouTube." else "COMMAND ACKNOWLEDGED: Launching YouTube.",
                isHindi = isHindi,
                action = CommandAction.LaunchApp("com.google.android.youtube", "YouTube", "https://www.youtube.com"),
                actionSummary = "LAUNCH: YouTube"
            )
        }

        // 2. Instagram
        if (matches(cleanInput, "instagram", "insta") && matchesAny(cleanInput, "kholo", "open", "chalao", "start", "dekho")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, Instagram open kar raha hoon." else "Accessing Instagram feeds, Sir.",
                displayText = if (isHindi) "COMMAND ACKNOWLEDGED: Opening Instagram." else "COMMAND ACKNOWLEDGED: Launching Instagram.",
                isHindi = isHindi,
                action = CommandAction.LaunchApp("com.instagram.android", "Instagram", "https://www.instagram.com"),
                actionSummary = "LAUNCH: Instagram"
            )
        }

        // 3. WhatsApp
        if (matches(cleanInput, "whatsapp", "whats app") && matchesAny(cleanInput, "kholo", "open", "chalao", "start", "message")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, WhatsApp khol raha hoon." else "Opening WhatsApp messaging hub, Sir.",
                displayText = if (isHindi) "COMMAND ACKNOWLEDGED: Opening WhatsApp." else "COMMAND ACKNOWLEDGED: Launching WhatsApp.",
                isHindi = isHindi,
                action = CommandAction.LaunchApp("com.whatsapp", "WhatsApp", "https://web.whatsapp.com"),
                actionSummary = "LAUNCH: WhatsApp"
            )
        }

        // 4. Facebook
        if (matches(cleanInput, "facebook", "fb") && matchesAny(cleanInput, "kholo", "open", "chalao", "start")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, Facebook open kar raha hoon." else "Accessing Facebook network, Sir.",
                displayText = if (isHindi) "COMMAND ACKNOWLEDGED: Opening Facebook." else "COMMAND ACKNOWLEDGED: Launching Facebook.",
                isHindi = isHindi,
                action = CommandAction.LaunchApp("com.facebook.katana", "Facebook", "https://www.facebook.com"),
                actionSummary = "LAUNCH: Facebook"
            )
        }

        // 5. Free Fire MAX
        if ((matches(cleanInput, "free fire", "freefire") || matches(cleanInput, "max")) && matchesAny(cleanInput, "kholo", "open", "chalao", "khelna", "play", "start")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, Free Fire MAX start kar raha hoon. Best of luck in the battleground." else "Launching Free Fire MAX battle protocol, Sir.",
                displayText = if (isHindi) "COMMAND ACKNOWLEDGED: Launching Free Fire MAX." else "COMMAND ACKNOWLEDGED: Initializing Free Fire MAX.",
                isHindi = isHindi,
                action = CommandAction.LaunchApp("com.dts.freefiremax", "Free Fire MAX", "https://play.google.com/store/apps/details?id=com.dts.freefiremax"),
                actionSummary = "LAUNCH: Free Fire MAX"
            )
        }

        // 6. Browser
        if (matches(cleanInput, "browser", "chrome", "google", "internet") && matchesAny(cleanInput, "kholo", "open", "start", "search")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, web browser khol raha hoon." else "Initiating web browser protocol, Sir.",
                displayText = if (isHindi) "COMMAND ACKNOWLEDGED: Opening Web Browser." else "COMMAND ACKNOWLEDGED: Opening Chrome/Web Browser.",
                isHindi = isHindi,
                action = CommandAction.LaunchApp("com.android.chrome", "Browser", "https://www.google.com"),
                actionSummary = "LAUNCH: Web Browser"
            )
        }

        // 7. Battery Status ("battery kitni hai", "battery status", "charge kitna hai")
        if (matches(cleanInput, "battery", "charge", "charging", "battery kitni") ||
            (matches(cleanInput, "charge") && matches(cleanInput, "kitna"))
        ) {
            val percent = telemetry.batteryPercent
            val chargingTextHindi = if (telemetry.isCharging) "aur phone charging par hai." else "aur charging disconnected hai."
            val chargingTextEn = if (telemetry.isCharging) "and currently connected to power source." else "running on internal arc reactor cells."

            val spoken = if (isHindi) {
                "Sir, battery level $percent percent hai, $chargingTextHindi"
            } else {
                "Sir, power cells are currently at $percent percent, $chargingTextEn"
            }

            return ProcessedCommandResult(
                spokenResponse = spoken,
                displayText = "BATTERY TELEMETRY: $percent% (${if (telemetry.isCharging) "CHARGING" else "DISCHARGING"}) | Health: ${telemetry.batteryHealth}",
                isHindi = isHindi,
                action = CommandAction.CheckBattery,
                actionSummary = "TELEMETRY: Battery Check ($percent%)"
            )
        }

        // 8. Time / Date ("time batao", "what time is it", "kya time hua")
        if (matchesAny(cleanInput, "time", "date", "samay", "tarikh") &&
            matchesAny(cleanInput, "batao", "what", "tell", "kya", "hua", "kitna")
        ) {
            val spoken = if (isHindi) {
                "Sir, abhi samay hai ${telemetry.currentTime}, aur aaj ki date hai ${telemetry.currentDate}."
            } else {
                "Sir, current local time is ${telemetry.currentTime}. Date is ${telemetry.currentDate}."
            }
            return ProcessedCommandResult(
                spokenResponse = spoken,
                displayText = "TEMPORAL TELEMETRY: Time: ${telemetry.currentTime} | Date: ${telemetry.currentDate}",
                isHindi = isHindi,
                action = CommandAction.AnnounceTime,
                actionSummary = "TELEMETRY: Temporal Sync"
            )
        }

        // 9. Flashlight / Torch ON
        if (matchesAny(cleanInput, "flashlight", "torch", "light", "batti") &&
            matchesAny(cleanInput, "on", "chalu", "jalao", "enable", "start")
        ) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, flashlight on kar di hai." else "Sir, primary illuminator is engaged.",
                displayText = "SYSTEM ACTION: Flashlight / Torch toggled ON.",
                isHindi = isHindi,
                action = CommandAction.ToggleTorch(true),
                actionSummary = "HARDWARE: Torch ON"
            )
        }

        // 10. Flashlight / Torch OFF
        if (matchesAny(cleanInput, "flashlight", "torch", "light", "batti") &&
            matchesAny(cleanInput, "off", "band", "bujhao", "disable", "stop")
        ) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, flashlight band kar di hai." else "Sir, illuminator powered down.",
                displayText = "SYSTEM ACTION: Flashlight / Torch toggled OFF.",
                isHindi = isHindi,
                action = CommandAction.ToggleTorch(false),
                actionSummary = "HARDWARE: Torch OFF"
            )
        }

        // 11. Wi-Fi Settings
        if (matches(cleanInput, "wifi", "wi-fi") && matchesAny(cleanInput, "kholo", "open", "setting", "settings")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, Wi-Fi settings khol raha hoon." else "Opening Wi-Fi network configuration panel, Sir.",
                displayText = "SYSTEM PANEL: Wi-Fi Settings",
                isHindi = isHindi,
                action = CommandAction.OpenSetting("Wi-Fi Settings", android.provider.Settings.ACTION_WIFI_SETTINGS),
                actionSummary = "SETTINGS: Wi-Fi"
            )
        }

        // 12. Bluetooth Settings
        if (matches(cleanInput, "bluetooth") && matchesAny(cleanInput, "kholo", "open", "setting", "settings")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, Bluetooth settings open kar raha hoon." else "Accessing Bluetooth interface, Sir.",
                displayText = "SYSTEM PANEL: Bluetooth Settings",
                isHindi = isHindi,
                action = CommandAction.OpenSetting("Bluetooth Settings", android.provider.Settings.ACTION_BLUETOOTH_SETTINGS),
                actionSummary = "SETTINGS: Bluetooth"
            )
        }

        // 13. System Settings
        if (matchesAny(cleanInput, "setting", "settings") && matchesAny(cleanInput, "kholo", "open", "main")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, system settings open kar raha hoon." else "Deploying Android system configuration, Sir.",
                displayText = "SYSTEM PANEL: Android System Settings",
                isHindi = isHindi,
                action = CommandAction.OpenSetting("Settings", android.provider.Settings.ACTION_SETTINGS),
                actionSummary = "SETTINGS: Main"
            )
        }

        // 14. Camera
        if (matches(cleanInput, "camera", "photo") && matchesAny(cleanInput, "kholo", "open", "chalao", "capture", "khicho")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, camera open kar raha hoon." else "Optical sensors online. Opening camera, Sir.",
                displayText = "HARDWARE: Initializing Camera.",
                isHindi = isHindi,
                action = CommandAction.OpenCamera,
                actionSummary = "HARDWARE: Camera"
            )
        }

        // 15. Phone Dialer
        if (matchesAny(cleanInput, "phone", "dialer", "call") && matchesAny(cleanInput, "kholo", "open", "lagao", "dial")) {
            return ProcessedCommandResult(
                spokenResponse = if (isHindi) "Sir, phone dialer open kar raha hoon." else "Opening communication dialer, Sir.",
                displayText = "COMMUNICATION: Opening Phone Dialer.",
                isHindi = isHindi,
                action = CommandAction.OpenDialer,
                actionSummary = "COMMUNICATION: Dialer"
            )
        }

        // 16. Greetings & Conversational Questions
        if (matchesAny(cleanInput, "who are you", "tum kaun ho", "kaun ho", "identify")) {
            val spoken = if (isHindi) {
                "Main Jarvis hoon, Tony Stark ka banaya gaya artificial intelligence system. Aapki sewa mein hazir hoon, Sir."
            } else {
                "I am JARVIS, Just A Rather Very Intelligent System, engineered by Stark Industries. Standing by to assist you, Sir."
            }
            return ProcessedCommandResult(
                spokenResponse = spoken,
                displayText = "CORE IDENTITY: JARVIS (Just A Rather Very Intelligent System) - Stark Industries Protocol v4.2",
                isHindi = isHindi,
                action = CommandAction.None
            )
        }

        if (matchesAny(cleanInput, "joke", "chutkula", "hasao")) {
            val spoken = if (isHindi) {
                "Sir, ek baar computer ne doctor se bola: mujhe virus ho gaya hai. Doctor bola: thoda antivirus install karo aur rest lo!"
            } else {
                "Sir, why did the neural network cross the road? To optimize the loss function on the other side."
            }
            return ProcessedCommandResult(
                spokenResponse = spoken,
                displayText = "HUMOR MATRIX: $spoken",
                isHindi = isHindi,
                action = CommandAction.None
            )
        }

        if (matchesAny(cleanInput, "hello", "hi jarvis", "namaste", "hey jarvis", "sun rahe ho")) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val timeGreeting = when (hour) {
                in 4..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                else -> "Good evening"
            }
            val spoken = if (isHindi) {
                "Namaste Sir. Jarvis online hai aur aapke command ka intezaar kar raha hai."
            } else {
                "$timeGreeting, Sir. Jarvis is online and all systems are fully operational."
            }
            return ProcessedCommandResult(
                spokenResponse = spoken,
                displayText = "SYSTEM STATUS: Online | $spoken",
                isHindi = isHindi,
                action = CommandAction.None
            )
        }

        if (matchesAny(cleanInput, "kaise ho", "how are you")) {
            val spoken = if (isHindi) {
                "Main bilkul theek hoon Sir, arc reactor 100 percent par operate kar raha hai. Aap bataiye main aapki kya madad kar sakta hoon?"
            } else {
                "All diagnostic routines report 100 percent nominal efficiency, Sir. How may I be of assistance today?"
            }
            return ProcessedCommandResult(
                spokenResponse = spoken,
                displayText = "DIAGNOSTICS: Core 100% nominal. Synaptic response optimal.",
                isHindi = isHindi,
                action = CommandAction.None
            )
        }

        // Fallback intelligent response
        val fallbackSpoken = if (isHindi) {
            "Sir, maine aapka message suna: '$rawInput'. Main YouTube, WhatsApp, Free Fire, Battery status, ya Flashlight control karne ke liye taiyaar hoon."
        } else {
            "Command received, Sir: '$rawInput'. All Stark HUD systems are online and awaiting your directives."
        }

        return ProcessedCommandResult(
            spokenResponse = fallbackSpoken,
            displayText = "INPUT PROCESSED: \"$rawInput\" - Awaiting explicit command parameters.",
            isHindi = isHindi,
            action = CommandAction.None
        )
    }

    private fun isHindiQuery(input: String): Boolean {
        val hindiMarkers = listOf(
            "kholo", "batao", "kitni", "hai", "karo", "chalao", "band", "jalao",
            "namaste", "kaise", "samay", "tarikh", "kaun", "hasao", "chutkula", "phone aaya"
        )
        return hindiMarkers.any { input.contains(it) } || input.any { it in '\u0900'..'\u097F' }
    }

    private fun matches(input: String, vararg targets: String): Boolean {
        return targets.any { input.contains(it) }
    }

    private fun matchesAny(input: String, vararg targets: String): Boolean {
        return targets.any { input.contains(it) }
    }
}
