package me.june8th.speakez.data.word

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Insert
    suspend fun insertWord(word: WordEntity): Long

    @Query("SELECT * FROM words WHERE categoryId = :categoryId ORDER BY wordText COLLATE NOCASE ASC")
    fun getWordsByCategory(categoryId: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isCustom = 1 ORDER BY wordText COLLATE NOCASE ASC")
    fun getAllCustomWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id AND isCustom = 1 LIMIT 1")
    suspend fun getCustomWord(id: Long): WordEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM words WHERE isCustom = 1 AND LOWER(TRIM(wordText)) = LOWER(TRIM(:wordText)))")
    suspend fun customWordExists(wordText: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM words WHERE isCustom = 1 AND id != :excludedId AND LOWER(TRIM(wordText)) = LOWER(TRIM(:wordText)))")
    suspend fun customWordExistsExcludingId(wordText: String, excludedId: Long): Boolean

    @Update
    suspend fun updateWord(word: WordEntity)

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun deleteWord(id: Long)
}
