package com.divyateja.jimvro.ui

import android.os.SystemClock
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.divyateja.jimvro.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityResultCompatibilityTest {
    @Test
    fun backupDocumentPickerLaunchesWithoutRequestCodeCrash() {
        launchExternalContract {
            activityResultRegistry
                .register("backup-test", ActivityResultContracts.CreateDocument("application/octet-stream")) {}
                .launch("jimvro-test-backup.db")
        }
    }

    @Test
    fun photoPickerLaunchesWithoutRequestCodeCrash() {
        launchExternalContract {
            activityResultRegistry
                .register("photo-test", ActivityResultContracts.PickVisualMedia()) {}
                .launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun launchExternalContract(action: MainActivity.() -> Unit) {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity(action)
            SystemClock.sleep(500)
            InstrumentationRegistry.getInstrumentation().uiAutomation.performGlobalAction(1)
        }
    }
}
