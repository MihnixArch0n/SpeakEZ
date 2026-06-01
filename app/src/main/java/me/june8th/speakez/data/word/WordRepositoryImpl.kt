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

    override suspend fun addCustomWord(word: WordEntity): Result<Long> {
        val normalizedText = word.wordText.trim()
        if (wordDao.customWordExists(normalizedText)) {
            return Result.failure(DuplicateCustomWordException())
        }
        return Result.success(wordDao.insertWord(word.copy(wordText = normalizedText)))
    }

    override suspend fun deleteWord(id: Long) {
        wordDao.deleteWord(id)
    }
}

class DuplicateCustomWordException : IllegalArgumentException("Custom word already exists")
