package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppLauncherMatrix
import com.example.ui.components.CallAlertBanner
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.HudHeader
import com.example.ui.components.JarvisArcReactor
import com.example.ui.components.PermissionDialog
import com.example.ui.components.QuickCommandChips
import com.example.ui.components.SoundWaveVisualizer
import com.example.ui.components.SystemControlsPanel
import com.example.ui.components.TerminalCard
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisBackgroundSecondary
import com.example.viewmodel.JarvisViewModel

@Composable
fun JarvisDashboardScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Multiple permissions launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        viewModel.refreshAppList()
    }

    val requestPermissions = {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = JarvisBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            JarvisBackground,
                            JarvisBackgroundSecondary,
                            JarvisBackground
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top HUD Status Header
                HudHeader(
                    status = uiState.status,
                    telemetry = uiState.telemetry,
                    selectedLanguage = uiState.selectedLanguage,
                    isMuted = uiState.isMuted,
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onToggleMute = { viewModel.toggleMute() }
                )

                // Scrollable Central HUD Dashboard
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(6.dp))

                    // Central Glowing Arc Reactor
                    JarvisArcReactor(
                        status = uiState.status,
                        audioWave = uiState.liveAudioWave,
                        isListening = uiState.isListening,
                        isSpeaking = uiState.isSpeaking,
                        onReactorClick = {
                            requestPermissions()
                            viewModel.toggleVoiceListening()
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sound Wave Visualizer
                    SoundWaveVisualizer(
                        amplitude = uiState.liveAudioWave,
                        isActive = uiState.isListening || uiState.isSpeaking
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Command Chips (e.g. YouTube kholo, Battery, Free Fire)
                    QuickCommandChips(
                        onCommandClick = { command ->
                            if (command == "SIMULATE_CALL") {
                                viewModel.simulateIncomingCall("Tony Stark", "+1 212-970-4133")
                            } else {
                                viewModel.submitTextCommand(command)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Incoming Call Alert & Announcer Protocol
                    CallAlertBanner(
                        callState = uiState.callState,
                        onDismissAlert = { viewModel.dismissCallAlert() },
                        onSimulateCall = { name, number ->
                            viewModel.simulateIncomingCall(name, number)
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Neural Console Terminal (Transcript & Response & Command input)
                    TerminalCard(
                        latestTranscript = uiState.latestTranscript,
                        latestResponse = uiState.latestResponse,
                        onExecuteTextCommand = { cmd -> viewModel.submitTextCommand(cmd) }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mobile App Control Matrix
                    AppLauncherMatrix(
                        apps = uiState.installedApps,
                        onLaunchApp = { pkg, name, url ->
                            viewModel.launchTargetApp(pkg, name, url)
                        },
                        onLaunchCamera = {
                            viewModel.submitTextCommand("Jarvis, camera kholo")
                        },
                        onLaunchDialer = {
                            viewModel.submitTextCommand("Jarvis, phone dialer kholo")
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // System Hardware & Settings Controls
                    SystemControlsPanel(
                        telemetry = uiState.telemetry,
                        onToggleFlashlight = { viewModel.toggleFlashlight() },
                        onOpenSetting = { settingAction -> viewModel.openSystemSetting(settingAction) },
                        onOpenPermissionDialog = { viewModel.setPermissionDialogVisible(true) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Security Clearance Dialog
            if (uiState.isPermissionDialogVisible) {
                PermissionDialog(
                    onRequestAllPermissions = { requestPermissions() },
                    onDismiss = { viewModel.setPermissionDialogVisible(false) }
                )
            }

            // Sensitive Action Confirmation Dialog
            if (uiState.confirmationDialogTitle != null && uiState.confirmationDialogMessage != null) {
                ConfirmationDialog(
                    title = uiState.confirmationDialogTitle ?: "Confirmation Required",
                    message = uiState.confirmationDialogMessage ?: "",
                    onConfirm = { viewModel.confirmPendingAction() },
                    onDismiss = { viewModel.dismissConfirmationDialog() }
                )
            }
        }
    }
}
