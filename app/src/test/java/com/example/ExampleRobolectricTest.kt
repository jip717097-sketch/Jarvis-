package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CommandAction
import com.example.model.SystemTelemetry
import com.example.service.CommandProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JARVIS AI", appName)
  }

  @Test
  fun `test command processor parses Hindi YouTube command`() {
    val processor = CommandProcessor()
    val telemetry = SystemTelemetry()
    val result = processor.processCommand("Jarvis, YouTube kholo", telemetry)

    assertTrue(result.isHindi)
    assertTrue(result.action is CommandAction.LaunchApp)
    val launchAction = result.action as CommandAction.LaunchApp
    assertEquals("com.google.android.youtube", launchAction.packageName)
  }

  @Test
  fun `test command processor parses battery inquiry`() {
    val processor = CommandProcessor()
    val telemetry = SystemTelemetry(batteryPercent = 92, isCharging = true)
    val result = processor.processCommand("Jarvis, battery kitni hai?", telemetry)

    assertTrue(result.action is CommandAction.CheckBattery)
    assertTrue(result.spokenResponse.contains("92 percent"))
  }
}
