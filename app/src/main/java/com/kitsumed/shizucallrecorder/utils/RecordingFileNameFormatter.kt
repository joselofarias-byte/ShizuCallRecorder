/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.utils

import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import androidx.annotation.StringRes
import com.kitsumed.shizucallrecorder.R
import com.kitsumed.shizucallrecorder.data.AppPreferences
import com.kitsumed.shizucallrecorder.data.call.CallDirection
import com.kitsumed.shizucallrecorder.data.call.EnrichedCallData
import com.kitsumed.shizucallrecorder.integrations.scrcpy.ScrcpyAudioCodec
import com.kitsumed.shizucallrecorder.services.callDetection.CallDetectionMode
import java.util.Date
import java.util.Locale

object RecordingFileNameFormatter {
    const val TAG = "SCR:RecordingFileNameFormatter"

    /**
     * Internal ownership prefix prepended to every new recording file name.
     *
     * This allows ShizuCallRecorder to distinguish its own recordings from files created by
     * other apps (e.g. EverCallRecorder, Cally) when they share the same SAF folder chosen
     * by the user. The prefix is never shown to the user: see [stripOwnershipPrefix].
     *
     * Picked to be distinctive and filesystem-safe. Must not contain characters that would
     * collide with the date separator "_" used inside the file name.
     */
    const val OWNED_FILE_PREFIX = "shizucall_"

    /**
     * Removes the [OWNED_FILE_PREFIX] from the beginning of a file name, if present.
     *
     * Used to hide the ownership marker from the user-facing display name and from the
     * internal parser, so the historical `{date}_{direction}_{phone_number}` layout is preserved.
     *
     * Safe to call on names without the prefix (returns them unchanged).
     */
    fun stripOwnershipPrefix(name: String): String =
        if (name.startsWith(OWNED_FILE_PREFIX)) name.substring(OWNED_FILE_PREFIX.length) else name

    /**
     * Returns true if the given file name (with or without extension) was created by this app
     * using the new naming scheme (i.e. it carries the [OWNED_FILE_PREFIX]).
     */
    fun isOwnedRecording(name: String): Boolean = name.startsWith(OWNED_FILE_PREFIX)

    /**
     * Returns true if the given file name matches the historical ShizuCallRecorder naming
     * scheme used before the ownership prefix was introduced.
     *
     * The historical scheme always starts with a timestamp of the form `yyyyMMdd_HHmmss` followed
     * (after optional timezone offset) by the call direction (`_in_` or `_out_`). This is distinctive
     * enough to exclude files from other apps (EverCallRecorder, Cally, etc.) that share the folder.
     *
     * @param name The raw file name including extension (e.g. "20260627_180212.012-0300_out_+598611.ogg").
     */
    fun isLegacyRecording(name: String): Boolean = LEGACY_NAME_REGEX.containsMatchIn(name)

    // Pattern: yyyyMMdd_HHmmss[.SSS+Z]  followed by  _in_ or _out_
    // The direction marker is the unique fingerprint of ShizuCallRecorder's default naming.
    private val LEGACY_NAME_REGEX = Regex("""^\d{8}_\d{2}[\d.\+\-:Z]*_(?:in|out)_""")

