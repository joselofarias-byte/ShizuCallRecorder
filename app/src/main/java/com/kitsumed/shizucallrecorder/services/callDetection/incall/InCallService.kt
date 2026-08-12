/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.services.callDetection.incall

import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import com.kitsumed.shizucallrecorder.data.AppPreferences
import com.kitsumed.shizucallrecorder.data.call.CallDirection
import com.kitsumed.shizucallrecorder.data.call.RawCallData
import com.kitsumed.shizucallrecorder.services.RecordingDecisionEngine
import com.kitsumed.shizucallrecorder.utils.AppLogger
import com.kitsumed.shizucallrecorder.utils.PhoneNumberManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.S)
class InCallService : InCallService() {

    private lateinit var appPreferences: AppPreferences
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var activeTrackedCall: Call? = null
    private var isPipelineExecuted = false

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            handleCallStateChanged(call, state)
        }
    }

    override fun onCreate() {
        super.onCreate()
        appPreferences = AppPreferences(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { serviceScope.cancel() }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        AppLogger.v("Received onCallAdded callback for call: ${call.details}")

        if (activeTrackedCall != null) {
            AppLogger.d("Parallel call detected. Discarding new call, dual-call scenario is not currently supported with InCallService implementation.")
            return
        }

        // Do not dereference PhoneAccountHandle here. Some dual-SIM OEM implementations expose
        // the call before an account has been selected and provide a null accountHandle.
        activeTrackedCall = call
        call.registerCallback(callCallback)
        AppLogger.i("Primary call session detected and tracking initialized. Current state is: ${callStateToString(call.details.state)} (${call.details.state})")

        if (call.details.state == Call.STATE_ACTIVE) {
            AppLogger.d("Received call in already ACTIVE state. Triggering handleCallStateChanged directly.")
            handleCallStateChanged(call, Call.STATE_ACTIVE)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        AppLogger.v("Received onCallRemoved callback for call: ${call.details}")

        if (call == activeTrackedCall) {
            AppLogger.i("Primary call session disconnected. Releasing callbacks. Ending recording.")
            releasePrimaryTrackedCall()
        } else {
            AppLogger.d("Received onCallRemoved for non-primary call. Ignoring.")
        }
    }

    private fun handleCallStateChanged(call: Call, state: Int) {
        AppLogger.v("Received onStateChanged callback for call: ${call.details}, current state: $state")
        if (call != activeTrackedCall) return
        AppLogger.d("Primary call state changed to ${callStateToString(call.details.state)} (${call.details.state})")

        if (state == Call.STATE_SELECT_PHONE_ACCOUNT) {
            AppLogger.d("Call is waiting for phone-account selection; accountHandle may be null. Deferring processing.")
            return
        }

        // Defensive null handling in addition to the known SELECT_PHONE_ACCOUNT case. OEMs may
        // expose an incomplete Details object in other transitional states as well.
        val accountHandle = call.details.accountHandle
        if (accountHandle == null) {
            AppLogger.d("Call accountHandle is null in state ${callStateToString(state)}; deferring processing.")
            return
        }

        val telecomManager = getSystemService(TELECOM_SERVICE) as? TelecomManager
        val packageName = accountHandle.componentName.packageName
        val isCallFromSystemDialer = packageName == telecomManager?.systemDialerPackage ||
            packageName == telecomManager?.defaultDialerPackage ||
            packageName == "com.android.phone"

        if (!isCallFromSystemDialer && !appPreferences.isRecordThirdPartyCallsEnabled()) {
            AppLogger.i("Tracked call resolved to package $packageName (not system/default dialer). Releasing the primary lock because third-party recording is disabled.")
            releasePrimaryTrackedCall()
            return
        }

        if (state != Call.STATE_ACTIVE || isPipelineExecuted) return
        isPipelineExecuted = true

        val details = call.details
        val rawNumber = details.handle?.schemeSpecificPart ?: ""
        val direction = when (details.callDirection) {
            Call.Details.DIRECTION_INCOMING -> CallDirection.INCOMING
            Call.Details.DIRECTION_OUTGOING -> CallDirection.OUTGOING
            else -> CallDirection.OUTGOING
        }

        val osCallerName = details.contactDisplayName ?: details.callerDisplayName
        val rawCallData = RawCallData(
            rawPhoneNumber = PhoneNumberManager.normalisePhoneNumber(rawNumber),
            direction = direction,
            osProvidedCallerName = osCallerName,
            packageName = packageName
        )

        AppLogger.i("Primary call became ACTIVE. Triggering Decision Engine Pipeline.")
        val isSelfManaged = details.hasProperty(Call.Details.PROPERTY_SELF_MANAGED)
        val isVoip = details.hasProperty(Call.Details.PROPERTY_VOIP_AUDIO_MODE)
        val isWifiCall = details.hasProperty(Call.Details.PROPERTY_WIFI)
        AppLogger.d("Primary call details - isSelfManaged: $isSelfManaged, isVoip: $isVoip, isWifiCall: $isWifiCall")

        serviceScope.launch {
            val sent = RecordingDecisionEngine.getInstance(this@InCallService).executeDecisionPipeline(rawCallData)
            if (!sent) {
                AppLogger.e("Failed to start recording foreground service, intent dispatch failed. Resetting execution flag.")
                isPipelineExecuted = false
            }
        }
    }

    private fun releasePrimaryTrackedCall() {
        val trackedCall = activeTrackedCall ?: return
        runCatching { trackedCall.unregisterCallback(callCallback) }
        if (isPipelineExecuted) {
            RecordingDecisionEngine.getInstance(this).endRecordingSession()
            isPipelineExecuted = false
        }
        activeTrackedCall = null
    }

    private fun callStateToString(state: Int): String = when (state) {
        Call.STATE_NEW -> "NEW"
        Call.STATE_DIALING -> "DIALING"
        Call.STATE_RINGING -> "RINGING"
        Call.STATE_HOLDING -> "HOLDING"
        Call.STATE_ACTIVE -> "ACTIVE"
        Call.STATE_DISCONNECTED -> "DISCONNECTED"
        Call.STATE_SELECT_PHONE_ACCOUNT -> "SELECT_PHONE_ACCOUNT"
        else -> "UNKNOWN_STATE($state)"
    }
}
