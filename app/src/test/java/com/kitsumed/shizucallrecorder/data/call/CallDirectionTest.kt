package com.kitsumed.shizucallrecorder.data.call

import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CallDirectionTest {

    @Test
    fun ringingIsIncoming() {
        assertEquals(CallDirection.INCOMING, CallDirection.fromCallStateOrNull(TelephonyManager.CALL_STATE_RINGING))
    }

    @Test
    fun offhookWithoutPriorRingingIsOutgoing() {
        assertEquals(CallDirection.OUTGOING, CallDirection.fromCallStateOrNull(TelephonyManager.CALL_STATE_OFFHOOK))
    }

    @Test
    fun idleHasNoDirection() {
        assertNull(CallDirection.fromCallStateOrNull(TelephonyManager.CALL_STATE_IDLE))
    }

    @Test
    fun fromTokenRoundTrip() {
        assertEquals(CallDirection.INCOMING, CallDirection.fromToken("incoming"))
        assertEquals(CallDirection.OUTGOING, CallDirection.fromToken("outgoing"))
        assertNull(CallDirection.fromToken("unknown"))
    }
}
