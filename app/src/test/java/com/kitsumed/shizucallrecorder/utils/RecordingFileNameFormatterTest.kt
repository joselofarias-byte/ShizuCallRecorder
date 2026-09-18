package com.kitsumed.shizucallrecorder.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordingFileNameFormatterTest {

    @Test
    fun composePrependsOwnershipPrefixWhenEnabled() {
        val name = RecordingFileNameFormatter.composeRecordingFileName(
            baseName = "20260918_133500.000-0000_in_+33612345678",
            containerExtension = ".ogg",
            prependOwnershipPrefix = true
        )
        assertEquals("shizucall_20260918_133500.000-0000_in_+33612345678.ogg", name)
        assertTrue(RecordingFileNameFormatter.isOwnedRecording(name))
    }

    @Test
    fun composeHonorStyleNameWithoutPrefix() {
        val name = RecordingFileNameFormatter.composeRecordingFileName(
            baseName = "+33612345678_20260719093015",
            containerExtension = ".ogg",
            prependOwnershipPrefix = false
        )
        assertEquals("+33612345678_20260719093015.ogg", name)
        assertFalse(RecordingFileNameFormatter.isOwnedRecording(name))
        assertFalse(RecordingFileNameFormatter.isLegacyRecording(name))
    }

    @Test
    fun libraryListsHonorNamesOnlyWhenPrefixNotRequired() {
        val honorName = "+33612345678_20260719093015.ogg"
        assertFalse(RecordingFileNameFormatter.shouldListRecording(honorName, ownershipPrefixRequired = true))
        assertTrue(RecordingFileNameFormatter.shouldListRecording(honorName, ownershipPrefixRequired = false))
    }

    @Test
    fun libraryListsPrefixedAndLegacyDefaultNames() {
        val owned = "shizucall_20260918_133500.012-0300_out_+598611.ogg"
        val legacy = "20260627_180212.012-0300_out_+598611.ogg"
        assertTrue(RecordingFileNameFormatter.shouldListRecording(owned, ownershipPrefixRequired = true))
        assertTrue(RecordingFileNameFormatter.shouldListRecording(legacy, ownershipPrefixRequired = true))
        assertEquals("20260918_133500.012-0300_out_+598611.ogg", RecordingFileNameFormatter.stripOwnershipPrefix(owned))
        assertEquals(legacy, RecordingFileNameFormatter.stripOwnershipPrefix(legacy))
    }
}
