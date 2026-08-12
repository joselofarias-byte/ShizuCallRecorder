/*
 * ShizuCallRecorder: FOSS Call recording powered through ADB/Shizuku!
 *  Copyright (C) 2026-present kitsumed (Med)
 *  This software is licensed under the GNU General Public License v3 or later, with additional terms as permitted under Section 7.
 *  The full license text is available in the LICENSE file at the root of this project.
 *  This software is distributed WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.kitsumed.shizucallrecorder.utils

import android.content.Context
import android.telephony.TelephonyManager
import com.google.i18n.phonenumbers.MetadataLoader
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * A singleton manager for handling phone number parsing and validation, also include an implementation of Google libphonenumber library for Android.
 * To comply with FAQ recommendations most methods are suspend functions to ensure they do not block the main UI thread.
 * https://github.com/google/libphonenumber/blob/master/FAQ.md#system-considerations
 */
class PhoneNumberManager private constructor(context: Context) {
    /** Store the application context to avoid passing it around and to prevent memory leaks. */
    private val appContext: Context = context.applicationContext
    private val phoneUtil: PhoneNumberUtil

    /**
     * Initializes the PhoneNumberUtil instance.
     * This instance overrides the default metadata loading mechanism to load from the app's assets,
     * as recommended by the FAQ at https://github.com/google/libphonenumber/blob/master/FAQ.md#optimize-loads for Android Apps
     */
    init {
        val assetLoader = MetadataLoader { metadataFileName ->
            val fileName = metadataFileName.substringAfterLast("/")
            appContext.assets.open("phonenumber_data/$fileName")
        }
        phoneUtil = PhoneNumberUtil.createInstance(assetLoader)
    }

    companion object {
        @Volatile
        private var INSTANCE: PhoneNumberManager? = null

        fun getInstance(context: Context): PhoneNumberManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PhoneNumberManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun normalisePhoneNumber(phoneNumber: String): String {
            val trimmed = phoneNumber.trim().lowercase()
            val anonymousTokens = listOf("unknown", "private", "anonymous", "+anonymous", "+", "#")
            if (trimmed in anonymousTokens) return ""
            val digits = trimmed.filter { it.isDigit() }
            return if (trimmed.startsWith("+")) "+$digits" else digits
        }
    }

    fun getDeviceCountryIso(): String {
        val telephonyManager = appContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkIso = runCatching { telephonyManager.networkCountryIso }.getOrNull()
        if (!networkIso.isNullOrBlank()) return networkIso.lowercase()
        val simIso = runCatching { telephonyManager.simCountryIso }.getOrNull()
        if (!simIso.isNullOrBlank()) return simIso.lowercase()
        return Locale.getDefault().country.lowercase()
    }

    /**
     * Parses using the device/default region first. If that result is invalid, retries as an
     * international number because some dialers/providers omit the leading '+'.
     */
    suspend fun parsePhoneNumber(rawNumber: String, defaultRegion: String = getDeviceCountryIso()): Phonenumber.PhoneNumber? = withContext(Dispatchers.Default) {
        return@withContext try {
            val parsedNumber = phoneUtil.parse(rawNumber, defaultRegion.uppercase())
            if (phoneUtil.isValidNumber(parsedNumber)) {
                parsedNumber
            } else {
                val internationalNumber = if (rawNumber.startsWith("+")) rawNumber else "+$rawNumber"
                AppLogger.v("Parsed number ($rawNumber) is invalid in region ($defaultRegion); retrying as international: $internationalNumber")
                phoneUtil.parse(internationalNumber, "ZZ")
            }
        } catch (e: Exception) {
            AppLogger.e("Error parsing phone number: ${e.message}", e)
            null
        }
    }

    suspend fun formatToE164(phoneNumber: Phonenumber.PhoneNumber): String? = withContext(Dispatchers.Default) {
        phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
    }

    suspend fun getRegionCode(phoneNumber: Phonenumber.PhoneNumber): String? = withContext(Dispatchers.Default) {
        phoneUtil.getRegionCodeForNumber(phoneNumber)
    }

    suspend fun isNumberFromDifferentCountry(phoneNumber: Phonenumber.PhoneNumber, compareCountryIso: String = getDeviceCountryIso()): Boolean {
        val numberRegion = getRegionCode(phoneNumber)
        return numberRegion != null && !numberRegion.equals(compareCountryIso, ignoreCase = true)
    }
}
