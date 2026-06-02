package me.june8th.speakez.data.word

import kotlinx.coroutines.flow.Flow
import me.june8th.speakez.domain.repository.WordRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDao: WordDao,
) : WordRepository {
    override fun getWordsByCategory(categoryId: String): Flow<List<WordEntity>> {
        return wordDao.getWordsByCategory(categoryId)
    }

    override fun getAllCustomWords(): Flow<List<WordEntity>> {
        return wordDao.getAllCustomWords()
    }

    override suspend fun getCustomWord(id: Long): WordEntity? {
        return wordDao.getCustomWord(id)
    }

    override suspend fun addCustomWord(word: WordEntity): Result<Long> {
        val normalizedText = word.wordText.trim()
        if (wordDao.customWordExists(normalizedText)) {
            return Result.failure(DuplicateCustomWordException())
        }
        return Result.success(wordDao.insertWord(word.copy(wordText = normalizedText)))
    }

    override suspend fun updateCustomWord(word: WordEntity): Result<Unit> {
        val normalizedText = word.wordText.trim()
        if (wordDao.getCustomWord(word.id) == null) {
            return Result.failure(CustomWordNotFoundException())
        }
        if (wordDao.customWordExistsExcludingId(normalizedText, word.id)) {
            return Result.failure(DuplicateCustomWordException())
        }
        wordDao.updateWord(
            word.copy(
                wordText = normalizedText,
                categoryId = CUSTOM_WORDS_CATEGORY_ID,
                isCustom = true,
            ),
        )
        return Result.success(Unit)
    }

    override suspend fun deleteWord(id: Long) {
        wordDao.deleteWord(id)
    }
}

class DuplicateCustomWordException : IllegalArgumentException("Custom word already exists")

class CustomWordNotFoundException : IllegalArgumentException("Custom word does not exist")
