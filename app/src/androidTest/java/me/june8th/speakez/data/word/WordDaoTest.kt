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
import org.junit.Assert.assertNull
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

    @Test
    fun getUpdateAndObserveCustomWord() = runTest {
        val id = dao.insertWord(
            WordEntity(
                wordText = "Uống nước",
                assetType = WordAssetType.EMOJI,
                assetValue = "🥤",
            ),
        )

        val inserted = dao.getCustomWord(id)
        assertEquals("Uống nước", inserted?.wordText)

        dao.updateWord(
            requireNotNull(inserted).copy(
                wordText = "Nước",
                assetType = WordAssetType.MULBERRY,
                assetValue = "file:///android_asset/mulberry/nước.svg",
            ),
        )

        val updatedWords = dao.getAllCustomWords().first { words ->
            words.singleOrNull()?.wordText == "Nước"
        }
        assertEquals(WordAssetType.MULBERRY, updatedWords.single().assetType)
        assertEquals("file:///android_asset/mulberry/nước.svg", updatedWords.single().assetValue)

        dao.deleteWord(id)

        assertTrue(dao.getAllCustomWords().first { it.isEmpty() }.isEmpty())
        assertNull(dao.getCustomWord(id))
    }

    @Test
    fun repositoryUpdateAllowsOwnNameAndRejectsAnotherCustomWordName() = runTest {
        val repository = WordRepositoryImpl(dao)
        val firstId = repository.addCustomWord(
            WordEntity(
                wordText = "Có",
                assetType = WordAssetType.EMOJI,
                assetValue = "✅",
            ),
        ).getOrThrow()
        repository.addCustomWord(
            WordEntity(
                wordText = "Không",
                assetType = WordAssetType.EMOJI,
                assetValue = "❌",
            ),
        ).getOrThrow()

        assertFalse(dao.customWordExistsExcludingId("  CÓ ", firstId))
        repository.updateCustomWord(
            requireNotNull(repository.getCustomWord(firstId)).copy(
                wordText = "  CÓ ",
                assetValue = "👍",
            ),
        ).getOrThrow()

        assertEquals("CÓ", repository.getCustomWord(firstId)?.wordText)
        assertEquals("👍", repository.getCustomWord(firstId)?.assetValue)

        val duplicateResult = repository.updateCustomWord(
            requireNotNull(repository.getCustomWord(firstId)).copy(wordText = " không "),
        )
        assertTrue(duplicateResult.exceptionOrNull() is DuplicateCustomWordException)
        assertEquals("CÓ", repository.getCustomWord(firstId)?.wordText)
    }
}
