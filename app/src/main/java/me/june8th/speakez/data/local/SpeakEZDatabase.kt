package me.june8th.speakez.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.june8th.speakez.data.quickphrase.QuickPhraseDao
import me.june8th.speakez.data.quickphrase.QuickPhraseEntity
import me.june8th.speakez.data.word.WordAssetTypeConverter
import me.june8th.speakez.data.word.WordDao
import me.june8th.speakez.data.word.WordEntity

@Database(
    entities = [
        QuickPhraseEntity::class,
        WordEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(ActionTypeConverter::class, WordAssetTypeConverter::class)
abstract class SpeakEZDatabase : RoomDatabase() {
    abstract fun quickPhraseDao(): QuickPhraseDao
    abstract fun wordDao(): WordDao
}
