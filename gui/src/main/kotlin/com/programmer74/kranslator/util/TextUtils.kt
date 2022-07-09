package com.programmer74.kranslator.util

import kotlin.math.ceil
import kotlin.math.roundToInt

object TextUtils {

  fun splitTranslatedParagraphToLines(
    translatedText: String,
    originalTextLines: List<String>
  ): List<String> {
    if (originalTextLines.size == 1) return listOf(translatedText)
    val originalTextWords = originalTextLines.associateWith { it.words() }
    val translatedTextWords = translatedText.words()

    val originalWordsCount = originalTextWords.map { it.value.size }.sum()
    val originalLinesCount = originalTextLines.size
    val originalWordsPerLine = ceil(originalWordsCount * 1.0 / originalLinesCount).roundToInt()

    var translatedTextLines = translatedTextWords
    var additionalWordsPerLine = 0
    while (translatedTextLines.size > originalTextLines.size) {
      translatedTextLines = translatedTextWords
          .chunked(originalWordsPerLine + additionalWordsPerLine)
          .map { it.joinToString(" ") }
      additionalWordsPerLine += 1
    }
    return translatedTextLines
  }

  private fun String.words(): List<String> = this.split(' ', '\t', '\n')
}