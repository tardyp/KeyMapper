package io.github.sds100.keymapper.util

import androidx.annotation.IntRange

/**
 * Created by sds100 on 24/04/2021.
 */

fun String.getWordBoundaries(@IntRange(from = 0L) cursorPosition: Int): Pair<Int, Int>? {
    if (this.isBlank()) return null

    //return null if there is just whitespace around the position
    if (getOrNull(cursorPosition - 1)?.isWhitespace() == true && getOrNull(cursorPosition)?.isWhitespace() == true) {
        return null
    }

    var lastSpaceIndex: Int? = null
    var firstBoundary: Int? = null
    var secondBoundary: Int? = null

    for ((index, c) in this.withIndex()) {
        if (c.isWhitespace()) {
            lastSpaceIndex = index

            if (index > cursorPosition) {
                secondBoundary = lastSpaceIndex
                break
            }
        }

        if (index == cursorPosition) {
            firstBoundary = lastSpaceIndex?.plus(1)
        }
    }

    return Pair(firstBoundary ?: 0, secondBoundary ?: lastIndex)
}
