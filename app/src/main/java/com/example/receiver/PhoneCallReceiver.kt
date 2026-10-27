package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.example.service.CallAnnouncementManager

class PhoneCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: ""

            Log.d("PhoneCallReceiver", "Phone state changed: $stateStr, number: $incomingNumber")

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    CallAnnouncementManager.onIncomingCallDetected(context, incomingNumber)
                }
                TelephonyManager.EXTRA_STATE_IDLE, TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    CallAnnouncementManager.onCallEnded()
                }
            }
        }
    }
}
