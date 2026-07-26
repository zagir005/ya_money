package com.zagirlek.accounts

/**
 * Проверяет, что строка содержит ровно один emoji-графем:
 * обычный emoji, флаг, keycap или составную ZWJ-последовательность.
 */
internal fun String.isSingleEmoji(): Boolean {
    if (isEmpty()) return false

    val codePoints = codePoints().toArray()
    if (codePoints.size == 2 && codePoints.all(::isRegionalIndicator)) {
        return true
    }
    if (codePoints.isKeycapEmoji()) {
        return true
    }

    fun componentEnd(startIndex: Int): Int? {
        if (startIndex >= codePoints.size || !isEmojiBase(codePoints[startIndex])) {
            return null
        }

        var index = startIndex + 1
        if (codePoints.getOrNull(index) == VariationSelector16) index++
        if (codePoints.getOrNull(index)?.let(::isEmojiModifier) == true) index++
        return index
    }

    var index = componentEnd(0) ?: return false
    if (codePoints.first() == BlackFlag && codePoints.getOrNull(index)?.let(::isTagCharacter) == true) {
        while (codePoints.getOrNull(index)?.let(::isTagCharacter) == true) index++
        if (codePoints.getOrNull(index) != CancelTag) return false
        index++
    }

    while (codePoints.getOrNull(index) == ZeroWidthJoiner) {
        index = componentEnd(index + 1) ?: return false
    }
    return index == codePoints.size
}

private fun IntArray.isKeycapEmoji(): Boolean {
    if (isEmpty() || first() !in KeycapBases) return false
    return when (size) {
        2 -> this[1] == CombiningEnclosingKeycap
        3 -> this[1] == VariationSelector16 && this[2] == CombiningEnclosingKeycap
        else -> false
    }
}

private fun isEmojiBase(codePoint: Int): Boolean = when {
    isRegionalIndicator(codePoint) || isEmojiModifier(codePoint) -> false
    codePoint in 0x1F000..0x1FAFF -> true
    codePoint in 0x2600..0x27BF -> true
    codePoint in 0x231A..0x231B -> true
    codePoint in 0x23E9..0x23F3 -> true
    codePoint in 0x23F8..0x23FA -> true
    codePoint in 0x2194..0x2199 -> true
    codePoint in 0x21A9..0x21AA -> true
    codePoint in 0x25AA..0x25AB -> true
    codePoint in 0x25FB..0x25FE -> true
    codePoint in 0x2B05..0x2B07 -> true
    codePoint in setOf(
        0x00A9,
        0x00AE,
        0x203C,
        0x2049,
        0x2122,
        0x2139,
        0x2328,
        0x23CF,
        0x24C2,
        0x25B6,
        0x25C0,
        0x2934,
        0x2935,
        0x2B1B,
        0x2B1C,
        0x2B50,
        0x2B55,
        0x3030,
        0x303D,
        0x3297,
        0x3299,
    ) -> true
    else -> false
}

private fun isRegionalIndicator(codePoint: Int): Boolean =
    codePoint in 0x1F1E6..0x1F1FF

private fun isEmojiModifier(codePoint: Int): Boolean =
    codePoint in 0x1F3FB..0x1F3FF

private fun isTagCharacter(codePoint: Int): Boolean =
    codePoint in 0xE0020..0xE007E

private val KeycapBases = setOf(
    '#'.code,
    '*'.code,
    '0'.code,
    '1'.code,
    '2'.code,
    '3'.code,
    '4'.code,
    '5'.code,
    '6'.code,
    '7'.code,
    '8'.code,
    '9'.code,
)
private const val VariationSelector16 = 0xFE0F
private const val CombiningEnclosingKeycap = 0x20E3
private const val ZeroWidthJoiner = 0x200D
private const val BlackFlag = 0x1F3F4
private const val CancelTag = 0xE007F
