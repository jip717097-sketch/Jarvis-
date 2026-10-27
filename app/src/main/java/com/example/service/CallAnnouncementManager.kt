package com.example.service

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import com.example.model.CallStateInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CallAnnouncementManager {

    private val _callState = MutableStateFlow(CallStateInfo())
    val callState: StateFlow<CallStateInfo> = _callState.asStateFlow()

    private var onIncomingCallListener: ((callerName: String?, number: String) -> Unit)? = null

    fun registerListener(listener: (callerName: String?, number: String) -> Unit) {
        onIncomingCallListener = listener
    }

    fun unregisterListener() {
        onIncomingCallListener = null
    }

    fun onIncomingCallDetected(context: Context, incomingNumber: String) {
        val callerName = resolveContactName(context, incomingNumber)
        _callState.value = CallStateInfo(
            isRinging = true,
            incomingNumber = incomingNumber,
            callerName = callerName,
            lastAnnouncedTime = System.currentTimeMillis()
        )
        onIncomingCallListener?.invoke(callerName, incomingNumber)
    }

    fun onCallEnded() {
        _callState.value = _callState.value.copy(isRinging = false)
    }

    fun simulateIncomingCall(callerName: String = "Tony Stark", number: String = "+1 212-970-4133") {
        _callState.value = CallStateInfo(
            isRinging = true,
            incomingNumber = number,
            callerName = callerName,
            lastAnnouncedTime = System.currentTimeMillis()
        )
        onIncomingCallListener?.invoke(callerName, number)
    }

    fun dismissCallAlert() {
        _callState.value = _callState.value.copy(isRinging = false)
    }

    private fun resolveContactName(context: Context, number: String): String? {
        if (number.isBlank()) return null
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    if (nameIndex >= 0) it.getString(nameIndex) else null
                } else null
            }
        } catch (e: SecurityException) {
            Log.w("CallAnnounce", "No contact permission to query name: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e("CallAnnounce", "Error querying contact name", e)
            null
        }
    }
}
