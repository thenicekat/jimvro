package com.divyateja.jimvro.ui

import androidx.lifecycle.Lifecycle
import com.divyateja.jimvro.data.ProgressPhotoEntity
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressPhotoPickerTest {
    @Test
    fun pickerHandoffDoesNotLockPhotoVault() {
        assertFalse(shouldRelockPhotos(Lifecycle.Event.ON_STOP, photoPickerInFlight = true))
    }

    @Test
    fun ordinaryBackgroundingLocksPhotoVault() {
        assertTrue(shouldRelockPhotos(Lifecycle.Event.ON_STOP, photoPickerInFlight = false))
        assertFalse(shouldRelockPhotos(Lifecycle.Event.ON_PAUSE, photoPickerInFlight = false))
    }

    @Test
    fun poseLabelOnlyUsesSavedPosePrefix() {
        assertEquals("Front", ProgressPhotoEntity(capturedOn = "2026-09-14", uri = "photo", notes = "Front · after training").poseLabel())
        assertNull(ProgressPhotoEntity(capturedOn = "2026-09-14", uri = "photo", notes = "after training").poseLabel())
    }
}
