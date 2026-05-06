package me.june8th.speakez.data.mock

import me.june8th.speakez.domain.model.VocabularyItem

object MockVocabularyRepository {
    fun getCategories(): List<VocabularyItem> = listOf(
        VocabularyItem(
            id = "1",
            title = "Ăn uống",
            iconColorHex = "0xFF0B7A75",
            containerColorHex = "0xFFDDF7F4",
            category = "Food"
        ),
        VocabularyItem(
            id = "2",
            title = "Y tế",
            iconColorHex = "0xFFB54708",
            containerColorHex = "0xFFFFE8D6",
            category = "Health"
        ),
        VocabularyItem(
            id = "3",
            title = "Hoạt động",
            iconColorHex = "0xFF2F5AA8",
            containerColorHex = "0xFFDDE8FF",
            category = "Activity"
        ),
        VocabularyItem(
            id = "4",
            title = "Cảm xúc",
            iconColorHex = "0xFF8E3B9E",
            containerColorHex = "0xFFF3E0F8",
            category = "Emotion"
        ),
        VocabularyItem(
            id = "5",
            title = "Cơ thể",
            iconColorHex = "0xFF7A5B00",
            containerColorHex = "0xFFFFF0C2",
            category = "Body"
        ),
    )
}

