package com.example.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import com.example.model.AppLauncherItem

class JarvisAppLauncher(private val context: Context) {

    val targetApps = listOf(
        AppLauncherItem(
            id = "youtube",
            name = "YouTube",
            packageName = "com.google.android.youtube",
            category = "MEDIA",
            hindiCommand = "Jarvis, YouTube kholo",
            englishCommand = "Jarvis, open YouTube",
            fallbackWebUrl = "https://www.youtube.com"
        ),
        AppLauncherItem(
            id = "instagram",
            name = "Instagram",
            packageName = "com.instagram.android",
            category = "SOCIAL",
            hindiCommand = "Jarvis, Instagram kholo",
            englishCommand = "Jarvis, open Instagram",
            fallbackWebUrl = "https://www.instagram.com"
        ),
        AppLauncherItem(
            id = "whatsapp",
            name = "WhatsApp",
            packageName = "com.whatsapp",
            category = "COMMUNICATION",
            hindiCommand = "Jarvis, WhatsApp kholo",
            englishCommand = "Jarvis, open WhatsApp",
            fallbackWebUrl = "https://web.whatsapp.com"
        ),
        AppLauncherItem(
            id = "facebook",
            name = "Facebook",
            packageName = "com.facebook.katana",
            category = "SOCIAL",
            hindiCommand = "Jarvis, Facebook kholo",
            englishCommand = "Jarvis, open Facebook",
            fallbackWebUrl = "https://www.facebook.com"
        ),
        AppLauncherItem(
            id = "freefire",
            name = "Free Fire MAX",
            packageName = "com.dts.freefiremax",
            category = "GAMING",
            hindiCommand = "Jarvis, Free Fire MAX kholo",
            englishCommand = "Jarvis, open Free Fire MAX",
            fallbackWebUrl = "https://play.google.com/store/apps/details?id=com.dts.freefiremax"
        ),
        AppLauncherItem(
            id = "browser",
            name = "Web Browser",
            packageName = "com.android.chrome",
            category = "UTILITY",
            hindiCommand = "Jarvis, browser kholo",
            englishCommand = "Jarvis, open browser",
            fallbackWebUrl = "https://www.google.com"
        )
    )

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            // Also check launch intent
            context.packageManager.getLaunchIntentForPackage(packageName) != null
        } catch (e: Exception) {
            false
        }
    }

    fun getAppsWithStatus(): List<AppLauncherItem> {
        return targetApps.map { app ->
            val installed = isAppInstalled(app.packageName) ||
                    (app.id == "browser" && canOpenBrowser())
            app.copy(isInstalled = installed)
        }
    }

    private fun canOpenBrowser(): Boolean {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
        return browserIntent.resolveActivity(context.packageManager) != null
    }

    fun launchAppByPackage(packageName: String, fallbackUrl: String? = null): AppLaunchResult {
        // Special case for browser
        if (packageName == "com.android.chrome" || packageName == "browser") {
            return launchBrowser(fallbackUrl ?: "https://www.google.com")
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            AppLaunchResult.Success("Launched application successfully.")
        } else {
            // Check fallback web URL
            if (!fallbackUrl.isNullOrBlank()) {
                AppLaunchResult.NotInstalled(
                    message = "Application is not installed on this device.",
                    fallbackUrl = fallbackUrl
                )
            } else {
                AppLaunchResult.NotInstalled(
                    message = "Application is not installed on this device.",
                    fallbackUrl = "https://play.google.com/store/apps/details?id=$packageName"
                )
            }
        }
    }

    fun launchBrowser(url: String = "https://www.google.com"): AppLaunchResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            AppLaunchResult.Success("Opening browser.")
        } catch (e: Exception) {
            Log.e("JarvisLauncher", "Failed to launch browser", e)
            AppLaunchResult.Error("Could not launch web browser: ${e.message}")
        }
    }

    fun launchCamera(): AppLaunchResult {
        return try {
            val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            AppLaunchResult.Success("Optical sensor initialized. Camera opened.")
        } catch (e: Exception) {
            AppLaunchResult.Error("Could not access camera: ${e.message}")
        }
    }

    fun launchDialer(): AppLaunchResult {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            AppLaunchResult.Success("Comms link active. Dialer opened.")
        } catch (e: Exception) {
            AppLaunchResult.Error("Could not launch phone dialer: ${e.message}")
        }
    }

    fun openPlayStore(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    sealed class AppLaunchResult {
        data class Success(val message: String) : AppLaunchResult()
        data class NotInstalled(val message: String, val fallbackUrl: String) : AppLaunchResult()
        data class Error(val message: String) : AppLaunchResult()
    }
}
