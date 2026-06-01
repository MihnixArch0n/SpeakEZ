package me.june8th.speakez.domain.repository

import kotlinx.coroutines.flow.Flow
import me.june8th.speakez.data.word.WordEntity

interface WordRepository {
    fun getWordsByCategory(categoryId: String): Flow<List<WordEntity>>

    fun getAllCustomWords(): Flow<List<WordEntity>>

    suspend fun addCustomWord(word: WordEntity): Result<Long>

    suspend fun deleteWord(id: Long)
}
