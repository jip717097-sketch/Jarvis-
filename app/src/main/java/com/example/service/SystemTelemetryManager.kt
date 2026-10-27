package com.example.service

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.example.model.SystemTelemetry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SystemTelemetryManager(private val context: Context) {

    private val _telemetry = MutableStateFlow(SystemTelemetry())
    val telemetry: StateFlow<SystemTelemetry> = _telemetry.asStateFlow()

    private var cameraManager: CameraManager? = null
    private var cameraIdWithFlash: String? = null
    private var isTorchEnabled = false

    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var batteryReceiver: BroadcastReceiver? = null

    init {
        initFlashlight()
        registerBatteryReceiver()
        startClockAndStatsTicker()
    }

    private fun initFlashlight() {
        try {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            cameraManager?.let { manager ->
                val ids = manager.cameraIdList
                for (id in ids) {
                    val characteristics = manager.getCameraCharacteristics(id)
                    val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                    val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                    if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        cameraIdWithFlash = id
                        break
                    }
                }
                if (cameraIdWithFlash == null && ids.isNotEmpty()) {
                    cameraIdWithFlash = ids[0]
                }
            }
        } catch (e: Exception) {
            Log.e("JarvisTelemetry", "Unable to init camera for flashlight: ${e.message}")
        }
    }

    fun toggleFlashlight(): Boolean {
        return setFlashlight(!isTorchEnabled)
    }

    fun setFlashlight(enable: Boolean): Boolean {
        return try {
            val id = cameraIdWithFlash
            if (id != null && cameraManager != null) {
                cameraManager?.setTorchMode(id, enable)
                isTorchEnabled = enable
                _telemetry.value = _telemetry.value.copy(isFlashlightOn = enable)
                true
            } else {
                false
            }
        } catch (e: CameraAccessException) {
            Log.e("JarvisTelemetry", "CameraAccessException when setting torch", e)
            false
        } catch (e: Exception) {
            Log.e("JarvisTelemetry", "Exception setting torch", e)
            false
        }
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let { updateBatteryFromIntent(it) }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val initialIntent = context.registerReceiver(batteryReceiver, filter)
        initialIntent?.let { updateBatteryFromIntent(it) }
    }

    private fun updateBatteryFromIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percent = if (level >= 0 && scale > 0) (level * 100) / scale else 85

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val healthCode = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
        val healthStr = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Optimal (100%)"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Critical Low"
            else -> "Nominal"
        }

        _telemetry.value = _telemetry.value.copy(
            batteryPercent = percent,
            isCharging = isCharging,
            batteryHealth = healthStr
        )
    }

    private fun startClockAndStatsTicker() {
        scope.launch {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

            while (isActive) {
                val now = Date()
                val timeStr = timeFormat.format(now)
                val dateStr = dateFormat.format(now).uppercase(Locale.getDefault())

                val (availRam, totalRam) = getMemoryInfo()
                val (netType, isConnected) = getNetworkStatus()

                _telemetry.value = _telemetry.value.copy(
                    currentTime = timeStr,
                    currentDate = dateStr,
                    availableRamMb = availRam,
                    totalRamMb = totalRam,
                    networkType = netType,
                    isNetworkAvailable = isConnected
                )

                delay(1000)
            }
        }
    }

    private fun getMemoryInfo(): Pair<Long, Long> {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val availMb = memInfo.availMem / (1024 * 1024)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        return Pair(availMb, totalMb)
    }

    private fun getNetworkStatus(): Pair<String, Boolean> {
        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connManager == null) return Pair("DISCONNECTED", false)

        val activeNetwork = connManager.activeNetwork ?: return Pair("OFFLINE", false)
        val capabilities = connManager.getNetworkCapabilities(activeNetwork) ?: return Pair("OFFLINE", false)

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> Pair("WI-FI ONLINE", true)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> Pair("CELLULAR 5G/LTE", true)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> Pair("ETHERNET ACTIVE", true)
            else -> Pair("ONLINE", true)
        }
    }

    fun openSystemSetting(settingAction: String): Boolean {
        return try {
            val intent = Intent(settingAction).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e("JarvisTelemetry", "Could not open setting $settingAction", e)
            false
        }
    }

    fun destroy() {
        batteryReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: Exception) {
                // ignore
            }
        }
        if (isTorchEnabled) {
            setFlashlight(false)
        }
    }
}
