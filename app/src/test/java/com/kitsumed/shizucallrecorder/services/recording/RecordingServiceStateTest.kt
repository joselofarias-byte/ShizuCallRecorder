package com.kitsumed.shizucallrecorder.services.recording

import com.kitsumed.shizucallrecorder.data.call.CallDirection
import com.kitsumed.shizucallrecorder.data.call.EnrichedCallData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingServiceStateTest {
    private val metadata = EnrichedCallData(
        normalisedPhoneNumber = "",
        direction = CallDirection.INCOMING
    )

    @Test
    fun onlyStandbyAcceptsAReplacementStartOrStandbyCommand() {
        assertFalse(RecordingServiceState.Standby(metadata).isSessionInProgress)
        assertTrue(RecordingServiceState.Starting(metadata).isSessionInProgress)
        assertTrue(
            RecordingServiceState.Active(
                engine = AudioRecordingEngine(),
                metadata = metadata
            ).isSessionInProgress
        )
    }
}
