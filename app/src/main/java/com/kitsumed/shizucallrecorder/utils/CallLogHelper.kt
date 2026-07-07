/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.utils

import android.content.Context
import android.provider.CallLog
import com.kitsumed.shizucallrecorder.data.call.CallDirection
import kotlinx.coroutines.delay

object CallLogHelper {
    private const val TAG = "SCR:CallLogHelper"

    /**
     * Tries to query the call log for the most recent call matching the given direction, and returns the associated phone number.
     * @return The phone number from the most recent call log entry matching the direction, or null if no valid entry is found after multiple attempts.
     */
    suspend fun tryGetFinalNumberFromLog(
        context: Context,
        direction: CallDirection?
    ): String? {
        val typeSelection = when (direction) {
            // We do not want to include missed or rejected calls here since they are useless to us, and in a Dual-call scenario could lead to picking the wrong number.
            CallDirection.INCOMING -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.INCOMING_TYPE}"
            CallDirection.OUTGOING -> "${CallLog.Calls.TYPE} = ${CallLog.Calls.OUTGOING_TYPE}"
            else -> null
        }
        // Try multiples times with a delay in case the OS didn't write the call log entry yet (only written after the call ended).
        for (i in 1..4) {
            try {
                val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(CallLog.Calls.NUMBER),
                    typeSelection, null,
                    "${CallLog.Calls.DATE} DESC"
                )
                cursor?.use {
                    if (it.moveToFirst()) {
                        return it.getString(0)
                    }
                }
            } catch (e: Exception) {
                AppLogger.w(TAG, "Failed to query call log for fallback number", e)
            }
            if (i < 4) delay(400)
        }
        return null
    }
}
