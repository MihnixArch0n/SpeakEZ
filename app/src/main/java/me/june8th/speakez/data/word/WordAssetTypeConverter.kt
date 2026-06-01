package me.june8th.speakez.data.word

import androidx.room.TypeConverter

class WordAssetTypeConverter {
    @TypeConverter
    fun fromWordAssetType(assetType: WordAssetType): String = assetType.name

    @TypeConverter
    fun toWordAssetType(value: String): WordAssetType = WordAssetType.valueOf(value)
}
