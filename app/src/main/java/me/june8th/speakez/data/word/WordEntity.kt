package me.june8th.speakez.data.word

import androidx.room.Entity
import androidx.room.PrimaryKey

const val CUSTOM_WORDS_CATEGORY_ID = "CUSTOM_WORDS"

enum class WordAssetType {
    MULBERRY,
    EMOJI,
}

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordText: String,
    val categoryId: String = CUSTOM_WORDS_CATEGORY_ID,
    val isCustom: Boolean = true,
    val assetType: WordAssetType,
    val assetValue: String,
)
