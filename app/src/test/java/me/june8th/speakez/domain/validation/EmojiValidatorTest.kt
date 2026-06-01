package me.june8th.speakez.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiValidatorTest {
    @Test
    fun acceptsSingleEmojiAndJoinedEmoji() {
        assertTrue(EmojiValidator.isSingleEmoji("😊"))
        assertTrue(EmojiValidator.isSingleEmoji("🖐️"))
        assertTrue(EmojiValidator.isSingleEmoji("👨‍👩‍👧"))
        assertTrue(EmojiValidator.isSingleEmoji("👍🏽"))
        assertTrue(EmojiValidator.isSingleEmoji("1️⃣"))
    }

    @Test
    fun rejectsTextEmptyAndMultipleEmoji() {
        assertFalse(EmojiValidator.isSingleEmoji(""))
        assertFalse(EmojiValidator.isSingleEmoji("hello"))
        assertFalse(EmojiValidator.isSingleEmoji("😊😊"))
        assertFalse(EmojiValidator.isSingleEmoji("1"))
    }
}