    /**
     * Represents the supported placeholders that can be used in the file name template.
     * Binds the literal tag used in formatting to a localized description for the UI.
     * @param tag The literal placeholder string that will be replaced in the template (e.g., "{date}").
     * @param descriptionResId The string resource ID for the description of this placeholder
     * @param supportedModes The set of CallDetectionModes in which this placeholder can be used/may be expected to work.
     */
    enum class FileNamePlaceholder(val tag: String, @param:StringRes val descriptionResId: Int, val supportedModes: Set<CallDetectionMode>)  {
        DATE("{date}", R.string.placeholder_date_desc, setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        DATE_YEAR("{date:year}", R.string.placeholder_date_year_desc, setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        DATE_MONTH("{date:month}", R.string.placeholder_date_month_desc, setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        DATE_DAY("{date:day}", R.string.placeholder_date_day_desc, setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        DATE_HOURS("{date:hours}", R.string.placeholder_date_hours_desc, setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        DATE_MINUTES("{date:minutes}", R.string.placeholder_date_minutes_desc, setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        DATE_SECONDS("{date:seconds}", R.string.placeholder_date_seconds_desc, setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        DIRECTION("{direction}", R.string.placeholder_direction_desc, setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        PHONE_NUMBER("{phone_number}", R.string.placeholder_phone_number_desc, setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        CALLER_NAME("{caller_name}", R.string.placeholder_caller_name_desc, setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        CROSS_COUNTRY("{cross_country}", R.string.placeholder_cross_country_desc,setOf(CallDetectionMode.PhoneState, CallDetectionMode.InCallService)),
        PACKAGE_NAME("{package_name}", R.string.placeholder_package_name_desc, setOf(CallDetectionMode.InCallService))
    }

    /**
     * Formats a filename based on the user defined string template and the recording metadata and audio codec.
     * Supported placeholders: [FileNamePlaceholder]
     *
     * @param context The context needed to resolve contacts and read preferences.
     * @param metadata Defines the main properties (direction, phone number, cross country).
     * @param codec The selected ScrcpyAudioCodec used to determine the file extension.
     * @param customFormat An optional custom format string to use instead of the one from preferences. Useful for testing or one-off formatting without changing user settings.
     * @return A filesystem-safe filename string.
     */
    fun formatFileName(
        context: Context,
        metadata: EnrichedCallData,
        codec: ScrcpyAudioCodec,
        customFormat: String? = null
    ): String {
        val template = customFormat ?: AppPreferences(context).getFileNameTemplate()

        // Capture a single instant so that {date} and the granular {date:...} sub-fields all describe the same moment.
        val now = Date()
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss.SSSZ", Locale.CANADA).format(now)
        val dateYearStr = SimpleDateFormat("yyyy", Locale.CANADA).format(now)
        val dateMonthStr = SimpleDateFormat("MM", Locale.CANADA).format(now)
        val dateDayStr = SimpleDateFormat("dd", Locale.CANADA).format(now)
        val dateHoursStr = SimpleDateFormat("HH", Locale.CANADA).format(now)
        val dateMinutesStr = SimpleDateFormat("mm", Locale.CANADA).format(now)
        val dateSecondsStr = SimpleDateFormat("ss", Locale.CANADA).format(now)

        val directionStr = when (metadata.direction) {
            CallDirection.INCOMING -> "in"
            CallDirection.OUTGOING -> "out"
        }

        val phoneStr = metadata.getBestNumber()
        var callerNameStr = ""

        if (template.contains(FileNamePlaceholder.CALLER_NAME.tag) && phoneStr.isNotEmpty()) {
            callerNameStr = metadata.callerName ?: ""
        }

        val crossCountryStr = metadata.isCrossCountry.toString()

        val packageName = if (metadata.packageName.isNullOrBlank())
        {
            "" // If package name is not available, return empty string.
        } else
        {
            getAppName(context, metadata.packageName)
        }

        val baseName = template
            .replace(FileNamePlaceholder.DATE_YEAR.tag, dateYearStr)
            .replace(FileNamePlaceholder.DATE_MONTH.tag, dateMonthStr)
            .replace(FileNamePlaceholder.DATE_DAY.tag, dateDayStr)
            .replace(FileNamePlaceholder.DATE_HOURS.tag, dateHoursStr)
            .replace(FileNamePlaceholder.DATE_MINUTES.tag, dateMinutesStr)
            .replace(FileNamePlaceholder.DATE_SECONDS.tag, dateSecondsStr)
            .replace(FileNamePlaceholder.DATE.tag, dateStr)
            .replace(FileNamePlaceholder.DIRECTION.tag, directionStr)
            .replace(FileNamePlaceholder.PHONE_NUMBER.tag, phoneStr)
            .replace(FileNamePlaceholder.CALLER_NAME.tag, callerNameStr)
            .replace(FileNamePlaceholder.CROSS_COUNTRY.tag, crossCountryStr)
            .replace(FileNamePlaceholder.PACKAGE_NAME.tag, packageName)

        AppLogger.v(TAG, "Formatted base filename: '$baseName' with template '$template'")
        // Prepend the internal ownership prefix so this recording can be told apart from files
        // created by other apps sharing the same SAF folder. The prefix is stripped before display.
        return "$OWNED_FILE_PREFIX$baseName${codec.containerExtension}"
    }

    /**
     * Attempts to resolve the user-friendly app name from a package name. If resolution fails, it falls back to returning the package name itself.
     */
    private fun getAppName(context: Context, packageName: String): String {
        val pm = context.packageManager
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            AppLogger.w(TAG, "Could not resolve app name for package '$packageName', got NameNotFoundException (privacy restriction?). Returning package name as fallback.")
            // Fallback: return the package name itself
            packageName
        }
    }
}