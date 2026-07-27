package com.divyateja.jimvro.ui

import androidx.lifecycle.Lifecycle
import org.junit.Assert.assertFalse
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
}
