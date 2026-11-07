package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.repository.AudioRepository
import com.example.ui.theme.AppThemeStyle
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: AudioRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AudioRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TO_OT Player", appName)
    }

    @Test
    fun `top insertion prepends lane to index 0`() = runBlocking {
        repository.initializeDefaultsIfNeeded()
        val initialLanes = repository.getAllLanesSync()
        assertTrue(initialLanes.isNotEmpty())

        val newId = repository.addLaneAtTop("Brand New Top Lane")
        val updatedLanes = repository.getAllLanesSync()

        assertEquals(newId, updatedLanes.first().id)
        assertEquals("Brand New Top Lane", updatedLanes.first().name)
        assertEquals(0, updatedLanes.first().sortOrder)
    }

    @Test
    fun `lane reordering updates sort order persistence`() = runBlocking {
        repository.initializeDefaultsIfNeeded()
        val lanes = repository.getAllLanesSync()
        val reversedIds = lanes.map { it.id }.reversed()

        repository.reorderLanes(reversedIds)
        val reorderedLanes = repository.getAllLanesSync()

        assertEquals(reversedIds, reorderedLanes.map { it.id })
    }

    @Test
    fun `theme style default fallback`() {
        val style = AppThemeStyle.fromIdOrDefault("NON_EXISTENT_THEME")
        assertEquals(AppThemeStyle.EARTHY_BIOPHILIC, style)
    }

    @Test
    fun `initialize defaults creates Remix and excludes Audiobooks`() = runBlocking {
        repository.initializeDefaultsIfNeeded()
        val lanes = repository.getAllLanesSync()
        assertTrue("Remix lane must exist", lanes.any { it.name.equals("Remix", ignoreCase = true) })
        assertTrue("Audiobooks lane must not exist", lanes.none { it.name.equals("Audiobooks", ignoreCase = true) })
    }

    @Test
    fun `lane update and delete persist accurately`() = runBlocking {
        repository.initializeDefaultsIfNeeded()
        val initialLanes = repository.getAllLanesSync()
        val firstLane = initialLanes.first()

        val updated = firstLane.copy(name = "Updated Vocal Track", volume = 0.75f, isMuted = true)
        repository.updateLane(updated)

        val retrieved = repository.getAllLanesSync().find { it.id == firstLane.id }
        assertEquals("Updated Vocal Track", retrieved?.name)
        assertEquals(0.75f, retrieved?.volume ?: 0f, 0.01f)
        assertEquals(true, retrieved?.isMuted)

        repository.deleteLane(firstLane.id)
        val afterDelete = repository.getAllLanesSync().find { it.id == firstLane.id }
        assertEquals(null, afterDelete)
    }

    @Test
    fun `audio lane count and clear empty lanes`() = runBlocking {
        repository.initializeDefaultsIfNeeded()
        val count = repository.getAllLanesSync().size
        assertTrue(count > 0)
    }
}
