package me.june8th.speakez.domain.validation

object EmojiValidator {
    fun isSingleEmoji(value: String): Boolean {
        if (value.isBlank()) return false

        val codePoints = value.codePoints().toArray()
        var hasEmojiBase = false
        var requiresKeycap = false
        var hasKeycap = false
        var regionalIndicatorCount = 0
        var previousWasJoiner = false

        codePoints.forEachIndexed { index, codePoint ->
            when {
                isModifier(codePoint) || isVariationSelector(codePoint) -> {
                    previousWasJoiner = false
                }
                codePoint == KEYCAP -> {
                    hasKeycap = true
                    previousWasJoiner = false
                }
                isEmojiBase(codePoint) -> {
                    hasEmojiBase = true
                    if (isKeycapBase(codePoint)) requiresKeycap = true
                    if (isRegionalIndicator(codePoint)) regionalIndicatorCount++
                    if (index > 0 && !previousWasJoiner && regionalIndicatorCount == 0) {
                        return false
                    }
                    previousWasJoiner = false
                }
                codePoint == ZERO_WIDTH_JOINER && hasEmojiBase && index < codePoints.lastIndex -> {
                    previousWasJoiner = true
                }
                else -> return false
            }
        }

        return hasEmojiBase &&
            !previousWasJoiner &&
            regionalIndicatorCount <= 2 &&
            (!requiresKeycap || hasKeycap)
    }

    private fun isEmojiBase(codePoint: Int): Boolean {
        return codePoint in 0x1F000..0x1FAFF ||
            codePoint in 0x2600..0x27BF ||
            codePoint in 0x2300..0x23FF ||
            isRegionalIndicator(codePoint) ||
            isKeycapBase(codePoint)
    }

    private fun isRegionalIndicator(codePoint: Int): Boolean = codePoint in 0x1F1E6..0x1F1FF

    private fun isModifier(codePoint: Int): Boolean = codePoint in 0x1F3FB..0x1F3FF

    private fun isKeycapBase(codePoint: Int): Boolean {
        return codePoint in 0x0030..0x0039 || codePoint == 0x0023 || codePoint == 0x002A
    }

    private fun isVariationSelector(codePoint: Int): Boolean = codePoint == 0xFE0F

    private const val ZERO_WIDTH_JOINER = 0x200D
    private const val KEYCAP = 0x20E3
}
