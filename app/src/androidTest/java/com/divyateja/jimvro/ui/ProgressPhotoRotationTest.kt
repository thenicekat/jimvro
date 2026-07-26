package com.divyateja.jimvro.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressPhotoRotationTest {
    @Test
    fun rotationIsSavedToPhotoFile() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "rotation-test.jpg")
        Bitmap.createBitmap(40, 20, Bitmap.Config.ARGB_8888).let { bitmap ->
            file.outputStream().use { output -> bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output) }
            bitmap.recycle()
        }

        rotateProgressPhoto(file.absolutePath, 90f)
        BitmapFactory.decodeFile(file.absolutePath).let { rotated ->
            assertEquals(20, rotated.width)
            assertEquals(40, rotated.height)
            rotated.recycle()
        }

        rotateProgressPhoto(file.absolutePath, -90f)
        BitmapFactory.decodeFile(file.absolutePath).let { restored ->
            assertEquals(40, restored.width)
            assertEquals(20, restored.height)
            restored.recycle()
        }
        file.delete()
    }
}
