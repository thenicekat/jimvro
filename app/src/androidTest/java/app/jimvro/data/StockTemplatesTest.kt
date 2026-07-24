package app.jimvro.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StockTemplatesTest {
    @Test fun seedsFiveTemplatesOnceWithExpectedExercises() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, JimvroDatabase::class.java).build()
        val repository = JimvroRepository(database)
        repository.seedStockTemplates()
        repository.seedStockTemplates()
        val templates = repository.templates.first()
        assertEquals(listOf("Upper A", "Lower A", "Upper B", "Lower B", "Upper C"), templates.map { it.name })
        assertEquals(listOf(6, 6, 8, 6, 8), templates.map { repository.templateLines(it.id).first().size })
        database.close()
    }
}
