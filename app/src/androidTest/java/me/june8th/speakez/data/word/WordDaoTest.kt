package me.june8th.speakez.data.word

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.june8th.speakez.data.local.SpeakEZDatabase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WordDaoTest {
    private lateinit var database: SpeakEZDatabase
    private lateinit var dao: WordDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SpeakEZDatabase::class.java,
        ).allowMainThreadQueries()
            .build()
        dao = database.wordDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertObserveDetectDuplicateAndDelete() = runTest {
        val id = dao.insertWord(
            WordEntity(
                wordText = "Xin chào",
                assetType = WordAssetType.EMOJI,
                assetValue = "😊",
            ),
        )

        assertEquals(1, dao.getWordsByCategory(CUSTOM_WORDS_CATEGORY_ID).first().size)
        assertEquals(1, dao.getAllCustomWords().first().size)
        assertTrue(dao.customWordExists("  XIN CHÀO  "))

        dao.deleteWord(id)

        assertFalse(dao.customWordExists("Xin chào"))
        assertTrue(dao.getAllCustomWords().first().isEmpty())
    }
}
