package com.kitsumed.shizucallrecorder.integrations.scrcpy

import org.junit.Assert.assertEquals
import org.junit.Test

class ScrcpyEnumsTest {

    @Test
    fun audioSourceFromKeyFallsBackToVoiceCall() {
        assertEquals(ScrcpyAudioSource.VOICE_CALL, ScrcpyAudioSource.fromKey("voice-call"))
        assertEquals(ScrcpyAudioSource.VOICE_COMMUNICATION, ScrcpyAudioSource.fromKey("mic-voice-communication"))
        assertEquals(ScrcpyAudioSource.VOICE_CALL, ScrcpyAudioSource.fromKey("not-a-real-source"))
    }

    @Test
    fun audioCodecFromKeyAndFourCC() {
        assertEquals(ScrcpyAudioCodec.OPUS, ScrcpyAudioCodec.fromKey("opus"))
        assertEquals(ScrcpyAudioCodec.AAC, ScrcpyAudioCodec.fromKey("aac"))
        assertEquals(ScrcpyAudioCodec.OPUS, ScrcpyAudioCodec.fromKey("unknown"))
        assertEquals(ScrcpyAudioCodec.OPUS, ScrcpyAudioCodec.fromFourCC(0x6F707573))
        assertEquals(ScrcpyAudioCodec.AAC, ScrcpyAudioCodec.fromFourCC(0x00616163))
    }
}
